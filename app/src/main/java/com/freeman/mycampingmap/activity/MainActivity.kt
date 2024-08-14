package com.freeman.mycampingmap.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.freeman.mycampingmap.compose.NavGraph
import com.freeman.mycampingmap.ui.theme.MyCampingMapUITheme
import com.freeman.mycampingmap.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    val TAG = this::class.java.simpleName

    private val mainViewModel by viewModels<MainViewModel>()
//    private val mapViewModel by viewModels<MapViewModel>()
//    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Google Sign-In 초기화
        enableEdgeToEdge()
        setContent {
            MyCampingMapUITheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavGraph(mainViewModel, navController = navController)
                }
            }
        }

//        mainViewModel.syncAllCampingList.observe(this) {
//            MyLog.d(TAG, "syncAllCampingList.observe() Size = ${it.size}")
//            mapViewModel._syncAllCampingList.value = it
//        }

    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyCampingMapUITheme {
        val navController = rememberNavController()
        NavGraph(viewModel = viewModel(), navController = navController)
    }
}