package com.example.testcomposeui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testcomposeui.ui.theme.TestComposeUITheme
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback

@Composable
fun MapScreen(mapViewModel: MapViewModel = viewModel()) {
    val googleMap by mapViewModel.googleMap.observeAsState()
    //내 위치.
    val currentLocation by mapViewModel.currentLocation.observeAsState()

    AndroidView(
        factory = { context ->
            MapView(context).apply {
                onCreate(null)
                onResume()
                getMapAsync(OnMapReadyCallback { googleMap ->
                    //구글맵 준비되면 호출.
                    mapViewModel.setGoogleMap(googleMap)
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    googleMap?.let { map ->
        currentLocation?.let { myLocation ->
            //카메라 이동.
            mapViewModel.setMyLocationMarker(myLocation)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapActivityPreview() {
    TestComposeUITheme {
        MapScreen()
    }
}