import org.jetbrains.kotlin.daemon.client.KotlinCompilerClient.compile

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "dev.panwar.kurentogroupvideocalling"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.panwar.kurentogroupvideocalling"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding=true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("io.socket:socket.io-client:2.1.0") {
        // excluding org.json which is provided by Android
        exclude("org.json" ,"json")
    }
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("fi.vtt.nubomedia:kurento-room-client-android:1.1.2")
//    implementation ("org.webrtc:google-webrtc:1.0.32006")
    implementation ("com.mesibo.api:webrtc:1.0.5")

}