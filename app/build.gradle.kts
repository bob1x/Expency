plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.expency"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.expency"
        minSdk = 33
        targetSdk = 34
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.core)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation (libs.google.firebase.auth)
    implementation (libs.firebase.firestore)
    implementation (libs.firebase.ui.auth)
    implementation (libs.com.google.firebase.firebase.auth)
    implementation (libs.play.services.auth)
    implementation (libs.drawerlayout)
    implementation (libs.material.v130)
    implementation (libs.firebase.core)
    implementation (libs.androidx.material)
    implementation (libs.androidx.ui)
    implementation (libs.androidx.foundation)
    implementation (libs.androidx.appcompat.v161)
    implementation (libs.mpandroidchart)
    implementation (libs.androidx.recyclerview)


}