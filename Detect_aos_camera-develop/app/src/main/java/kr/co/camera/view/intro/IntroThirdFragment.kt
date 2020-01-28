package kr.co.camera.view.intro

import android.os.Bundle
import android.view.View
import kr.co.camera.R
import kr.co.camera.base.BaseFragment
import kr.co.camera.databinding.FragmentIntroThirdBinding
import kr.co.camera.etc.extension.toSpanned

class IntroThirdFragment : BaseFragment<FragmentIntroThirdBinding>() {

    companion object {
        fun newInstance(): IntroThirdFragment =
            IntroThirdFragment()
    }

    override val layoutRes: Int
        get() = R.layout.fragment_intro_third

    override fun setView(view: View?, savedInstanceState: Bundle?, arguments: Bundle?) {
        val textData = "<a><font color='#ff0000'>눈과 코 사이 각도를 맞춰</font> 촬영하고, 사진이 뚜렷하고 밝게 나왔는지, 콧구멍과 가운데 <font color='#ff0000'>선이 맞춰졌는지 확인</font>해 주세요.</a>"
        getBinding().introThirdTv1.text = textData.toSpanned()
    }
}