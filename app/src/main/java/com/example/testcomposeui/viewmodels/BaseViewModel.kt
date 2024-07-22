package com.example.testcomposeui.viewmodels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testcomposeui.MyApplication
import com.example.testcomposeui.db.CampingSite
import com.example.testcomposeui.db.CampingSiteDatabase
import com.example.testcomposeui.db.CampingSiteRepository
import com.example.testcomposeui.viewmodels.BaseViewModel.LiveDataBus._selectCampingSite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseViewModel : ViewModel() {

    object LiveDataBus {
        //LiveData를 이용한 이벤트 버스
        val _selectCampingSite: MutableLiveData<Event<CampingSite>> = MutableLiveData<Event<CampingSite>>()
        val selectCampingSite: LiveData<Event<CampingSite>> = _selectCampingSite

        //내 위치.
        val _currentMyLocation = MutableLiveData<Location?>()
        val currentMyLocation: LiveData<Location?> get() = _currentMyLocation

    }

    open fun selectCampingSite(campingSite: CampingSite) {
        _selectCampingSite.value = Event(campingSite)
    }

    class Event<out T>(private val content: T) {

        private var hasBeenHandled = false

        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }

        fun peekContent(): T = content
    }

    private val campingSiteRepository: CampingSiteRepository
    val allCampingSites: LiveData<List<CampingSite>>

    init {
        val campingSiteDao = CampingSiteDatabase.getDatabase(MyApplication.context).campingSiteDao()
        campingSiteRepository = CampingSiteRepository(campingSiteDao)
        allCampingSites = campingSiteRepository.allCampingSites
    }

    fun dbCampingSiteInsert(
        campingSite: CampingSite,
        onSuccess: (Boolean) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        // 캠핑장 추가
        try {
            campingSiteRepository.insert(campingSite)
            withContext(Dispatchers.Main) {
                onSuccess(true)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onSuccess(false)
            }
        }
    }

    fun dbCampingSiteDelete(
        campingSite: CampingSite,
        onSuccess: (Boolean) -> Unit,
    ) = viewModelScope.launch(Dispatchers.IO) {
        // 캠핑장 삭제
        try {
            campingSiteRepository.delete(campingSite)
            withContext(Dispatchers.Main) {
                onSuccess(true)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onSuccess(false)
            }
        }

    }

}