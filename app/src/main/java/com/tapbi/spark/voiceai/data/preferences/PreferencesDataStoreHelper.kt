package com.tapbi.spark.voiceai.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PreferencesDataStoreHelper @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        const val DEFAULT_STRING = ""

        const val DEFAULT_INT = 0
        const val DEFAULT_LONG = 0L
        const val DEFAULT_FLOAT = 0f
        const val DEFAULT_DOUBLE = 0.0
    }

    suspend fun putInt(key: Preferences.Key<Int>, value: Int) {
        dataStore.edit { pref ->
            pref[key] = value
        }
    }

    suspend fun getInt(key: Preferences.Key<Int>, default: Int? = null): Int {
        val preferences = dataStore.data.first()
        return preferences[key] ?: (default ?: DEFAULT_INT)
    }

    fun getFlowInt(
        key: Preferences.Key<Int>,
        default: Int? = null,
        isDistinctUntilChanged: Boolean = true
    ): Flow<Int> {
        val flow = dataStore.data
            .map { it[key] ?: (default ?: DEFAULT_INT) }

        return if (isDistinctUntilChanged) {
            flow.distinctUntilChanged()
        } else {
            flow
        }
    }

    suspend fun putString(key: Preferences.Key<String>, value: String) {
        dataStore.edit { pref ->
            pref[key] = value
        }
    }

    suspend fun getString(key: Preferences.Key<String>, default: String? = null): String {
        val preferences = dataStore.data.first()
        return preferences[key] ?: (default ?: DEFAULT_STRING)
    }

    fun getFlowString(
        key: Preferences.Key<String>, default: String? = null,
        isDistinctUntilChanged: Boolean = true
    ): Flow<String> {
        val flow =
            dataStore.data.map { it[key] ?: (default ?: DEFAULT_STRING) }
        return if (isDistinctUntilChanged) {
            flow.distinctUntilChanged()
        } else {
            flow
        }
    }

    suspend fun putBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        dataStore.edit { pref ->
            pref[key] = value
        }
    }

    suspend fun getBoolean(key: Preferences.Key<Boolean>, default: Boolean): Boolean {
        val preferences = dataStore.data.first()
        return preferences[key] ?: default
    }

    fun getFlowBoolean(
        key: Preferences.Key<Boolean>, default: Boolean,
        isDistinctUntilChanged: Boolean = true
    ): Flow<Boolean> {
        val flow =
            dataStore.data.map { it[key] ?: default }
        return if (isDistinctUntilChanged) {
            flow.distinctUntilChanged()
        } else {
            flow
        }
    }

    suspend fun putLong(key: Preferences.Key<Long>, value: Long) {
        dataStore.edit { pref ->
            pref[key] = value
        }
    }

    suspend fun getLong(key: Preferences.Key<Long>, default: Long? = null): Long {
        val preferences = dataStore.data.first()
        return preferences[key] ?: (default ?: DEFAULT_LONG)
    }

    fun getFlowLong(
        key: Preferences.Key<Long>, default: Long? = null,
        isDistinctUntilChanged: Boolean = true
    ): Flow<Long> {
        val flow =
            dataStore.data.map { it[key] ?: (default ?: DEFAULT_LONG) }
        return if (isDistinctUntilChanged) {
            flow.distinctUntilChanged()
        } else {
            flow
        }
    }

    suspend fun putFloat(key: Preferences.Key<Float>, value: Float) {
        dataStore.edit { pref ->
            pref[key] = value
        }
    }

    suspend fun getFloat(key: Preferences.Key<Float>, default: Float? = null): Float {
        val preferences = dataStore.data.first()
        return preferences[key] ?: (default ?: DEFAULT_FLOAT)
    }

    fun getFlowFloat(
        key: Preferences.Key<Float>, default: Float? = null,
        isDistinctUntilChanged: Boolean = true
    ): Flow<Float> {
        val flow =
            dataStore.data.map { it[key] ?: (default ?: DEFAULT_FLOAT) }
        return if (isDistinctUntilChanged) {
            flow.distinctUntilChanged()
        } else {
            flow
        }
    }

    suspend fun putDouble(key: Preferences.Key<Double>, value: Double) {
        dataStore.edit { pref ->
            pref[key] = value
        }
    }

    suspend fun getDouble(key: Preferences.Key<Double>, default: Double? = null): Double {
        val preferences = dataStore.data.first()
        return preferences[key] ?: (default ?: DEFAULT_DOUBLE)
    }

    fun getFlowDouble(
        key: Preferences.Key<Double>, default: Double? = null,
        isDistinctUntilChanged: Boolean = true
    ): Flow<Double> {
        val flow =
            dataStore.data.map { it[key] ?: (default ?: DEFAULT_DOUBLE) }
        return if (isDistinctUntilChanged) {
            flow.distinctUntilChanged()
        } else {
            flow
        }
    }

    suspend fun <T> removeKey(key: Preferences.Key<T>) {
        dataStore.edit { prefs ->
            prefs.remove(key)
        }
    }

    suspend fun resetAll() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun <T> containKey(key: Preferences.Key<T>): Boolean {
        val prefs = dataStore.data.first()
        return prefs.contains(key)
    }

}