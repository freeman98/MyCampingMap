package com.example.testcomposeui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testcomposeui.ui.theme.TestComposeUITheme
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback

val TAG = "MapActivityCompose"

@Composable
fun MapScreen() {
    Log.d(TAG, "MapScreen()")
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            //지도 그리기.
            MapView()
            //상단 검색.
            SearchBox(onSearch = { searchText ->
                // 검색어를 사용하여 검색 수행
                Log.d(TAG, "검색어: $searchText")
            })
        }
    }

}

@Composable
fun SearchBox(onSearch: (String) -> Unit) {
    //상단 가로 정력 입력창 과 검색 버튼
    Log.d(TAG, "SearchBox()")
    var searchText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .height(100.dp)
            .padding(start = 10.dp, end = 10.dp, top = 30.dp),
    ) {
        //텍스트 입력.
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("검색") },
            modifier = Modifier.weight(1f)
        )
        //공간.
        Spacer(modifier = Modifier.width(10.dp))
        //검색 버튼.
        Button(
            onClick = {
                if(searchText.isNotEmpty()) {
                    onSearch(searchText)
                    searchText = ""
                }
            },
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text("검색")
        }
    }
}

@Composable
fun MapView(mapViewModel: MapViewModel = viewModel()) {
    Log.d(TAG, "MapView()")
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
        //현재 위치가 있을경우.
        currentLocation?.let { myLocation ->
            //현재 위치로 카메라 이동.
            mapViewModel.setMyLocationMarker(myLocation)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    TestComposeUITheme {
        MapScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBoxPreview() {
    TestComposeUITheme {
        SearchBox(onSearch = {})
    }
}