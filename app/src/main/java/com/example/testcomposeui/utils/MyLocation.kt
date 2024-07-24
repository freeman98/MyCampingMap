package com.example.testcomposeui.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.example.testcomposeui.MyApplication
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import kotlin.math.pow

object MyLocation {

    private val TAG: String = this::class.java.simpleName
    
    // 마지막으로 조회된 경도, 위도
    var latestLatitude: String = ""
    var latestLongitude: String = ""

    /**
     * 위치정보를 조회
     * 현재 위치를 조회할 수 있으면 조회하고(getCurrentLocation)
     * 조회할 수 없다면 마지막 위치 값 사용(as-is 코드: getLastKnownLocation)
     */
    fun requestLocation(onResult: (Boolean, String, String) -> Unit) {
        MyLog.d(TAG, "requestLocation()")
        try {
            val isGrant = ActivityCompat.checkSelfPermission(MyApplication.context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(MyApplication.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (isGrant
                    && servicesEnabled()
                    && GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MyApplication.context) == ConnectionResult.SUCCESS) {
                try {
                    LocationServices.getFusedLocationProviderClient(MyApplication.context).getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                            .addOnSuccessListener { location: Location? ->
                                if (location != null) {
                                    latestLatitude = location.latitude.toString()
                                    latestLongitude = location.longitude.toString()
                                    Handler(Looper.getMainLooper()).post { onResult(true, latestLatitude, latestLongitude) }
                                } else {
//                                    Handler(Looper.getMainLooper()).post { onResult(false, "", "") }
                                    getLastLocation(onResult)
                                }
                            }
                            .addOnFailureListener {
                                Handler(Looper.getMainLooper()).post { onResult(false, "", "") }
                            }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Handler(Looper.getMainLooper()).post { onResult(false, "", "") }
                }
            } else {
                getLastLocation(onResult)
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
            Handler(Looper.getMainLooper()).post { onResult(false, "", "") }
        }
    }

    private fun getLastLocation(onResult: (Boolean, String, String) -> Unit) {
        (MyApplication.context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)?.let { locationManager ->
            var location: Location? = null
            if (location == null &&
                ActivityCompat.checkSelfPermission(MyApplication.context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { location = it }
            }
            if (location == null &&
                ActivityCompat.checkSelfPermission(MyApplication.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let { location = it }
            }

            location?.let { it ->
                latestLatitude = it.latitude.toString()
                latestLongitude = it.longitude.toString()
                Handler(Looper.getMainLooper()).post { onResult(true, latestLatitude, latestLongitude) }
            } ?: run {
                Handler(Looper.getMainLooper()).post { onResult(false, "", "") }
            }
        } ?: run {
            Handler(Looper.getMainLooper()).post { onResult(false, "", "") }
        }
    }

    fun servicesEnabled(): Boolean {
        (MyApplication.context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)?.let { locationManager ->
            // [설정] - [위치] - [위치 서비스] - [Google 위치 정확도] - [위치 정확도 개선]  on/off 설정값.
            var network_provider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            val gps_provider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            MyLog.d(TAG,"servicesEnabled() network_provider = $network_provider")
            MyLog.d(TAG,"servicesEnabled() gps_provider = $gps_provider")
            return network_provider || gps_provider
        } ?: run {
            return false
        }
    }

    // TODO: 20220713, v20.1.0, freeman, Google 위치 정확도 개선 여부 조회 API 개발
    fun gpsServicesEnabled(): Boolean {
        (MyApplication.context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)?.let { locationManager ->
            // [설정] - [위치] - [위치 서비스] - [Google 위치 정확도] - [위치 정확도 개선]  on/off 설정값.
            val gps_provider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            MyLog.d(TAG,"gpsServicesEnabled() gps_provider = $gps_provider")
            return gps_provider
        } ?: run {
            return false
        }
    }

    fun networkServicesEnabled(): Boolean {
        (MyApplication.context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)?.let { locationManager ->
            var network_provider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            MyLog.d(TAG,"networkServicesEnabled() network_provider = $network_provider")
            return network_provider
        } ?: run {
            return false
        }
    }

    fun hasLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //정확한 위치 권한
            val permission_fine_location = MyApplication.context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            //대략적인 위치 권한
            val permission_coarse_location = MyApplication.context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            MyLog.d(TAG,"permission_fine_location = $permission_fine_location")
            MyLog.d(TAG,"permission_coarse_location = $permission_coarse_location")

            permission_fine_location == PackageManager.PERMISSION_GRANTED ||
                    permission_coarse_location == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun parseLatLng(latLngString: String): LatLng? {
        // 정규식으로 lat/lng 추출
        val regex = Regex("lat/lng: \\(([^,]+),([^\\)]+)\\)")
        val matchResult = regex.find(latLngString)
        return if (matchResult != null) {
            val (lat, lng) = matchResult.destructured
            LatLng(lat.toDouble(), lng.toDouble())
        } else {
            null
        }
    }

}