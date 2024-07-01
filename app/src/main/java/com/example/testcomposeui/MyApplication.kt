package com.example.testcomposeui

import android.app.Application
import android.content.Context
import com.google.android.libraries.places.api.Places

class MyApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private lateinit var instance: MyApplication

        val context: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        Places.initialize(applicationContext, "AIzaSyBWBSrgjli6i8M6XIyCcVuwxXcrLn8LIrQ")


    }
}