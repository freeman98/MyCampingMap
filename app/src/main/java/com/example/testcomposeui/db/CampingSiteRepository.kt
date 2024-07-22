package com.example.testcomposeui.db

import androidx.lifecycle.LiveData

class CampingSiteRepository(private val campingSiteDao: CampingSiteDao) {
    val allCampingSites: LiveData<List<CampingSite>> = campingSiteDao.getAllCampingSites()

    suspend fun insert(campingSite: CampingSite) {
        campingSiteDao.insert(campingSite)
    }

    suspend fun delete(campingSite: CampingSite) {
        campingSiteDao.delete(campingSite)
    }
}