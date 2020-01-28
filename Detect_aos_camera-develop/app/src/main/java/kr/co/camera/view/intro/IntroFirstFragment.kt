package kr.co.camera.view.intro

import android.os.Bundle
import android.view.View
import kr.co.camera.R
import kr.co.camera.base.BaseFragment
import kr.co.camera.databinding.FragmentIntroFirstBinding
import kr.co.camera.etc.extension.toSpanned

class IntroFirstFragment : BaseFragment<FragmentIntroFirstBinding>() {

    companion object {
        fun newInstance(): IntroFirstFragment =
            IntroFirstFragment()
    }

    override val layoutRes: Int
        get() = R.layout.fragment_intro_first

    override fun setView(view: View?, savedInstanceState: Bundle?, arguments: Bundle?) {
        val textData = "<a>반드시 <font color='#ff0000'>밝은 자연광</font>이 있는 실내/외에서 조명을 등진 채 찰영하되, 강아지에게 <font color='#ff0000'>그림자가 지지 않도록</font> 주의해 주세요.</a>"
        getBinding().introFirstTv1.text = textData.toSpanned()
    }
}