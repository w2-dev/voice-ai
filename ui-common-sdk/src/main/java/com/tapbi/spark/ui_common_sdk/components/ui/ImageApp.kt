package com.tapbi.spark.ui_common_sdk.components.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.tapbi.spark.ui_common_sdk.extension.shimmerPlaceholder
import com.tapbi.spark.ui_common_sdk.foundation.ColorDefinition

@Composable
fun ImageData(
    modifier: Modifier = Modifier,
    data: Any,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes defaultImage: Int? = null,
    contentDescription: String = "",
) {
    val painter =
        rememberAsyncImagePainter(
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(data)
                    .size(Size.ORIGINAL)
                    .build(),
        )
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when (
            painter.state
        ) {
            is AsyncImagePainter.State.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .shimmerPlaceholder(),
                )
            }

            is AsyncImagePainter.State.Error,
            is AsyncImagePainter.State.Empty,
                ->
                defaultImage?.let {
                    Image(
                        modifier =
                            Modifier
                                .fillMaxSize(),
                        painter = painterResource(id = defaultImage),
                        contentScale = contentScale,
                        contentDescription = contentDescription,
                    )
                } ?: Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(ColorDefinition.White),
                )

            else -> {
                Image(
                    modifier =
                        Modifier
                            .fillMaxSize(),
                    painter = painter,
                    contentScale = contentScale,
                    contentDescription = contentDescription,
                )
            }
        }
    }
}
