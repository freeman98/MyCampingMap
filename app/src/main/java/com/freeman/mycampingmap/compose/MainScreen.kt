package com.freeman.mycampingmap.compose

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.freeman.mycampingmap.R
import com.freeman.mycampingmap.activity.MapActivity
import com.freeman.mycampingmap.db.CampingSite
import com.freeman.mycampingmap.ui.theme.MyCampingMapUITheme
import com.freeman.mycampingmap.utils.MyLog
import com.freeman.mycampingmap.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val systemInsets = WindowInsets.systemBars
    // dp 값으로 변환 - 드로어 상단 패딩
    val topPadding = with(LocalDensity.current) { systemInsets.getTop(this).toDp() }

    //뒤로가기 버튼 이벤트 헨들러.
    MainScreenBackHandler(scaffoldState)
    //지도 액티비티 Result 런처
    val launcher = mapRememberLauncherForActivityResult()

    Scaffold(
        //상단바
        topBar = {
            CustomTopAppBar(
                title = "My Camping List",
                onNavigationIconClick = {
                    MyLog.d(viewModel.TAG, "onNavigationIconClick()")
                    scope.launch {
                        scaffoldState.drawerState.open()    //드로어 열기.
                    }
                }
            )
        },
        //사이드 메뉴
        drawerContent = {
            DrawerSideContent(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(top = topPadding + 10.dp, start = 10.dp, end = 10.dp),
                navController = navController
            ) {
                scope.launch {
                    scaffoldState.drawerState.close()    //드로어 닫기.
                }
            }
        },
        drawerGesturesEnabled = scaffoldState.drawerState.isOpen,   //드로어 터치 이벤트
        scaffoldState = scaffoldState,          //스캐폴드 상태
        drawerShape = customDrawerShape(topPadding),      //드로어 모양
        drawerElevation = 30.dp,                //드로어 그림자
        floatingActionButton = {
            FloatingActionButton(onClick = {
                MyLog.d("MainScreen() FloatingActionButton()")
                gotoMapActivity(context, launcher)
            }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "더하기", tint = Color.White)
            }
        },  //버튼
        floatingActionButtonPosition = FabPosition.End, //버튼 위치
        isFloatingActionButtonDocked = false,    //버튼 위치 조정
    ) { paddingValues ->
        // 메인 컨텐츠 영역에 paddingValues를 적용하여 content 컴포저블 호출
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            //캠핑장 리스트
            CampDataListView()
        }
    }
}

@Composable
fun mapRememberLauncherForActivityResult(viewModel: MainViewModel = viewModel()
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            MyLog.d("onActivityResult() RESULT_OK")
            // 액티비티에서 돌아온 결과를 처리하는 코드
            viewModel.getDBCampingSites()
        }
    }
}

