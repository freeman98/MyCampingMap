package com.freeman.mycampingmap.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CampingSiteDao {
    @Query("SELECT * FROM camping_site_table")
    fun getAllCampingSites(): List<CampingSite>

    @Query("SELECT * FROM camping_site_table WHERE id = :siteId")
    fun getCampingSiteById(siteId: String): Flow<CampingSite>

    //Room 데이터베이스에서 사용할 때, 삽입하려는 데이터가 이미
    // 존재하는 경우 기존 데이터를 새 데이터로 대체하는 것을 의미합니다.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(campingSite: CampingSite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(campingSites: List<CampingSite>)

    @Delete
    suspend fun delete(campingSite: CampingSite)
}