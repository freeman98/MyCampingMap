package com.example.testcomposeui

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testcomposeui.ui.theme.TestComposeUITheme
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.libraries.places.api.model.Place

val TAG = "MapActivityCompose"

@Composable
fun MapScreen(viewModel: MapViewModel, onBackPressed: () -> Unit) {
    Log.d(TAG, "MapScreen()")
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // WindowInsets를 사용하여 상태 바와 네비게이션 바의 크기를 얻습니다
        val systemInsets = WindowInsets.systemBars

        // dp 값으로 변환
        val topPadding = with(LocalDensity.current) { systemInsets.getTop(this).toDp() }
        val bottomPadding = with(LocalDensity.current) { systemInsets.getBottom(this).toDp() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding, bottom = bottomPadding)  //상단노티, 하단네비게이션 간격.
        ) {
            //지도 그리기.
            MapView()
            //내 위치로 가기 버튼
            MyLocationButton(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(5.dp),
                onClick = {viewModel.gotoMyLocation()}
            )
            //상단 검색.
            SearchBox(onSearch = { searchText ->
                    // 검색어를 사용하여 검색 수행
                    viewModel.searchPlace(searchText)
                },
                onBackPressed = {
                    Log.d(TAG, "MapScreen() onBackPressed()")
                    onBackPressed()
                }
            )

        }
    }

}

@Composable
fun MyLocationButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    //내 위치로 가기 버튼
    FloatingActionButton(
        onClick = {onClick()},
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "My Location"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyLocationButtonPreview() {
    TestComposeUITheme {
        MyLocationButton(onClick = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBox(onSearch: (String) -> Unit, onBackPressed: () -> Unit, mapViewModel: MapViewModel = viewModel()) {
    //상단 가로 정력 입력창 과 검색 버튼
    Log.d(TAG, "SearchBox()")
    var searchText by remember { mutableStateOf("") }
    //검색창이 보이지 않으면 false 보이면 true
    val isSearchListVisible = mapViewModel.isSearchListVisible.observeAsState(false).value

    Row(
        modifier = Modifier
            .height(80.dp)
            .padding(start = 10.dp, end = 10.dp, top = 5.dp),
    ) {
        IconButton(
            modifier = Modifier
                .size(40.dp)    //버튼 크기.
                .clip(CircleShape)  //버튼 모양.
                .align(Alignment.CenterVertically),  //세로 정렬.
            onClick = {
                //리스트 아이콘 클릭시.
                Log.d(TAG, "SearchBox() onListButton()")
                if(mapViewModel.getMarkerCount() != 0) {
                    mapViewModel.setSearchListVisible(!isSearchListVisible)
                } else {
                    //마커가 없을경우.
                    if(isSearchListVisible) mapViewModel.setSearchListVisible(false)
                }
            }
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.ic_list),
                contentDescription = "List Button"
            )
        }

        Spacer(modifier = Modifier.width(1.dp))

        //검색 입력 필드.
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("검색") },
            modifier = Modifier.weight(1f), //가로 늘리기.
            singleLine = true,      //한줄로.
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Black,   //검색창 테두리
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.Black,     //검색창 텍스트
                containerColor = Color.White,       //검색창 배경색
                cursorColor = Color.Blue            //검색창 커서
            )
        )
        //공간.
        Spacer(modifier = Modifier.width(5.dp))
        //검색 버튼.
        Button(
            onClick = {
                if(searchText.isNotEmpty()) {
                    onSearch(searchText)
                    searchText = ""
                }
            },
            shape = RectangleShape,
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text("검색")
        }
    }

    //검색 리스트 컴포즈
    SearchListView(
        onBackPressed = {
            Log.d(TAG, "SearchBox() onBackPressed()")
            onBackPressed()
        }
    )
}