@Composable
fun MainScreenBackHandler(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    scope: CoroutineScope = rememberCoroutineScope(),
    activity: Activity = LocalContext.current as Activity,
) {
    var backPressedOnce by remember { mutableStateOf(false) }
    val handler = remember { Handler(Looper.getMainLooper()) }

    //뒤로가기 버튼 눌렀을때
    BackHandler {
        //메인 화면에서 뒤로가기 버튼 눌렀을때
        MyLog.d("MainScreen() BackHandler()")
        if (scaffoldState.drawerState.isOpen) {
            //드로어가 열려있으면 닫기.
            scope.launch {
                scaffoldState.drawerState.close()
            }
            return@BackHandler
        }

        if (backPressedOnce) {
            // 두 번째 뒤로가기 눌렀을 때 - 앱종료
            activity.finishAffinity()
        } else {
            // 첫 번째 뒤로가기 눌렀을 때 - 토스트 띄우기 2초 대기 후 다시 눌러야 종료
            backPressedOnce = true
            handler.postDelayed({ backPressedOnce = false }, 2000)
            Toast.makeText(activity, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }   //BackHandler
}

fun customDrawerShape(topPadding: Dp) = object : Shape {
    // 드로어 모양을 정의하는 클래스
    override fun createOutline(
        size: Size,         //드로어 사이즈
        layoutDirection: LayoutDirection,   //레이아웃 방향
        density: Density    //밀도
    ): Outline {
        return Outline.Rounded(
            RoundRect(
                left = 0f,              //왼쪽 패딩.
                top = with(density) { topPadding.toPx() },  //상단 패딩.
                right = size.width,     //가로 크기.
                bottom = size.height,   //세로 크기.
                //You can also add bottomRightCornerRadius
                topRightCornerRadius = CornerRadius(x = 40f, y = 40f),      //오른쪽 상단 라운드
                bottomRightCornerRadius = CornerRadius(x = 40f, y = 40f)    //오른쪽 하단 라운드
            )
        )
    }
}


@Composable
fun DrawerSideContent(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: MainViewModel = viewModel(),
    onClickClose: () -> Unit
) {
    // DrawerContent 사이드 메뉴
    val user by viewModel.user.observeAsState()
    val typography = MaterialTheme.typography

    val paddingMdifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .then(modifier)
    ) {
        Row(modifier = paddingMdifier, verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = user?.email ?: "", style = typography.titleLarge
            )
            //사이드 메뉴 닫기 이벤트.
            DrawerSideMenuCloseButton(onClick = onClickClose)
        }
        Text(modifier = paddingMdifier, text = user?.username ?: "", style = typography.titleMedium)
        Divider(modifier = paddingMdifier, color = Color.Gray, thickness = 1.dp)
        Text(modifier = paddingMdifier, text = "MENU 1")
        Text(modifier = paddingMdifier, text = "MENU 2")
        Divider(modifier = paddingMdifier, color = Color.Gray, thickness = 1.dp)
        Text(modifier = paddingMdifier, text = "MENU 3")
        Text(modifier = paddingMdifier, text = "MENU 4")
        Divider(modifier = paddingMdifier, color = Color.Gray, thickness = 1.dp)
        Text(modifier = paddingMdifier, text = "MENU 5")
        Text(modifier = paddingMdifier, text = "MENU 6")
        Divider(modifier = paddingMdifier, color = Color.Gray, thickness = 1.dp)
        Text(modifier = paddingMdifier, text = "MENU 7")
        Text(modifier = paddingMdifier, text = "MENU 8")
        Divider(modifier = paddingMdifier, color = Color.Gray, thickness = 1.dp)
        Text(modifier = paddingMdifier, text = "MENU 9")
        Text(modifier = paddingMdifier, text = "MENU 16")
        Divider(modifier = paddingMdifier, color = Color.Gray, thickness = 1.dp)
        //로그아웃
        DrawerSideMenuLogout(navController)
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun DrawerSideMenuCloseButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Delete"
        )
    }
}

@Preview
@Composable
fun DrawerSideContentPreview() {
    DrawerSideContent(navController = NavHostController(LocalContext.current), onClickClose = {})
}

@Composable
fun DrawerSideMenuLogout(navController: NavHostController, viewModel: MainViewModel = viewModel()) {
    //로그아웃
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        LogoutDialog(
            onDismiss = { showDialog = false },
            onConfirm = { onConfirm ->
                MyLog.d("MainScreen", "SideMenuLogout() logout()")
                showDialog = false
                if (onConfirm) {
                    viewModel.logout()
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            })
    }

    TextButton(onClick = { showDialog = true }) {
        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout")
        Text(text = "로그아웃")
    }
}

@Composable
fun LogoutDialog(onDismiss: () -> Unit, onConfirm: (Boolean) -> Unit) {
    //로그아웃 확인 다이얼로그
    AlertDialog(
        onDismissRequest = onDismiss,   //다이얼로그 밖 클릭시 닫힘.
        title = { Text("로그아웃 확인") },
        text = { Text("로그아웃을 하겠습니까?") },
        confirmButton = {
            //확인 버튼
            TextButton(
                onClick = { onConfirm(true) }
            ) {
                Text("로그아웃")
            }
        },
        dismissButton = {
            //취소 버튼
            TextButton(
                onClick = { onConfirm(false) }
            ) {
                Text("취소")
            }
        }
    )

}

//공용으로 쓰는 상단 바.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    title: String,
    onNavigationIconClick: () -> Unit = {}
) {
    val context = LocalContext.current

    TopAppBar(
        title = { Text(text = title) },
        colors = TopAppBarDefaults.topAppBarColors(
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
//                MyLog.d(TAG, "actions IconButton()")
                gotoMapActivity(context)
            }) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = "Map"
                )
            }
        }

        )   //TopAppBar
}

fun gotoMapActivity(
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null
) {
    val intent = Intent(context, MapActivity::class.java)
    launcher?.launch(intent) ?: context.startActivity(intent)
}

