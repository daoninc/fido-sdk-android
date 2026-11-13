import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 36

    defaultConfig {
        applicationId = "com.daon.fido.sdk.sample.basic"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "4.11"
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
    namespace = "com.daon.fido.sdk.sample.basic"
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Pin AndroidX Core to retain FingerprintManagerCompat discovery on Android 8; Core 1.18.0 removes this support.
configurations.configureEach {
    resolutionStrategy.force(
        "androidx.core:core:1.17.0",
        "androidx.core:core-ktx:1.17.0"
    )
}

dependencies {
    implementation(libs.daon.fido)
    implementation(libs.daon.fido.device)
    implementation(libs.daon.fido.crypto)

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
    implementation(libs.constraintlayout)
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
