plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.clase.motorton"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.clase.motorton"
        minSdk = 23
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.firebase.firestore)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.google.firebase:firebase-auth:22.3.0")
    implementation ("com.google.android.gms:play-services-auth:21.0.0")
    implementation ("androidx.activity:activity-ktx:1.7.0")
    implementation ("org.osmdroid:osmdroid-android:6.1.16")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation ("androidx.appcompat:appcompat:1.2.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("org.json:json:20210307")
    implementation ("org.osmdroid:osmdroid-android:6.1.16")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.firebase:firebase-messaging:23.1.2")
    implementation ("androidx.work:work-runtime:2.8.1")
    implementation("com.google.guava:guava:31.1-android")
    implementation ("com.github.PhilJay:MPAndroidChart:3.1.0")
    implementation ("com.itextpdf:kernel:7.2.3")
    implementation ("com.itextpdf:layout:7.2.3")
    implementation("com.google.firebase:firebase-messaging:23.1.2")
}