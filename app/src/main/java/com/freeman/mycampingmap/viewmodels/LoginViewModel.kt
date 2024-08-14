package com.freeman.mycampingmap.viewmodels

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(): BaseViewModel() {

    val TAG = this::class.java.simpleName

    fun loginFacebook() {
        // 페이스북 로그인
        Log.d(TAG, "loginFacebook()")

    }

}