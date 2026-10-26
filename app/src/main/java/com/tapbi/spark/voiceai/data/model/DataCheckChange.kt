package com.tapbi.spark.voiceai.data.model

import com.squareup.moshi.Json

data class DataCheckChange(
    @param:Json(name = "version")
    val version: Int,
    @param:Json(name = "categories")
    val categories: String
)