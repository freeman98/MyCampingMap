package com.freeman.mycampingmap.viewmodels

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freeman.mycampingmap.App
import com.freeman.mycampingmap.auth.FirbaseEmailPassword
import com.freeman.mycampingmap.auth.FirebaseGoogleSignIn
import com.freeman.mycampingmap.auth.FirebaseManager
import com.freeman.mycampingmap.data.CampingDataUtil
import com.freeman.mycampingmap.db.CampingSite
import com.freeman.mycampingmap.db.CampingSiteDatabase
import com.freeman.mycampingmap.db.CampingSiteRepository
import com.freeman.mycampingmap.db.UserDatabase
import com.freeman.mycampingmap.db.UserFactory.createUser
import com.freeman.mycampingmap.db.UserRepository
import com.freeman.mycampingmap.utils.MyLog
import com.freeman.mycampingmap.viewmodels.BaseViewModel.LiveDataBus._selectCampingSite
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class BaseViewModel @Inject constructor(
    @ApplicationContext open val context: Context,
) : ViewModel() {

    @Inject
    lateinit var firebaseManager: FirebaseManager
    @Inject
    lateinit var firebaseGoogleSignIn: FirebaseGoogleSignIn
    @Inject
    lateinit var firebaseEmailPassword: FirbaseEmailPassword
    @Inject
    lateinit var campingDataUtil: CampingDataUtil

    //LiveData를 이용한 이벤트 버스
    object LiveDataBus {

        //메인화면에서 선택한 캠핑장 1회성 이벤트
        val _selectCampingSite: MutableLiveData<Event<CampingSite?>> =
            MutableLiveData<Event<CampingSite?>>()
        val selectCampingSite: LiveData<Event<CampingSite?>> = _selectCampingSite

        //내 위치.
        val _currentMyLocation = MutableLiveData<Location?>()
        val currentMyLocation: LiveData<Location?> = _currentMyLocation
    }

    var userRepository: UserRepository
    val campingSiteRepository: CampingSiteRepository

    init {
        MyLog.d("", "BaseViewModel() init")
        val userDao = UserDatabase.getDatabase(App.appContext).userDao()
        userRepository = UserRepository(userDao)

        val campingSiteDao = CampingSiteDatabase.getDatabase(App.appContext).campingSiteDao()
        campingSiteRepository = CampingSiteRepository(campingSiteDao)
    }

    suspend fun asyncDBAllCampingSiteSelect(): List<CampingSite> {
        // 캠핑장 전체 조회
        return campingSiteRepository.getAllCampingSites()
    }

    fun dbAllDeleteCampingSite() {
        campingSiteRepository.allDelete()
    }

    fun dbCampingSiteInsert(
        campingSite: CampingSite,
        onSuccess: (Boolean) -> Unit,
    ) {
        // 캠핑장 추가
        try {
            campingSiteRepository.insert(campingSite)
            onSuccess(true)
        } catch (e: Exception) {
            onSuccess(false)
        }
    }

    fun dbCampingSiteDelete(
        campingSite: CampingSite,
        onSuccess: (Boolean) -> Unit,
    ) {
        // 캠핑장 삭제
        try {
            campingSiteRepository.delete(campingSite)
            onSuccess(true)
        } catch (e: Exception) {
            onSuccess(false)
        }

    }

    // 내 캠핑장 리스트 정보
    val _syncAllCampingList = MutableLiveData<List<CampingSite>>()
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

    fun emailPasswdLogin(
        context: Context,
        email: String,
        password: String,
        saveUserData: Boolean = false,
        onComplete: (Boolean, String) -> Unit,
    ) {
        // 파이어베이스 이메일 로그인.
        MyLog.d("", "emailPasswdLogin() = $email, $password")
        _isLoading.value = true
        firebaseEmailPassword.signIn(email, password) { success, user ->
            if (success) {
                user?.let {
                    viewModelScope.launch {
                        if (saveUserData) {
                            createUser(
                                context,
                                uid = user.uid,
                                email = email,
                                password = password
                            )
                        }
                        _isLoading.value = false
                        onComplete(true, "로그인 성공")
                    }   //viewModelScope.launch
                }
            } else {
                _isLoading.value = false
                onComplete(false, "로그인 실패")
            }
        }

    }

}