package com.tapbi.spark.ui_common_sdk.components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme

enum class ButtonAppStyle {
    OK,       // Primary Action (Blue/Brand theme)
    CANCEL,   // Cancel / Secondary Action (White-Grey-Black tone)
    DANGER    // Danger / Delete Action (Red tone)
}

@Composable
fun ButtonApp(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonAppStyle = ButtonAppStyle.OK,
    enabled: Boolean = true,
    height: Dp = 50.dp,
    shape: Shape = RoundedCornerShape(25.dp)
) {
    val colors = AppTheme.color

    when (style) {
        ButtonAppStyle.OK -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier.height(height),
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.colorButtonOkBg,
                    contentColor = colors.colorButtonOkText,
                    disabledContainerColor = colors.colorButtonDisabledBg,
                    disabledContentColor = colors.colorButtonDisabledText
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        ButtonAppStyle.CANCEL -> {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier.height(height),
                shape = shape,
                border = BorderStroke(
                    1.dp,
                    if (enabled) colors.colorButtonCancelBorder else colors.colorButtonDisabledText.copy(alpha = 0.5f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.colorButtonCancelBg,
                    contentColor = colors.colorButtonCancelText,
                    disabledContainerColor = colors.colorButtonDisabledBg,
                    disabledContentColor = colors.colorButtonDisabledText
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) colors.colorButtonCancelText else colors.colorButtonDisabledText
                )
            }
        }

        ButtonAppStyle.DANGER -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier.height(height),
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.colorButtonDangerBg,
                    contentColor = colors.colorWhite,
                    disabledContainerColor = colors.colorButtonDisabledBg,
                    disabledContentColor = colors.colorButtonDisabledText
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
