package com.example.testcomposeui.compose

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testcomposeui.R
import com.example.testcomposeui.data.CampingDataUtil.addFirebaseCampingSite
import com.example.testcomposeui.data.UserDataUtil.firebaseSaveUser
import com.example.testcomposeui.ui.theme.TestComposeUITheme
import com.example.testcomposeui.utils.IntentUtil.Companion.callPhone
import com.example.testcomposeui.utils.IntentUtil.Companion.openWebPage
import com.example.testcomposeui.viewmodels.BaseViewModel
import com.example.testcomposeui.viewmodels.MapViewModel
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.libraries.places.api.model.Place

@Composable
fun MapScreen(viewModel: MapViewModel, onBackPressed: () -> Unit) {
    Log.d(viewModel.TAG, "MapScreen()")
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
                onClick = { viewModel.gotoMyLocation() }
            )
            //상단 검색.
            SearchBox(onSearch = { searchText ->
                // 검색어를 사용하여 검색 수행
                viewModel.searchPlace(searchText)
            },
                onBackPressed = {
                    Log.d(viewModel.TAG, "MapScreen() onBackPressed()")
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
        onClick = { onClick() },
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
fun SearchBox(
    onSearch: (String) -> Unit,
    onBackPressed: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    //상단 가로 정력 입력창 과 검색 버튼
    Log.d(viewModel.TAG, "SearchBox()")
    var searchText by remember { mutableStateOf("") }
    //검색창이 보이지 않으면 false 보이면 true
    val isSearchListVisible = viewModel.isSearchListVisible.observeAsState(false).value

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
                Log.d(viewModel.TAG, "SearchBox() onListButton()")
                if (viewModel.getMarkerCount() != 0) {
                    viewModel.setSearchListVisible(!isSearchListVisible)
                } else {
                    //마커가 없을경우.
                    if (isSearchListVisible) viewModel.setSearchListVisible(false)
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
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
            ),
            onClick = {
                if (searchText.isNotEmpty()) {
                    onSearch(searchText)
                    searchText = ""
                }
            },
            shape = RectangleShape,
            modifier = Modifier
                .size(width = 75.dp, height = 55.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text("검색")
        }
    }

    //검색 리스트 컴포즈
    SearchListView(
        onBackPressed = {
            Log.d(viewModel.TAG, "SearchBox() onBackPressed()")
            onBackPressed()
        }
    )
}

@Composable
fun SearchListView(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(),
    onBackPressed: () -> Unit
) {
    Log.d(viewModel.TAG, "serchListView()")
    //지도에 표시될 Place list.
    val placesList = viewModel.placesList.observeAsState(initial = emptyList()).value
    //검색창이 보이지 않으면 false 보이면 true
    val isSearchListVisible = viewModel.isSearchListVisible.observeAsState(false).value
    //뒤로가기 버튼 눌렀는지 체크. 누르면 true
    val isBackPressed = remember { mutableStateOf(false) }
    // LazyListState를 기억합니다.
    val listState = rememberLazyListState()

    BackHandler {
        //백키를 눌렀을때
        Log.d(viewModel.TAG, "isSearchListVisible = $isSearchListVisible")
        //검색창이 보이지 않으면 백키 처리.
        if (!isSearchListVisible) {
            isBackPressed.value = true
        } else {
            //검색창이 보이면 닫기 액션.
            viewModel.setSearchListVisible(false)
        }
        Log.d(viewModel.TAG, "BackHandler() $isBackPressed")
    }

    if (isBackPressed.value && !isSearchListVisible) {
        //isBackPressed = true, isSearchListVisible = false 일때
        Log.d(viewModel.TAG, "SearchListView() onBackPressed()")
        onBackPressed()
    }

    LaunchedEffect(key1 = placesList) {
        //placesList 값이 변경이 있을 경우에만.
        if (placesList.isNotEmpty()) {
            //검색 결과가 있을경우.
//            Log.d(TAG, "placesList.size ${placesList.size}")
            viewModel.setSearchListVisible(true) //검색창 보이게.
            listState.scrollToItem(0)   //리스트 상단으로 이동.
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
        if (placesList.isEmpty()) {
            viewModel.setSearchListVisible(false)
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
        LazyColumn(
            state = listState,  // LazyListState를 LazyColumn에 전달합니다.
            modifier = modifier.padding(vertical = 14.dp /*상하 패딩.*/)
        ) {
            items(placesList) { place ->
                SerchListViewCard(
                    place,
                    onCardClick = { selectPlace ->
                        //카드 클릭 이벤트.
                        Log.d(viewModel.TAG, "onCardClick() $selectPlace")
                        viewModel.gotoPlace(selectPlace)
                        viewModel.setSearchListVisible(false)
                    },
                    onClickCampingSite = { place ->
                        //캠핑장 저장.
                        viewModel.insertCampingSite(place,
                            onSuccess = { isSuccess ->
                                //캠핑장 정보 저장
                                if (isSuccess) {
                                    firebaseSaveUser { isSuccess, user, message ->
                                        //사용자 정보 저장 성공.
                                        if (isSuccess) {
                                            //캠핑장 정보 저장.
                                            addFirebaseCampingSite(user, place)
                                        }

                                    }
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SerchListViewCard(
    place: Place,
    onCardClick: (Place) -> Unit,
    onClickCampingSite: (Place) -> Unit
) {
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
        Column(
            /*
            - horizontalArrangement Arrangement = 요소를 어떤식으로 배열할지 설정, Start, End, Center 만 존재.
             */
            modifier = Modifier.padding(10.dp) //패징값.
        ) {
            place.name?.let {
                Text(text = it, style = typography.titleLarge)
            }
            place.address?.let {
                Text(text = "- 주소 : $it", style = typography.bodyMedium)
            }
            place.phoneNumber?.let {
//                Text(text = "- 전화 : $it", style = typography.bodyMedium)
                //전화 번호 앞자리 치환.
                val phoneNumber = it.replaceFirst("+82 ", "0")
                ClickableText(text = AnnotatedString("- 전화 : $phoneNumber"),
                    style = typography.bodyMedium,
                    onClick = {
                        Log.d("", "SerchListViewCard() onClick: $phoneNumber")
                        callPhone(phoneNumber)
                    })
            }
            place.websiteUri?.let { uri ->
//                Text(text = "- 사이트 : $it", style = typography.bodyMedium)
                ClickableText(text = AnnotatedString("- 사이트 : $uri"),
                    style = typography.bodyMedium,
                    onClick = {
                        Log.d("", "SerchListViewCard() onClick: $uri")
                        openWebPage(uri)
                    })
            }
            place.rating?.let {
                Text(text = "- 별점 : $it (${place.userRatingsTotal})", style = typography.bodyMedium)
            }

            place.reviews?.let {
                Text(text = "- 리뷰 : ${it.size}", style = typography.bodyMedium)
            }
            place.priceLevel?.let {
                Text(text = "- 가격 : $it", style = typography.bodyMedium)
            }
            Button(onClick = { onClickCampingSite(place) }
//                onClick = {
//                saveUser { isSuccess, user, message ->
//                    //사용자 정보 저장 성공.
//                    if (isSuccess) {
//                        //캠핑장 정보 저장.
//                        addCampingSite(user, place)
//                    }
//                }
//            }
            ) {
                Text(text = "저장")
            }

        }
    }
}   //SerchListViewCard()

@Preview
@Composable
fun SearchListViewPreview() {
    TestComposeUITheme {
        SearchListView(onBackPressed = {})
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun MapView(viewModel: MapViewModel = viewModel()) {
    Log.d(viewModel.TAG, "MapView()")
    val googleMap by viewModel.googleMap.observeAsState()
    //내 현재 위치 - 현재 위치를 기억 하기 위해 LiveDataBus 사용.
    val currentLocation by BaseViewModel.LiveDataBus.currentMyLocation.observeAsState()
    //지도에 표시될 Place map.
    val markerPlaceMap by viewModel.markerPlaceMap.observeAsState()
    // mainActivity 리스트에서 선택한 캠핑장
    val selectedCampingSite by viewModel.campingSite.observeAsState()

    AndroidView(
        factory = { context ->
            MapView(context).apply {
                Log.d(viewModel.TAG, "AndroidView onCreate()")
                onCreate(null)
                onResume()
                getMapAsync(OnMapReadyCallback { googleMap ->
                    //구글맵 준비되면 호출.
                    viewModel.setGoogleMap(googleMap)
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    googleMap?.let { map ->
        //리스트에서 선택한 캠핑장 이 있을경우
        LaunchedEffect(selectedCampingSite) {
            selectedCampingSite?.let {
                //메인 리스트에서 선택한 캠핑장 정보가 있다면.
                Log.d(viewModel.TAG, "selectedCampingSite = $it")
                viewModel.selectCampingSiteMarker(it)
            }
        }

        //현재 위치가 있을경우.
        LaunchedEffect(currentLocation) {
            if(selectedCampingSite == null) {
                currentLocation?.let { myLocation ->
                    //현재 위치로 카메라 이동.
                    viewModel.setMyLocationMarker(myLocation)
                }
            }

        }

        //지도에 표시될 Places 리스트가 있을경우 만 실행.
        LaunchedEffect(markerPlaceMap) {
            markerPlaceMap?.let { viewModel.gotoFirstPlace(it) }
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