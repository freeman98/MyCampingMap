package com.freeman.mycampingmap.compose

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.freeman.mycampingmap.App
import com.freeman.mycampingmap.utils.validateAndLoginCheck
import com.freeman.mycampingmap.viewmodels.MainViewModel

@Composable
fun SignupScreen(
    viewModel: MainViewModel,
    navController: NavHostController,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailTextField(email = email, emailInput = { email = it })
        Spacer(modifier = Modifier.height(8.dp))
        PasswdTextField(password = password, passwdInput = { password = it })
        Spacer(modifier = Modifier.height(8.dp))
        PasswdTextField(
            password = confirmPassword,
            passwdInput = { confirmPassword = it },
            "비밀번호 확인"
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            // 로딩 상태일 때
            CircularProgressIndicator()
        } else {
            // 로딩이 아닐 때
            // 회원가입 버튼
            SignupButton(
                viewModel = viewModel,
                email = email, password = password,
                confirmPassword = confirmPassword,
                navController = navController,
                isLoading = { isLoading = it })
            Spacer(modifier = Modifier.height(8.dp))
            // 로그인 화면으로 이동
            SignupGotoLginScreenTextButton(navController = navController)
        }

    }
}

@Composable
fun SignupGotoLginScreenTextButton(navController: NavHostController) {
    TextButton(onClick = {
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Signup.route) { inclusive = true }
        }
    }) {
        Text(text = "로그인 화면으로")
    }
}

@Composable
fun SignupButton(
    email: String, password: String, confirmPassword: String, isLoading: (Boolean) -> Unit,
    navController: NavHostController,
    viewModel: MainViewModel
) {
    // 회원가입 버튼
    val context = LocalContext.current
    val onComplete = { success: Boolean, message: String ->
        if (success) {
            // 회원가입 성공
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
        isLoading(false)   // 로딩 상태 해제
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    Button(onClick = {
        // 회원가입 처리 로직
        if (!validateAndLoginCheck(context, email, password, confirmPassword)) return@Button

        isLoading(true)    // 로딩 상태로 변경
        // 회원가입 API 호출
        viewModel.emailRegisterUser(
            email = email,
            password = password,
            onComplete = onComplete
        )
    }) {
        Text(text = "회원가입")
    }
}

@Preview
@Composable
fun SignupScreenPreview() {
    SignupScreen(viewModel(), navController = NavHostController(LocalContext.current))
}