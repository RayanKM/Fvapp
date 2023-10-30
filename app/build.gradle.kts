plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.apollographql.apollo3") version "3.8.2"

}

android {
    namespace = "com.fanzverse.fvapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fanzverse.fvapp"
        minSdk = 24
        targetSdk = 33
        versionCode = 3
        versionName = "1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
    }
}
apollo {
    service("Service") {
        packageName.set("com.example.fvapp")
    }
}
dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("org.chromium.net:cronet-embedded:113.5672.61")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.amplifyframework:aws-auth-cognito:2.13.0")
    implementation ("com.amplifyframework:core:2.13.0")
    implementation ("com.amplifyframework:aws-api:2.13.0")
    implementation ("com.amplifyframework:aws-storage-s3:2.13.0")
    implementation ("com.amplifyframework:aws-datastore:2.13.0")
    implementation ("com.amazonaws:aws-android-sdk-appsync:3.3.1")

    implementation("com.apollographql.apollo3:apollo-runtime:3.8.2")


    //toast
    implementation ("com.github.Spikeysanju:MotionToast:1.4")

    //Nav
    implementation ("com.github.ismaeldivita:chip-navigation-bar:1.4.0")

    //fragmentnav
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.2")

    //vidplayer
    implementation ("com.google.android.exoplayer:exoplayer:2.19.1")

    //Glide library
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")


    implementation ("com.github.dhaval2404:imagepicker:2.1")

    //Time
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")

}