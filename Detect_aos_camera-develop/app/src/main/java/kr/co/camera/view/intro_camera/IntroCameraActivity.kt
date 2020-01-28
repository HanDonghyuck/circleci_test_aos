package kr.co.camera.view.intro_camera

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.view.View
import kr.co.camera.R
import kr.co.camera.base.BaseActivity
import kr.co.camera.databinding.ActivityIntroCameraBinding
import kr.co.camera.etc.Const
import kr.co.camera.etc.Dlog
import kr.co.camera.etc.extension.actionBarHide
import kr.co.camera.etc.extension.replaceFragment
import kr.co.camera.view.CameraActivity

class IntroCameraActivity : BaseActivity<ActivityIntroCameraBinding>() {

    private var petName: String? = null
    private var headPosition = 0

    private var hdCountDown: Handler? = null
    private var isClicked: Boolean = false

    override fun getContentView(): Int = R.layout.activity_intro_camera

    override fun initView() {
        Dlog.i("initView")

        actionBarHide()
        getBinding().activity = this

        petName = intent.getStringExtra(Const.INTENT.EXTRA_PET_NAME)
        headPosition = intent.getIntExtra(Const.INTENT.EXTRA_POSITION_HEAD, 0)

        changeUi(1, headPosition)
        startCount()

        getBinding().introCameraTvStart.setOnClickListener {
            if (isClicked) {
                if (getBinding().introCameraTvStart.text == getString(R.string.ok)) {
                    startActivity(Intent(this, CameraActivity::class.java).apply {
                        putExtra(Const.INTENT.EXTRA_PET_NAME, petName)
                        putExtra(Const.INTENT.EXTRA_POSITION_HEAD, headPosition)
                    })
                    finish()
                }
                return@setOnClickListener
            }

            if (getBinding().introCameraTvStart.text == getString(R.string.next)) {
                changeUi(2, headPosition)
                startCount()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    // region 생명주기
    override fun onResume() {
        super.onResume()
        Dlog.i("onResume")
    }

    override fun onPause() {
        super.onPause()
        Dlog.i("onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Dlog.i("onDestroy")

        if (hdCountDown != null)
            hdCountDown?.removeMessages(0)
    }
    // endregion

    private fun changeUi(position: Int, headPosition: Int) {
        if (position == 1) {
            getBinding().introCameraTvStart.text = getString(R.string.next)
            replaceFragment(R.id.intro_camera_fl, IntroCameraFirstFragment.newInstance(headPosition))
        } else {
            isClicked = true
            getBinding().introCameraTvStart.text = getString(R.string.ok)
            if (headPosition == 1)
                replaceFragment(R.id.intro_camera_fl, ShortHeadFragment.newInstance(headPosition))
            else
                replaceFragment(R.id.intro_camera_fl, LongHeadFragment.newInstance(headPosition))
        }
    }

    @SuppressLint("HandlerLeak")
    private fun startCount() {
        val mStartTime = System.currentTimeMillis()
        hdCountDown = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val count = 4 - ((System.currentTimeMillis() - mStartTime) / 1000).toInt()
                when (count) {
                    4 -> {
                        hdCountDown?.sendEmptyMessageDelayed(0, 100)
                        getBinding().introCameraTvStart.text = "1"
                    }
                    3 -> {
                        hdCountDown?.sendEmptyMessageDelayed(0, 100)
                        getBinding().introCameraTvStart.text = "2"
                    }
                    2 -> {
                        hdCountDown?.sendEmptyMessageDelayed(0, 100)
                        getBinding().introCameraTvStart.text = "1"
                    }
                    1 -> {
                        hdCountDown?.sendEmptyMessageDelayed(0, 100)
                        getBinding().introCameraTvStart.text = "2"
                    }
                    else -> getBinding().introCameraTvStart.text =
                        if (isClicked) getString(R.string.ok) else getString(R.string.next)
                }
            }
        }
        hdCountDown?.sendEmptyMessageDelayed(0, 10)
    }
}