package com.example.testcomposeui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testcomposeui.data.User

open class BaseViewModel: ViewModel() {

    object LiveDataBus {
        //LiveData를 이용한 이벤트 버스
        val _selectUser: MutableLiveData<User> = MutableLiveData()
        val selectUser: LiveData<User> = _selectUser
    }


}