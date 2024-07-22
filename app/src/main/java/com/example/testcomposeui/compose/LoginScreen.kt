package com.example.testcomposeui.compose

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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.testcomposeui.MyApplication
import com.example.testcomposeui.utils.validateAndLoginCheck
import com.example.testcomposeui.viewmodels.MainViewModel

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: MainViewModel = viewModel()
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            // 로딩 상태일 때
            CircularProgressIndicator()
        } else {

            Button(onClick = {
                // 로그인 처리 로직
                if (!validateAndLoginCheck(email, password)) return@Button

                isLoading = true    // 로딩 상태로 변경
                viewModel.emailLogin(email = email, password = password, saveUserData = true) { success, message ->
                    if (success) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                    isLoading = false    // 로딩 상태 해제
                    Toast.makeText(MyApplication.context, message, Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(text = "로그인")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = {
                navController.navigate(Screen.Signup.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }) {
                Text(text = "회원가입")
            }

        }   //isLoading

    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = NavHostController(LocalContext.current))
}