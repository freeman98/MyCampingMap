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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapScreen(mapViewModel: MapViewModel = viewModel()) {
    val googleMap by mapViewModel.googleMap.observeAsState()
    val currentLocation by mapViewModel.currentLocation.observeAsState()

//    LaunchedEffect(Unit) {
//        mapViewModel.fetchCurrentLocation()
//    }
    mapViewModel.fetchCurrentLocation()
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                onCreate(null)
                onResume()
                getMapAsync(OnMapReadyCallback { googleMap ->
                    //구글맵 준비되면 호출.
                    mapViewModel.setGoogleMap(googleMap)
                    setupGoogleMap(googleMap)
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    googleMap?.let { map ->
        currentLocation?.let { myLocation ->
            //카메라 이동.
            mapViewModel.setMyLocation(myLocation)
        }
    }
}

fun setupGoogleMap(googleMap: GoogleMap) {
    //초기 위치. 설정 및 카메라 이동.
    val seoul = LatLng(37.5665, 126.9780)
//    googleMap.addMarker(
//        MarkerOptions()
//            .position(seoul)
//            .title("Marker in Sydney")
//    )
    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15f))
}

@Preview(showBackground = true)
@Composable
fun MapActivityPreview() {
    TestComposeUITheme {
        MapScreen()
    }
}