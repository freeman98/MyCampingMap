package com.example.testcomposeui

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.testcomposeui.MyApplication.Companion.context
import com.example.testcomposeui.utils.MyLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import kotlin.math.pow

class MapViewModel(context: Context) : BaseViewModel() {

    companion object {
        const val DEFAULT_ZOOM_LEVEL: Float = 13F
        val SEOUL_LATLNG = LatLng(37.5665, 126.9780) // 서울의 좌표
        private val placesClient: PlacesClient = Places.createClient(MyApplication.context)

        //지도 검색 타입 캠핑장.
        val typeList = listOf("campground")

        //검색될 필드.
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.TYPES,
            Place.Field.PRIMARY_TYPE,
            Place.Field.PHONE_NUMBER,
            Place.Field.ICON_URL,
            Place.Field.ICON_BACKGROUND_COLOR,
            Place.Field.REVIEWS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.BUSINESS_STATUS,
            Place.Field.CURBSIDE_PICKUP,
            Place.Field.OPENING_HOURS,
            Place.Field.PRICE_LEVEL,
            Place.Field.RATING,
            Place.Field.USER_RATINGS_TOTAL,
            Place.Field.DELIVERY,
            Place.Field.DINE_IN,
            Place.Field.EDITORIAL_SUMMARY,
            Place.Field.PHOTO_METADATAS,
            Place.Field.WEBSITE_URI
        )

    }

    private val _isSearchListVisible = MutableLiveData<Boolean>()
    val isSearchListVisible get() = _isSearchListVisible

    // 마커를 저장할 리스트
    private var _markerPlaceMap = MutableLiveData<MutableMap<Marker, Place>>()
    val markerPlaceMap get() = _markerPlaceMap

    private val _placesList = MutableLiveData<List<Place>>()
    val placesList: LiveData<List<Place>> get() = _placesList

    init {
        _markerPlaceMap.observeForever { markerPlaceMap ->
            _placesList.value = markerPlaceMap.values.toList()
        }
    }

    //지도에 표시될 Place 리스트
//    private val _places = MutableLiveData<List<Place>>()
//    val places: LiveData<List<Place>> get() = _places


    //내 위치 마커.
    private var myCurrentMarker: Marker? = null


