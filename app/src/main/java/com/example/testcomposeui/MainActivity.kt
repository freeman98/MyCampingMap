package com.example.testcomposeui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testcomposeui.ui.theme.TestComposeUITheme
import com.google.android.gms.maps.MapFragment

class MainActivity : ComponentActivity() {

    companion object {
        open val LOCATION_PERMISSION_REQUEST_CODE = 777
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestComposeUITheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    GreetingWithButton(name = "Android", modifier = Modifier.padding(innerPadding))
//                }
                Surface(color = MaterialTheme.colorScheme.background) {
//                    MyApp(modifier = Modifier.fillMaxSize())
                    MainTopAppBar()
                }

            }
        }

        checkPermission()
    }

    private fun checkPermission() {
        // 위치 권한 확인 및 현재 위치 버튼 활성화 (필요한 경우)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestComposeUITheme {
//        MyApp(modifier = Modifier.fillMaxSize())
        MainTopAppBar()
    }
}