package com.freeman.mycampingmap.activity

import android.app.Application
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import com.freeman.mycampingmap.utils.MyLog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity: ComponentActivity() {

    @Inject
    lateinit var app: Application

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyLog.d("BaseActivity", "onCreate() app = $app")
    }
}