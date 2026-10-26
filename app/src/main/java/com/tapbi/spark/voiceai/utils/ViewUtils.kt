package com.tapbi.spark.voiceai.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.TextPaint
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.Display
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

    fun disableAnimateRCV(recycler: RecyclerView) {
        recycler.animation = null
        (recycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    fun setTextViewDrawableColor(textView: TextView, color: Int) {
        for (drawable in textView.compoundDrawables) {
            drawable?.colorFilter = PorterDuffColorFilter(
                color, PorterDuff.Mode.SRC_IN
            )
        }
    }

    fun measureTextWidth(view: TextView, text: String?): Float {
        if (TextUtils.isEmpty(text)) {
            return 0f
        }
        val paint: TextPaint = view.paint
        return paint.measureText(text)
    }

    private fun hasNavigationBar(context: Context): Boolean {
        val hasNavBarId =
            context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return if (hasNavBarId > 0) {
            context.resources.getBoolean(hasNavBarId)
        } else {
            val hasPermanentMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
            val display =
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val realSize = Point()
            val screenSize = Point()

            display.getRealSize(realSize)
            display.getSize(screenSize)

            realSize.y != screenSize.y || realSize.x != screenSize.x || !hasPermanentMenuKey
        }
    }

    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId =
            Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = Resources.getSystem().getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun hasOnScreenSystemBar(activity: Activity): Boolean {
        val display: Display = activity.windowManager.defaultDisplay
        var rawDisplayHeight = 0
        try {
            val getRawHeight = Display::class.java.getMethod("getRawHeight")
            rawDisplayHeight = getRawHeight.invoke(display) as Int
        } catch (ex: Exception) {
        }
        val UIRequestedHeight = display.height
        return rawDisplayHeight - UIRequestedHeight > 0
    }

     fun getNaviBarHeight(context: Context): Int {
        var result = 0
        val resourceId =
            context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun getScreenWidth(context: Context): Int {
        val displaymetrics = context.resources.displayMetrics
        return displaymetrics.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        val displaymetrics = context.resources.displayMetrics
        return displaymetrics.heightPixels
    }


    fun pxToDp(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun dpToPx(context: Context, dipValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f)
    }

    fun pxToSp(context: Context, pxValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    fun spToPx(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    fun formatDate(time: Long): String {
        return DateFormat.format("d.M.yyyy", time).toString()
    }

    fun getBitmapFromView(v: View): Bitmap {
        val b = Bitmap.createBitmap(
            v.width,
            v.height,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
        v.layout(v.left, v.top, v.right, v.bottom)
        v.draw(c)
        return b
    }

    fun setUpWrapHeight(bottomSheetDialog: BottomSheetDialog) {
        val behavior: BottomSheetBehavior<*> = bottomSheetDialog.behavior
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }

    fun hideKeyBoard(context: Context, edt: AppCompatEditText) {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edt.windowToken, 0)
    }

    fun showKeyboard(context: Context, edt: AppCompatEditText) {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edt, InputMethodManager.SHOW_IMPLICIT)
    }
