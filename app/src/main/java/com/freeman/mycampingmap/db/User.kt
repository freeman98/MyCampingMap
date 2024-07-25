package com.freeman.mycampingmap.db

import androidx.room.Entity
import androidx.room.PrimaryKey

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