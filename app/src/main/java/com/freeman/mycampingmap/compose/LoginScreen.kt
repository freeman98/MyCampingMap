package com.freeman.mycampingmap.compose

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.freeman.mycampingmap.App
import com.freeman.mycampingmap.R
import com.freeman.mycampingmap.utils.MyLog
import com.freeman.mycampingmap.utils.validateAndLoginCheck
import com.freeman.mycampingmap.viewmodels.MainViewModel

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    navController: NavHostController,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
                viewModel = viewModel,
                email = email,
                password = password,
                navController = navController
            )
            Spacer(modifier = Modifier.height(8.dp))
            LoginScreenTextButton(navController = navController)
            Spacer(modifier = Modifier.height(8.dp))
            SNSLoginContent(viewModel = viewModel, navController = navController)
        }   //isLoading

    }
}

@Composable
fun SNSLoginContent(
    viewModel: MainViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(modifier = modifier) {
        //구글 로그인
        val launcher = googleRememberLauncherForActivityResult(viewModel, navController)
        SNSLoginIconButton(
            resId = R.drawable.ibtn_light_rd_google,
            onClick = {
                //구글 로그인 초기화
                viewModel.firebaseLoginGoogleInit(launcher)
            }
        )

        SNSLoginIconButton(resId = R.drawable.ibtn_light_facebook,
            onClick = {
                //페이스북 로그인
                viewModel.loginFacebook()

            })
    }
}

@Composable
fun googleRememberLauncherForActivityResult(
    viewModel: MainViewModel,
    navController: NavHostController,
): ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult> {
    //구글 로그인
    val context = LocalContext.current
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        MyLog.d("LoginScreen", "activityResult.resultCode : ${activityResult.resultCode}")
        if (activityResult.resultCode == Activity.RESULT_OK) {

            viewModel.firebaseLoginGoogle(
                activityResult = activityResult,
                saveUserData = true,
            ) { success, message ->
                if (success) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
        } else {
            //구글 로그인 실패 하면 앱종료
            (context as? Activity)?.finishAffinity()
        }
    }
}

@Composable
fun SNSLoginIconButton(resId: Int, onClick: () -> Unit) {
    IconButton(
        modifier = Modifier
            .size(60.dp)
            .padding(5.dp), onClick = onClick
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Google Sign In",
            modifier = Modifier.size(60.dp)
        )
    }
}

@Preview
@Composable
fun SNSLoginContentPreview() {
    SNSLoginContent(
        viewModel = viewModel(),
        navController = rememberNavController(),
        modifier = Modifier
    )
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
    viewModel: MainViewModel,
    email: String,
    password: String,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val onCLickLogin = onClickLogin@{
        // 로그인 처리 로직
        if (!validateAndLoginCheck(context = context, email = email, password = password)) {
            return@onClickLogin
        }

        viewModel.emailPasswdLogin(
            context = context, email = email, password = password, saveUserData = true
        ) { success, message ->
            if (success) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 로그인 버튼
    Button(onClick = onCLickLogin) {
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
    LoginScreen(viewModel(), navController = NavHostController(LocalContext.current))
}