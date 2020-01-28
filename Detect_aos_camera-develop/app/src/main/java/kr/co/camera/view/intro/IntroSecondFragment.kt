package kr.co.camera.view.intro

import android.os.Bundle
import android.view.View
import kr.co.camera.R
import kr.co.camera.base.BaseFragment
import kr.co.camera.databinding.FragmentIntroSecondBinding
import kr.co.camera.etc.extension.toSpanned

class IntroSecondFragment: BaseFragment<FragmentIntroSecondBinding>() {

    companion object {
        fun newInstance(): IntroSecondFragment =
            IntroSecondFragment()
    }

    override val layoutRes: Int
        get() = R.layout.fragment_intro_second

    override fun setView(view: View?, savedInstanceState: Bundle?, arguments: Bundle?) {
        val textData = "<a>강아지가 움직임이 없는 안정된 상태에서 <font color='#ff0000'>코 정면과 렌즈가 평행</font>하게 촬영해 주세요.\n<font color='#ff0000'>* 코에 물기는 가볍게 닦아주세요.</font></a>"
        getBinding().introSecondTv1.text = textData.toSpanned()
    }
}