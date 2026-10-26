package com.tapbi.spark.ui_common_sdk.foundation

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tapbi.spark.ui_common_sdk.R

@Immutable
class TypographyApp internal constructor(
    val normal: TextStyle,
    val medium: TextStyle,
    val semiBold: TextStyle,
    val bold: TextStyle,
) {
    constructor (
        defaultFontFamily: FontFamily = DEFAULT_FONT_FAMILY,
        normalText: TextStyle =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 26.sp,
            ),
        mediumText: TextStyle =
            TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 26.sp,
            ),
        semiBoldText: TextStyle =
            TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 26.sp,
            ),
        boldText: TextStyle =
            TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
            ),

        ) : this(
        normal = normalText.withDefaultFontFamily(defaultFontFamily),
        medium = mediumText.withDefaultFontFamily(defaultFontFamily),
        semiBold = semiBoldText.withDefaultFontFamily(defaultFontFamily),
        bold = boldText.withDefaultFontFamily(defaultFontFamily),
    )

    val materialTypography = Typography()

    companion object {
        // Add more fonts to the family when it needs
        private val DEFAULT_FONT_FAMILY =
            FontFamily(
                Font(R.font.sf_pro_display_regular, FontWeight.Normal),
                Font(R.font.sf_pro_display_medium, FontWeight.Medium),
                Font(R.font.sf_pro_display_semi_bold, FontWeight.SemiBold),
                Font(R.font.sf_pro_display_bold, FontWeight.Bold),
            )
    }
}

private fun TextStyle.withDefaultFontFamily(default: FontFamily): TextStyle {
    return if (fontFamily != null) this else copy(fontFamily = default)
}
