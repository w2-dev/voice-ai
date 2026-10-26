package com.tapbi.spark.voiceai.extension

import java.util.Locale

fun String.capitalizeWords(
    locale: Locale = Locale.ENGLISH
): String {
    if (isBlank()) return this

    return split(Regex("\\s+"))
        .joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase(locale) else c.toString()
            }
        }
}