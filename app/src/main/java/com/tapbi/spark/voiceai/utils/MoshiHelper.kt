package com.tapbi.spark.voiceai.utils

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiHelper {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()!!
}