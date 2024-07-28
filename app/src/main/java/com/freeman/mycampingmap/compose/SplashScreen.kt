package com.freeman.mycampingmap.compose

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.freeman.mycampingmap.R
import com.freeman.mycampingmap.viewmodels.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SplashScreen(
    navController: NavHostController,
    viewModel: MainViewModel = viewModel()
) {
//    Log.d("Splash", "SplashScreen() ")
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash_animation))
    val lottieAnimatable = rememberLottieAnimatable()
    val user by viewModel.user.observeAsState()

    LaunchedEffect(composition) {
        lottieAnimatable.animate(
            composition = composition,
//            iterations = LottieConstants.IterateForever
            iterations = 1 // 1회만 반복
        )
    }

    val locationPermissionState = rememberPermissionState(
        // 위치 권한
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

//    Log.d("Splash", "user : $user")
    LaunchedEffect(user) {
        if (locationPermissionState.status.isGranted) {
            // 위치 권한이 허용 일때. - 로그인 처리 로직
            user?.let {
                viewModel.loginTypeCheckUser(it) { success, message ->
                    if (success) {
                        Log.d("Splash", "로그인 성공")
                        //로그인 성공시 메인화면으로 이동.
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        Log.d("Splash", "로그인 실패")
                    }
                }
            }
        } else {
            // 위치 권한이 거부 일때.
            locationPermissionState.launchPermissionRequest()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = lottieAnimatable.progress,
            modifier = Modifier.fillMaxSize()
        )

        if (user == null) {
            // 로그인 정보가 없는경우 로그인 버튼 노출
            LoginButton(
                modifier = Modifier.offset(y = 150.dp),
                navController
            )
        }

    }
}

@Composable
fun LoginButton(modifier: Modifier = Modifier, navController: NavHostController) {
    Button(
        onClick = {
            //로그인 화면 이동.
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        },
        modifier = modifier
    ) {
        Text(text = "로그인")
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(navController = NavHostController(LocalContext.current))
}