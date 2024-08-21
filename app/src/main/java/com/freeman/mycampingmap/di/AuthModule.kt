package com.freeman.mycampingmap.di

import android.content.Context
import com.freeman.mycampingmap.auth.FirbaseEmailPassword
import com.freeman.mycampingmap.auth.FirebaseGoogleSignIn
import com.freeman.mycampingmap.auth.FirebaseManager
import com.freeman.mycampingmap.data.CampingDataUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object AuthModule {

    val TAG = this::class.java.simpleName

    @Provides
    @ViewModelScoped
    fun provideFirebaseManager(context: Context, firebaseGoogleSignIn: FirebaseGoogleSignIn): FirebaseManager =
        FirebaseManager(context, firebaseGoogleSignIn)

    @Provides
    @ViewModelScoped
    fun provideFirebaseEmailPassword(context: Context): FirbaseEmailPassword =
        FirbaseEmailPassword(context)

    @Provides
    @ViewModelScoped
    fun provideFirebaseGoogleSignIn(context: Context): FirebaseGoogleSignIn =
        FirebaseGoogleSignIn(context)

    @Provides
    @ViewModelScoped
    fun provideCampingDataUtil(context: Context, firebaseManager: FirebaseManager): CampingDataUtil =
        CampingDataUtil(context, firebaseManager)

}