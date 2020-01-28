package kr.co.camera.etc.extension

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat

// region toast message
fun Context.showToast(message: String) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }

fun Context.showToast(@StringRes messageRes: Int) { Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show() }

fun Context.showToastLong(message: String) { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }

fun Context.showToastLong(@StringRes messageRes: Int) { Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show() }
// endregion

// region keyboard
fun Context.hideKeyboard(view: View) { (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(view.windowToken, 0) }

fun Context.showKeyboard(view: View) { (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.showSoftInput(view, InputMethodManager.SHOW_FORCED) }
// endregion

// region Dialog or Message
fun Context.alertDialog(@StringRes messageId: Int, listener: DialogInterface.OnClickListener? = null) {
    alertDialog(getString(messageId), listener)
}

fun Context.alertDialog(message: String, listener: DialogInterface.OnClickListener? = null) {
    AlertDialog.Builder(this)
        .setCancelable(false)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, listener)
        .show()
}

fun Context.confirmDialog(@StringRes messageId: Int,
                          okListener: DialogInterface.OnClickListener? = null,
                          cancelListener: DialogInterface.OnClickListener? = null) {
    confirmDialog(getString(messageId), okListener, cancelListener)
}

fun Context.confirmDialog(message: String,
                          okListener: DialogInterface.OnClickListener? = null,
                          cancelListener: DialogInterface.OnClickListener? = null) {
    AlertDialog.Builder(this)
        .setCancelable(false)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, okListener)
        .setNegativeButton(android.R.string.cancel, cancelListener)
        .show()
}

fun Context.confirmDialogCustom(@StringRes messageId: Int, okText: String, noText: String,
                                okListener: DialogInterface.OnClickListener? = null,
                                cancelListener: DialogInterface.OnClickListener? = null) {
    confirmDialogCustom(getString(messageId), okText, noText, okListener, cancelListener)
}

fun Context.confirmDialogCustom(message: String, okText: String, noText: String,
                                okListener: DialogInterface.OnClickListener? = null,
                                cancelListener: DialogInterface.OnClickListener? = null) {
    AlertDialog.Builder(this)
        .setCancelable(false)
//        .setTitle("AlertDialog Title")
        .setMessage(message)
        .setPositiveButton(okText, okListener)
        .setNegativeButton(noText, cancelListener)
        .create().show()
}
// endregion


fun Context.checkPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED