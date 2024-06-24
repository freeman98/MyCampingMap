package com.example.testcomposeui.utils

import com.google.android.gms.maps.model.LatLng

class CampData {
    var name: String = "캠핑장 이름."
    val address: String = "주소"
    val latLng: LatLng? = null
    val imgUrl: String = "https://randomuser.me/api/portraits/women/11.jpg"

    override fun toString(): String {
        return "CampData(name='$name', address='$address', latLng=$latLng, imgUrl='$imgUrl')"
    }

}

object CampDummyDataProvider {
    var count: Int = 0
    val campList = List<CampData> (100) { CampData().apply { name += count++.toString() }  }
}