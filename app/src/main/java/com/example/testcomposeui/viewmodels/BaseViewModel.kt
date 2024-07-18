package com.example.testcomposeui.viewmodels

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testcomposeui.data.CampingSite
import com.example.testcomposeui.datamodel.RequestPermissionModel
import com.example.testcomposeui.utils.SingleLiveEvent
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

open class BaseViewModel: ViewModel() {

    object LiveDataBus {
        //LiveData를 이용한 이벤트 버스
        val _selectCampingSite: MutableLiveData<CampingSite> = MutableLiveData()
        val selectCampingSite: LiveData<CampingSite> = _selectCampingSite

        //내 위치.
        val _currentMyLocation = MutableLiveData<Location?>()
        val currentMyLocation: LiveData<Location?> get() = _currentMyLocation

        //내 계정 userID
        val _userID = MutableLiveData<String>()
        val userID: LiveData<String> = _userID
    }

    /**
     * 퍼미션 체크 및 사용자 권한 동의 시스템 팝업을 띄우기 위한 MPSingleLiveEvent 정의
     */
    var requestPermissionEvent = SingleLiveEvent<RequestPermissionModel>()

    companion object {
        lateinit var auth: FirebaseAuth
//        @SuppressLint("StaticFieldLeak")
        lateinit var googleSignInClient: GoogleSignInClient
//        lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    }

    init {

    }

}