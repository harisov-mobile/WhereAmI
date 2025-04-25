plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt.plugin)
}

android {
  namespace = "ru.internetcloud.whereami"
  compileSdk = 35

  defaultConfig {
    applicationId = "ru.internetcloud.whereami"
    minSdk = 25
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions { jvmTarget = "17" }

  buildFeatures {
    viewBinding = true
    buildConfig = true
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.fragment.ktx)

//  testImplementation(libs.junit)
//  androidTestImplementation(libs.androidx.junit)
//  androidTestImplementation(libs.androidx.espresso.core)

  // reflection-free flavor
  implementation(libs.com.github.kirich1409)

  // Hilt
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)

  // Osmdroid
  implementation(libs.org.osmdroid.osmdroid.android)
  implementation(libs.com.github.mkergall.osmbonuspack)
}
