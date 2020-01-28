package kr.co.camera.etc.extension

import android.app.Activity
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity

// 상태바 색상 변경
fun Activity.changeStatusBar(@ColorRes color: Int) {
    val window = window
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = this.getColorById(color)
}

fun AppCompatActivity.actionBarHide() {
    supportActionBar?.hide()
}

fun AppCompatActivity.setActionBarHome(toolbar: Toolbar, @DrawableRes res: Int) {
    setSupportActionBar(toolbar)
    supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(true)
        setHomeButtonEnabled(true)
        setHomeAsUpIndicator(res)  // 세줄짜리 이미지
    }
}

// fragment 변경
fun FragmentActivity.replaceFragment(@IdRes res: Int, fragment: androidx.fragment.app.Fragment) {
    val fragmentTransaction: androidx.fragment.app.FragmentTransaction = this.supportFragmentManager.beginTransaction()
    fragmentTransaction.replace(res, fragment)
    fragmentTransaction.addToBackStack(null)
//    fragmentTransaction.commit()
    fragmentTransaction.commitAllowingStateLoss()
}