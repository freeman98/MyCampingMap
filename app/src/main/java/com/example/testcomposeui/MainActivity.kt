package com.example.testcomposeui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.testcomposeui.ui.theme.TestComposeUITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestComposeUITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GreetingWithButton(name = "Android", modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Greeting 컴포저블 함수, 기본적으로 "Hello {name}!" 텍스트를 표시
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

// Greeting 텍스트와 Button을 수평으로 배치하는 컴포저블 함수
@Composable
fun GreetingWithButton(name: String, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("Hello, $name!") }

    Row(
        modifier = Modifier
            .fillMaxSize()  // 부모의 최대 크기를 채움
            .padding(16.dp), // 패딩 설정
        verticalAlignment = Alignment.CenterVertically,  // 수직 정렬을 중앙으로 설정
        horizontalArrangement = Arrangement.Absolute.Left   // 수평 정렬을 중앙으로 설정

    ) {
        // Greeting 텍스트 컴포저블 호출
        Greeting(text, modifier)
        // 텍스트와 버튼 사이에 공간을 추가하는 Spacer
        Spacer(modifier = Modifier.width(16.dp))
        // 텍스트와 버튼 사이에 공간을 추가하는 Spacer
        Button(onClick = { text = "Hello, Compose!" }) {
            Text("Click Me")
        }
    }
}

// Android Studio의 미리보기를 위한 컴포저블 함수
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    // 미리보기에서 사용할 Theme 설정
    TestComposeUITheme {
        GreetingWithButton("Android")
    }
}