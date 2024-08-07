package com.freeman.mycampingmap.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import com.freeman.mycampingmap.MyApplication
import com.freeman.mycampingmap.R
import com.freeman.mycampingmap.data.CampingDataUtil.createCampingSiteData
import com.freeman.mycampingmap.db.CampingSite
import com.freeman.mycampingmap.db.LoginType
import com.freeman.mycampingmap.db.User
import com.freeman.mycampingmap.db.UserFactory.createUser
import com.freeman.mycampingmap.utils.MyLog
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object FirebaseManager {

    private const val TAG = "FirebaseAuthManager"

    fun emailSignIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        // 파이어베이스 이메일 로그인
        MyLog.d(TAG, "emailSignIn() : email = $email, password = $password")

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun emailSignUp(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        // 파이어베이스 이메일 회원가입
        MyLog.d(TAG, "emailSignUp() : email = $email, password = $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "회원 가입 성공.")
                } else {
                    //가입 실패.
                    try {
                        throw task.exception!!
                    } catch (existEmail: FirebaseAuthUserCollisionException) {
                        // 이메일이 이미 사용 중임을 사용자에게 알림
                        onResult(false, "이 이메일 주소는 이미 사용 중입니다.")
                    } catch (e: Exception) {
                        // 기타 잠재적인 예외 처리
                        onResult(false, "등록 실패")
                    }
                }
            }
    }

    fun firebaseAuthTokenLogin(
        onResult: (Boolean, String?) -> Unit
    ) {
        // 파이어베이스 인증 토큰 로그인
        MyLog.d(TAG, "firebaseAuthTokenLogin()")
        // 파이어베이스 인증
        FirebaseAuth.getInstance().currentUser?.getIdToken(true)
            ?.addOnCompleteListener { idTokenTask ->
                if (idTokenTask.isSuccessful) {
                    val idToken: String = idTokenTask.result?.token ?: ""
                    Log.d(TAG, "idToken = $idToken")
                    onResult(true, null)
                } else {
                    onResult(false, "구글 로그인 토큰 만료")
                }
            } ?: run {
            onResult(false, "로그인 실패")
        }
    }

    fun firebaseSaveUser(onComplet: (Boolean, User, String) -> Unit) {
        // 파이어베이스 사용자 정보 저장
        MyLog.d(TAG, "firebaseSaveUser()")

        FirebaseAuth.getInstance().currentUser?.let { firebaseUser ->
            // 사용자 정보를 User 객체로 생성
            val user = createUserData(firebaseUser)
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid).set(user)
                .addOnSuccessListener {
                    MyLog.d(TAG, "User successfully written!")
                    onComplet(true, user, "유져 정보 저장 성공")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error writing user", e)
                    onComplet(false, user, "유져 정보 저장 실패")
                }
        }
    }

    private fun createUserData(firebaseUser: FirebaseUser): User {
        // 사용자 정보를 User 객체로 생성
        return User(
            uid = firebaseUser.uid,
            username = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: ""
        )
    }

    fun addFirebaseCampingSites(
        campingSites: List<CampingSite>,
        onComplete: (Boolean) -> Unit
    ) {
        //파이어스토어 데이터베이스에 캠핑장 리스트 일괄 저장.
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            MyLog.d(TAG, "addFirebaseCampingSites() = $uid")
            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()
            //파이어스토어 데이터베이스에 캠핑장 리스트 일괄 저장.
            campingSites.forEach { campingSite ->
                batch.set(
                    db.collection("users").document(uid)
                        .collection("my_camping_list").document(campingSite.id), campingSite
                )
            }
            batch.commit()
                .addOnSuccessListener {
                    MyLog.d(TAG, "Batch write succeeded.")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Batch write failed.", e)
                    onComplete(false)
                }
        }
    }

    fun addFirebaseCampingSite(user: User, place: Place) {
        //파이어스토어 데이터베이스에 저장.
//        MyLog.d(TAG, "addFirebaseCampingSite() user = $user")

        // 사용자 정보를 User 객체로 생성
        val campingSite = createCampingSiteData(place)
//        MyLog.d(TAG, "addFirebaseCampingSite() = $campingSite")

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(user.uid)
            .collection("my_camping_list")
            .document(campingSite.id).set(campingSite)
            .addOnSuccessListener {
                // 데이터 저장 성공
                Log.d("", "addOnSuccessListener()")
                Toast.makeText(MyApplication.context, "저장 성공", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // 데이터 저장 실패
                Log.w("", "addOnFailureListener() = ", e)
                Toast.makeText(MyApplication.context, "저장 실패", Toast.LENGTH_SHORT).show()
            }
    }

    fun checkUserExists(userId: String, onResult: (Boolean) -> Unit) {
        // 파이어스토어 데이터베이스에 기존 회원인지 체크.
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onResult(true) // 기존 회원
                } else {
                    onResult(false) // 최초 가입
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting document: ", exception)
                onResult(false) // 오류 발생 시 최초 가입으로 간주
            }
    }

    fun deleteFirebaseCampingSite(
        campingSite: CampingSite, onComplete: (Boolean) -> Unit
    ) {
        //파이어스토어 데이터베이스에 저장된 캠핑장 정보 삭제.
//        MyLog.d(TAG, "deleteFirebaseCampingSite() campingSite = $campingSite")

        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            MyLog.d(TAG, "deleteFirebaseCampingSite() = $uid")
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid)
                .collection("my_camping_list").document(campingSite.id)
                .delete()
                .addOnSuccessListener {
                    MyLog.d(TAG, "DocumentSnapshot successfully deleted!")
                    // 삭제 성공 처리
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    println("Error deleting document: $e")
                    // 삭제 실패 처리
                    onComplete(false)
                }
        }
    }

    suspend fun asyncAllFirebaseCampingSites(): List<CampingSite> {
        return suspendCoroutine { continuation ->
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(uid)
                    .collection("my_camping_list")
                    .get()
                    .addOnSuccessListener { result ->
                        val campingSites = mutableListOf<CampingSite>()
                        for (document in result) {
                            val campingSite = document.toObject(CampingSite::class.java)
                            campingSites.add(campingSite)
                        }
                        continuation.resume(campingSites)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error getting documents: ", e)
                        continuation.resume(emptyList())
                    }
            } else {
                continuation.resume(emptyList())
            }
        }
    }

    fun firebaseAuthWithGoogle(
        idToken: String,
        saveUserData: Boolean = false,
//        coroutineScope: CoroutineScope,
        onComplete: (Boolean, String) -> Unit
    ) {
        // 파이어베이스 구글 메일 인증
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    MyLog.d("signInWithCredential:success saveUserData = $saveUserData")
                    // 성공 처리
                    val uid = task.result?.user?.uid
                    val email = task.result?.user?.email
                    val displayName = task.result?.user?.displayName

                    if (saveUserData) {
                        MyLog.d("task.result?.user = ${task.result?.user?.uid}")
                        val u = createUser(
//                            coroutinScope = coroutineScope,
                            uid = uid ?: "",
                            email = email ?: "",
                            username = displayName ?: "",
                            idToken = idToken,
                            loginType = LoginType.GOOGLE
                        )
                        MyLog.d("firebaseAuthWithGoogle() createUser = $u")
                    }
                    onComplete(true, "")
                } else {
                    // 실패 처리
                    Log.w("signInWithCredential:failure", task.exception)
                    onComplete(false, "")
                }
            }
    }

    fun firebaseLoginGoogleInit(
        context: Context,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) {
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(context.resources.getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

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


//        val googleSignInOptions = GoogleSignInOptions
//            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(context.resources.getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//
//        val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
//        launcher.launch(googleSignInClient.signInIntent)
    }

    fun firebaseLoginGoogle(
        activityResult: ActivityResult,
//        coroutionScope: CoroutineScope,
        saveUserData: Boolean = false,
        onComplete: (Boolean, String) -> Unit
    ) {
        MyLog.d(TAG, "loginGoogle()")
        val signInClient: SignInClient = Identity.getSignInClient(MyApplication.context)
        val credential = signInClient.getSignInCredentialFromIntent(activityResult.data)
        val idToken = credential.googleIdToken
        MyLog.d(TAG, "loginGoogle() idToken = $idToken")
        when {
            idToken != null -> {
                // Got an ID token from Google. Use it to authenticate
                // with Firebase.
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success")
                            val user = FirebaseAuth.getInstance().currentUser
//                            updateUser(idToken, user, coroutionScope, saveUserData) { success, message ->
                            updateUser(idToken, user, saveUserData) { success, message ->

                            if (success) onComplete(true, "")
                                else onComplete(false, "")
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.exception)
                            onComplete(false, "")
                        }
                    }
            }

            else -> {
                // Shouldn't happen.
                Log.d(TAG, "No ID token!")
                onComplete(false, "")
            }
        }