//    fun setCurrentLocation(location: Location) {
//        _currentMyLocation.value = location
//    }

    private val _googleMap = MutableLiveData<GoogleMap?>()
    val googleMap: LiveData<GoogleMap?> get() = _googleMap

    fun setGoogleMap(map: GoogleMap) {
        Log.d(TAG, "setGoogleMap()")
        _googleMap.value = map

        //초기 위치. 설정 및 카메라 이동.
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        val uiSettings = map.uiSettings
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        }

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

        //지도 클릭 이벤트.
        map.setInfoWindowAdapter(CustomInfoWindowAdapter(context))

        LiveDataBus._currentMyLocation.value?.let {
            //저장된 내위치가 있을경우.
            Log.d(TAG, "currentMyLocation = $it")
            val latlng = LatLng(it.latitude, it.longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM_LEVEL))

        } ?: run {
            Log.d(TAG, "currentMyLocation = null")
            //저장된 내위치가 없을경우 - 내 위치 찾기.
            myCurrentLocation { isSuccess, latitude, longitude ->
                if (!isSuccess) {
                    //지도 초기화 떄 실패하면 서울을 기본 좌표로 정한다.
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL_LATLNG, DEFAULT_ZOOM_LEVEL))
                }
            }
        }

        //지도 롱터치 이벤트 - 해당 터치 위치를 중심으로 대략적인 화면 범위 내에 있는 캠핑장 검색.
        map.setOnMapLongClickListener { latLng ->
            Log.d(TAG, "setOnMapClickListener() latLng = $latLng")
            //placesClient을 이용하여 클릭한 지도 근방의 지도 정보를 가져온다.
            searchNearbyPlaces(latLng) { places ->
//                Log.d(TAG, "searchNearbyPlaces() = $it")
                removeAllMarkers()
                addMapMarkers(places)
            }
        }

        map.setOnInfoWindowClickListener {marker ->
            val place = _markerPlaceMap.value?.get(marker)
            Log.d(TAG, "OnInfoWindowClickListener() $place")
        }

    }

    class CustomInfoWindowAdapter(context: Context) : GoogleMap.InfoWindowAdapter {
        // 마커 클릭 시 나오는 말풍선 커스텀
        private val window: View = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)

        override fun getInfoWindow(marker: Marker): View? {
            render(marker, window)
            return window
        }

        override fun getInfoContents(marker: Marker): View? {
            return null
        }

        private fun render(marker: Marker, view: View) {
            val title = marker.title
            val snippet = marker.snippet
            val place = marker.tag as? Place
            Log.d(TAG, "render() place.rating = ${place?.rating}")

            val titleUi = view.findViewById<TextView>(R.id.title)
            titleUi.text = place?.rating?.let {"$title  ${it}/5.0" } ?: title

            val snippetUi = view.findViewById<TextView>(R.id.snippet)
            snippetUi.text = snippet
        }
    }


    private fun addMapMarkers(places: List<Place>?) {
        val map = mutableMapOf<Marker, Place>()
        places?.forEach { place ->
            val markerOption = createMarkerOptions(place.latLng, place.name, place.address, R.drawable.ic_location_transparent)
            _googleMap.value?.addMarker(markerOption)?.let { marker ->
                marker.tag = place
                map[marker] = place
            }
        }
        if(map.isEmpty()) Toast.makeText(MyApplication.context, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
        _markerPlaceMap.value = map
    }

    private fun myCurrentLocation(onResult: (Boolean, String, String) -> Unit) {
        MyLocation.requestLocation { isSuccess, latitude, longitude ->
            Log.d(TAG, "myCurrentLocation() isSuccess = $isSuccess, latitude=$latitude, longitude=$longitude")
            if (isSuccess) {
                //내 위치 찾기 성공.
                val mylocation = Location("").apply {
                    this.latitude = latitude.toDouble()
                    this.longitude = longitude.toDouble()
                }
                LiveDataBus._currentMyLocation.value = mylocation
                val latlng = LatLng(mylocation.latitude, mylocation.longitude)
                latlng.let {

//                    myCurrentMarker?.let { marker ->
//                        //null 아니면 내 위치 마커를 초기화 한다
//                        marker.remove()
//                        myCurrentMarker = null
//                    }

//                    val map = _googleMap.value
                    //내위치 마커 옵션.
//                    val myMarkerOptions = createMarkerOptions(it, "You are here", null, android.R.drawable.ic_menu_mylocation)
//                    myCurrentMarker = map?.addMarker() //내위치 마커 추가.
                    _googleMap.value?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, DEFAULT_ZOOM_LEVEL))
                }

            } else {
                //위치 찾기 실패.
                onResult(false, latitude, longitude)
            }

        }
    }

    // 마커옵션을 생성하는 함수.
    private fun createMarkerOptions(
        position: LatLng,
        title: String,
        snippet: String?,
        marker_id: Int
    ): MarkerOptions {
        val bitmap: Bitmap = BitmapFactory.decodeResource(MyApplication.context.resources, marker_id)
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
            val myLatLng = LatLng(myLocation.latitude, myLocation.longitude)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLatLng, DEFAULT_ZOOM_LEVEL)
            map.animateCamera(cameraUpdate)
        }
    }

    private fun searchText(text: String, onResult: (List<Place>?) -> Unit) {
        //캠핑장 이름 검색.
        val LantLng = _googleMap.value?.cameraPosition?.target
        val bounds = CircularBounds.newInstance(LantLng, calculateRadiusInMeters())
        val request = SearchByTextRequest.builder(text, placeFields)
            .setLocationBias(bounds)
            .setIncludedType("campground")
            .build()
        placesClient.searchByText(request)
            .addOnSuccessListener { response ->
                onResult(response.places)

            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    //특정 좌표를 중심으로 화면에 보이는 대략정인 범위 내에 있는 캠핑장 검색하는 함수
    private fun searchNearbyPlaces(latLng: LatLng, onResult: (List<Place>?) -> Unit) {
        val bounds = CircularBounds.newInstance(latLng, calculateRadiusInMeters())
        val request = SearchNearbyRequest.builder(bounds, placeFields)
            .setIncludedPrimaryTypes(typeList)
            .build()

        placesClient.searchNearby(request)
            .addOnSuccessListener { response ->
                onResult(response.places)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Place not found: ${exception.message}")
                onResult(null)
            }
    }

    // 지도의 넓이를 계산하는 함수
    private fun calculateRadiusInMeters(): Double {
        _googleMap.value?.let { map ->
            val zoomLevel = map.cameraPosition.zoom
            val mapWidthInPixels = MyApplication.context.resources.displayMetrics.widthPixels
//            Log.d(TAG, "zoomLevel = $zoomLevel")

            val worldSize = 40075016.686 // Earth's circumference in meters
            val scale = 2.0.pow(zoomLevel.toDouble()) // 2^zoomLevel
            val mapWidthMeters = worldSize / scale // The width of the map in meters at the given zoom level
            //반지름
            val radius = ((mapWidthMeters * mapWidthInPixels) / (2 * 256)) / 2 // 256 is the tile size in pixels
            //최대 반지름은 50000m
            Log.d(TAG, "radius = $radius")
            return if (radius < 50000.0) radius else 50000.0

        }
        return 0.0
    }

    // 모든 마커를 삭제하는 함수
    fun removeAllMarkers() {
        Log.d(TAG, "removeAllMarkers()")
        _markerPlaceMap.value?.let { map ->
            if (map.isEmpty()) return
            for (marker in map.keys) {
                marker.remove()
            }
            map.clear()
        }
    }

    fun gotoFirstPlace(map: MutableMap<Marker, Place>) {
        //첫번쨰 장소로 지도 이동.
        Log.d(TAG, "gotoFastPlace()")
        var count = 0
        map.let {
            it.forEach { (marker, place) ->
                val latLng = place.latLng
                if (count++ == 0) {
                    _googleMap.value?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                    return@forEach
                }
            }
        }
    }

    //검색 버튼.
    fun searchPlace(searchText: String) {
        Log.d(TAG, "searchPlac() = $searchText")
        searchText(searchText) { places ->
            removeAllMarkers()
            addMapMarkers(places)
        }
    }

    fun setSearchListVisible(visible: Boolean) {
        _isSearchListVisible.value = visible
    }

    fun getMarkerCount(): Int {
        return _markerPlaceMap.value?.size ?: 0
    }

    fun gotoMyLocation() {
        //내 위치로 이동.
        myCurrentLocation { isSuccess, latitude, longitude ->
            if (!isSuccess) {
                //지도 초기화 떄 실패하면 서울을 기본 좌표로 정한다.
                Toast.makeText(MyApplication.context, "내 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun gotoPlace(place: Place?) {
        Log.d(TAG, "gotoPlace()")
        //파라미터로 넘어오는 Place를 기준으로 지도 이동.
        place?.latLng?.let {
            _googleMap.value?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f))
        }
    }
}