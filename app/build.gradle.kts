import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("androidx.room")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    // keystore.properties file, in the rootProject folder.
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    // Initialize a new Properties() object called keystoreProperties.
    val keystoreProperties = Properties()
    // Load keystore.properties file into the keystoreProperties object.
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    namespace = "com.example.mafscan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.maf.mafscan"
        minSdk = 30
        targetSdk = 33
        versionCode = 3
        versionName = "06_03_25_V1.2"

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
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    //implementation(libs.firebase.crashlytics)
    // Import the BoM for the Firebase platform
    implementation(platform(libs.firebase.bom))

    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.google.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // To use room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Ui dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.core.splashscreen)
    implementation(libs.constraintlayout)
    implementation(libs.constraintlayout.compose)

    // external lib and dependencies
    implementation(libs.jtds)
    implementation(libs.datalogic.android.sdk)

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}