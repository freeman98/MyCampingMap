package com.freeman.mycampingmap.auth

import android.util.Log
import android.widget.Toast
import com.freeman.mycampingmap.MyApplication
import com.freeman.mycampingmap.data.CampingDataUtil.createCampingSiteData
import com.freeman.mycampingmap.db.CampingSite
import com.freeman.mycampingmap.db.User
import com.freeman.mycampingmap.utils.MyLog
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
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
        onResult: (Boolean, String?) -> Unit) {
        // 파이어베이스 인증 토큰 로그인
        MyLog.d(TAG, "firebaseAuthTokenLogin()")
        // 파이어베이스 인증
        FirebaseAuth.getInstance().currentUser?.getIdToken(true)
            ?.addOnCompleteListener { idTokenTask ->
                if (idTokenTask.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, "계정 인증에 실패 하였습니다.")
                }
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
        onComplete: (Boolean) -> Unit) {
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
        MyLog.d(TAG, "addFirebaseCampingSite() user = $user")

        // 사용자 정보를 User 객체로 생성
        val campingSite = createCampingSiteData(place)
        MyLog.d(TAG, "addFirebaseCampingSite() = $campingSite")

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

    suspend fun getAllFirebaseCampingSites(): List<CampingSite> {
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


}