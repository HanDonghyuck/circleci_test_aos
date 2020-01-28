package kr.co.camera.view.camera

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.media.Image
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer

class Operator {
    fun imageToBitmap(image: Image): Bitmap {
        val mYuvMat = imageToMat(image, CvType.CV_8UC1)
        val bgrMat = Mat(image.width, image.height, CvType.CV_8UC4)

        Imgproc.cvtColor(mYuvMat, bgrMat, Imgproc.COLOR_YUV2BGR_I420)

        val rgbaMatOut = Mat()
        Imgproc.cvtColor(bgrMat, rgbaMatOut, Imgproc.COLOR_BGR2RGBA, 0)

        var bitmap = Bitmap.createBitmap(bgrMat.cols(), bgrMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(rgbaMatOut, bitmap)

        val matrix = Matrix()
        matrix.postRotate(90f)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        return bitmap
    }

    private fun imageToMat(image: Image, type: Int): Mat {
        var buffer: ByteBuffer
        var rowStride: Int
        var pixelStride: Int
        val width = image.width
        val height = image.height
        var offset = 0

        val planes = image.planes
        val data = ByteArray(image.width * image.height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8)
        val rowData = ByteArray(planes[0].rowStride)

        for (i in planes.indices) {
            buffer = planes[i].buffer
            buffer.rewind()
            rowStride = planes[i].rowStride
            pixelStride = planes[i].pixelStride
            val w = if (i == 0) width else width / 2
            val h = if (i == 0) height else height / 2
            for (row in 0 until h) {
                val bytesPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8
                if (pixelStride == bytesPerPixel) {
                    val length = w * bytesPerPixel
                    buffer.get(data, offset, length)

                    if (h - row != 1) {
                        buffer.position(buffer.position() + rowStride - length)
                    }
                    offset += length
                } else {


                    if (h - row == 1) {
                        buffer.get(rowData, 0, width - pixelStride + 1)
                    } else {
                        buffer.get(rowData, 0, rowStride)
                    }

                    for (col in 0 until w) {
                        data[offset++] = rowData[col * pixelStride]
                    }
                }
            }
        }

        val mat = Mat(height + height / 2, width, type)
        mat.put(0, 0, data)

        return mat
    }
}