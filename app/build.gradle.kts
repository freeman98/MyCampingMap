plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
    alias(libs.plugins.googleServices)
    id("kotlin-kapt")
    //Hilt
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
}

kapt {
    correctErrorTypes = true
}

//enableAggregatingTask는 Hilt annotation processors의 컴파일 시간을 줄여주고 호출이 될 때만 실행되게 만들어줍
hilt {
    enableAggregatingTask = true
}

android {
    namespace = "com.freeman.mycampingmap"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.freeman.mycampingmap"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    //Hilt
    val hiltVersion by extra("2.50")
    implementation ("com.google.dagger:hilt-android:$hiltVersion")
    kapt ("com.google.dagger:hilt-android-compiler:$hiltVersion")

    implementation(libs.lifecycle.viewmodel.ktx)
    implementation (libs.ui)
    implementation (libs.ui.tooling.preview)
//    implementation (libs.androidx.room.runtime)
//    implementation (libs.androidx.room.ktx)
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")
    kapt ("androidx.room:room-compiler:2.6.1") // Kapt 사용
    implementation (libs.lifecycle.viewmodel.compose)
    implementation (libs.play.services.location)
    implementation (libs.places)
    implementation (libs.androidx.material)
    implementation (libs.github.glide)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.common.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)  //이미치 저리 관련.
    annotationProcessor (libs.compiler)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.adapter.rxjava3)
    implementation (libs.rxandroid)
    implementation (libs.play.services.maps.v1900)
    implementation (libs.places)
    implementation (libs.firebase.firestore.ktx)
    implementation (platform(libs.firebase.bom))
    implementation (libs.firebase.auth.ktx)
    implementation (libs.play.services.auth)
    implementation (libs.accompanist.permissions)
    implementation (libs.lottie.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}