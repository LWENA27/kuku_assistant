plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.fowltyphoidmonitor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fowltyphoidmonitor"
        minSdk = 23
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.circleimageview)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.swiperefreshlayout)
    implementation(libs.gson)
    implementation(libs.mpandroidchart)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Existing dependencies
    implementation(libs.core.ktx)
    implementation(libs.appcompat.v161)
    implementation(libs.material.v1110)
    implementation(libs.constraintlayout.v214)

    // Add these new dependencies for Supabase integration
    implementation(libs.okhttp)
    implementation(libs.gson.v2101)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(libs.junit.v115)
    androidTestImplementation(libs.espresso.core.v351)

    // For disk caching of API responses
    implementation("com.squareup.retrofit2:retrofit-mock:2.9.0")
    implementation("com.squareup.okhttp3:okhttp-tls:4.9.1")
// Add to app/build.gradle dependencies section
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
}