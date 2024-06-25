package com.example.testcomposeui

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Observer
import com.example.testcomposeui.ui.theme.TestComposeUITheme

class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        BaseViewModel.LiveDataBus._selectUser.observe(this, Observer { user ->
            // User 데이터 사용
            user?.let { Log.d(TAG, "onCreate: $user") }
        })

        setContent {
            TestComposeUITheme {
                MapScreen()
            }
        }

    }
}
