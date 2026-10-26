package com.tapbi.spark.voiceai.utils

import android.content.Context
import com.tapbi.spark.voiceai.R
import com.tapbi.spark.voiceai.common.Constant
import com.tapbi.spark.voiceai.common.models.Language

object LocaleUtils {
    fun getLanguages(context: Context): List<Language> {
        val order = listOf(
            Language(
                R.drawable.national_vietnamese,
                Constant.VIETNAM_LANGUAGE_CODE,
                context.getString(R.string.vn)
            ),
            Language(
                R.drawable.national_korean,
                Constant.KOREA_LANGUAGE_CODE,
                context.getString(R.string.korean)
            ),
            Language(
                R.drawable.national_indoesian,
                Constant.INDONESIA_LANGUAGE_CODE,
                context.getString(R.string.indonesian)
            ),
            Language(
                R.drawable.national_russian,
                Constant.RUSSIAN_LANGUAGE_CODE,
                context.getString(R.string.russian)
            ),
            Language(
                R.drawable.national_german,
                Constant.GERMAN_LANGUAGE_CODE,
                context.getString(R.string.german)
            ),
            Language(
                R.drawable.national_italia,
                Constant.ITALIA_LANGUAGE_CODE,
                context.getString(R.string.italian)
            ),
            Language(
                R.drawable.national_english,
                Constant.LANGUAGE_EN,
                context.getString(R.string.english)
            ),
            Language(
                R.drawable.national_spanish,
                Constant.SPANISH_LANGUAGE_CODE,
                context.getString(R.string.spanish)
            ),
            Language(
                R.drawable.national_portuguese,
                Constant.PORTUGAL_LANGUAGE_CODE,
                context.getString(R.string.portuguese)
            ),
            Language(
                R.drawable.national_french,
                Constant.FRENCH_LANGUAGE_CODE,
                context.getString(R.string.french)
            ),
            Language(
                R.drawable.national_hindi,
                Constant.HINDI_LANGUAGE_CODE,
                context.getString(R.string.indian_hindi)
            ),
            Language(
                R.drawable.national_japanese,
                Constant.JAPAN_LANGUAGE_CODE,
                context.getString(R.string.japanese)
            )
        )
        return order
    }
}