package com.example.testcomposeui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testcomposeui.ui.theme.TestComposeUITheme

@Composable
fun MapScreen(mapViewModel: MapViewModel = viewModel()) {

}

@Preview(showBackground = true)
@Composable
fun MapActivityPreview() {
    TestComposeUITheme {
        MapScreen()
    }
}