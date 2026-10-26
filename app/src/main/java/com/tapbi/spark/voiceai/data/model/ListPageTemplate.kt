package com.tapbi.spark.voiceai.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ListPageTemplate(
    @param:Json(name = "Items")
    val list: List<PagesTemplate>
)