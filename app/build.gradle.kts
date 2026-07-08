plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "co.coffeery.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "co.coffeery.app"
        // minSdk 26 (Android 8.0): modern-enough baseline covering >95% of active
        // devices while giving us full vector drawables, adaptive icons and
        // java.time without desugaring.
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "2.1.0"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
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
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")

    // SplashScreen API — branded launch screen showing the Coffeery icon
    // and background colour during cold start. Works API 26+ (minSdk 26).
    implementation("androidx.core:core-splashscreen:1.0.1")

    // AppCompat — needed for runtime theme/language switching via
    // AppCompatDelegate.setApplicationLocales.
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Compose — deliberately NO material3 / material. We build our own design
    // system on top of foundation + ui to keep a unique visual identity.
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")

    // Lifecycle + ViewModel (StateFlow driven MVVM)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // Room (local-first persistence)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // NOTE: the bundled research-data asset is parsed with Android's built-in
    // org.json (no extra dependency / compiler plugin required).

    debugImplementation("androidx.compose.ui:ui-tooling")
}
