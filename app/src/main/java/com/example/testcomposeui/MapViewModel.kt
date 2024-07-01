package com.example.testcomposeui

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.testcomposeui.utils.MyLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class MapViewModel(application: Context): BaseViewModel() {

    private val _currentLocation = MutableLiveData<Location?>()
    val currentLocation: LiveData<Location?> get() = _currentLocation
    fun setCurrentLocation(location: Location) {
        _currentLocation.value = location
    }

    private val _googleMap = MutableLiveData<GoogleMap?>()
    val googleMap: LiveData<GoogleMap?> get() = _googleMap
    fun setGoogleMap(map: GoogleMap) {
        _googleMap.value = map
    }

    fun fetchCurrentLocation() {
        MyLocation.requestLocation { success, latitude, longitude ->
            if (success) {
                _currentLocation.value = Location("").apply {
                    this.latitude = latitude.toDouble()
                    this.longitude = longitude.toDouble()
                }
            } else {
                // Handle location fetch failure
            }
        }
    }

    fun setMyLocation(myLocation: Location) {
        //내위치에 마커를 찍고 이동.
        _googleMap.value?.let { map ->
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(myLocation.latitude, myLocation.longitude), 17f)
            map.animateCamera(cameraUpdate)
        }
    }

}