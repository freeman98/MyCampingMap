package com.example.testcomposeui

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testcomposeui.data.CampingSite

open class BaseViewModel: ViewModel() {

    object LiveDataBus {
        //LiveData를 이용한 이벤트 버스
        val _selectCampingSite: MutableLiveData<CampingSite> = MutableLiveData()
        val selectCampingSite: LiveData<CampingSite> = _selectCampingSite

        //내 위치.
        val _currentMyLocation = MutableLiveData<Location?>()
        val currentMyLocation: LiveData<Location?> get() = _currentMyLocation
    }


}