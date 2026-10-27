package com.tapbi.spark.voiceai.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PrefKeys {
    val CURRENT_LANGUAGE = stringPreferencesKey("CURRENT_LANGUAGE")
    val FIREBASE_REMOTE_VERSION_UPDATE_THEME = longPreferencesKey("FIREBASE_REMOTE_VERSION_UPDATE_THEME")

    val IS_LOCK_SCREEN_ACTIVE = booleanPreferencesKey("IS_LOCK_SCREEN_ACTIVE")
    val IS_VOICE_LOCK_ENABLED = booleanPreferencesKey("IS_VOICE_LOCK_ENABLED")
    val VOICE_PASSPHRASE = stringPreferencesKey("VOICE_PASSPHRASE")
    val SELECTED_SPEECH_LANGUAGE = stringPreferencesKey("SELECTED_SPEECH_LANGUAGE")
    val IS_PIN_LOCK_ENABLED = booleanPreferencesKey("IS_PIN_LOCK_ENABLED")
    val PIN_PASSWORD = stringPreferencesKey("PIN_PASSWORD")
    val IS_PATTERN_LOCK_ENABLED = booleanPreferencesKey("IS_PATTERN_LOCK_ENABLED")
    val PATTERN_LOCK_DATA = stringPreferencesKey("PATTERN_LOCK_DATA")
    val PATTERN_GRID_ROWS = intPreferencesKey("PATTERN_GRID_ROWS")
    val PATTERN_GRID_COLS = intPreferencesKey("PATTERN_GRID_COLS")
    val IS_FINGERPRINT_ENABLED = booleanPreferencesKey("IS_FINGERPRINT_ENABLED")
    val SECURITY_QUESTION_INDEX = intPreferencesKey("SECURITY_QUESTION_INDEX")
    val SECURITY_ANSWER = stringPreferencesKey("SECURITY_ANSWER")
}
