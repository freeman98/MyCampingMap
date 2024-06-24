package com.example.testcomposeui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

//MVVM 모델 과 컴포스를 적용
class MainViewModel: ViewModel(){

    private val _navigateToSecondActivity = MutableStateFlow(false)
    val navigateToSecondActivity: StateFlow<Boolean> = _navigateToSecondActivity

    private val _text = MutableStateFlow("Hello, World!")
    val text: StateFlow<String> = _text

    private val _checked = MutableStateFlow(false)
    val checked: StateFlow<Boolean> = _checked

    private val _sliderPosition = MutableStateFlow(0.5f)
    val sliderPosition: StateFlow<Float> = _sliderPosition

    fun onTextChanged(newText: String) {
        _text.value = newText
    }

    fun onCheckedChanged(newChecked: Boolean) {
        _checked.value = newChecked
    }

    fun onSliderPositionChanged(newPosition: Float) {
        _sliderPosition.value = newPosition
    }

    fun onButtonClick() {
        _navigateToSecondActivity.value = true
    }

    fun onNavigatedToSecondActivity() {
        _navigateToSecondActivity.value = false
    }



}