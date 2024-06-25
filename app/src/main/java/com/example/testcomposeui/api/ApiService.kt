package com.example.testcomposeui.api

import com.example.testcomposeui.data.User
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("users")
    suspend fun getUsers(): Response<List<User>>
}