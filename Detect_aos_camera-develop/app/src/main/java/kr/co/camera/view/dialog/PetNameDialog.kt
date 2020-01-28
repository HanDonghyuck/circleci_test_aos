package kr.co.camera.view.dialog

import android.content.Context
import android.view.WindowManager
import kr.co.camera.R
import kr.co.camera.base.BaseDialog
import kr.co.camera.databinding.DialogPetNameBinding
import kr.co.camera.etc.extension.showToast

class PetNameDialog(context: Context): BaseDialog<DialogPetNameBinding>(context) {

    interface DCallback {
        fun getPetName(petName: String)
    }

    private var callback: DCallback? = null    //아이템 클릭시 실행 함수

    fun setCallBack(callBack: DCallback) {
        this.callback = callBack
    }

    override val layoutRes: Int
        get() = R.layout.dialog_pet_name

    override fun initView() {
        // Dialog 사이즈 조절 하기
        window?.run {
            val params = this.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            this.attributes = params as WindowManager.LayoutParams
        }

        getBinding().dialog = this
    }

    fun onClickNext() {
        val petName = getBinding().petNameEt.text
        if(petName.isEmpty()) {
            context.showToast("펫 이름을 입력해주세요.")
            return
        }
        callback?.getPetName(getBinding().petNameEt.toString())
        cancel()
    }
}