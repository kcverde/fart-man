plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
}

android {
  namespace = "com.kcverde.fartman"
  compileSdk { version = release(37) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.kcverde.fartman"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  buildFeatures { compose = true }

  lint {
    // A warning nobody has to act on is a warning nobody reads, so every
    // finding either gets fixed or gets an entry in the baseline with a reason.
    warningsAsErrors = true
    abortOnError = true

    // Dependency freshness is Renovate's job. These three resolve the latest
    // version over the network, so leaving them on would turn CI red the day a
    // new Kotlin ships, without a commit having touched anything.
    disable += setOf("AndroidGradlePluginVersion", "GradleDependency", "NewerVersionAvailable")

    baseline = file("lint-baseline.xml")

    // The HTML report is the one worth reading; CI uploads it on failure.
    htmlReport = true
    sarifReport = true
  }

  // Robolectric needs real resources to inflate the app's theme and drawables.
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.lifecycle.viewmodel.savedstate)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)

  // Tests all run on the JVM: plain JUnit for game logic, Robolectric +
  // Roborazzi for the screens. There is no androidTest source set, because
  // nothing here needs a real device.
  testImplementation(platform(libs.androidx.compose.bom))
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)

  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)

  ksp(libs.androidx.room.compiler)
}
