package com.freeman.mycampingmap.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CampingSiteRepository(private val campingSiteDao: CampingSiteDao) {
//    val allCampingSites: List<CampingSite> = campingSiteDao.getAllCampingSites()

    suspend fun allCampingList(): List<CampingSite> {
        return withContext(Dispatchers.IO) {
            campingSiteDao.getAllCampingSites()
        }
    }

    suspend fun insertAll(campingSites: List<CampingSite>) {
        campingSiteDao.insertAll(campingSites)
    }

    suspend fun insert(campingSite: CampingSite) {
        campingSiteDao.insert(campingSite)
    }

    suspend fun delete(campingSite: CampingSite) {
        campingSiteDao.delete(campingSite)
    }

    suspend fun allDelete() {
        campingSiteDao.allDelete()
    }
}