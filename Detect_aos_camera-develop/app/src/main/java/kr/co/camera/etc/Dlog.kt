package kr.co.camera.etc

import android.util.Log
import kr.co.camera.GlobalApplication

object Dlog {
    private val TAG = GlobalApplication::class.java.simpleName

    private fun buildLogMsg(message: String): String {
        val ste = Thread.currentThread().stackTrace[4]
        val sb = StringBuilder()
        sb.append("[")
        sb.append(ste.fileName.replace(".java", ""))
        sb.append("::")
        sb.append(ste.methodName)
        sb.append("]")
        sb.append(message)

        return sb.toString()
    }

    // Log Level Error
    fun e(message: String) {
        if (GlobalApplication.DEBUG) Log.e(
            TAG,
            buildLogMsg(message)
        )
    }

    // Log Level Warning
    fun w(message: String) {
        if (GlobalApplication.DEBUG) Log.w(
            TAG,
            buildLogMsg(message)
        )
    }

    // Log Level Information
    fun i(message: String) {
        if (GlobalApplication.DEBUG) Log.i(
            TAG,
            buildLogMsg(message)
        )
    }

    // Log Level Debug
    fun d(message: String) {
        if (GlobalApplication.DEBUG) Log.d(
            TAG,
            buildLogMsg(message)
        )
    }

    // Log Level Verbose
    fun v(message: String) {
        if (GlobalApplication.DEBUG) Log.v(
            TAG,
            buildLogMsg(message)
        )
    }
}