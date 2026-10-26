package com.tapbi.spark.voiceai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tapbi.spark.voiceai.common.Constant

@JsonClass(generateAdapter = true)
@Entity(tableName = Constant.TEMPLATE_TABLE)
data class PagesTemplate(
    @PrimaryKey
    @param:Json(name = "id")
    var id: Long = 0,

    @param:Json(name = "version")
    var version: Int = 0,

    @param:Json(name = "category")
    var category: Long = 0,

    @param:Json(name = "thumbs")
    var thumbs: List<String> = arrayListOf()
)