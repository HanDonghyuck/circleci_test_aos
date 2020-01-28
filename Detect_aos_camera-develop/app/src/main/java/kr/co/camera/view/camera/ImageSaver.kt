package kr.co.camera.view.camera

import android.media.Image
import kr.co.camera.etc.Dlog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Saves a JPEG [Image] into the specified [File].
 */
internal class ImageSaver(
    private val image: Image,   /** The JPEG image */
    private val file: File      /** The file we save the image into. */
) : Runnable {

    override fun run() {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        var output: FileOutputStream? = null
        try {
            output = FileOutputStream(file).apply {
                write(bytes)
            }
        } catch (e: IOException) {
            Dlog.e("e = $e")
        } finally {
            image.close()
            output?.let {
                try {
                    it.close()
                } catch (e: IOException) {
                    Dlog.e("e = $e")
                }
            }
        }
    }
}
