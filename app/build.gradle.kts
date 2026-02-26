plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.myproject"
    compileSdk = 36 // پیشنهاد: فعلاً روی 34 یا 35 پایدار بمانید (36 هنوز خیلی جدید است)

    // مطمئن شوید این نسخه NDK در SDK Manager نصب شده باشد
    ndkVersion = "29.0.13599879"

    defaultConfig {
        applicationId = "com.example.myproject"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- بخش حیاتی برای حل مشکل شما ---
        externalNativeBuild {
            cmake {
                cppFlags("")
                // این خط به CMake می‌گوید برای چه پردازنده‌هایی کد نیتیو بسازد
                abiFilters.addAll(listOf( "arm64-v8a"))
            }
        }

        ndk {
            // این خط برای محدود کردن خروجی نهایی APK/Bundle به معماری‌های مورد نیاز است
            abiFilters.addAll(listOf( "arm64-v8a"))
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "4.1.2" // نسخه استاندارد CMake را چک کنید
        }
    }

    buildFeatures {
        compose = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Compose dependencies
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.material:material-icons-extended")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(composeBom)
}