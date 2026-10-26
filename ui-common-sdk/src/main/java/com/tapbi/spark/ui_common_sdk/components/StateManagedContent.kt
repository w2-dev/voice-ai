package com.tapbi.spark.ui_common_sdk.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.AnimationConstants.DefaultDurationMillis
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

sealed class ContentState {
    object LOADING : ContentState()

    object LOADED : ContentState()

    object ERROR : ContentState()

    object EMPTY : ContentState()

    val isLoaded get() = this == LOADED
    val isLoading get() = this == LOADING
    val isEmpty get() = this == EMPTY
    val isError get() = this == ERROR
}

@Composable
fun StateManagedContent(
    modifier: Modifier = Modifier,
    contentState: ContentState,
    animDurationMillis: Int = DefaultDurationMillis,
    loaded: @Composable () -> Unit,
    loading: @Composable () -> Unit = {},
    error: @Composable () -> Unit,
    empty: @Composable () -> Unit = {},
) {
    Crossfade(
        contentState,
        modifier,
        animationSpec = tween(durationMillis = animDurationMillis),
    ) {
        when (it) {
            ContentState.LOADED -> loaded()
            ContentState.LOADING -> loading()
            ContentState.ERROR -> error()
            ContentState.EMPTY -> empty()
        }
    }
}

@Composable
fun StateManagedContent(
    modifier: Modifier = Modifier,
    hasData: Boolean = true,
    animDurationMillis: Int = DefaultDurationMillis,
    content: @Composable () -> Unit,
    noDataContent: @Composable () -> Unit,
) {
    Crossfade(
        hasData,
        modifier,
        animationSpec = tween(durationMillis = animDurationMillis),
    ) {
        if (it) {
            content()
        } else {
            noDataContent()
        }
    }
}