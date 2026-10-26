package com.tapbi.spark.voiceai.manager

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsManager private constructor() {
    fun logEvent(s: String, bundle: Bundle?) {
        firebaseAnalytics!!.logEvent(s, bundle)
    }

    companion object {
        private var instance: AnalyticsManager? = null
        private var firebaseAnalytics: FirebaseAnalytics? = null

        fun init(context: Context) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
            instance = AnalyticsManager()
        }

        fun getInstance(): AnalyticsManager {
            if (instance == null) {
                instance = AnalyticsManager()
            }
            return instance!!
        }
    }
}