package com.tapbi.spark.voiceai.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PrefKeys {
    // File Name SharedPreferences dành cho Service
    const val PREF_LOCK_SERVICE_NAME = "LOCK_SERVICE_PREFS"

    // String Keys dùng chung cho cả DataStore và SharedPreferences (ServicePreferences)
    const val KEY_CURRENT_LANGUAGE = "CURRENT_LANGUAGE"
    const val KEY_FIREBASE_REMOTE_VERSION_UPDATE_THEME = "FIREBASE_REMOTE_VERSION_UPDATE_THEME"
    const val KEY_IS_FIRST_LAUNCH = "IS_FIRST_LAUNCH"
    const val KEY_IS_SPLASH_COMPLETED = "IS_SPLASH_COMPLETED"

    const val KEY_IS_LOCK_SCREEN_ACTIVE = "IS_LOCK_SCREEN_ACTIVE"
    const val KEY_IS_VOICE_LOCK_ENABLED = "IS_VOICE_LOCK_ENABLED"
    const val KEY_VOICE_PASSPHRASE = "VOICE_PASSPHRASE"
    const val KEY_SELECTED_SPEECH_LANGUAGE = "SELECTED_SPEECH_LANGUAGE"
    const val KEY_IS_PIN_LOCK_ENABLED = "IS_PIN_LOCK_ENABLED"
    const val KEY_PIN_PASSWORD = "PIN_PASSWORD"
    const val KEY_IS_PATTERN_LOCK_ENABLED = "IS_PATTERN_LOCK_ENABLED"
    const val KEY_PATTERN_LOCK_DATA = "PATTERN_LOCK_DATA"
    const val KEY_PATTERN_GRID_ROWS = "PATTERN_GRID_ROWS"
    const val KEY_PATTERN_GRID_COLS = "PATTERN_GRID_COLS"
    const val KEY_IS_FINGERPRINT_ENABLED = "IS_FINGERPRINT_ENABLED"
    const val KEY_SECURITY_QUESTION_INDEX = "SECURITY_QUESTION_INDEX"
    const val KEY_SECURITY_ANSWER = "SECURITY_ANSWER"

    // DataStore Preferences Keys
    val CURRENT_LANGUAGE = stringPreferencesKey(KEY_CURRENT_LANGUAGE)
    val FIREBASE_REMOTE_VERSION_UPDATE_THEME =
        longPreferencesKey(KEY_FIREBASE_REMOTE_VERSION_UPDATE_THEME)

    val IS_LOCK_SCREEN_ACTIVE = booleanPreferencesKey(KEY_IS_LOCK_SCREEN_ACTIVE)
    val IS_VOICE_LOCK_ENABLED = booleanPreferencesKey(KEY_IS_VOICE_LOCK_ENABLED)
    val VOICE_PASSPHRASE = stringPreferencesKey(KEY_VOICE_PASSPHRASE)
    val SELECTED_SPEECH_LANGUAGE = stringPreferencesKey(KEY_SELECTED_SPEECH_LANGUAGE)
    val IS_PIN_LOCK_ENABLED = booleanPreferencesKey(KEY_IS_PIN_LOCK_ENABLED)
    val PIN_PASSWORD = stringPreferencesKey(KEY_PIN_PASSWORD)
    val IS_PATTERN_LOCK_ENABLED = booleanPreferencesKey(KEY_IS_PATTERN_LOCK_ENABLED)
    val PATTERN_LOCK_DATA = stringPreferencesKey(KEY_PATTERN_LOCK_DATA)
    val PATTERN_GRID_ROWS = intPreferencesKey(KEY_PATTERN_GRID_ROWS)
    val PATTERN_GRID_COLS = intPreferencesKey(KEY_PATTERN_GRID_COLS)
    val IS_FINGERPRINT_ENABLED = booleanPreferencesKey(KEY_IS_FINGERPRINT_ENABLED)
    val SECURITY_QUESTION_INDEX = intPreferencesKey(KEY_SECURITY_QUESTION_INDEX)
    val SECURITY_ANSWER = stringPreferencesKey(KEY_SECURITY_ANSWER)
}
