plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.parkingfinder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.parkingfinder"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation ("androidx.lifecycle:lifecycle-livedata:2.8.7")
    implementation( "androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation ("androidx.lifecycle:lifecycle-runtime:2.8.7")
    implementation ("androidx.lifecycle:lifecycle-common-java8:2.8.7")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.firebase.firestore)
    implementation("androidx.annotation:annotation:1.7.1")
    implementation(libs.play.services.location)
    implementation(libs.work.runtime)
    implementation(libs.firebase.database)
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.storage)
    implementation(libs.room.common)
    implementation(libs.room.runtime)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.swiperefreshlayout)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor(libs.room.compiler)
}