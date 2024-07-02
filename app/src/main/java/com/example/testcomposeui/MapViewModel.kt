package com.example.testcomposeui

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.testcomposeui.utils.MyLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapViewModel(private val context: Context): BaseViewModel() {

    companion object {
        const val DEFAULT_ZOOM_LEVEL: Float = 15F
        val SEOUL_LATLNG = LatLng(37.5665, 126.9780) // 서울의 좌표
    }
    //내 위치 마커.
    private var myCurrentMarker: Marker? = null

    //내 위치.
    private val _currentLocation = MutableLiveData<Location?>()
    val currentLocation: LiveData<Location?> get() = _currentLocation
    fun setCurrentLocation(location: Location) {
        _currentLocation.value = location
    }

    private val _googleMap = MutableLiveData<GoogleMap?>()
    val googleMap: LiveData<GoogleMap?> get() = _googleMap

    fun setGoogleMap(map: GoogleMap) {
        Log.d(TAG, "setGoogleMap()")
        _googleMap.value = map

        //초기 위치. 설정 및 카메라 이동.
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        val uiSettings = map.uiSettings
        // 줌 레벨 설정
        uiSettings.isZoomControlsEnabled = true
        // 컴퍼스 활성화
        uiSettings.isCompassEnabled = true
        // 현재 위치 버튼 활성화
        uiSettings.isMyLocationButtonEnabled = true

        // 제스처 활성화
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = true
        uiSettings.isZoomGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
        uiSettings.isRotateGesturesEnabled = true

        // 모든 제스처 활성화
        uiSettings.setAllGesturesEnabled(true)

        //지도 기본 위치 지정.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL_LATLNG, DEFAULT_ZOOM_LEVEL))

        myCurrentLocation{isSuccess, latitude, longitude ->
            if(!isSuccess) {
                //지도 초기화 떄 실패하면 서울을 기본 좌표로 정한다.
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL_LATLNG, DEFAULT_ZOOM_LEVEL))
            }
        }

    }

    private fun myCurrentLocation(onResult: (Boolean, String, String) -> Unit) {
        MyLocation.requestLocation { isSuccess, latitude, longitude ->
            Log.d(TAG, "myCurrentLocation() isSuccess = $isSuccess, latitude=$latitude, longitude=$longitude")
            if(isSuccess) {
                //내 위치 찾기 성공.
                val mylocation = Location("").apply {
                    this.latitude = latitude.toDouble()
                    this.longitude = longitude.toDouble()
                }
                _currentLocation.value = mylocation
                val latlng = LatLng(mylocation.latitude, mylocation.longitude)
                latlng.let { latlng ->
                    //내위치 마커 옵션.
                    val myMarkerOptions = createMarkerOptions(
                        latlng, "You are here", null,  android.R.drawable.ic_menu_mylocation)

                    myCurrentMarker?.let { marker ->
                        //null 아니면 내 위치 마커를 초기화 한다
                        marker.remove()
                        myCurrentMarker = null
                    }

                    val map = _googleMap.value
                    myCurrentMarker = map?.addMarker(myMarkerOptions) //내위치 마커 추가.
                    map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM_LEVEL))
                }

            } else {
                //위치 찾기 실패.
                onResult(false, latitude, longitude)
            }

        }
    }

    // 마커옵션을 생성하는 함수.
    private fun createMarkerOptions(position: LatLng, title: String, snippet: String?, marker_id: Int): MarkerOptions {
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, marker_id)
        return MarkerOptions()
            .position(position)
            .title(title)
            .snippet(snippet)
            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
    }

    fun setMyLocationMarker(myLocation: Location) {
        //내위치에 마커를 찍고 이동.
        Log.d(TAG, "setMyLocationMarker()")
        _googleMap.value?.let { map ->

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(myLocation.latitude, myLocation.longitude), 17f)
            map.animateCamera(cameraUpdate)
        }
    }

}