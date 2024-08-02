package com.freeman.mycampingmap.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.freeman.mycampingmap.MyApplication
import com.freeman.mycampingmap.utils.MyLog
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey val uid: String,
    val email: String,
    val password: String = "",
    val username: String = "",
    val idToken: String = "",
    val loginType: LoginType = LoginType.EMAIL
)

enum class LoginType {
    EMAIL,
    GOOGLE,
    FACEBOOK
}

object UserFactory {
    fun createUser(
        coroutinScope: CoroutineScope,
        uid: String,
        email: String,
        password: String = "",
        username: String = "",
        idToken: String = "",
        loginType: LoginType = LoginType.EMAIL
    ): User {
        // 사용자 정보를 데이터베이스에 저장
        MyLog.d("createUser() = $uid, $email, $password, $username, $idToken, $loginType")
        val user = User(
            uid = uid,
            email = email,
            password = password,
            username = username,
            idToken = idToken,
            loginType = loginType
        )
//        userDao.insertUser(user)
        coroutinScope.launch(Dispatchers.IO) {
            UserDatabase.getDatabase(MyApplication.context).userDao().insertUser(user)
//            userDao.insertUser(user)
        }
        return user
    }
}
