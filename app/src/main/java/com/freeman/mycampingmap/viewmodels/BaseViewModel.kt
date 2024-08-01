package com.freeman.mycampingmap.viewmodels

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.freeman.mycampingmap.MyApplication
import com.freeman.mycampingmap.auth.FirebaseManager.emailSignIn
import com.freeman.mycampingmap.auth.FirebaseManager.firebaseAuthTokenLogin
import com.freeman.mycampingmap.db.CampingSite
import com.freeman.mycampingmap.db.CampingSiteDatabase
import com.freeman.mycampingmap.db.CampingSiteRepository
import com.freeman.mycampingmap.db.LoginType
import com.freeman.mycampingmap.db.User
import com.freeman.mycampingmap.db.UserDao
import com.freeman.mycampingmap.db.UserDatabase
import com.freeman.mycampingmap.db.UserFactory.createUser
import com.freeman.mycampingmap.utils.MyLog
import com.freeman.mycampingmap.viewmodels.BaseViewModel.LiveDataBus._selectCampingSite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseViewModel : ViewModel() {

    //LiveData를 이용한 이벤트 버스
    object LiveDataBus {

        //메인화면에서 선택한 캠핑장 1회성 이벤트
        val _selectCampingSite: MutableLiveData<Event<CampingSite?>> = MutableLiveData<Event<CampingSite?>>()
        val selectCampingSite: LiveData<Event<CampingSite?>> = _selectCampingSite

        //내 위치.
        val _currentMyLocation = MutableLiveData<Location?>()
        val currentMyLocation: LiveData<Location?> get() = _currentMyLocation

    }

    val userDao: UserDao = UserDatabase.getDatabase(MyApplication.context).userDao()
    val user: LiveData<User?> = userDao.getUser().asLiveData()

    // 내 캠핑장 리스트 정보
    open val _syncAllCampingList = MutableLiveData<List<CampingSite>>()
    val syncAllCampingList: LiveData<List<CampingSite>> = _syncAllCampingList

    //로딩 컨트롤을 위한 LiveData
    open val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    open fun selectCampingSite(campingSite: CampingSite) {
        _selectCampingSite.value = Event(campingSite)
    }

    // LiveData를 이용한 1회성 이벤트
    class Event<out T>(private val content: T) {

        private var hasBeenHandled = false

        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }

        fun peekContent(): T = content
    }

    val campingSiteRepository: CampingSiteRepository
//    val dbAllCampingSites: LiveData<List<CampingSite>>

    init {
        val campingSiteDao = CampingSiteDatabase.getDatabase(MyApplication.context).campingSiteDao()
        campingSiteRepository = CampingSiteRepository(campingSiteDao)
//        dbAllCampingSites = campingSiteRepository.allCampingSites
    }

    suspend fun dbAllCampingSiteSelect(): List<CampingSite> {
        // 캠핑장 전체 조회
        return campingSiteRepository.allCampingList()
    }

    fun dbCampingSiteInsert(
        campingSite: CampingSite,
        onSuccess: (Boolean) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        // 캠핑장 추가
        try {
            campingSiteRepository.insert(campingSite)
            withContext(Dispatchers.Main) {
                onSuccess(true)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onSuccess(false)
            }
        }
    }

    fun dbCampingSiteDelete(
        campingSite: CampingSite,
        onSuccess: (Boolean) -> Unit,
    ) = viewModelScope.launch(Dispatchers.IO) {
        // 캠핑장 삭제
        try {
            campingSiteRepository.delete(campingSite)
            withContext(Dispatchers.Main) {
                onSuccess(true)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onSuccess(false)
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
        MyLog.d("", "emailLogin() = $email, $password")
        _isLoading.value = true
        val auth = FirebaseAuth.getInstance()
        emailSignIn(email = email, password = password) { success, message ->
            if (success) firebaseAuthTokenLogin { success, message ->
                if (success) {
                    auth.currentUser?.let { firebaseUser ->
                        viewModelScope.launch {
                            // 사용자 정보를 데이터베이스에 저장
                            if (saveUserData) createUser(
                                this,
                                userDao = userDao,
                                uid = firebaseUser.uid,
                                email = email,
                                password = password
                            )
                            _isLoading.value = false
                            onComplete(true, "로그인 성공")
                        }   //viewModelScope.launch
                    }
                } else {
                    _isLoading.value = false
                    onComplete(false, message ?: "로그인 실패")
                }
            } else {
                _isLoading.value = false
                onComplete(false, message ?: "로그인 실패")
            }
        }

    }

}