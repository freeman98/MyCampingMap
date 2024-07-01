package com.example.testcomposeui

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.testcomposeui.data.User
import com.example.testcomposeui.ui.theme.TestComposeUITheme

@Composable
fun MainTopAppBar() {
    Scaffold(
        topBar = {
            CustomSmallTopAppBar(
                title = "My Camping List",
                onNavigationIconClick = {
                    Log.d(TAG, "onNavigationIconClick()")
                }
            )
        }
    ) { paddingValues ->
        // 메인 컨텐츠 영역에 paddingValues를 적용하여 content 컴포저블 호출
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
//            MapLsit()
//            CampDataListView(campDatas = CampDummyDataProvider.campList)
            CampDataListView()
        }
    }
}

//공용으로 쓰는 상단 바.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSmallTopAppBar(
    title: String,
    onNavigationIconClick: () -> Unit = {}
) {
    SmallTopAppBar(
        title = { Text(text = title) },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            /* 상단바 배경색. */
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            /* 텍스트 색 */
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            // 여기에 추가 액션 아이콘을 배치할 수 있습니다.
            IconButton(onClick = {
                Log.d(TAG, "actions IconButton()")
            }) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favorite"
                )
            }
        }
    )
}


//@Composable
//fun MapLsit(
//    modifier: Modifier = Modifier,
//    names: List<String> = List(1000) { "$it" }
//) {
//    LazyColumn(modifier = modifier.padding(vertical = 14.dp)) {
//        items(items = names) { name ->
//            MapItme(name = name)
//        }
//    }
//}
//
//@Composable
//private fun MapItme(name: String, modifier: Modifier = Modifier) {
//    //rememberSaveable 를 이용해서 스크롤을 해도 해당 확장 값을 기억 한다.
//    var expanded by rememberSaveable { mutableStateOf(false) }
//
//    val extraPadding by animateDpAsState(
//        if (expanded) 48.dp else 0.dp,
//        animationSpec = spring( //확장하거나 축소할때 애니메이션과 효과를 설정.
//            dampingRatio = Spring.DampingRatioMediumBouncy,
//            stiffness = Spring.StiffnessVeryLow
//        )
//    )
//    Surface(
//        color = MaterialTheme.colorScheme.primary,
//        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
//    ) {
//        Row(modifier = Modifier.padding(24.dp)) {   //가로행.
//            Column( //세로
//                modifier = Modifier
//                    .weight(1f)
//                    .padding(bottom = extraPadding.coerceAtLeast(0.dp))
//            ) {
//                Text(text = "Hello, ")
//                Text(text = name)
//            }
//
//            ElevatedButton(
//                onClick = { expanded = !expanded }
//            ) {
//                Text(if (expanded) "Show less" else "Show more")
//            }
//        }
//    }
//}

