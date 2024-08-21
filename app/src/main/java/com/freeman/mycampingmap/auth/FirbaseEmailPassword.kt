package com.freeman.mycampingmap.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.freeman.mycampingmap.utils.MyLog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import javax.inject.Inject

class FirbaseEmailPassword (val context: Context) {

    val TAG = this::class.java.simpleName

    private var auth: FirebaseAuth = Firebase.auth

    fun createAccount(email: String, password: String, onComplete: (Boolean, String) -> Unit) {
        // [START create_user_with_email]
        MyLog.d(TAG, "emailRegisterUser() = $email, $password")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
//                    updateUI(user)
                    onComplete(true, "회원 가입 성공")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        context,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
//                    updateUI(null)
                    onComplete(false, "회원 가입 실패")
                }
            }
        // [END create_user_with_email]
    }

    fun signIn(email: String, password: String, onComplete: (Boolean, FirebaseUser?) -> Unit) {
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
//                    updateUI(user)
                    onComplete(true, user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        context,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
//                    updateUI(null)
                    onComplete(true, null)
                }
            }
        // [END sign_in_with_email]
    }

    private fun sendEmailVerification() {
        // [START send_email_verification]
        val user = auth.currentUser!!
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                // Email Verification sent
            }
        // [END send_email_verification]
    }

    private fun updateUI(user: FirebaseUser?) {

    }


}