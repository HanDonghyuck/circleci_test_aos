package kr.co.camera.etc.extension

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.widget.Toast

fun String.toSpanned(): Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY) else @Suppress("DEPRECATION") Html.fromHtml(this)

fun Context?.showToast(message: String) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }