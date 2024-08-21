package com.freeman.mycampingmap.viewmodels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freeman.mycampingmap.data.CampingDataUtil
import com.freeman.mycampingmap.db.CampingSite
import com.freeman.mycampingmap.db.LoginType
import com.freeman.mycampingmap.db.User
import com.freeman.mycampingmap.utils.MyLocation
import com.freeman.mycampingmap.utils.MyLocation.parseLatLng
import com.freeman.mycampingmap.utils.MyLocation.requestLocation
import com.freeman.mycampingmap.utils.MyLog
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(@ApplicationContext override val context: Context): BaseViewModel(context) {

    val TAG = this::class.java.simpleName

    // 캠핑장 삭제 플레그 - 삭제중에 데이터 싱크가 일어나지 않게 하기 위해.
    var isDeleting: Boolean = false

    val loginUser: MutableLiveData<User?> = userRepository.userData

    fun getDBUser() {
        MyLog.d(TAG, "getDBUser()")
        //db에 저장된 사용자 정보 가져오기.
        userRepository.getUser()
    }

    fun loginTypeCheckUser(
        user: User,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
        onComplete: (Boolean, String) -> Unit
    ) {
//        MyLog.d(TAG, "loginTypeCheckUser() = $user")
        viewModelScope.launch {
            //db에 저장된 사용자 정보를 이용하여 로그인 신청.
            when (user.loginType) {
                LoginType.EMAIL -> { /* 이메일 로그인 */
                    emailPasswdLogin(
                        context = context,
                        email = user.email,
                        password = user.password,
                        onComplete = onComplete
                    )
                }

                LoginType.GOOGLE -> { /* 구글 로그인 */
                    firebaseGoogleSignIn.firebaseAuthWithGoogle { success, message ->
                        if (success) {
                            onComplete(true, "")
                        } else {
                            firebaseLoginGoogleInit(launcher)
                            onComplete(false, "")
                        }
                    }
                }

                LoginType.FACEBOOK -> { /* 페이스북 로그인 */
                }
            }   //when

        }   //viewModelScope.launch
    }

    fun emailRegisterUser(email: String, password: String, onComplete: (Boolean, String) -> Unit) {
        // 파이어베이스 이메일 회원가입.
        MyLog.d(TAG, "emailRegisterUser() = $email, $password")
        firebaseEmailPassword.createAccount(email, password) { success, message ->
            if (success) firebaseGoogleSignIn.firebaseAuthWithGoogle { success, message ->
                if (success) {
                    onComplete(success, message ?: "회원 가입 성공")
                } else {
                    onComplete(success, message ?: "회원 가입 실패")
                }
            }
        }
    }

    fun getDBCampingSites() {
        //db데이터 사이트 정보 가져오기.
        MyLog.d(TAG, "getDBCampingSites()")
        if (isDeleting) return
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val dbAllSites = async { asyncDBAllCampingSiteSelect() }
            val localSites = dbAllSites.await()
            MyLog.d(TAG, "getDBCampingSites() localSites.size : ${localSites.size}")
            //채널에 캠핑장 정보 전달.
            channelCampingSites.send(localSites)

            withContext(Dispatchers.Main) {
                _syncAllCampingList.value = localSites
            }
        }

        _isLoading.value = false
    }

    private val channelCampingSites = Channel<List<CampingSite>>()

    fun distanceFromCurrentLocation() {
        //캠핑장 리스트에 내 위치와 거리 정보 추가.
        viewModelScope.launch {
            val campingSites = channelCampingSites.receive()

            if (campingSites.isEmpty()) return@launch
            MyLog.d(TAG, "distanceFromCurrentLocation() campingSites.size : ${campingSites.size}")

            requestLocation(context = context, onResult = { isSuccess, latitude, longitude ->
                MyLog.d(TAG, "distanceFromCurrentLocation() isSuccess : $isSuccess, latitude : $latitude, longitude : $longitude")

                if (!isSuccess) return@requestLocation
                campingSites.forEach { campingSite ->
                    parseLatLng(campingSite.location).let { latLng ->
                        if (latLng == null) return@forEach
                        campingSite.distanceFromCurrentLocation =
                            MyLocation.distanceBetween(
                                latitude.toDouble(),
                                longitude.toDouble(),
                                latLng.latitude,
                                latLng.longitude
                            )
//                        MyLog.d(TAG, "campingSite.distanceFromCurrentLocation : $campingSite")
                    }
                }
                _syncAllCampingList.value = emptyList()
                _syncAllCampingList.value = campingSites
            })
        }

    }

    fun syncCampingSites() {
        //db데이터 또는 파이어베이스 사이트 정보 동기화.
        if (isDeleting) return
        _isLoading.value = true
        MyLog.d(TAG, "syncCampingSites()")
        viewModelScope.launch(Dispatchers.IO) {

            val dbAllCampingSiteSelect = async { asyncDBAllCampingSiteSelect() }
            val fierbaseCampingSites = async { firebaseManager.asyncAllFirebaseCampingSites() }

            val localSites = dbAllCampingSiteSelect.await()
            val remoteSites = fierbaseCampingSites.await()
            MyLog.d(TAG, "syncCampingSites() localSites.size : ${localSites.size}")
            MyLog.d(TAG, "syncCampingSites() remoteSites.size : ${remoteSites.size}")
            withContext(Dispatchers.Main) {
                _syncAllCampingList.value = localSites + remoteSites
            }

            val syncCampingSites = campingDataUtil.syncCampingSites(
//                firebaseManager,
                localSites, remoteSites,
                campingSiteRepository
            )
            //채널에 캠핑장 정보 전달.
            channelCampingSites.send(syncCampingSites)

            withContext(Dispatchers.Main) {
                MyLog.d(TAG, "syncCampingSites() syncCampingSites.size : ${syncCampingSites.size}")
                _syncAllCampingList.value = syncCampingSites
                _isLoading.value = false
            }
        }
    }

    fun deleteCampingList(id: String) {
        //db 캠핑장 정보 삭제.
        _syncAllCampingList.value?.let { campingSites ->
            val updatedList = campingSites.toMutableList()
            updatedList.removeAll { it.id == id }
            _syncAllCampingList.value = updatedList
        }
    }

    fun deleteCampingSite(campingSite: CampingSite) {
        isDeleting = true
        dbCampingSiteDelete(campingSite) {
            //db 캠핑장 정보 삭제.
            MyLog.d(TAG, "dbCampingSiteDelete() = $it")
            //파이어스토어 데이터베이스에 저장된 캠핑장 정보 삭제.
            firebaseManager.deleteFirebaseCampingSite(campingSite) { success ->
                if (success) {
                    deleteCampingList(campingSite.id)
                    Toast.makeText(context, "삭제 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show()
                }
                isDeleting = false
            }
        }
    }

    fun logout() {
        //로그아웃
        MyLog.d(TAG, "logout()")
        //db 로그아웃
        viewModelScope.launch(Dispatchers.Main) {
            userRepository.deleteUser()    //유져 db정보 삭제
            dbAllDeleteCampingSite()    //db 캠핑장 정보 삭제
            FirebaseAuth.getInstance().signOut() //파이어베이스 로그아웃
            delay(300)
        }
    }

    fun loginFacebook() {
        // 페이스북 로그인
        Log.d(TAG, "loginFacebook()")

    }

    fun firebaseLoginGoogleInit(launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>) {
        firebaseManager.firebaseLoginGoogleInit(launcher)
    }

    fun firebaseLoginGoogle(
        activityResult: ActivityResult,
        saveUserData: Boolean = false,
        onComplete: (Boolean, String) -> Unit,
    ) {
        firebaseManager.firebaseLoginGoogle(activityResult, saveUserData, onComplete)
    }

}
