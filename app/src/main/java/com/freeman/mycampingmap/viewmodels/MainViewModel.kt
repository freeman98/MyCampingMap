package com.freeman.mycampingmap.viewmodels

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.freeman.mycampingmap.MyApplication
import com.freeman.mycampingmap.auth.FirebaseManager.deleteFirebaseCampingSite
import com.freeman.mycampingmap.auth.FirebaseManager.emailSignIn
import com.freeman.mycampingmap.auth.FirebaseManager.emailSignUp
import com.freeman.mycampingmap.auth.FirebaseManager.firebaseAuthTokenLogin
import com.freeman.mycampingmap.auth.FirebaseManager.getAllFirebaseCampingSites
import com.freeman.mycampingmap.data.CampingDataUtil
import com.freeman.mycampingmap.db.CampingSite
import com.freeman.mycampingmap.db.LoginType
import com.freeman.mycampingmap.db.User
import com.freeman.mycampingmap.db.UserDao
import com.freeman.mycampingmap.db.UserDatabase
import com.freeman.mycampingmap.utils.MyLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

//MVVM 모델 과 컴포스를 적용
class MainViewModel : BaseViewModel() {

    val TAG = MainViewModel::class.java.simpleName

    private val userDao: UserDao = UserDatabase.getDatabase(MyApplication.context).userDao()
    val user: LiveData<User?> = userDao.getUser().asLiveData()

    // 내 캠핑장 리스트 정보
    private val _syncAllCampingList = MutableLiveData<List<CampingSite>>()
    val syncAllCampingList: LiveData<List<CampingSite>> = _syncAllCampingList

    //파이어베이스 캠핑장 리스트 정보
    private val _firebaseCampingSites = MutableLiveData<List<CampingSite>>()
    val firebaseCampingSites: LiveData<List<CampingSite>> = _firebaseCampingSites

    private val _dbAllCampingSites = MutableLiveData<List<CampingSite>>()
    val dbAllCampingSites: LiveData<List<CampingSite>> = _dbAllCampingSites

//    fun getLoginUser(onComplete: (User?) -> Unit) {
//        viewModelScope.launch {
//            userDao.getUser().collect { user ->
//                MyLog.d(TAG, "getLoginUser() = $user")
//                onComplete(user)
//            }
//        }
//    }

