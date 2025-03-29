// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        // Add classpath for Google Services plugin - this is important
        classpath("com.google.gms:google-services:4.4.2")
    }
}

// Make sure all projects have access to the necessary repositories
allprojects {
    repositories {
        google()
        mavenCentral()
        // Add any other repositories you might need
    }
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}