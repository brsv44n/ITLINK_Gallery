import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt)
}

val localProperties = Properties().apply {
    val localFile = project.rootProject.file("local.properties")
    if (localFile.exists()) {
        load(localFile.inputStream())
    }
}

android {
    namespace = "com.brsv.itlink_gallery"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.brsv.itlink_gallery"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false

            val baseUrl = localProperties.getProperty("base.url", "")
            buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val baseUrl = localProperties.getProperty("base.url", "")
            buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    hilt {
        enableAggregatingTask = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

//    // For instrumentation tests
//    androidTestImplementation("com.google.dagger:hilt-android-testing:2.55")
//    kspAndroidTest("com.google.dagger:hilt-compiler:2.55")
//
//    // For local unit tests
//    testImplementation("com.google.dagger:hilt-android-testing:2.55")
//    kspTest("com.google.dagger:hilt-compiler:2.55")

    //Network
    implementation(libs.retrofit)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    //Atomic
    implementation(libs.kotlinx.atomicfu)

    //Coroutines
    implementation(libs.kotlinx.coroutines.core)

    //Coil
    implementation(libs.coil.compose)
    implementation(libs.coil)

    //Hilt
    implementation(libs.dagger.hilt)
    implementation(libs.hilt.viewmodel)
    ksp(libs.dagger.hilt.compiler)
}
