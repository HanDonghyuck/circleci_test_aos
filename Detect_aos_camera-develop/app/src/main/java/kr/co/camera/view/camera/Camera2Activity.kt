package kr.co.camera.view.camera

import android.view.View
import kr.co.camera.R
import kr.co.camera.base.BaseActivity
import kr.co.camera.databinding.ActivityCamera2Binding
import kr.co.camera.etc.Dlog
import kr.co.camera.etc.extension.actionBarHide
import kr.co.camera.etc.extension.replaceFragment

class Camera2Activity: BaseActivity<ActivityCamera2Binding>() {
    override fun getContentView(): Int = R.layout.activity_camera2

    override fun initView() {
        actionBarHide()

        //window.decorView.apply{            systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN        }

//        val petName = intent.getStringExtra(Const.INTENT.EXTRA_PET_NAME)
//        val petHeadPosition= intent.getIntExtra(Const.INTENT.EXTRA_POSITION_HEAD, 0)

//        replaceFragment(R.id.container, Camera2BasicFragment.newInstance(petName, petHeadPosition))
        replaceFragment(R.id.container, Camera2NewFragment.newInstance())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Dlog.i("onBackPressed")
        finish()
    }
}