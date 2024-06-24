package com.example.testcomposeui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.testcomposeui.ui.theme.TestComposeUITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestComposeUITheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    GreetingWithButton(name = "Android", modifier = Modifier.padding(innerPadding))
//                }
                Surface(color = MaterialTheme.colorScheme.background) {
//                    MyApp(modifier = Modifier.fillMaxSize())
                    MainTopAppBar()
                }

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestComposeUITheme {
//        MyApp(modifier = Modifier.fillMaxSize())
        MainTopAppBar()
    }
}