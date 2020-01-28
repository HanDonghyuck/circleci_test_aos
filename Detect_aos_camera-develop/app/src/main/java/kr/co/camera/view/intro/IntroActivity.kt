package kr.co.camera.view.intro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kr.co.camera.R
import kr.co.camera.base.BaseActivity
import kr.co.camera.databinding.ActivityIntroBinding
import kr.co.camera.etc.Dlog
import kr.co.camera.etc.extension.actionBarHide
import kr.co.camera.etc.extension.showToast
import kr.co.camera.view.LoginActivity

class IntroActivity: BaseActivity<ActivityIntroBinding>() {

    companion object {
        private const val PERMISSION_CAMERA = 100
    }

    override fun getContentView(): Int = R.layout.activity_intro

    override fun initView() {
        Dlog.i("initView")

        getBinding().activity = this
        getBinding().introTvStart.text = getString(R.string.skip)

        actionBarHide()
        requestPermissionCamera()

        // region 인트로 뷰페이져 작업
        val introViewPagerAdapter = VpIntroAdapter(supportFragmentManager)
        getBinding().introVp.adapter = introViewPagerAdapter
        getBinding().introTl.setupWithViewPager(getBinding().introVp)
        getBinding().introVp.addOnPageChangeListener(object: androidx.viewpager.widget.ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if(position == 2) getBinding().introTvStart.text = getString(R.string.start)
                else getBinding().introTvStart.text = getString(R.string.skip)
            }

            override fun onPageSelected(position: Int) {}
        })
        // endregion
    }

    fun onClickStart() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
        finish()
    }

    // region 권한
    private fun requestPermissionCamera() {
        //권한이 부여되어 있는지 확인
        val permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val permissionWriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
            permissionWriteStorage == PackageManager.PERMISSION_GRANTED) {
            Dlog.i("카메라 및 저장소 권한 있음")
        } else {
            Dlog.i("카메라 또는 저장소 권한이 없음")

            // 권한설정 dialog에서 거부를 누르면 ActivityCompat.shouldShowRequestPermissionRationale 메소드의 반환값이 true가 된다.
            // 단, 사용자가 "Don't ask again"을 체크한 경우 거부하더라도 false를 반환하여, 직접 사용자가 권한을 부여하지 않는 이상, 권한을 요청할 수 없게 된다.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ) {
                //이곳에 권한이 왜 필요한지 설명하는 Toast나 dialog를 띄워준 후, 다시 권한을 요청한다.
                showToast("앱을 사용하기 위해선 반드시 권한이 필요합니다")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_CAMERA
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_CAMERA
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 허가시
                    showToast("권한이 허가 되었습니다.")
                } else {
                    // 권한 거부시
                    showToast("앱을 사용하기 위해선 필수 권한입니다.")
                    requestPermissionCamera()
                }
                return
            }
        }
    }
    // endregion

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}