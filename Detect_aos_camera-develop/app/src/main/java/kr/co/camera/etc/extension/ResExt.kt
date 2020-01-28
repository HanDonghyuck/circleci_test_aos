package kr.co.camera.etc.extension

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import android.util.Log
import android.util.TypedValue

// pixel 값을 dp로 변환
fun Context.pxToDp(px: Float): Float {
    var density: Float = this.resources.displayMetrics.density
    Log.w("결과", "결과 : px = $px")
    Log.w("결과", "결과 : 1. density = $density")

    Log.w("결과", "결과 : 2. density = $density")
    return px / density
}

fun Int.toPx(context: Context): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics).toInt()

fun Context.getDrawableById(@DrawableRes res: Int): Drawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getDrawable(res) else resources.getDrawable(res)

fun Context.getColorById(@ColorRes res: Int): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) getColor(res) else resources.getColor(res)