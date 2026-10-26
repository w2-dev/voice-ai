import org.jetbrains.kotlin.gradle.dsl.JvmTarget

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.baselineprofile)
}

evaluationDependsOn(":app")

val targetApplicationIds by lazy {
    val appAndroid = project(":app").extensions.getByType(ApplicationExtension::class.java)
    val defaultApplicationId = requireNotNull(appAndroid.defaultConfig.applicationId) {
        "The :app module must define defaultConfig.applicationId."
    }
    appAndroid.productFlavors.associate { flavor ->
        flavor.name to (flavor.applicationId ?: defaultApplicationId)
    }
}

android {
    namespace = "com.tapbi.spark.basejetpackcompose.baselineprofile"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"

    flavorDimensions += "version"
    productFlavors {
        create("dev") {
            dimension = "version"
            testInstrumentationRunnerArguments["targetPackage"] = targetApplicationIds.getValue(name)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.uiautomator)
}

tasks.withType<DeviceProviderInstrumentTestTask>().configureEach {
    doNotTrackState("Connected Android test reports are not reliably readable on this machine.")
}
