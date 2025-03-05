plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "com.daon.fido.sdk.sample.basic"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
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
    namespace = "com.daon.fido.sdk.sample.basic"
}

dependencies {

    implementation(libs.daon.fido)
    implementation(libs.daon.fido.device)
    implementation(libs.daon.fido.crypto)

    //implementation(libs.daon.fido.auth.common)
    implementation(libs.daon.fido.auth.authenticator)
    implementation(libs.daon.fido.auth.face)

    // Daon Face
    implementation(libs.daon.face)
    implementation(libs.daon.face.quality)
    //Daon Passive
    implementation(libs.daon.face.liveness.dfl)
    implementation(libs.daon.face.matcher)
    implementation(libs.daon.face.detector)
    

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout.compose)
    implementation(libs.gson)
    implementation(libs.biometric)
    implementation(libs.androidx.core)
    implementation(libs.play.integrity)
    implementation(libs.androidx.preference)

    // CameraX core library
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    // CameraX View class
    implementation(libs.camera.view)

    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.espresso.core)
    testImplementation(libs.junit)

}
