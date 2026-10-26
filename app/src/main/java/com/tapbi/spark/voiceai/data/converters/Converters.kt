package com.tapbi.spark.voiceai.data.converters

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class Converters {

    private val moshi: Moshi by lazy { Moshi.Builder().build() }

    // ===== Generic List Adapter =====
    private inline fun <reified T> listAdapter(): JsonAdapter<List<T>> {
        val type = Types.newParameterizedType(List::class.java, T::class.java)
        return moshi.adapter(type)
    }

    // ===== String =====
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return listAdapter<String>().toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrEmpty()) return emptyList()
        return listAdapter<String>().fromJson(json) ?: emptyList()
    }
}