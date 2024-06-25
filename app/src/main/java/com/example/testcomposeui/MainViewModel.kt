package com.example.testcomposeui

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.testcomposeui.api.RetrofitInstance
import com.example.testcomposeui.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//MVVM 모델 과 컴포스를 적용
class MainViewModel: BaseViewModel(){

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getUsers()
                if (response.isSuccessful) {
                    val userList = response.body() ?: emptyList()
                    for (user in userList) {
                        Log.d(TAG, user.toString())
                    }
                    _users.value = userList
                } else {
                    // Handle HTTP error
                }
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
    }


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