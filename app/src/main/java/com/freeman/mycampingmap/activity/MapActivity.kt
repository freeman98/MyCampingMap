package com.freeman.mycampingmap.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.freeman.mycampingmap.compose.MapScreen
import com.freeman.mycampingmap.ui.theme.MyCampingMapUITheme
import com.freeman.mycampingmap.utils.MyLog
import com.freeman.mycampingmap.viewmodels.BaseViewModel
import com.freeman.mycampingmap.viewmodels.MapViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MapActivity : BaseActivity() {

    val TAG: String = this::class.java.simpleName
    private val viewModel by viewModels<MapViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyCampingMapUITheme {
//                val viewModel: MapViewModel = viewModel
                MapScreen(viewModel) {
                    //앱 종료
                    MyLog.d(TAG, "MapActivity onBackPressed()")
                    val intent = Intent()
                    setResult(RESULT_OK, intent)
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

        // MapViewModel의 syncAllCampingList를 관찰하여 UI를 업데이트
        viewModel.syncAllCampingList.observe(this) { campingSites ->
            // UI를 업데이트하는 로직을 여기에 작성
            MyLog.d("MapActivity", "Received camping sites: ${campingSites.size}")
        }

    }

//    class MapViewModelFactory: ViewModelProvider.Factory {
//        override fun <T : ViewModel> create(modelClass: Class<T>): T {
//            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
//                @Suppress("UNCHECKED_CAST")
//                return MapViewModel() as T
//            }
//            throw IllegalArgumentException("Unknown ViewModel class")
//        }
//    }
}
