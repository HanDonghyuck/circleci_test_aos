package kr.co.camera.etc

import android.graphics.Bitmap
import java.io.File
import android.graphics.Bitmap.CompressFormat
import java.io.FileOutputStream
import java.io.OutputStream


object Const {
    object INTENT {
        const val EXTRA_PET_NAME = "EXTRA_PET_NAME"
        const val EXTRA_CALLED_USER = "EXTRA_CALLED_USER"
        const val EXTRA_POSITION_HEAD = "EXTRA_POSITION_HEAD"

        const val EXTRA_IMAGE_FILE_DATA = "EXTRA_IMAGE_FILE_DATA"
        const val EXTRA_IMAGE_FILE_NAME = "EXTRA_IMAGE_FILE_NAME"
        const val EXTRA_IMAGE_DATA = "EXTRA_IMAGE_DATA"
        const val EXTRA_LUX = "EXTRA_LUX"
        const val EXTRA_WIDTH = "EXTRA_WIDTH"
        const val EXTRA_HEIGHT = "EXTRA_HEIGHT"
        const val EXTRA_ORIENTATION = "EXTRA_ORIENTATION"

        const val EXTRA_RESULT = "EXTRA_RESULT"
    }

    var IMAGE_BITMAP: Bitmap? = null
    var ORIGIN_FILE: File? = null
    var SMALL_FILE: File? = null
    var PICTURE_BYTE_ARRAY: ByteArray? = null

    fun saveBitmapAsFile(bitmap: Bitmap?, filepath: String) {
        val file = File(filepath)
        var os: OutputStream? = null

        try {
            os = FileOutputStream(file)

            bitmap?.compress(CompressFormat.PNG, 100, os)

            os.flush()
            os.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}