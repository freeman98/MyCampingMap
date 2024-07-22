package com.example.testcomposeui.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_table LIMIT 1")
    fun getUser(): Flow<User?>

    @Insert
    suspend fun insertUser(user: User)  //

    @Query("DELETE FROM user_table")
    suspend fun deleteUser()

}