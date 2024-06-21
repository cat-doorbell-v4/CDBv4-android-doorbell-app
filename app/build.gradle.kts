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

        // Add ABI filters to restrict builds to specific architectures
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")  // Ensure the directory is correct
        }
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

    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.gpu)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.task.audio)
    implementation(libs.tensorflow.lite.task.vision)
    implementation(libs.tensorflow.lite.task.vision.v020)


    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)

    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}

