plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("de.undercouch.download") version "5.0.5"

}

configurations.all {
    resolutionStrategy {
        force("org.tensorflow:tensorflow-lite:2.9.0")
        force("org.tensorflow:tensorflow-lite-support:0.4.2")
    }
}

android {
    namespace = "com.example.cdbv4_pixel_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cdbv4_pixel_app"
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
}

project.ext.set("ASSET_DIR", file("src/main/assets"))
project.ext.set("TEST_ASSETS_DIR", file("src/test/assets"))

apply {
    from("download_models.gradle")
}

tasks.register("downloadModels") {
    dependsOn(
        ":downloadAudioClassifierModel",
        ":downloadModelFile",
        ":downloadModelFile0",
        ":downloadModelFile1",
        ":downloadModelFile2",
        ":copyTestModel"
    )
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("org.tensorflow:tensorflow-lite:2.12.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.12.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.2")
    implementation("org.tensorflow:tensorflow-lite-task-audio:0.4.2")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.2")

    implementation("androidx.camera:camera-core:1.1.0")
    implementation("androidx.camera:camera-camera2:1.1.0")
    implementation("androidx.camera:camera-lifecycle:1.1.0")
    implementation("androidx.camera:camera-view:1.3.3")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")

    implementation("androidx.lifecycle:lifecycle-service:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
}
