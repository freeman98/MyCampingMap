package com.freeman.mycampingmap.viewmodels

import android.util.Log
import androidx.activity.result.ActivityResult
import com.freeman.mycampingmap.auth.FirebaseManager.firebaseLoginGoogle
import kotlinx.coroutines.CoroutineScope

class LoginViewModel : BaseViewModel() {

    val TAG = this::class.java.simpleName

    fun loginGoogle(
        activityResult: ActivityResult,
        coroutionScope: CoroutineScope,
        saveUserData: Boolean = false,
        onComplete: (Boolean, String) -> Unit
    ) {

        firebaseLoginGoogle(
            activityResult = activityResult,
            coroutionScope = coroutionScope,
            saveUserData = saveUserData,
            userDao = userDao,
            onComplete = onComplete
        )
    }   //loginGoogle

    fun loginFacebook() {
        // 페이스북 로그인
        Log.d(TAG, "loginFacebook()")

    }

}