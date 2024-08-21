package com.freeman.mycampingmap.di

import android.app.Application
import android.content.Context
import com.freeman.mycampingmap.App
import com.freeman.mycampingmap.auth.FirbaseEmailPassword
import com.freeman.mycampingmap.auth.FirebaseGoogleSignIn
import com.freeman.mycampingmap.auth.FirebaseManager
import com.freeman.mycampingmap.data.CampingDataUtil
import com.freeman.mycampingmap.utils.MyLog
import com.freeman.mycampingmap.viewmodels.MainViewModel
import com.freeman.mycampingmap.viewmodels.MapViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    val TAG = this::class.java.simpleName

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        MyLog.d(TAG, "provideContext()")
        return application.applicationContext
    }


}