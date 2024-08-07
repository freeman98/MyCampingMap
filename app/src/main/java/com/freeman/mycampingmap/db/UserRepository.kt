package com.freeman.mycampingmap.db

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class UserRepository(private val userDao: UserDao) {

    val userData = MutableLiveData<User?>()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
//    fun getUser(): Flow<User?> = userDao.getUser()

    fun getUser() {
        coroutineScope.launch(Dispatchers.Main) {
            userData.value = asyncUser().await()
        }
    }

    private fun asyncUser(): Deferred<User?> =
        coroutineScope.async(Dispatchers.IO) {
            return@async userDao.getUser()
        }


    fun insertUser(user: User) {
        coroutineScope.launch(Dispatchers.IO) {
            userDao.insertUser(user)
        }
    }

    fun deleteUser() {
        coroutineScope.launch(Dispatchers.IO) {
            userDao.deleteUser()
        }
    }

}