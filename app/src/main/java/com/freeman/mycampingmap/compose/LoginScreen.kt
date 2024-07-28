package com.freeman.mycampingmap.compose

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.freeman.mycampingmap.MyApplication
import com.freeman.mycampingmap.utils.validateAndLoginCheck
import com.freeman.mycampingmap.viewmodels.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavHostController, viewModel: LoginViewModel = viewModel()
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
//    var isLoading by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.observeAsState(false)

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
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            // 로딩 상태일 때
            CircularProgressIndicator()
        } else {
            //로그인
            LoginScreenButton(
                email = email,
                password = password,
                viewModel = viewModel,
                navController = navController
            )
            Spacer(modifier = Modifier.height(8.dp))
            LoginScreenTextButton(navController = navController)
        }   //isLoading

    }
}

@Composable
fun LoginScreenTextButton(navController: NavHostController) {
    TextButton(onClick = {
        navController.navigate(Screen.Signup.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }) {
        Text(text = "회원가입")
    }
}

@Composable
fun LoginScreenButton(
    email: String,
    password: String,
    viewModel: LoginViewModel,
    navController: NavHostController
) {
    // 로그인 버튼
    Button(onClick = {
        // 로그인 처리 로직
        if (!validateAndLoginCheck(email, password)) return@Button
        viewModel.emailLogin(
            email = email, password = password, saveUserData = true
        ) { success, message ->
            if (success) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            Toast.makeText(MyApplication.context, message, Toast.LENGTH_SHORT).show()
        }
    }) {
        Text(text = "로그인")
    }
}

@Composable
fun PasswdTextField(password: String, passwdInput: (String) -> Unit, label: String = "비밀번호") {
    // password
    // 비밀번호 입력 textField에 우측 버튼을 누르면 입력한 내용이 보이고 안보이게 하기 위해
    var shouldShowPassword by remember { mutableStateOf(false) }

    // shouldShowPassword의 값에 따라 이미지를 지정
//    val passwordResource : (Boolean) -> Int = {
//        if(it) { // true
//            R.drawable.baseline_visibility_24
//        }else{
//            R.drawable.baseline_visibility_off_24
//        }
//    }

    OutlinedTextField(modifier = Modifier.fillMaxWidth(),
        value = password,
        singleLine = true,
        leadingIcon = {
            Icon(  // 왼쪽 Icon 지정
                imageVector = Icons.Default.Person, contentDescription = null
            )
        },
//        trailingIcon = {
//            IconButton(onClick = {
//                // 버튼이 눌려지면 비밀번호가 보이도록
//                shouldShowPassword = !shouldShowPassword
//            }) {
//                Icon(
//                    imageVector = Icons.Default., contentDescription = null
//                )
//            }
//        },
        // 비밀번호가
        visualTransformation = if (shouldShowPassword) VisualTransformation.None
        else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // 키보드 타입 지정
        onValueChange = { passwdInput(it) },
        label = { Text(label) },
        placeholder = { Text("password") })
}

@Preview
@Composable
fun PasswdTextFieldPreview() {
    PasswdTextField(password = "test", passwdInput = {})
}

@Composable
fun EmailTextField(email: String, emailInput: (String) -> Unit) {
    OutlinedTextField(modifier = Modifier.fillMaxWidth(),
        value = email,
        singleLine = true,
        leadingIcon = {
            Icon(  // 왼쪽 Icon 지정
                imageVector = Icons.Default.Email, contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = {}) {
                Icon( // 오른쪽에 Icon 지정
                    imageVector = Icons.Default.CheckCircle, contentDescription = null
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // 키보드 타입 지정
        onValueChange = { emailInput(it) },   // 입력값 변경
        label = { Text(text = "이메일") }, // 라벨
        placeholder = { Text(" @ .com") }   // 힌트
    )
}

@Preview
@Composable
fun EmailTextFieldPreview() {
    EmailTextField(email = "test", emailInput = {})
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = NavHostController(LocalContext.current))
}