package com.example.testcomposeui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.testcomposeui.compose.NavGraph
import com.example.testcomposeui.ui.theme.TestComposeUITheme

class MainActivity : BaseActivity() {

    val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            TestComposeUITheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }

    }


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
        val navController = rememberNavController()
        NavGraph(navController = navController)
    }
}