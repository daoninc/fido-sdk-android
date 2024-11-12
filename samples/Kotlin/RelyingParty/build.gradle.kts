// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val kotlinVersion by extra { "1.9.25" }
    val composeVersion by extra { "1.4.0" }
    val composeNavigationVersion by extra { "2.5.3" }

}

plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
    id("com.google.dagger.hilt.android") version("2.50") apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}
