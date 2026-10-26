package com.tapbi.spark.voiceai.utils

import timber.log.Timber

class MyDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String {
        return "${element.fileName}:${element.lineNumber}"
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val element = Throwable().stackTrace[5] // vị trí tương đối
        val method = element.methodName
        super.log(priority, tag, "[$method] $message", t)
    }
}