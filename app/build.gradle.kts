
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.invest"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.invest"
        minSdk = 26
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    //Auth UI
    implementation (libs.firebase.ui.auth)
    implementation(libs.firebase.auth)
    //implementation(libs.play.services.auth)
    implementation(libs.play.services.auth.v2120) // If sign in with google not working use libs.play.services.auth (version 20.0.7)

    //Real-Time DB
    implementation(libs.firebase.database)

    //Alpha Vantage Java
    implementation (libs.alphavantage.java)

    //Retrofit for network calls
    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    //Gson for JSON parsing
    implementation (libs.gson)

    //Robolectric for testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.9")

    //Navigation bar
    implementation ("com.google.android.material:material:1.4.0")

    //Line chart
    implementation ("com.github.PhilJay:MPAndroidChart:v3.0.3")
}
