import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.aistudio.nexfyremesas.app"
  compileSdk = 36
  buildToolsVersion = "36.0.0"

  defaultConfig {
    applicationId = "com.aistudio.nexfyremesas.app"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    multiDexEnabled = true

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    ndk {
      abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
    }
  }

  // ── NDK para librerías nativas de OsmAnd ──
  ndkVersion = "27.0.12077973"

  // This is from OsmAndCore_android.aar - for some reason it's not inherited
  androidResources {
    noCompress += listOf("qz", "png")
  }

  packaging {
    jniLibs {
      pickFirsts += listOf(
        "lib/armeabi-v7a/libc++_shared.so",
        "lib/arm64-v8a/libc++_shared.so",
        "lib/x86_64/libc++_shared.so",
        "lib/x86/libc++_shared.so"
      )
    }
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      val debugStore = file("${rootDir}/debug.keystore")
      if (debugStore.exists()) {
        storeFile = debugStore
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    isCoreLibraryDesugaringEnabled = true
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  // Kotlin 2.0+ tiene el Compose Compiler integrado — NO se necesita composeOptions

  lint {
    abortOnError = false
    warningsAsErrors = false
  }

  testOptions { unitTests { isIncludeAndroidResources = true } }

  flavorDimensions += listOf("coreversion", "abi")
  productFlavors {
    create("armonly") {
      dimension = "abi"
      ndk {
        abiFilters += listOf("arm64-v8a", "armeabi-v7a")
      }
    }
    create("fat") {
      dimension = "abi"
      ndk {
        abiFilters += listOf("arm64-v8a", "x86", "x86_64", "armeabi-v7a")
      }
    }
    create("legacy") {
      dimension = "coreversion"
    }
    create("opengl") {
      dimension = "coreversion"
    }
  }
}

secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.auth)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.retrofit)

  // ── OsmAnd Full Library SDK (precompiled from Ivy) ──
  implementation("net.osmand:OsmAnd-java:master-snapshot:android@jar")
  add("debugImplementation", "net.osmand:OsmAnd:master-snapshot:debug@aar")
  add("releaseImplementation", "net.osmand:OsmAnd:master-snapshot:release@aar")
  add("debugImplementation", "net.osmand.shared:OsmAnd-shared-android:master-snapshot:debug@aar")
  add("releaseImplementation", "net.osmand.shared:OsmAnd-shared-android:master-snapshot:release@aar")

  implementation("net.osmand:OsmAndCore_androidNativeRelease:master-snapshot@aar")
  implementation("net.osmand:OsmAndCore_android:master-snapshot@aar")

  // ── OsmAnd required dependencies ──
  implementation("androidx.multidex:multidex:2.0.1")
  implementation("androidx.gridlayout:gridlayout:1.1.0")
  implementation("androidx.cardview:cardview:1.0.0")
  implementation("androidx.appcompat:appcompat:1.7.1")
  implementation("com.google.android.material:material:1.12.0")
  implementation("androidx.browser:browser:1.8.0")
  implementation("androidx.preference:preference:1.2.1")
  implementation("androidx.lifecycle:lifecycle-process:2.8.7")
  implementation("androidx.activity:activity:1.9.3")

  implementation("commons-logging:commons-logging:1.2")
  implementation("commons-codec:commons-codec:1.17.1")
  implementation("org.apache.commons:commons-compress:1.27.1")
  implementation("com.moparisthebest:junidecode:0.1.1")
  implementation("org.immutables:gson:2.10.1")
  implementation("com.vividsolutions:jts-core:1.14.0")
  implementation("com.google.openlocationcode:openlocationcode:1.0.4")
  implementation("com.android.billingclient:billing:7.1.1")

  implementation("com.squareup.picasso:picasso:2.71828")
  implementation("me.zhanghai.android.materialprogressbar:library:1.4.2")
  implementation("org.mozilla:rhino:1.7.15")

  implementation("com.getkeepsafe.taptargetview:taptargetview:1.15.0") {
    exclude(group = "com.android.support")
  }
  add("debugImplementation", "net.osmand:MPAndroidChart:custom-snapshot-debug@aar")
  add("releaseImplementation", "net.osmand:MPAndroidChart:custom-snapshot-release@aar")
  implementation("com.github.HITGIF:TextFieldBoxes:1.4.5") {
    exclude(group = "com.android.support")
  }
  implementation("com.github.scribejava:scribejava-apis:7.1.1") {
    exclude(group = "com.fasterxml.jackson.core")
  }
  implementation("com.jaredrummler:colorpicker:1.1.0")
  implementation("com.google.android.gms:play-services-location:21.3.0")
  implementation("net.osmand:antpluginlib:3.8.0@aar")
  implementation("com.google.android.play:review:2.0.2")

  implementation("androidx.core:core:1.13.1")
  implementation("androidx.car.app:app:1.4.0")
  implementation("androidx.car.app:app-projected:1.4.0")

  implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
  implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.21")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-common:2.0.21")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
  implementation("com.squareup.okio:okio:3.9.1")
  implementation("co.touchlab:stately-concurrent-collections:2.1.0")

  implementation("androidx.sqlite:sqlite:2.4.0")
  implementation("androidx.sqlite:sqlite-framework:2.4.0")
  implementation("net.sf.kxml:kxml2:2.3.0")
  implementation("com.facebook.shimmer:shimmer:0.5.0@aar")

  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")

  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
