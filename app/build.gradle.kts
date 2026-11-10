import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("dagger.hilt.android.plugin")
}

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.restrusher.partypuzl"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.restrusher.partypuzl"
        minSdk = 24
        targetSdk = 36
        versionCode = 13
        versionName = "1.0"

        buildConfigField(
            "String",
            "PACK_BANK_URL",
            "\"https://logang-bot.github.io/partypuzl-packs/v1/packs.json\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("shared") {
            if (!keystoreProperties.isEmpty) {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    val envSigningConfig = signingConfigs.getByName(
        if (keystoreProperties.isEmpty) "debug" else "shared"
    )

    buildTypes {
        debug {
            signingConfig = envSigningConfig
        }
        create("staging") {
            initWith(getByName("release"))
            signingConfig = envSigningConfig
        }
        release {
            isMinifyEnabled = false
            signingConfig = envSigningConfig
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Animation
    implementation(libs.androidx.compose.animation)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // Coil
    implementation(libs.coil.compose)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.org.json)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.storage)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestUtil(libs.androidx.test.services)
    androidTestUtil(libs.androidx.test.orchestrator)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.core.splashscreen)


    // Play Billing
    implementation(libs.billing)
}

// ScreenshotTour writes "<locale>__<theme>__NN_name.png" through TestStorage; Gradle pulls those to
// connected_android_test_additional_output before uninstalling. This sorts them into
// store-screenshots/<locale>/<theme>/ at the repo root. See docs/store-screenshots.md.
val collectStoreScreenshots by tasks.registering(Copy::class) {
    from(layout.buildDirectory.dir("outputs/connected_android_test_additional_output")) {
        include("**/*__*__*.png")
    }
    into(rootProject.layout.projectDirectory.dir("store-screenshots"))
    eachFile { path = name.split("__").joinToString("/") }
    includeEmptyDirs = false
}

tasks.matching { it.name == "connectedDebugAndroidTest" }.configureEach {
    finalizedBy(collectStoreScreenshots)
}