@Composable
fun SearchListView(modifier: Modifier = Modifier, mapViewModel: MapViewModel = viewModel(), onBackPressed: () -> Unit) {
    Log.d(TAG, "serchListView()")
    //지도에 표시될 Place list.
    val placesList = mapViewModel.placesList.observeAsState(initial = emptyList()).value
    //검색창이 보이지 않으면 false 보이면 true
    val isSearchListVisible = mapViewModel.isSearchListVisible.observeAsState(false).value
    //뒤로가기 버튼 눌렀는지 체크. 누르면 true
    val isBackPressed = remember { mutableStateOf(false) }

    BackHandler {
        //백키를 눌렀을때
        Log.d(TAG, "isSearchListVisible = $isSearchListVisible")
        //검색창이 보이지 않으면 백키 처리.
        if(!isSearchListVisible) {
            isBackPressed.value = true
        } else {
            //검색창이 보이면 닫기 액션.
            mapViewModel.setSearchListVisible(false)
        }
        Log.d(TAG, "BackHandler() $isBackPressed")
    }

    if ( isBackPressed.value && !isSearchListVisible) {
        //isBackPressed = true, isSearchListVisible = false 일때
        Log.d(TAG, "SearchListView() onBackPressed()")
        onBackPressed()
    }

    LaunchedEffect(key1 = placesList) {
        //placesList 값이 변경이 있을 경우에만.
        if(placesList.isNotEmpty()) {
            //검색 결과가 있을경우.
            Log.d(TAG, "placesList.size ${placesList.size}")
            //검색창이 보이지 않으면 리턴.
            mapViewModel.setSearchListVisible(true)
        } else {
            //검색 결과가 없을경우.
            return@LaunchedEffect
        }
    }

    //검색창이 보이지 않으면 리턴.
    if (!isSearchListVisible) {
        return
    } else {
        //검색창이 보이지만 리스트가 없다면 검색창 닫기.
        if(placesList.isEmpty()) {
            mapViewModel.setSearchListVisible(false)
        }
    }

    //메모리 관리가 들어간 LazyColumn
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp)     //상단 검색 영역.
            .background(MaterialTheme.colorScheme.background)
    ) {
        //메모리 관리가 들어간 LazyColumn
        LazyColumn(modifier = modifier.padding(vertical = 14.dp /*상하 패딩.*/)) {
            items(placesList) { place ->
                SerchListViewCard(
                    place,
                    onCardClick = { selectPlace ->
                        Log.d(TAG, "onCardClick() $selectPlace")
                    }
                )
            }
        }
    }
}

@Composable
fun SerchListViewCard (place: Place, onCardClick: (Place) -> Unit) {
//    Log.d(TAG, "SerchListViewCard() $place")
    val typography = MaterialTheme.typography
    val elevation = CardDefaults.cardElevation(
        defaultElevation = 0.dp
    )

    Card(
        modifier = Modifier
            .clickable(onClick = { onCardClick(place) }) //카드 클릭 이벤트.
            .fillMaxWidth()     //가로 전체 화면 다쓴다.
            .padding(10.dp),    //카드간 간격.
        elevation = elevation   //그림자 영역 지정.
    ) {
        Row(
            /*
            - horizontalArrangement Arrangement = 요소를 어떤식으로 배열할지 설정, Start, End, Center 만 존재.
             */
            modifier = Modifier.padding(10.dp), //패징값.
            verticalAlignment = Alignment.Bottom, //세로 정렬 설정.
            horizontalArrangement = Arrangement.spacedBy(10.dp) //가로 간격 설정.
        ) {
            place.name?.let {
                Text(
                    text = it,
                    style = typography.titleLarge
                )
            }
        }

    }

}

@Preview
@Composable
fun SearchListViewPreview() {
    TestComposeUITheme {
        SearchListView(onBackPressed = {})
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun MapView(mapViewModel: MapViewModel = viewModel()) {
    Log.d(TAG, "MapView()")
    val googleMap by mapViewModel.googleMap.observeAsState()
    //내 현재 위치 - 현재 위치를 기억 하기 위해 LiveDataBus 사용.
    val currentLocation by BaseViewModel.LiveDataBus.currentMyLocation.observeAsState()
    //지도에 표시될 Place map.
    val markerPlaceMap by mapViewModel.markerPlaceMap.observeAsState()

    AndroidView(
        factory = { context ->
            MapView(context).apply {
                Log.d(TAG, "AndroidView onCreate()")
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
        LaunchedEffect(currentLocation) {
            currentLocation?.let { myLocation ->
                //현재 위치로 카메라 이동.
                mapViewModel.setMyLocationMarker(myLocation)
            }
        }

        //지도에 표시될 Places 리스트가 있을경우 만 실행.
        LaunchedEffect(markerPlaceMap) {
            markerPlaceMap?.let {mapViewModel.gotoFirstPlace(it)}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    TestComposeUITheme {
        MapScreen(viewModel()) {}
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBoxPreview() {
    TestComposeUITheme {
        SearchBox(onSearch = {}, onBackPressed = {})
    }
}