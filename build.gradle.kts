// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    val hiltVersion by extra("2.50")

    repositories {
        google() // Make sure you have the Google repository
        mavenCentral()
    }
    dependencies {
        // ... other dependencies ...
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.googleServices) apply false
    kotlin("kapt") version "1.9.0"
    id("com.google.dagger.hilt.android") version "2.50" apply false

}


