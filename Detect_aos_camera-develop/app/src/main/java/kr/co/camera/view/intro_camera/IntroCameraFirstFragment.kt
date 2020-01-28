package kr.co.camera.view.intro_camera

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import kr.co.camera.R
import kr.co.camera.base.BaseFragment
import kr.co.camera.databinding.FragmentIntroCameraFirstBinding
import kr.co.camera.etc.Dlog
import kr.co.camera.etc.extension.getDrawableById

class IntroCameraFirstFragment: BaseFragment<FragmentIntroCameraFirstBinding>() {

    companion object {
        const val ARG_POSITION = "ARG_POSITION"
        fun newInstance(position: Int): IntroCameraFirstFragment = IntroCameraFirstFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_POSITION, position)
            }
        }
    }

    override val layoutRes: Int
        get() = R.layout.fragment_intro_camera_first

    private var position: Int = 0
    private var hdCountDown: Handler? = null
    private val hdCountPerOne: Handler by lazy { Handler() }
    private var isActive: Boolean = true

    override fun setView(view: View?, savedInstanceState: Bundle?, arguments: Bundle?) {
        Dlog.i("setView")

        arguments?.let {
            position = it.getInt(ARG_POSITION, 0)
        }

        var textData: String? = null
        when(position) {
            0 -> {
                textData = "장두종은 머리를 살짝 뒤로 젖혀\n렌즈와 코, 눈을 평행하게 맞춘 후 코의 평평한\n면을 정면으로 촬영해 주세요."
                getBinding().introCameraFirstIvDog.background = context?.getDrawableById(R.drawable.tutorial_02_1)
            }
            1 -> {
                textData = "단두종은 렌즈와 코를 평행하게 맞춘 후\n코의 평평한 면을 정면으로 촬영해 주세요."
                getBinding().introCameraFirstIvDog.background = context?.getDrawableById(R.drawable.tutorial_01_1)
            }
        }

        getBinding().introCameraFirstTv1.text = textData
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

                    when {
                        count > 2 -> {
                            hdCountDown?.sendEmptyMessageDelayed(0, 100)
                            getBinding().introCameraFirstIvDog.background =
                                if(position == 0) context?.getDrawableById(R.drawable.tutorial_02_1)
                                else context?.getDrawableById(R.drawable.tutorial_01_1)
                        }
                        count > 0 -> {
                            hdCountDown?.sendEmptyMessageDelayed(0, 100)
                            getBinding().introCameraFirstIvDog.background =
                                if(position == 0) context?.getDrawableById(R.drawable.tutorial_02_2)
                                else context?.getDrawableById(R.drawable.tutorial_01_2)
                        }
                        else -> {
                            getBinding().introCameraFirstIvDog.background =
                                if(position == 0) context?.getDrawableById(R.drawable.tutorial_02_1)
                                else context?.getDrawableById(R.drawable.tutorial_01_1)
                            startCountPerOne()
                        }
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
                if (count == 0 || count%2 == 0) {
                    getBinding().introCameraFirstIvDog.background =
                        if(position == 0) context?.getDrawableById(R.drawable.tutorial_02_2)
                        else context?.getDrawableById(R.drawable.tutorial_01_2)
                } else {
                    getBinding().introCameraFirstIvDog.background =
                        if(position == 0) context?.getDrawableById(R.drawable.tutorial_02_1)
                        else context?.getDrawableById(R.drawable.tutorial_01_1)
                }
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