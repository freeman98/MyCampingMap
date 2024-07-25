package com.freeman.mycampingmap.viewmodels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freeman.mycampingmap.MyApplication
import com.freeman.mycampingmap.db.CampingSite
import com.freeman.mycampingmap.db.CampingSiteDatabase
import com.freeman.mycampingmap.db.CampingSiteRepository
import com.freeman.mycampingmap.viewmodels.BaseViewModel.LiveDataBus._selectCampingSite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseViewModel : ViewModel() {

    //LiveData를 이용한 이벤트 버스
    object LiveDataBus {

        //메인화면에서 선택한 캠핑장 1회성 이벤트
        val _selectCampingSite: MutableLiveData<Event<CampingSite?>> = MutableLiveData<Event<CampingSite?>>()
        val selectCampingSite: LiveData<Event<CampingSite?>> = _selectCampingSite

        //내 위치.
        val _currentMyLocation = MutableLiveData<Location?>()
        val currentMyLocation: LiveData<Location?> get() = _currentMyLocation

    }

    //로딩 컨트롤을 위한 LiveData
    val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    open fun selectCampingSite(campingSite: CampingSite) {
        _selectCampingSite.value = Event(campingSite)
    }

    // LiveData를 이용한 1회성 이벤트
    class Event<out T>(private val content: T) {

        private var hasBeenHandled = false

        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }//                    CampingDataUtil.syncCampingSites(localSites, remoteSites) { syncCampingSites ->
//                        MyLog.d(TAG, "syncCampingSites() localSites.size = " +
//                                "${localSites.size}, remoteSites.size = ${remoteSites.size}")
//                        _syncAllCampingList.value = syncCampingSites
//                        MyLog.d(TAG, "syncCampingSites() syncCampingSites.size = ${syncCampingSites.size}")
//                    }
        }

        fun peekContent(): T = content
    }

    val campingSiteRepository: CampingSiteRepository
//    val dbAllCampingSites: LiveData<List<CampingSite>>

    init {
        val campingSiteDao = CampingSiteDatabase.getDatabase(MyApplication.context).campingSiteDao()
        campingSiteRepository = CampingSiteRepository(campingSiteDao)
//        dbAllCampingSites = campingSiteRepository.allCampingSites
    }

    suspend fun dbAllCampingSiteSelect(): List<CampingSite> {
        // 캠핑장 전체 조회
        return campingSiteRepository.allCampingList()
//        try {
//            campingSiteRepository.allCampingList()
//            withContext(Dispatchers.Main) {
//                onSuccess(true)
//            }
//        } catch (e: Exception) {
//            withContext(Dispatchers.Main) { onSuccess(true) }
//        }
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