package kr.co.camera.view.camera

import android.util.Size
import java.lang.Long
import java.util.*

/**
 * Compares two `Size`s based on their areas.
 */
internal class CompareSizesByArea : Comparator<Size> {

    // We cast here to ensure the multiplications won't overflow
    override fun compare(lhs: Size, rhs: Size) =
        Long.signum((lhs.width.toLong() * (lhs.width.toLong() / lhs.height.toFloat()) - rhs.width.toLong() * (rhs.width.toLong() / rhs.height.toFloat())).toLong())

}
