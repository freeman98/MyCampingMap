package com.freeman.mycampingmap.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import com.freeman.mycampingmap.data.CampingDataUtil
import com.freeman.mycampingmap.db.CampingSite
import com.freeman.mycampingmap.db.LoginType
import com.freeman.mycampingmap.db.User
import com.freeman.mycampingmap.db.UserFactory.createUser
import com.freeman.mycampingmap.utils.MyLog
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseManager(
    val context: Context,
    private var firebaseGoogleSignIn: FirebaseGoogleSignIn
) {

    private val TAG = this::class.java.simpleName

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

    fun createUserData(firebaseUser: FirebaseUser): User {
        // 사용자 정보를 User 객체로 생성
        return User(
            uid = firebaseUser.uid,
            username = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: ""
        )
    }

    fun addFirebaseCampingSites(
        campingSites: List<CampingSite>,
        onComplete: (Boolean) -> Unit,
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

    fun addFirebaseCampingSite(
        campingDataUtil: CampingDataUtil,
        user: User, place: Place,
    ) {
        //파이어스토어 데이터베이스에 저장.
//        MyLog.d(TAG, "addFirebaseCampingSite() user = $user")

        // 사용자 정보를 User 객체로 생성
        val campingSite = campingDataUtil.createCampingSiteData(place)
//        MyLog.d(TAG, "addFirebaseCampingSite() = $campingSite")

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(user.uid)
            .collection("my_camping_list")
            .document(campingSite.id).set(campingSite)
            .addOnSuccessListener {
                // 데이터 저장 성공
                Log.d("", "addOnSuccessListener()")
                Toast.makeText(context, "저장 성공", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // 데이터 저장 실패
                Log.w("", "addOnFailureListener() = ", e)
                Toast.makeText(context, "저장 실패", Toast.LENGTH_SHORT).show()
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
        campingSite: CampingSite, onComplete: (Boolean) -> Unit,
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

    private fun firebaseAuthWithGoogle(
        idToken: String,
        saveUserData: Boolean = false,
        onComplete: (Boolean, String) -> Unit,
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
                            context,
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
//        firebaseGoogleSignIn: FirebaseGoogleSignIn,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    ) {
        // 구글 로그인 초기화
        val signInRequest = firebaseGoogleSignIn.getSignInRequest()
        // [구글 로그인 시작]
        firebaseGoogleSignIn.beginSignIn(signInRequest, launcher)
    }

    fun firebaseLoginGoogle(
//        firebaseGoogleSignIn: FirebaseGoogleSignIn,
        activityResult: ActivityResult,
        saveUserData: Boolean = false,
        onComplete: (Boolean, String) -> Unit,
    ) {
        MyLog.d(TAG, "firebaseLoginGoogle()")

        val signInClient: SignInClient = Identity.getSignInClient(context)
        val credential = signInClient.getSignInCredentialFromIntent(activityResult.data)
        val idToken = credential.googleIdToken
        MyLog.d(TAG, "loginGoogle() idToken = $idToken")
        when {
            idToken != null -> {
                firebaseGoogleSignIn.firebaseAuthWithGoogle(idToken) { success, user ->
                    if (success) {
                        if (saveUserData) {
                            dbUpdateUser(idToken, user) { _, _ -> }
                        }
                        onComplete(true, "")
                    } else {
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
    }   //loginGoogle

    private fun dbUpdateUser(
        idToken: String,
        user: FirebaseUser?,
        onComplete: (Boolean, String) -> Unit,
    ) {
        MyLog.d(TAG, "loginGoogle() idToken = $idToken")
        MyLog.d(TAG, "loginGoogle() user.udi = ${user?.uid}")
        MyLog.d(TAG, "loginGoogle() user.email = ${user?.email}")
        MyLog.d(TAG, "loginGoogle() user.displayName = ${user?.displayName}")
        val u = createUser(
            context,
            uid = user?.uid ?: "",
            email = user?.email ?: "",
            username = user?.displayName ?: "",
            idToken = idToken,
            loginType = LoginType.GOOGLE
        )
        MyLog.d("firebaseAuthWithGoogle() createUser = $u")
        onComplete(true, "User 정보 저장 성공")
    }

}