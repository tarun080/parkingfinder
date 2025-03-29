plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
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

    // More aggressive exclusion of duplicate files
    packaging {
        resources {
            excludes.add("META-INF/*")
            excludes.add("META-INF/LICENSE*")
            excludes.add("META-INF/NOTICE*")
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("/*.properties")
            excludes.add("/*.txt")
            excludes.add("/*.xml")
            excludes.add("/*.version")
            excludes.add("/*.kotlin_module")
            excludes.add("android/os/*")
        }
    }

    // Add configurations block to handle dependency conflicts
    configurations.all {
        // Resolve conflicts for specific dependencies
        resolutionStrategy {
            force("androidx.core:core:1.10.0")
            force("androidx.lifecycle:lifecycle-runtime:2.6.1")
            // Add other forced versions as needed
        }
    }
}

dependencies {
    // Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1") // Downgrade to a stable version

    // OSMDroid dependencies - simplified to reduce conflicts
    implementation("org.osmdroid:osmdroid-android:6.1.16")

    // Core dependencies
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime:2.6.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.5.0")) // Use a stable version
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-database")

    // Room database
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // UI components
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation("androidx.navigation:navigation-ui:2.7.5")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Preference for OSMDroid
    implementation("androidx.preference:preference:1.2.1")

    // Other utilities
    implementation("com.google.guava:guava:31.1-android")
    implementation("androidx.annotation:annotation:1.7.1")
    implementation("androidx.work:work-runtime:2.8.1")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}