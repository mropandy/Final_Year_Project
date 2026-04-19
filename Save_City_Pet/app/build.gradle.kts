import java.util.Properties

// app/build.gradle.kts
plugins {
    // 💡 修正：移除這裡的所有 version 和 alias 括號內的內容
    // 讓它自動去抓 Project 等級設定好的 8.7.3
    id("com.android.application")
    id("com.google.gms.google-services")
}

// 讀取 local.properties 的邏輯維持不變...


// 1. 讀取根目錄下的 local.properties 檔案
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

android {
    namespace = "com.example.save_city_pet"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.save_city_pet"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 2. 注入 BuildConfig
        val cloudName = localProperties.getProperty("CLOUDINARY_CLOUD_NAME") ?: "\"\""
        val apiKey = localProperties.getProperty("CLOUDINARY_API_KEY") ?: "\"\""

        // 確保這是您目前登入並設定 Preset 的那個 Cloud Name
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"dmjxt60yw\"")

        buildConfigField("String", "CLOUDINARY_API_KEY", apiKey)
    }

    buildFeatures {
        buildConfig = true
        mlModelBinding = true// 必須開啟，Java 才抓得到 BuildConfig
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

    // Firebase 相關
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")

    // TensorFlow Lite
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation("org.tensorflow:tensorflow-lite:2.13.0")
    implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.4.4")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.tools.core)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Cloudinary
    implementation("com.cloudinary:cloudinary-android:2.5.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