@Composable
fun CampDataListView(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    Log.d(viewModel.TAG, "CampDataListView()")
    val context = LocalContext.current
    val syncAllCampingList by viewModel.syncAllCampingList.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)

    LaunchedEffect(Unit) {
        //db데이터 또는 파이어베이스 사이트 정보가 변경될때 만 호출.
        viewModel.syncCampingSites()
    }

    Log.d(viewModel.TAG, "CampDataListView() syncAllCampingList.size = ${syncAllCampingList.size}")
    //메모리 관리가 들어간 LazyColumn
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 14.dp /*상하 패딩.*/)
            ) {
                items(syncAllCampingList) { campingSites ->
                    CampDataViewCard(campingSites,
                        //카드 클릭 이벤트
                        onCardClick = { campingSite ->
//                    Log.d(viewModel.TAG, "onCardClick() $campingSite")
                            viewModel.selectCampingSite(campingSite)
                            gotoMapActivity(context)
                        },
                        //카드 삭제 이벤트.
                        onCardDeleteClick = { campingSite ->
                            viewModel.deleteCampingSite(campingSite)
                        }

                    )   //CampDataViewCard
                }   //items
            }   //LazyColumn
        }   //if (isLoading)
    }
}

@Preview(showBackground = true)
@Composable
fun CampDataViewCardPreview() {
    MyCampingMapUITheme {
        CampDataViewCard(capingSite = CampingSite(), onCardClick = {}, onCardDeleteClick = {})
    }
}

@Composable
fun CampDataViewCard(
    capingSite: CampingSite,
    onCardClick: (CampingSite) -> Unit,
    onCardDeleteClick: (CampingSite) -> Unit
) {
    val typography = MaterialTheme.typography
    val elevation = CardDefaults.cardElevation(
        defaultElevation = 0.dp
    )

    Card(
        modifier = Modifier
            .clickable(onClick = { onCardClick(capingSite) }) //카드 클릭 이벤트.
            .fillMaxWidth()     //가로 전체 화면 다쓴다.
            .padding(10.dp),    //카드간 간격.
        shape = RoundedCornerShape(12.dp),
        elevation = elevation   //그림자 영역 지정.
    ) {
        Row(
            /*
            - horizontalArrangement Arrangement = 요소를 어떤식으로 배열할지 설정, Start, End, Center 만 존재.
             */
            modifier = Modifier.padding(10.dp), //패징값.
            verticalAlignment = Alignment.Bottom, //세로 정렬 설정.
            horizontalArrangement = Arrangement.spacedBy(10.dp) //가로 간격 설정.
//            horizontalArrangement = Arrangement.End
        ) {
            ProfileImg(
                modifier = Modifier.align(Alignment.Top),
                imgUrl = "https://randomuser.me/api/portraits/women/11.jpg"
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = capingSite.name,
                    style = typography.titleLarge
                )
                Text(
                    text = capingSite.address,
                    style = typography.titleMedium
                )
            }
            CardDeleteImageButton(
                capingSite,
                modifier = Modifier.align(Alignment.Top),
                onClick = { onCardDeleteClick(capingSite) }
            )
        }
    }
}

@Composable
fun CardDeleteImageButton(
    campingSite: CampingSite,
    modifier: Modifier = Modifier,
    onClick: (CampingSite) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    // 카드 삭제 버튼
    IconButton(
        modifier = modifier,
        onClick = {
            showDialog = true
        }
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Delete"
        )
    }

    if (showDialog) {
        // 삭제 확인 다이얼로그
        CardDeleteAlartDialog(
            onDismiss = { showDialog = false }
        ) { delete ->
            if (delete) {
                onClick(campingSite)
            }
            showDialog = false
        }
    }
}

@Composable
fun CardDeleteAlartDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    //삭제 확인 다이얼로그
    AlertDialog(
        onDismissRequest = onDismiss,   //다이얼로그 밖 클릭시 닫힘.
        title = { Text("삭제 확인") },
        text = { Text("이 캠핑장을 삭제하시겠습니까?") },
        confirmButton = {
            //확인 버튼
            TextButton(
                onClick = { onConfirm(true) }
            ) {
                Text("삭제")
            }
        },
        dismissButton = {
            //취소 버튼
            TextButton(
                onClick = { onConfirm(false) }
            ) {
                Text("취소")
            }
        }
    )
}

@Preview
@Composable
fun CardDeleteAlartDialogPreview() {
    CardDeleteAlartDialog(onDismiss = {}, onConfirm = {})
}

@Composable
fun ProfileImg(modifier: Modifier = Modifier, imgUrl: String?) {
    // 이미지 비트맵
    val bitmap: MutableState<Bitmap?> = remember { mutableStateOf(null) }

    //이미지 모디파이어
    val imageModifier = modifier
        .size(50.dp, 50.dp)
        .clip(RoundedCornerShape(10.dp)) //이미지 모서리 라운드
//        .clip(CircleShape)  //이미지 원형


    //이미지 관리 라이브러리.
    Glide.with(LocalContext.current)
        .asBitmap()
        .load(imgUrl)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
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
fun MainScreenPreview() {
    MyCampingMapUITheme {
        MainScreen(navController = NavHostController(LocalContext.current))
    }
}
