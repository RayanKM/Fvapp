
buildscript {
    dependencies {
        classpath ("com.android.tools.build:gradle:8.0.0") // Use a compatible version
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.0.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
}