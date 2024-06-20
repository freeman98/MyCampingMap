package com.example.testcomposeui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


//MVVM 모델 과 컴포스를 적용
@Composable
fun MainScreen(mainViewModel: MainViewModel = viewModel()) {
    //MainViewModel 참조하기.
    val text by mainViewModel.text.collectAsState()
    val checked by mainViewModel.checked.collectAsState()
    val sliderPosition by mainViewModel.sliderPosition.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = text)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { mainViewModel.onButtonClick() }) {
            Text("Click Me")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Example Image"
        )
        Spacer(modifier = Modifier.height(8.dp))
        Checkbox(checked = checked, onCheckedChange = { mainViewModel.onCheckedChanged(it) })
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(value = text, onValueChange = { mainViewModel.onTextChanged(it) })
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = sliderPosition,
            onValueChange = { mainViewModel.onSliderPositionChanged(it) })
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainScreen()
}