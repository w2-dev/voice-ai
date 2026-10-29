package com.tapbi.spark.voiceai.ui.main.lockoverlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme
import com.tapbi.spark.voiceai.extension.hasOverlayPermission
import com.tapbi.spark.voiceai.extension.isAtLeastApi26
import com.tapbi.spark.voiceai.extension.isAtLeastApi28
import com.tapbi.spark.voiceai.extension.isAtLeastApi29
import com.tapbi.spark.voiceai.extension.isAtLeastApi30
import com.tapbi.spark.voiceai.service.OverlayLifecycleOwner
import com.tapbi.spark.voiceai.ui.main.lockoverlay.themes.OverlayThemeConfig
import timber.log.Timber

class LockScreenViewBinder(
    private val context: Context,
    private val onUnlockRequested: () -> Unit
) {
    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null
    private var activeLifecycleOwner: OverlayLifecycleOwner? = null

    companion object {
        private const val TAG = "LockScreenViewBinder"
    }

    fun show(themeConfig: OverlayThemeConfig = OverlayThemeConfig()) {
        if (!context.hasOverlayPermission()) {
            Timber.tag(TAG).w("Cannot show lock overlay: Overlay permission not granted")
            return
        }
        if (composeView != null) return

        @Suppress("DEPRECATION")
        val flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (isAtLeastApi26) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.FILL
            if (isAtLeastApi30) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (isAtLeastApi28) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        try {
            val lifecycleOwner = OverlayLifecycleOwner().apply {
                performRestore(null)
                handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                handleLifecycleEvent(Lifecycle.Event.ON_START)
                handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }
            activeLifecycleOwner = lifecycleOwner

            val viewModelStoreOwner = object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = ViewModelStore()
            }

            composeView = ComposeView(context).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
                setViewTreeViewModelStoreOwner(viewModelStoreOwner)

                setContent {
                    AppTheme {
                        LockOverlayContent(
                            themeConfig = themeConfig,
                            onUnlockClick = {
                                onUnlockRequested()
                            }
                        )
                    }
                }
            }

            composeView?.setOnApplyWindowInsetsListener { view, insets ->
                if (isAtLeastApi30) {
                    view.windowInsetsController?.hide(
                        WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
                    )
                }
                insets
            }

            windowManager.addView(composeView, params)

            // Hide status bar & system navigation bar + Exclude System Gestures
            composeView?.post {
                if (isAtLeastApi29) {
                    try {
                        val metrics = context.resources.displayMetrics
                        val rect =
                            android.graphics.Rect(0, 0, metrics.widthPixels, metrics.heightPixels)
                        composeView?.systemGestureExclusionRects = listOf(rect)
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "Error setting systemGestureExclusionRects")
                    }
                }

                if (isAtLeastApi30) {
                    composeView?.windowInsetsController?.apply {
                        hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                        systemBarsBehavior =
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                } else {
                    @Suppress("DEPRECATION")
                    composeView?.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            )
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error adding Compose overlay to WindowManager")
            composeView = null
            activeLifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            activeLifecycleOwner = null
        }
    }

    fun hide() {
        composeView?.let { view ->
            try {
                if (view.isAttachedToWindow) {
                    windowManager.removeViewImmediate(view)
                } else {
                    windowManager.removeView(view)
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error removing overlay view from WindowManager")
                try {
                    windowManager.removeView(view)
                } catch (_: Exception) {
                }
            } finally {
                composeView = null
            }
        }
        try {
            activeLifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            activeLifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            activeLifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error destroying lifecycleOwner")
        } finally {
            activeLifecycleOwner = null
        }
    }
}
