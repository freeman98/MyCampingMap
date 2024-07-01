package com.example.testcomposeui

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.testcomposeui.api.RetrofitInstance
import com.example.testcomposeui.data.User
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

//MVVM 모델 과 컴포스를 적용
class MainViewModel: BaseViewModel(){

    val compositeDisposable = CompositeDisposable()
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    init {
        fetchUsers()
    }

    fun fetchUsers() {
        val disposable = RetrofitInstance.api.getUsers()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { userList ->
                    for (user in userList) {
                        Log.d(TAG, user.toString())
                    }
                    _users.value = userList
                },
                { error -> error.printStackTrace() }
            )
        compositeDisposable.add(disposable)
    }

//    private fun fetchUsers() {
//        Log.d(TAG, "fetchUsers()")
//        // 코루틴을 사용하여 API 호출
//        viewModelScope.launch {
//            try {
//                val response = RetrofitInstance.api.getUsers()
//                if (response.isSuccessful) {
//                    // API 호출 성공
//                    val userList = response.body() ?: emptyList()
//                    for (user in userList) {
//                        Log.d(TAG, user.toString())
//                    }
//                    _users.value = userList
//                } else {
//                    // Handle HTTP error
//                    Log.e(TAG, "API 호출 실패: ${response.code()}")
//                }
//            } catch (e: Exception) {
//                // Handle exceptions
//                Log.e(TAG, "API 호출 실패: ${e.message}")
//            }
//        }
//    }


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