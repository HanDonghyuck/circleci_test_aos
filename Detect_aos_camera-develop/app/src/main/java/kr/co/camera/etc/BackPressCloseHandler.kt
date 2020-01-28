package kr.co.camera.etc

import android.app.Activity
import kr.co.camera.R
import kr.co.camera.etc.extension.showToast

class BackPressCloseHandler(private val context: Activity) {
    private var backKeyPressedTime = 0L

    fun onBackPressed() {
        if(System.currentTimeMillis() > backKeyPressedTime + 2000) {
            context.showToast(R.string.back_pressed_message)
            backKeyPressedTime = System.currentTimeMillis()
            return
        }

        if(System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            context.finish()
        }
    }
}