package kr.co.camera.view.intro_camera

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import kr.co.camera.R
import kr.co.camera.base.BaseFragment
import kr.co.camera.databinding.FragmentShortHeadBinding
import kr.co.camera.etc.Dlog
import kr.co.camera.etc.extension.getDrawableById

class ShortHeadFragment: BaseFragment<FragmentShortHeadBinding>() {

    companion object {
        const val ARG_POSITION = "ARG_POSITION"
        fun newInstance(position: Int): ShortHeadFragment = ShortHeadFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_POSITION, position)
            }
        }
    }

    override val layoutRes: Int
        get() = R.layout.fragment_short_head

    private var position : Int = 0
    private var hdCountDown: Handler? = null
    private val hdCountPerOne: Handler by lazy { Handler() }
    private var isActive: Boolean = true

    override fun setView(view: View?, savedInstanceState: Bundle?, arguments: Bundle?) {

        arguments?.let {
            position = it.getInt(ARG_POSITION)
        }

        getBinding().shortHeadIvHead.background = context?.getDrawableById(R.drawable.dog_01_guide_red)

        val textData = "밝은 곳에서 눈과 코 사이의 각도, 사각 박스 안 콧구멍과 가운데 선을 맞춘 후 초점을 맞춰 촬영해 주세요."
        getBinding().shortHeadTv.text = textData

        startCount()
    }

    @SuppressLint("HandlerLeak")
    private fun startCount() {
        val mStartTime = System.currentTimeMillis()
        hdCountDown = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (isActive) {
                    val count = 4 - ((System.currentTimeMillis() - mStartTime) / 1000).toInt()
                    Dlog.i("결과 : 카운트 다운 : $count")

                    if (count > 2) {
                        hdCountDown?.sendEmptyMessageDelayed(0, 100)
                        getBinding().shortHeadIvHead.visibility = View.GONE
                    } else if (count > 0) {
                        hdCountDown?.sendEmptyMessageDelayed(0, 100)
                        getBinding().shortHeadIvHead.visibility = View.VISIBLE
                    } else {
                        getBinding().shortHeadIvHead.visibility = View.GONE
                        startCountPerOne()
                    }
                } else
                    hdCountDown?.sendEmptyMessageDelayed(0, 100)
            }
        }
        hdCountDown?.sendEmptyMessageDelayed(0, 10)
    }

    private fun startCountPerOne() {
        var count = 0
        val delay = 1000 //milliseconds

        hdCountPerOne.postDelayed(object : Runnable {
            override fun run() {
                Dlog.i("결과 : 카운트 다운 : $count")
                getBinding().shortHeadIvHead.visibility = if (count == 0 || count%2 == 0) View.VISIBLE else View.GONE
                count++
                hdCountPerOne.postDelayed(this, delay.toLong())
            }
        }, delay.toLong())
    }

    // region 생명주기
    override fun onResume() {
        super.onResume()
        Dlog.i("onResume")

        isActive = true
    }

    override fun onPause() {
        super.onPause()
        Dlog.i("onPause")

        isActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
        Dlog.i("onDestroy")

        if (hdCountDown != null)
            hdCountDown?.removeMessages(0)

        hdCountPerOne.removeMessages(0)
    }
    // endregion
}