package kr.co.camera.etc

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object BindingAdapters {
    @BindingAdapter("app:loadImage")
    @JvmStatic fun ImageView.loadImage(imageUrl: Int) {
        Glide.with(this.context)
            .load(imageUrl)
            .into(this)
    }

    @BindingAdapter("app:loadImageDrawable")
    @JvmStatic fun ImageView.loadImageDrawable(drawable: Drawable) {
        this.background = drawable
    }

    @BindingAdapter("app:loadImageDrawableRes")
    @JvmStatic fun ImageView.loadImageDrawableRes(resId: Int) {
        this.setBackgroundResource(resId)
    }
}