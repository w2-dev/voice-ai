package com.tapbi.spark.ui_common_sdk.extension

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.createBitmap

fun Painter.toBitmap(
    width: Int,
    height: Int
): Bitmap {

    val bitmap = createBitmap(width, height)

    val canvas = Canvas(bitmap)

    val composeCanvas = androidx.compose.ui.graphics.Canvas(canvas)

    val drawScope = CanvasDrawScope()

    drawScope.draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = composeCanvas,
        size = Size(width.toFloat(), height.toFloat())
    ) {
        draw(size)
    }

    return bitmap
}