package com.freeman.mycampingmap

import android.app.Application
import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
open class App: Application() {

    companion object {
        //    @Inject
        lateinit var appContext: Context
    }

        override fun onCreate() {
        super.onCreate()
        appContext = this.applicationContext
        Places.initialize(applicationContext, "AIzaSyBWBSrgjli6i8M6XIyCcVuwxXcrLn8LIrQ")
        FirebaseApp.initializeApp(this)
    }
}