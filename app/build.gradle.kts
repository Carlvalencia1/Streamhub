plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.secrets.gradle)
    alias(libs.plugins.jetbrainsKotlinSerialization)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.valencia.streamhub"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.valencia.streamhub"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true   // Habilitar BuildConfig
        resValues = true
    }

    // ✅ Simplificamos los flavors (solo mantenemos dev/prod pero sin URLs de APIs externas)
    flavorDimensions.add("environment")
    productFlavors {
        create("dev") {
            dimension = "environment"
            resValue("string", "app_name", "StreamHub Dev")
            buildConfigField("String", "API_BASE_URL", "\"https://tu-backend-dev.com/api/\"") // Cambia por tu URL real
        }
        create("prod") {
            dimension = "environment"
            resValue("string", "app_name", "StreamHub")
            buildConfigField("String", "API_BASE_URL", "\"https://tu-backend-prod.com/api/\"") // Cambia por tu URL real
        }
    }
}

secrets {
    propertiesFileName = "local.properties"
    defaultPropertiesFileName = "local.defaults.properties"
    ignoreList.add("sdk.dir")
}

ksp {
    arg("hilt.disableModulesHaveInstallInCheck", "true")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)

    // Networking (para consumir tu API de usuarios/streams)
    implementation(libs.com.squareup.retrofit2.retrofit)
    implementation(libs.com.squareup.retrofit2.converter.json)

    // Image loading
    implementation(libs.io.coil.kt.coil.compose)

    // Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Local Database (para cache o follows offline? Opcional)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ✅ Firebase Cloud Messaging (para notificaciones push)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Background tasks (para sincronización)
    implementation(libs.work.runtime.ktx)

    // Streaming
    implementation(libs.stream.webrtc)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.ui)
    implementation(libs.streampack.core)
    implementation(libs.streampack.rtmp)

    // Authentication (Google Sign-In)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.google.identity.googleid)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}