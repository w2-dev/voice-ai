package com.tapbi.spark.ui_common_sdk.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
fun Lifecycle.observeAsState(): State<Lifecycle.Event> {
    val state = remember { mutableStateOf(Lifecycle.Event.ON_ANY) }
    DisposableEffect(this) {
        val observer =
            LifecycleEventObserver { _, event ->
                state.value = event
            }
        this@observeAsState.addObserver(observer)
        onDispose {
            this@observeAsState.removeObserver(observer)
        }
    }
    return state
}

/**
 * https://developer.android.com/jetpack/compose/side-effects#disposableeffect
 * */
@Composable
fun OnLifecycleOwnerChanged(
    onStart: () -> Unit = {},
    onPause: () -> Unit = {},
    onStop: () -> Unit = {},
    onDestroy: () -> Unit = {},
    onCreate: () -> Unit = {},
    onResume: () -> Unit = {},
) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    // Safely update the current lambdas when a new one is provided
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnStop by rememberUpdatedState(onStop)
    val currentOnPause by rememberUpdatedState(onPause)
    val currentOnDestroy by rememberUpdatedState(onDestroy)
    val currentOnCreate by rememberUpdatedState(onCreate)
    val currentOnResume by rememberUpdatedState(onResume)

    // If `lifecycleOwner` changes, dispose and reset the effect
    DisposableEffect(lifecycleOwner) {
        // Create an observer that triggers our remembered callbacks
        // for sending analytics events
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        currentOnStart()
                    }
                    Lifecycle.Event.ON_STOP -> {
                        currentOnStop()
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        currentOnPause()
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        currentOnDestroy()
                    }
                    Lifecycle.Event.ON_CREATE -> {
                        currentOnCreate()
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        currentOnResume()
                    }
                    else -> {}
                }
            }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
