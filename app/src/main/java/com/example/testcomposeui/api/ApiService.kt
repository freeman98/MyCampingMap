package com.example.testcomposeui.api

import com.example.testcomposeui.db.User
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET

interface ApiService {
    @GET("users")
    fun getUsers(): Single<List<User>>
}