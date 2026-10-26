package com.tapbi.spark.voiceai.data.model

import com.tapbi.spark.ui_common_sdk.components.ContentState


data class ResultListPageTemplate(var status: ContentState, var list: List<PagesTemplate>)