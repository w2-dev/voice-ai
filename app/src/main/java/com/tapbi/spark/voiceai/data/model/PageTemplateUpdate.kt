package com.tapbi.spark.voiceai.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class PageTemplateUpdate(
    @param:Json(name = "check_change")
    val dataCheckChange: DataCheckChange,
    @param:Json(name = "updated_change")
    val list: List<PagesTemplate>
)