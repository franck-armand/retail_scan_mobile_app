plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mafscan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mafscan"
        minSdk = 30
        targetSdk = 33
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
    implementation(files("libs/BT_A700.jar"))
    implementation("com.github.datalogic:datalogic-android-sdk:1.42")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("net.sourceforge.jtds:jtds:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    // To use constraintlayout in compose
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0")
}