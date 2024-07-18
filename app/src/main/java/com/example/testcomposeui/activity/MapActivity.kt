package com.example.testcomposeui.activity

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testcomposeui.MyApplication
import com.example.testcomposeui.compose.MapScreen
import com.example.testcomposeui.ui.theme.TestComposeUITheme
import com.example.testcomposeui.viewmodels.BaseViewModel
import com.example.testcomposeui.viewmodels.MapViewModel

class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        BaseViewModel.LiveDataBus.selectCampingSite.observe(this, Observer { user ->
            // User 데이터 사용
            user?.let {
                Log.d(TAG, "onCreate: $it")
            }
        })

        setContent {
            TestComposeUITheme {
                val context = LocalContext.current
                val viewModel: MapViewModel = viewModel(
                    factory = MapViewModelFactory(context)
                )
                MapScreen(viewModel) {
                    //앱 종료
                    Log.d(TAG, "MapActivity onBackPressed()")
                    finish()
                }
            }
        }

    }

    class MapViewModelFactory(
        private val context: Context = MyApplication.context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MapViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
