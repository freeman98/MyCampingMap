package com.freeman.mycampingmap.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey val uid: String,
    val email: String,
    val password: String = "",
    val username: String = "",
    val loginType: LoginType = LoginType.EMAIL
)

enum class LoginType {
    EMAIL,
    GOOGLE,
    FACEBOOK
}

object UserFactory {
    suspend fun createUser(
        coroutinScope: CoroutineScope,
        userDao: UserDao,
        firebaseUser: FirebaseUser,
        email: String,
        password: String
    ) {
        // 사용자 정보를 데이터베이스에 저장
        var user = User(
            uid = firebaseUser.uid,
            email = email,
            password = password
        )
        coroutinScope.launch {
            userDao.insertUser(user)
        }
    }
}
