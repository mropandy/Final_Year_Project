// build.gradle.kts (Project: Save_City_Pet)
plugins {
    id("com.android.application") version "8.9.1" apply false
    id("com.android.library") version "8.9.1" apply false

    // 💡 關鍵：在這裡明確定義 Google Services 的版本
    id("com.google.gms.google-services") version "4.4.2" apply false
}
