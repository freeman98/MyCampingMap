package com.freeman.mycampingmap

import android.app.Application
import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp

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
        FirebaseApp.initializeApp(this)


    }
}