copy this berfore you run!!!

local.properties:

sdk.dir=C\:\\Users\\user\\AppData\\Local\\Android\\Sdk
CLOUDINARY_CLOUD_NAME="Root"
CLOUDINARY_API_KEY="623729695394899"
CLOUDINARY_API_SECRET="7ikRwLJDkocOhaoWUMlB5Km5NgA"

gradle.properties:

org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true

build.gradle.kts:

plugins {
    id("com.android.application") version "8.9.1" apply false
    id("com.android.library") version "8.9.1" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
