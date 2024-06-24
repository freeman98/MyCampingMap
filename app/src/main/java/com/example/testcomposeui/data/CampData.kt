package com.example.testcomposeui.data

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

class CampData() : Parcelable {
    var name: String = "캠핑장 이름 - "
    var address: String = "주소"
    var latLng: LatLng? = null
    var imgUrl: String = "https://randomuser.me/api/portraits/women/11.jpg"


    constructor(parcel: Parcel) : this() {
        name = parcel.readString().toString()
        address = parcel.readString().toString()
        latLng = parcel.readParcelable(LatLng::class.java.classLoader)
        imgUrl = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeParcelable(latLng, flags)
        parcel.writeString(imgUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "CampData(name='$name', address='$address', latLng=$latLng, imgUrl='$imgUrl')"
    }

    companion object CREATOR : Parcelable.Creator<CampData> {
        override fun createFromParcel(parcel: Parcel): CampData {
            return CampData(parcel)
        }

        override fun newArray(size: Int): Array<CampData?> {
            return arrayOfNulls(size)
        }
    }

}

object CampDummyDataProvider {
    var count: Int = 0
    val campList = List<CampData> (100) { CampData().apply { name += count++.toString() }  }
}