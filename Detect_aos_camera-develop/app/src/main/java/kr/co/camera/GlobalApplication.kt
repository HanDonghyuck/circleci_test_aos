package kr.co.camera

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.util.Base64
import kr.co.camera.etc.Dlog

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class GlobalApplication : Application() {

    companion object {
        var DEBUG: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()

        DEBUG = isDebuggable(this)
    }

    // region 해시 키 구하기
    private fun getHashKey() {
        try {
            val info = packageManager.getPackageInfo("kr.co.fitpet", PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                Dlog.i("key_hash = $hashKey")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

    }
    // endregion

    // region 현재 디버그모드여부를 리턴
    private fun isDebuggable(context: Context): Boolean {
        var debuggable = false

        val packageManager = context.packageManager
        try {
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
            debuggable = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        } catch (e: PackageManager.NameNotFoundException) {
            /* debuggable variable will remain false */
        }

        return debuggable
    }
    // endregion
}
