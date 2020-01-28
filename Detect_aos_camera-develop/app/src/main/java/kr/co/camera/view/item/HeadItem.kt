package kr.co.camera.view.item

import android.content.Context
import androidx.annotation.DrawableRes
import kr.co.camera.R

data class HeadItem(
    val title: String,
    @DrawableRes val imageUrl: Int,
    val example: String
)

fun Context.getHeadData(): ArrayList<HeadItem> {
    val data: ArrayList<HeadItem> = ArrayList()

    data.add(HeadItem(getString(R.string.head2), R.drawable.dog_01, getString(R.string.head2_ex)))
    data.add(HeadItem(getString(R.string.head1), R.drawable.dog_02, getString(R.string.head1_ex)))

    return data
}