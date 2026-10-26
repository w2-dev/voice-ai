package com.tapbi.spark.voiceai.data.remote

import com.tapbi.spark.voiceai.common.Constant
import com.tapbi.spark.voiceai.data.model.ListPageTemplate
import com.tapbi.spark.voiceai.data.model.PageTemplateUpdate
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ThemeApi {

    @POST(Constant.PARAM_UPDATE_THEME)
    suspend fun getUpdateTheme(
        @Header(Constant.HEADER_X_API_KEY) xApiKey: String,
        @Body body: Map<String, Int>
    ): PageTemplateUpdate

    @POST(Constant.PARAM_THEME)
    suspend fun getThemes(
        @Header(Constant.HEADER_X_API_KEY) xApiKey: String
    ): ListPageTemplate
}