    fun loginTypeCheckUser(user: User, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            //db에 저장된 사용자 정보를 이용하여 로그인 신청.
            MyLog.d(TAG, "checkUser() = $user")
            when (user.loginType) {
                LoginType.EMAIL -> { /* 이메일 로그인 */
                    emailLogin(
                        email = user.email,
                        password = user.password,
                        onComplete = onComplete
                    )
                }

                LoginType.GOOGLE -> { /* 구글 로그인 */
                }

                LoginType.FACEBOOK -> { /* 페이스북 로그인 */
                }
            }

        }
    }


    fun emailLogin(
        email: String,
        password: String,
        saveUserData: Boolean = false,
        onComplete: (Boolean, String) -> Unit
    ) {
        // 파이어베이스 이메일 로그인.
        MyLog.d(TAG, "emailLogin() = $email, $password")
        val auth = FirebaseAuth.getInstance()
        emailSignIn(email = email, password = password) { success, message ->
            if (success) firebaseAuthTokenLogin { success, message ->
                if (success) {
                    auth.currentUser?.let { firebaseUser ->
                        viewModelScope.launch {
                            // 사용자 정보를 데이터베이스에 저장
                            if (saveUserData) userDaoInsert(
                                firebaseUser = firebaseUser,
                                email = email,
                                password = password
                            )
                            onComplete(true, "계정 생성에 성공 하였습니다.")
                        }
                    }
                } else {
                    onComplete(false, message ?: "로그인 실패")
                }
            } else {
                onComplete(false, message ?: "로그인 실패")
            }
        }

    }

    fun emailRegisterUser(email: String, password: String, onComplete: (Boolean, String) -> Unit) {
        // 파이어베이스 이메일 회원가입.
        MyLog.d(TAG, "emailRegisterUser() = $email, $password")
        val auth = FirebaseAuth.getInstance()
        emailSignUp(email, password) { success, message ->
            if (success) firebaseAuthTokenLogin { success, message ->
                if (success) {
                    auth.currentUser?.let { firebaseUser ->
                        onComplete(success, message ?: "회원 가입 성공")
                    }
                } else {
                    onComplete(success, message ?: "회원 가입 실패")
                }
            } else {
                onComplete(success, message)
            }
        }
    }

    suspend fun userDaoInsert(firebaseUser: FirebaseUser, email: String, password: String) {
        // 사용자 정보를 데이터베이스에 저장
        val newUser = User(
            uid = firebaseUser.uid,
            email = email,
            password = password,
            username = firebaseUser.displayName ?: ""
        )
        userDao.insertUser(newUser)
    }

    fun firebaseAuthWithGoogle(idToken: String, onComplete: (Boolean, String) -> Unit) {
        // 파이어베이스 구글 메일 인증
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 성공 처리
                    MyLog.d(TAG, "signInWithCredential:success")
                } else {
                    // 실패 처리
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

//    fun getFirebaseCampingSite(): List<CampingSite> {
//        //파이어베이스 캠핑장 정보 가져오기.
//        val remoteSites = _firebaseCampingSites.value ?: emptyList()
//        MyLog.d(TAG, "getFirebaseCampingSite() Size = ${remoteSites.size}")
//        if (_firebaseCampingSites.value == null) {
            //파이어스토어 데이터베이스에 저장된 캠핑장 정보 가져오기. - 최초에만.
//            getAllFierbaseCampingSites { success, remoteSites ->
//                if (success) {
//                    _firebaseCampingSites.value = remoteSites
//                } else {
//                    Toast.makeText(MyApplication.context, "서버에서 캠핑장 목록 가져오기 실패", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            }
//        }
//    }

    fun syncCampingSites() {
        if(isDeleting) return
        _isLoading.value = true
        MyLog.d(TAG, "syncCampingSites()")
        viewModelScope.launch {
            val dbAllCampingSiteSelect = async { dbAllCampingSiteSelect() }
            val fierbaseCampingSites = async { getAllFirebaseCampingSites() }
            val localSites = dbAllCampingSiteSelect.await()
            val remoteSites = fierbaseCampingSites.await()
            MyLog.d(TAG, "syncCampingSites() localSites.size : ${localSites.size}")
            MyLog.d(TAG, "syncCampingSites() remoteSites.size : ${remoteSites.size}")
            _syncAllCampingList.value = localSites + remoteSites

            val syncCampingSites = CampingDataUtil.syncCampingSites(
                localSites, remoteSites,
                campingSiteRepository, this
            )
            MyLog.d(TAG, "syncCampingSites() syncCampingSites.size : ${syncCampingSites.size}")
            _syncAllCampingList.value = syncCampingSites

            _isLoading.value = false
        }
    }

    // 캠핑장 삭제 플레그 - 삭제중에 데이터 싱크가 일어나지 않게 하기 위해.
    private var isDeleting: Boolean = false

    fun deleteCampingSite(campingSite: CampingSite) {
        isDeleting = true
        dbCampingSiteDelete(campingSite) {
            //db 캠핑장 정보 삭제.
            MyLog.d(TAG, "dbCampingSiteDelete() = $it")
            //파이어스토어 데이터베이스에 저장된 캠핑장 정보 삭제.
            deleteFirebaseCampingSite(campingSite) { success ->
                if (success) {
                    deleteCampingList(campingSite.id)
                    Toast.makeText(MyApplication.context, "삭제 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(MyApplication.context, "삭제 실패", Toast.LENGTH_SHORT).show()
                }
                isDeleting = false
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

}