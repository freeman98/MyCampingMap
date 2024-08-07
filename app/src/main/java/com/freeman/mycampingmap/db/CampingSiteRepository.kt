package com.freeman.mycampingmap.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CampingSiteRepository(private val campingSiteDao: CampingSiteDao) {

    val allCampingSites = MutableLiveData<List<CampingSite>>()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    suspend fun getAllCampingSites(): List<CampingSite> {
        return asyncAllCampingList().await()
    }

    fun allCampingList() {
        coroutineScope.launch(Dispatchers.Main) {
            allCampingSites.value = asyncAllCampingList().await()
        }
    }

    private fun asyncAllCampingList(): Deferred<List<CampingSite>> =
        coroutineScope.async(Dispatchers.IO) {
            return@async campingSiteDao.getAllCampingSites()
        }

    fun insertAll(campingSites: List<CampingSite>) {
        coroutineScope.launch(Dispatchers.IO) {
            campingSiteDao.insertAll(campingSites)
        }
    }

    fun insert(campingSite: CampingSite) {
        coroutineScope.launch(Dispatchers.IO) {
            campingSiteDao.insert(campingSite)
        }
    }

    fun delete(campingSite: CampingSite) {
        coroutineScope.launch(Dispatchers.IO) {
            campingSiteDao.delete(campingSite)
        }
    }

    fun allDelete() {
        coroutineScope.launch(Dispatchers.IO) {
            campingSiteDao.allDelete()
        }
    }
}