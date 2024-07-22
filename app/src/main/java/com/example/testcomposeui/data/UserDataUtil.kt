package com.example.testcomposeui.data

import android.util.Log
import com.example.testcomposeui.db.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

object UserDataUtil {


    fun firebaseSaveUser(onComplet: (Boolean, User, String) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.let { firebaseUser ->
            // 사용자 정보를 User 객체로 생성
            val user = createUserData(firebaseUser)
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid).set(user)
                .addOnSuccessListener {
                    Log.d("MapScreen", "User successfully written!")
                    onComplet(true, user, "유져 정보 저장 성공")
                }
                .addOnFailureListener { e ->
                    Log.w("MapScreen", "Error writing user", e)
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
}