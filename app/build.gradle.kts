plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.google.service)
    alias(libs.plugins.google.crashlytics)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.tapbi.spark.voiceai"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.tapbi.spark.voiceai"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        flavorDimensions += "version"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    productFlavors {
        create("product") {
            dimension = "version"
        }
        create("dev") {
            dimension = "version"
            applicationId = "com.tapbi.spark.voiceai"
        }
        create("billing") {
            dimension = "version"
            applicationId = "vn.tapbi.testinappsubscription"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

baselineProfile {
    mergeIntoMain = true
    variants {
        create("dev") {
            from(project(":baselineprofile"))
        }
    }
}

tasks.matching { it.name == "copyBaselineProfileIntoSrc" }.configureEach {
    outputs.upToDateWhen { false }
    doFirst {
        val generatedProfilesDir =
            layout.projectDirectory.dir("src/main/generated/baselineProfiles").asFile
        generatedProfilesDir.mkdirs()
        generatedProfilesDir.listFiles()?.forEach { generatedFile ->
            generatedFile.deleteRecursively()
        }
    }
}

dependencies {
    implementation(fileTree("libs") {
        include("*.jar", "*.aar")
    })
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.material)
    implementation(libs.androidx.activity.compose)

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.profileinstaller)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.compose.runtime.livedata)

    // DI
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.startup.runtime)
    ksp(libs.hilt.compiler)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)

    /* Firebase */
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.config)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Image
    implementation(libs.coil.compose)
    implementation(libs.compose.materialPlaceholder)

    // Utils
    implementation(libs.timber)
    implementation(libs.datastore.preferences)
    implementation(libs.commons.compress)
    implementation(libs.androidx.biometric)

    implementation(libs.lottie.compose)

    // Page Indicator
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.compose.pager.indicator)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Ads
    implementation(libs.controladstrue)
    implementation(libs.play.services.ads)
    implementation(libs.applovin.sdk)

    /*Billing*/
    implementation(libs.billing)
    implementation(libs.threetenabp)
    implementation(libs.preference.ktx)
    implementation(libs.gson)

    // Model
    implementation(project(":ui-common-sdk"))
}
