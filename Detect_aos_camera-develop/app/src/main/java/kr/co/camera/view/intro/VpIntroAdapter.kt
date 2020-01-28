package kr.co.camera.view.intro

import kr.co.camera.view.intro.IntroFirstFragment
import kr.co.camera.view.intro.IntroSecondFragment
import kr.co.camera.view.intro.IntroThirdFragment

class VpIntroAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        return when(position) {
            0 -> { IntroFirstFragment.newInstance() }
            1 -> { IntroSecondFragment.newInstance() }
            2 -> { IntroThirdFragment.newInstance() }
//            3 -> { IntroForthFragment.newInstance() }
            else -> {
                IntroFirstFragment.newInstance()
            }
        }
    }

    override fun getCount(): Int {
        return 3
    }
}