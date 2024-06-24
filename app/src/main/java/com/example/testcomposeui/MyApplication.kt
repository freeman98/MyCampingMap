package com.example.testcomposeui

import android.app.Application
import com.google.android.libraries.places.api.Places

class MyApplication : Application() {

    companion object {
        lateinit var context: MyApplication
    }

    init {
        context = this
    }

    override fun onCreate() {
        super.onCreate()

        Places.initialize(applicationContext, "AIzaSyBWBSrgjli6i8M6XIyCcVuwxXcrLn8LIrQ")


    }
}