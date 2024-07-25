package com.freeman.mycampingmap.activity

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freeman.mycampingmap.compose.MapScreen
import com.freeman.mycampingmap.ui.theme.TestComposeUITheme
import com.freeman.mycampingmap.utils.MyLog
import com.freeman.mycampingmap.viewmodels.BaseViewModel
import com.freeman.mycampingmap.viewmodels.MapViewModel

class MapActivity : ComponentActivity() {

    private val viewModel by viewModels<MapViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TestComposeUITheme {
                val viewModel: MapViewModel = viewModel(
                    factory = MapViewModelFactory()
                )
                MapScreen(viewModel) {
                    //앱 종료
                    MyLog.d(TAG, "MapActivity onBackPressed()")
                    finish()
                }
            }
        }

        BaseViewModel.LiveDataBus.selectCampingSite.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { campingSite ->
                MyLog.d(TAG, "event.getContentIfNotHandled(): $campingSite")
                //1회성으로 캠핑장 사이트 정보를 처리 한다.
                viewModel.setSelectCampingSite(campingSite)
            }
        })

    }

    class MapViewModelFactory: ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MapViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
