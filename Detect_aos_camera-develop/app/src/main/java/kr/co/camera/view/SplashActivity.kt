package kr.co.camera.view

import android.content.Intent
import android.os.Handler
import kr.co.camera.R
import kr.co.camera.base.BaseActivity
import kr.co.camera.view.intro.IntroActivity

class SplashActivity: BaseActivity<kr.co.camera.databinding.ActivitySplashBinding>() {
    override fun getContentView(): Int = R.layout.activity_splash

    override fun initView() {
        getBinding().splashTvTitle.text = getString(R.string.login000)

        Handler().postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2000)
    }
}