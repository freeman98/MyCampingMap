package com.example.testcomposeui.data

import androidx.compose.runtime.Immutable

@Immutable  //절대적으로 변경되지 않음을 정의.
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = ""
)

