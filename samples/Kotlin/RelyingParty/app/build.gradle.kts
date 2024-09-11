plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.daon.fido.sdk.sample.kt"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.daon.fido.sdk.sample.kt"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "4.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles (
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


dependencies {

    implementation(libs.daon.fido.kt)
    implementation(libs.daon.fido.crypto)
    implementation(libs.daon.fido.device)

    implementation(libs.daon.fido.auth.common)
    implementation(libs.daon.fido.auth.authenticator)
    implementation(libs.daon.fido.auth.face.ifp)

    implementation(libs.daon.face)
    implementation(libs.daon.face.capture)
    implementation(libs.daon.face.quality)
    implementation(libs.daon.face.liveness)
    implementation(libs.daon.face.matcher)
    implementation(libs.daon.face.detector)

    // CameraX core library
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.video)
    implementation(libs.camera.view)

    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecyle.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.appcompat)
    implementation(libs.material)
    //Compose Navigation
    implementation(libs.androidx.compose.navigation)
    implementation(libs.gson)
    implementation(libs.biometric)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.lifecyle.viewmodel)

    //Hilt
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    //Accompanist-Permissions
    implementation(libs.accompanist.permission)
    //Jackson JSON converter
    implementation(libs.jackson)



    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation(libs.constraintlayout.compose)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.test.rule)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.compose.ui:ui-viewbinding")



}

kapt {
    correctErrorTypes = true
}