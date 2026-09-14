// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val kotlinVersion by extra { "2.2.0" }
    val composeVersion by extra { "1.4.0" }
    val composeNavigationVersion by extra { "2.5.3" }

}

plugins {
    id("com.android.application") version "9.2.1" apply false
    id("org.jetbrains.kotlin.android") version "2.4.10" apply false
    id("com.google.dagger.hilt.android") version("2.60.1") apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}
