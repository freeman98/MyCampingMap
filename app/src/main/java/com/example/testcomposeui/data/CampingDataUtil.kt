package com.example.testcomposeui.data

import android.util.Log
import android.widget.Toast
import com.example.testcomposeui.MyApplication
import com.example.testcomposeui.db.CampingSite
import com.example.testcomposeui.db.User
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.firestore.FirebaseFirestore

object CampingDataUtil {

    fun createCampingSiteData(place: Place): CampingSite {
        // 캠핑장 정보 객체 생성
        return CampingSite(
            id = place.id?.toString() ?: "",
            name = place.name ?: "",
            address = place.address ?: "",
            phoneNumber = place.phoneNumber ?: "",
            location = place.latLng?.toString() ?: "",
            rating = place.rating?.toString() ?: "",
            reviews = place.reviews?.toString() ?: "",
            websiteUri = place.websiteUri?.toString() ?: ""
        )
    }

    fun addFirebaseCampingSite(user: User, place: Place) {
        //파이어스토어 데이터베이스에 저장.
//    Log.d(TAG, "saveCampingSite() place= ${place}")
        val campingSite = createCampingSiteData(place)
        Log.d("MapScreen", "saveCampingSite() = $campingSite")

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(user.uid).collection("my_camping_list")
            .document(campingSite.id).set(campingSite)
            .addOnSuccessListener {
                // 데이터 저장 성공
                Log.d("", "addOnSuccessListener()")
                Toast.makeText(MyApplication.context, "저장 성공", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // 데이터 저장 실패
                Log.w("", "addOnFailureListener() = ", e)
                Toast.makeText(MyApplication.context, "저장 실패", Toast.LENGTH_SHORT).show()
            }
    }
}