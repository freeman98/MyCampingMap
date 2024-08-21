package com.freeman.mycampingmap.utils

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import com.freeman.mycampingmap.App

fun validateAndLoginCheck(
    context: Context,
    email: String,
    password: String,
    confirmPassword: String? = null
): Boolean {
    // 이메일과 비밀번호를 검증
    var check = true
    var message = ""
    if (email.isEmpty() || password.isEmpty()) {
        // 이메일과 비밀번호를 모두 입력해야 합니다.
        message = "이메일과 비밀번호를 모두 입력해야 합니다."
        check = false
    } else if (!isValidEmail(email)) {
        // 이메일 형식이 잘못된 경우
        message = "이메일 형식을 확인해 주세요."
        check = false
    } else if (!isValidPassword(password)) {
        // 비밀번호가 6자리 이상이 아닌 경우
        message = "비밀번호가 6자리 이상이 입력해 주세요"
        check = false
    } else {
        confirmPassword?.let {
            if (it != password) {
                // 비밀번호가 일치하지 않는 경우
                message = "비밀번호가 일치하지 않습니다."
                check = false
            }
        }
    }
    if (!check) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    return check
}

fun isValidEmail(email: String): Boolean {
    // 이메일 형식 검증
    val emailPattern = Patterns.EMAIL_ADDRESS
    return emailPattern.matcher(email).matches()
}

fun isValidPassword(password: String): Boolean {
    // 비밀번호 형식 검증
    return password.length >= 6
}