package com.freeman.mycampingmap.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.reactivex.rxjava3.annotations.NonNull

@Entity(tableName = "camping_site_table")
data class CampingSite(
    @PrimaryKey
    @NonNull
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    val location: String = "",
    val rating: String = "",
    val reviews: String = "",
    val websiteUri: String = "",
    var distanceFromCurrentLocation: Float = 0f // 거리 필드 추가
)