//        val task = GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
//        task.addOnCompleteListener { completedTask ->
//            MyLog.d(TAG, "loginGoogle() addOnCompleteListener()")
//            try {
//                val account = completedTask.getResult(ApiException::class.java)
//                val idToken = account.idToken
//                MyLog.d(TAG, "loginGoogle() addOnCompleteListener() idToken = $idToken")
//                if (idToken != null) {
//                    firebaseAuthWithGoogle(idToken, saveUserData, userDao, coroutionScope) { success, message ->
//                        Log.d(TAG, "loginGoogle() firebaseAuthWithGoogle : $success")
//                        if (success) onComplete(true, "")
//                    }
//                } else {
//                    MyLog.e("Google ID Token is null")
//                    onComplete(false, "")
//                }
//            } catch (e: ApiException) {
//                MyLog.e("Google sign in failed: ${e.statusCode} ${e.status}")
//                Toast.makeText(MyApplication.context, "구글 로그인 실패: ${e.statusCode}", Toast.LENGTH_SHORT).show()
//                onComplete(false, "")
//            }
//        }
    }   //loginGoogle

    private fun updateUser(
        idToken: String,
        user: FirebaseUser?,
//        coroutionScope: CoroutineScope,
        saveUserData:Boolean = false,
        onComplete: (Boolean, String) -> Unit
    ) {
        MyLog.d(TAG, "loginGoogle() idToken = $idToken")
        MyLog.d(TAG, "loginGoogle() user.udi = ${user?.uid}")
        MyLog.d(TAG, "loginGoogle() user.email = ${user?.email}")
        MyLog.d(TAG, "loginGoogle() user.displayName = ${user?.displayName}")

        firebaseAuthWithGoogle(
            idToken,
            saveUserData,
//            coroutionScope
        ) { success, message ->
            Log.d(TAG, "loginGoogle() firebaseAuthWithGoogle : $success")
            if (success) onComplete(true, "")
        }

    }

}