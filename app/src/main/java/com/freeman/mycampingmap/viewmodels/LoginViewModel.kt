package com.freeman.mycampingmap.viewmodels

import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.viewModelScope
import com.freeman.mycampingmap.auth.FirebaseManager.firebaseAuthWithGoogle
import com.freeman.mycampingmap.db.LoginType
import com.freeman.mycampingmap.db.UserFactory.createUser
import com.freeman.mycampingmap.utils.MyLog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel : BaseViewModel() {

    val TAG = this::class.java.simpleName

    fun loginGoogle(
        activityResult: ActivityResult,
        coroutionScope: CoroutineScope,
        saveUserData: Boolean = false,
        onComplete: (Boolean, String) -> Unit
    ) {
        MyLog.d(TAG, "loginGoogle()")
        val task = GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
        task.addOnCompleteListener { completedTask ->
            try {
                val account = completedTask.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken, saveUserData, userDao,coroutionScope) { success, message ->
                        Log.d(TAG, "loginGoogle() firebaseAuthWithGoogle : $success")
                        if (success) onComplete(true, "")
                    }
//                    coroutionScope.launch(Dispatchers.IO) {
//                        firebaseAuthWithGoogle(idToken, saveUserData, userDao,this) { success, message ->
//                            Log.d(TAG, "loginGoogle() firebaseAuthWithGoogle : $success")
//                            if (success) onComplete(true, "")
//                        }
//                    }
//                    onComplete(true, "")
                } else {
                    MyLog.e("Google ID Token is null")
                    onComplete(false, "")
                }
            } catch (e: ApiException) {
                MyLog.e("Google sign in failed: ${e.statusCode} ${e.status}")
                onComplete(false, "")
            }
        }
    }   //loginGoogle

    fun loginFacebook() {
        // 페이스북 로그인
        Log.d(TAG, "loginFacebook()")

    }

//    fun loginGoogleInit(
//        context: Context,
//        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
//    ) {
//        val googleSignInOptions = GoogleSignInOptions
//            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(context.resources.getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//
//        val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
//        launcher.launch(googleSignInClient.signInIntent)
//    }

}