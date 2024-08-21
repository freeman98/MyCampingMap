package com.freeman.mycampingmap.auth

import android.content.Context
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import com.freeman.mycampingmap.R
import com.freeman.mycampingmap.utils.MyLog
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject

class FirebaseGoogleSignIn (val context: Context) {

    val TAG = this::class.java.simpleName

    fun getSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(context.resources.getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).build()
    }

    fun beginSignIn(
        signInRequest: BeginSignInRequest,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>)
    {
        MyLog.d(TAG, "beginSignIn()")
        val signInClient: SignInClient = Identity.getSignInClient(context)
        signInClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                MyLog.d(TAG, "Google Sign-In success")
                val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                launcher.launch(intentSenderRequest)
            }
            .addOnFailureListener { e ->
                MyLog.e(TAG, "Google Sign-In failed: ${e.message}")
            }
    }

    fun firebaseAuthWithGoogle(
        onResult: (Boolean, String?) -> Unit,
    ) {
        // 파이어베이스 인증 토큰 로그인
        MyLog.d(TAG, "firebaseAuthWithGoogle()")
        // 파이어베이스 인증
        FirebaseAuth.getInstance().currentUser?.getIdToken(true)
            ?.addOnCompleteListener { idTokenTask ->
                if (idTokenTask.isSuccessful) {
                    val idToken: String = idTokenTask.result?.token ?: ""
                    MyLog.d(TAG, "idToken = $idToken")
                    onResult(true, null)
                } else {
                    onResult(false, "구글 로그인 토큰 만료")
                }
            } ?: run {
            onResult(false, "로그인 실패")
        }
    }   // firebaseAuthTokenLogin()

    // [START auth_with_google]
    fun firebaseAuthWithGoogle(idToken: String, onComplete: (Boolean, FirebaseUser?) -> Unit ) {
        val auth = FirebaseAuth.getInstance()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
//                    updateUI(user)
                    onComplete(true, user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
//                    updateUI(null)
                    onComplete(true, null)
                }
            }
    }
    // [END auth_with_google]
}