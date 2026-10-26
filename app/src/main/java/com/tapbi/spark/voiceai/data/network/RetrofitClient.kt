package com.tapbi.spark.voiceai.data.network

import com.tapbi.spark.voiceai.data.remote.ThemeApi
import com.tapbi.spark.voiceai.utils.MoshiHelper
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    private fun getRetrofitInstance(url: String): Retrofit {
        return Retrofit.Builder().baseUrl(url)
            .addConverterFactory(MoshiConverterFactory.create(MoshiHelper.moshi)).build()
    }

    fun getThemeApi(url: String): ThemeApi {
        return getRetrofitInstance(url)
            .create(ThemeApi::class.java)
    }

}