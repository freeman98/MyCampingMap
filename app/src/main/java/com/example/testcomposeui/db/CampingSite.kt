package com.example.testcomposeui.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "camping_site_table")
data class CampingSite(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    val location: String = "",
    val rating: String = "",
    val reviews: String = "",
    val websiteUri: String = ""
)