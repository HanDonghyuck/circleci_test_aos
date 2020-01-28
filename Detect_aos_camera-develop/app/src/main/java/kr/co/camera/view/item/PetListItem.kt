package kr.co.camera.view.item

import android.content.Context
import androidx.annotation.DrawableRes
import kr.co.camera.R

data class PetListItem(
    @DrawableRes val imageRes: Int,
    val petName: String
)

fun Context.getPetList(): ArrayList<PetListItem> {
    val data: ArrayList<PetListItem> = ArrayList()

    data.add(PetListItem(R.drawable.dog_01, "방방이"))
    data.add(PetListItem(R.drawable.dog_02, "김뚱"))
    data.add(PetListItem(R.drawable.dog_01, "김댕댕"))
    data.add(PetListItem(R.drawable.dog_02, "이철수"))

    return data
}