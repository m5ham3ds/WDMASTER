// ════════════════════════════════════════════════════════════════
// WiFi Card Master Pro – App-module Gradle (Kotlin DSL)
// ════════════════════════════════════════════════════════════════

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.wdmaster.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wdmaster.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// ════════════════════════════════════════════════════════════
// Dependencies versions
// ════════════════════════════════════════════════════════════
val nav_version = "2.7.6"
val lifecycle_version = "2.7.0"
val room_version = "2.6.1"
val work_version = "2.9.0"
val koin_version = "3.5.6"

dependencies {

    // ── AndroidX Core ──────────────────────────────
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // ── Material Design ────────────────────────────
    implementation("com.google.android.material:material:1.11.0")

    // ── Layouts ────────────────────────────────────
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    // ── Navigation Component ───────────────────────
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // ── Lifecycle Components ───────────────────────
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-common:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycle_version")

    // ── Coroutines ─────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // ── Room Database ──────────────────────────────
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // ── DataStore ──────────────────────────────────
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // ── WorkManager ────────────────────────────────
    implementation("androidx.work:work-runtime-ktx:$work_version")

    // ── Preferences ────────────────────────────────
    implementation("androidx.preference:preference-ktx:1.2.1")

    // ── WebView & Network ──────────────────────────
    implementation("androidx.webkit:webkit:1.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ── JSON Parsing ───────────────────────────────
    implementation("com.google.code.gson:gson:2.10.1")

    // ── Security & Encryption ──────────────────────
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ── Logging ────────────────────────────────────
    implementation("com.jakewharton.timber:timber:5.0.1")

    // ── Utilities ──────────────────────────────────
    implementation("org.greenrobot:eventbus:3.3.1")
    implementation("com.airbnb.android:lottie:6.3.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:compiler:4.16.0")

    // ── Apache Commons ─────────────────────────────
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("commons-codec:commons-codec:1.16.0")

    // ── Koin Dependency Injection (الآمن فقط) ──────
    implementation("io.insert-koin:koin-android:$koin_version")
    implementation("io.insert-koin:koin-core:$koin_version")
    // ⛔ محظور: koin-androidx-viewmodel

    // ── Testing ────────────────────────────────────
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("androidx.room:room-testing:$room_version")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("org.mockito:mockito-android:5.8.0")

    // ── Debug (LeakCanary) ─────────────────────────
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.13")
}
