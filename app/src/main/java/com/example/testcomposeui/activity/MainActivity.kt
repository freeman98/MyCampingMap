package com.example.testcomposeui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.testcomposeui.compose.MainTopAppBar
import com.example.testcomposeui.data.Const.Companion.RC_SIGN_IN
import com.example.testcomposeui.ui.theme.TestComposeUITheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : BaseActivity() {

    val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            TestComposeUITheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainTopAppBar()
                }
            }
        }

//        checkPermission()
//        signIn()
    }


//    private fun signIn() {
//        //Gmail 로그인.
//        val signInIntent = googleSignInClient.signInIntent
//        startActivityForResult(signInIntent, RC_SIGN_IN)
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
////        super.onActivityResult(requestCode, resultCode, data)
//        //로그인 결과.
//
//        if (requestCode == RC_SIGN_IN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                val account = task.getResult(ApiException::class.java)!!
//                firebaseAuthWithGoogle(account.idToken!!)
//            } catch (e: ApiException) {
//                Toast.makeText(this, "Google 로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

//    private fun firebaseAuthWithGoogle(idToken: String) {
////        Log.d(TAG, "firebaseAuthWithGoogle() idToken = $idToken")
//        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    val user = auth.currentUser
//                    Log.d(TAG, "firebaseAuthWithGoogle() user?.uid = ${user?.uid}")
//                    Toast.makeText(this, "로그인 성공: ${user?.displayName}", Toast.LENGTH_SHORT).show()
//                    // 로그인 성공 후 다음 화면으로 이동
//                } else {
//                    Toast.makeText(this, "인증 실패", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestComposeUITheme {
//        MyApp(modifier = Modifier.fillMaxSize())
//        MainTopAppBar(requestPermissionLauncher = )
    }
}