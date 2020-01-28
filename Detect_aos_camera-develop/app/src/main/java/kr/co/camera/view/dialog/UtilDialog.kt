package kr.co.camera.view.dialog

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.Window
import kotlinx.android.synthetic.main.dialog_loading_progress_server.view.*
import kr.co.camera.R
import kr.co.camera.etc.extension.getColorById

class UtilDialog {

    companion object {
        private var mInstance: UtilDialog? = null
        val instance: UtilDialog
            get() {
                if (mInstance == null)
                    mInstance = UtilDialog()

                return mInstance!!
            }
    }

    private var mprogDialog: ProgressDialog? = null
    private var customDialog: Dialog? = null

    private var hdAutoClose: Handler? = null

//    @Synchronized
//    fun showIndicator(context: Context?) {
//        if (customDialog == null) {
//
//            customDialog = Dialog(context!!)
//            customDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
//
//            customDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//            val inflater_prog = LayoutInflater.from(context)
//            val v_prog = inflater_prog.inflate(R.layout.dialog_animation_loading_progress, null)
//            val aaa = v_prog.findViewById<ImageView>(R.id.indicator_progressBar)
//            val aaa2 = aaa.background as AnimationDrawable
//            aaa2.start()
//            customDialog!!.setContentView(v_prog)
//        } else {
//
//        }
//        if (context == null) {
//            return
//        }
//        if (!(context as Activity).isFinishing) {
//            customDialog!!.setCancelable(false)
//            customDialog!!.show()
//        }
//    }
//
//    @Synchronized
//    fun showCustomProgressDialog(context: Context?, message: String) {
//
//        if (customDialog == null) {
//
//            customDialog = Dialog(context!!)
//            customDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
//
//            customDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//            val inflater_prog = LayoutInflater.from(context)
//            var v_prog = inflater_prog.inflate(R.layout.dialog_loading_progress, null)
//            if (message != "") {
//                v_prog = inflater_prog.inflate(R.layout.dialog_loading_progress_with_dialog, null)
//            }
//
//            customDialog!!.setContentView(v_prog)
//
//        } else {
//
//        }
//        if (context == null) {
//            return
//        }
//        if (!(context as Activity).isFinishing) {
//            customDialog!!.setCancelable(false)
//            customDialog!!.show()
//        }
//    }

    @Synchronized
    fun showCustomProgressDialogAboutServer(context: Context?) {
        if (customDialog == null) {
            customDialog = Dialog(context!!)
            customDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)

            customDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val inflater = LayoutInflater.from(context)
            val dialog = inflater.inflate(R.layout.dialog_loading_progress_server, null)

            dialog.loading_pb_circle.indeterminateDrawable.setColorFilter(context.getColorById(R.color.color_4A4A4A), android.graphics.PorterDuff.Mode.MULTIPLY)

            customDialog!!.setContentView(dialog)
        }

        if (context == null) {
            return
        }

        if (!(context as Activity).isFinishing) {
            customDialog!!.setCancelable(false)
            customDialog!!.show()
        }
    }

    @Synchronized
    fun closeCustomProgressDialog() {
        try {
            if (hdAutoClose != null)
                hdAutoClose!!.removeMessages(1001)

            if (customDialog != null) {
                if (customDialog!!.isShowing)
                    customDialog!!.dismiss()
                customDialog = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}