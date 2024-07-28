package com.freeman.mycampingmap.viewmodels

import androidx.lifecycle.viewModelScope
import com.freeman.mycampingmap.auth.FirebaseManager.emailSignIn
import com.freeman.mycampingmap.auth.FirebaseManager.firebaseAuthTokenLogin
import com.freeman.mycampingmap.db.UserFactory.createUser
import com.freeman.mycampingmap.utils.MyLog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginViewModel : BaseViewModel() {

    val TAG = this::class.java.simpleName


//    fun emailLogin(
//        email: String,
//        password: String,
//        saveUserData: Boolean = false,
//        onComplete: (Boolean, String) -> Unit
//    ) {
//        // 파이어베이스 이메일 로그인.
//        MyLog.d(TAG, "emailLogin() = $email, $password")
//        _isLoading.value = true
//        val auth = FirebaseAuth.getInstance()
//        emailSignIn(email = email, password = password) { success, message ->
//            if (success) firebaseAuthTokenLogin { success, message ->
//                if (success) {
//                    auth.currentUser?.let { firebaseUser ->
//                        viewModelScope.launch {
//                            // 사용자 정보를 데이터베이스에 저장
//                            if (saveUserData) createUser(
//                                this,
//                                userDao = userDao,
//                                firebaseUser = firebaseUser,
//                                email = email,
//                                password = password
//                            )
//                            _isLoading.value = false
//                            onComplete(true, "로그인 성공")
//                        }
//                    }
//                } else {
//                    _isLoading.value = false
//                    onComplete(false, message ?: "로그인 실패")
//                }
//            } else {
//                _isLoading.value = false
//                onComplete(false, message ?: "로그인 실패")
//            }
//        }
//
//    }
}