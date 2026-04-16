plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.save_city_pet"
    // 建議改回 34 或 35，目前的 36 寫法可能會導致 Sync 失敗
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.save_city_pet"
        minSdk = 24
        targetSdk = 36 // 建議與 compileSdk 一致
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

    // Firebase 相關
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")


    // 💡 建議額外加入 Glide，用來讀取 Firebase 上的圖片
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
