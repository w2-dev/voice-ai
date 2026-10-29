package com.tapbi.spark.voiceai.utils

import timber.log.Timber

object NativeLibLoader {
    private var isLoaded = false
    private var loadError: Throwable? = null

    @Synchronized
    fun ensureLoaded(): Boolean {
        if (isLoaded) return true
        if (loadError != null) return false

        return try {
            System.loadLibrary("sherpa-onnx-jni")
            isLoaded = true
            Timber.d("libsherpa-onnx-jni.so loaded successfully")
            true
        } catch (t: Throwable) {
            loadError = t
            Timber.e(t, "Failed to load libsherpa-onnx-jni.so")
            false
        }
    }
}
