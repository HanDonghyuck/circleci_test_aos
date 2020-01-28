package kr.co.camera.view

import android.content.Intent
import kr.co.camera.R
import kr.co.camera.base.BaseActivity
import kr.co.camera.databinding.ActivityLoginBinding
import kr.co.camera.etc.BackPressCloseHandler
import kr.co.camera.etc.Const
import kr.co.camera.etc.Dlog
import kr.co.camera.view.camera.Camera2Activity
import kr.co.camera.view.pet_list.PetListActivity

class LoginActivity: BaseActivity<ActivityLoginBinding>() {

    private var headPosition: Int = -1
    private var petName: String? = null
    private val backPressCloseHandler: BackPressCloseHandler by lazy { BackPressCloseHandler(this) }

    override fun getContentView(): Int = R.layout.activity_login

    override fun initView() {
        Dlog.i("initView")

        getBinding().activity = this
        getBinding().loginTvTitle.text = getString(R.string.login000)
        getBinding().loginTvTitle2.text = "비문 촬영 서치 시스"
        getBinding().loginBtPetList.text = getString(R.string.login003)
        getBinding().loginBtStartSearch.text = getString(R.string.login004)
    }

    fun onClickPetList() {
        startActivity(Intent(this, PetListActivity::class.java))
    }

    fun onClickStartSearch() {
        startActivity(Intent(this, Camera2Activity::class.java))
    }

    override fun onBackPressed() {
        backPressCloseHandler.onBackPressed()
    }
}