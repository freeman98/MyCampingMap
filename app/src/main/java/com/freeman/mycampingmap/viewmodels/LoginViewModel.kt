package com.freeman.mycampingmap.viewmodels

import android.util.Log

class LoginViewModel : BaseViewModel() {

    val TAG = this::class.java.simpleName

    fun loginFacebook() {
        // 페이스북 로그인
        Log.d(TAG, "loginFacebook()")

    }

}