package com.freeman.mycampingmap.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.freeman.mycampingmap.db.CampingSite

class SharedViewModel : ViewModel() {
    private val _syncAllCampingList = MutableLiveData<List<CampingSite>>()
    val syncAllCampingList: LiveData<List<CampingSite>> = _syncAllCampingList

    fun updateCampingList(campingList: List<CampingSite>) {
        _syncAllCampingList.value = campingList
    }
}