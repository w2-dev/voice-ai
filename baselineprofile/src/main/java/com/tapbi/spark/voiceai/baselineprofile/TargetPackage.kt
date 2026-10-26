package com.tapbi.spark.voiceai.baselineprofile

import androidx.test.platform.app.InstrumentationRegistry

val TARGET_PACKAGE: String
    get() {
        val arguments = InstrumentationRegistry.getArguments()
        arguments.getString("targetPackage")?.takeIf { it.isNotBlank() }?.let { return it }

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val testPackage = instrumentation.context.packageName
        val targetPackage = instrumentation.targetContext.packageName
        return targetPackage.takeIf { it != testPackage }
            ?: error("Missing targetPackage instrumentation argument for Macrobenchmark target app.")
    }