@Composable
//fun CampDataListView(modifier: Modifier = Modifier, campDatas: List<CampData>) {
fun CampDataListView(modifier: Modifier = Modifier, mainViewModel: MainViewModel = viewModel()) {
    Log.d(TAG, "CampDataListView()")
    val context = LocalContext.current
    val users = mainViewModel.users.observeAsState(initial = emptyList()).value
//    LaunchedEffect(Unit) {    //최초1회만 실행됨.
    LaunchedEffect(users) { //users값이 변경될때 블럭이 실행됨.
        mainViewModel.fetchUsers()
    }

    //메모리 관리가 들어간 LazyColumn
    LazyColumn(modifier = modifier.padding(vertical = 14.dp /*상하 패딩.*/)) {
        items(users) {
            CampDataView(it, onCardClick = { user ->
                Log.d(TAG, "onCardClick() $user")
                // User 데이터 발행
                BaseViewModel.LiveDataBus._selectUser.postValue(user)
                val intent = Intent(context, MapActivity::class.java)
                context.startActivity(intent)
            })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CampDataViewComposePreview() {
    TestComposeUITheme {
        val user = User(id = 0, name = "이름", username = "사용자 이름", email = "이메일", address = null, phone = null, website = null, company = null)
        CampDataView(user = user, onCardClick = {})
    }
}

@Composable
fun CampDataView(user: User, onCardClick: (User) -> Unit) {
    val typography = MaterialTheme.typography
    val elevation = CardDefaults.cardElevation(
        defaultElevation = 10.dp
    )

    Card(
        modifier = Modifier
            .clickable(onClick = { onCardClick(user) })
            .fillMaxWidth()     //가로 전체 화면 다쓴다.
            .padding(10.dp),    //카드간 간격.
        shape = RoundedCornerShape(12.dp),
        elevation = elevation   //그림자 영역 지정.
    ) {
        Row(
            /*
            - horizontalArrangement Arrangement = 요소를 어떤식으로 배열할지 설정, Start, End, Center 만 존재.
            -
             */
            modifier = Modifier.padding(10.dp), //패징값.
            verticalAlignment = Alignment.Bottom, //세로 정렬 설정.
            horizontalArrangement = Arrangement.spacedBy(10.dp) //가로 간격 설정.
//            horizontalArrangement = Arrangement.End
        ) {
//            Box(
//                modifier =
//                Modifier
//                    .size(width = 60.dp, height = 60.dp)
//                    .clip(CircleShape)
//                    .background(MyBlue)
//            )
            ProfileImg(user.imgUrl)

            Column() {
                user.name?.let {
                    Text(
                        text = it,
                        style = typography.titleLarge
                    )
                }
                user.email?.let {
                    Text(
                        text = it,
                        style = typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileImg(imgUrl: String?, modifier: Modifier = Modifier) {
    // 이미지 비트맵
    val bitmap: MutableState<Bitmap?> = remember { mutableStateOf(null) }

    //이미지 모디파이어
    val imageModifier = modifier
        .size(50.dp, 50.dp)
//        .clip(RoundedCornerShape(10.dp)) //이미지 모서리 라운드
        .clip(CircleShape)  //이미지 원형


    //이미지 관리 라이브러리.
    Glide.with(LocalContext.current)
        .asBitmap()
        .load(imgUrl)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                //bitmap 생성됬을때 호출.
                bitmap.value = resource
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        })

    //비트맵이 있다면
    bitmap.value?.asImageBitmap()?.let { fetchedBitmap ->
        Image(
            bitmap = fetchedBitmap,
            contentScale = ContentScale.Fit,
            modifier = imageModifier,
            contentDescription = null
        )
    }
    //이미지가 없다면 기본 아이콘 표시.
        ?: Image(
            painter = painterResource(id = R.drawable.ic_camp_image),
            contentScale = ContentScale.Fit,
            modifier = imageModifier,
            contentDescription = null
        )
}

@Preview(showBackground = true)
@Composable
fun MyTopBarComposePreview() {
    TestComposeUITheme {
        MainTopAppBar()
    }
}


//MVVM 모델 과 컴포스를 적용
//@Composable
//fun MainCompose(mainViewModel: MainViewModel = viewModel()) {
//    //MainViewModel 참조하기.
//    val context = LocalContext.current
//    val text by mainViewModel.text.collectAsState()
//    val checked by mainViewModel.checked.collectAsState()
//    val sliderPosition by mainViewModel.sliderPosition.collectAsState()
//    val navigateToSecondActivity by mainViewModel.navigateToSecondActivity.collectAsState()
//
//    // Observe navigation state
//    if (navigateToSecondActivity) {
//        val intent = Intent(context, MapActivity::class.java)
//        context.startActivity(intent)
//        mainViewModel.onNavigatedToSecondActivity()
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text(text = text)
//        Spacer(modifier = Modifier.height(8.dp))
//        Button(onClick = { mainViewModel.onButtonClick() }) {
//            Text("Click Me")
//        }
//        Spacer(modifier = Modifier.height(8.dp))
//        Image(
//            painter = painterResource(id = R.drawable.ic_launcher_foreground),
//            contentDescription = "Example Image"
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//        Checkbox(checked = checked, onCheckedChange = { mainViewModel.onCheckedChanged(it) })
//        Spacer(modifier = Modifier.height(8.dp))
//        BasicTextField(value = text, onValueChange = { mainViewModel.onTextChanged(it) })
//        Spacer(modifier = Modifier.height(8.dp))
//        Slider(
//            value = sliderPosition,
//            onValueChange = { mainViewModel.onSliderPositionChanged(it) })
//    }
//}
//
//// Greeting 컴포저블 함수, 기본적으로 "Hello {name}!" 텍스트를 표시
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//// Greeting 텍스트와 Button을 수평으로 배치하는 컴포저블 함수
//@Composable
//fun GreetingWithButton(name: String, modifier: Modifier = Modifier) {
//    var text by remember { mutableStateOf("Hello, $name!") }
//
//    Row(
//        modifier = Modifier
//            .fillMaxSize()  // 부모의 최대 크기를 채움
//            .padding(16.dp), // 패딩 설정
//        verticalAlignment = Alignment.CenterVertically,  // 수직 정렬을 중앙으로 설정
//        horizontalArrangement = Arrangement.Absolute.Left   // 수평 정렬을 중앙으로 설정
//
//    ) {
//        // Greeting 텍스트 컴포저블 호출
//        Greeting(text, modifier)
//        // 텍스트와 버튼 사이에 공간을 추가하는 Spacer
//        Spacer(modifier = Modifier.width(16.dp))
//        // 텍스트와 버튼 사이에 공간을 추가하는 Spacer
//        Button(onClick = { text = "Hello, Compose!" }) {
//            Text("Click Me")
//        }
//    }
//}

//@Composable
//fun OnboardingScreen(
//    onContinueClicked: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    //시작 첫화면.
//    Column(
//        modifier = modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("Welcome to the Basics Codelab!")
//        Button(
//            modifier = Modifier.padding(vertical = 24.dp),
//            onClick = onContinueClicked
//        ) {
//            Text("Continue")
//        }
//    }
//
//}
//
//@Preview(showBackground = true, widthDp = 320, heightDp = 320)
//@Composable
//fun OnboardingScreenPreview() {
//    TestComposeUITheme {
//        OnboardingScreen(onContinueClicked = {})
//    }
//}
