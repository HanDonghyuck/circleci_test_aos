package kr.co.camera.view.dialog

import android.content.Context
import kr.co.camera.base.BaseRecyclerView
import android.view.WindowManager
import kr.co.camera.BR
import kr.co.camera.R
import kr.co.camera.base.BaseDialog
import kr.co.camera.databinding.DialogChoiceBinding
import kr.co.camera.databinding.ItemChoiceBinding
import kr.co.camera.view.item.ChoiceItem
import kr.co.camera.view.item.getChoiceData
import kr.co.camera.view.item.getChoiceHead

class ChoiceDialog(context: Context,
                   var selectedValue: String): BaseDialog<DialogChoiceBinding>(context) {

    interface DCallback {
        fun getChoiceItem(position: Int,
                          selectedValue: String)
    }

    private var callback: DCallback? = null    //아이템 클릭시 실행 함수

    fun setCallBack(callBack: DCallback) {
        this.callback = callBack
    }

    override val layoutRes: Int
        get() = R.layout.dialog_choice

    override fun initView() {
        // Dialog 사이즈 조절 하기
        window?.run {
            val params = this.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            this.attributes = params as WindowManager.LayoutParams
        }


        val adapter = object : BaseRecyclerView.Adapter<ChoiceItem, ItemChoiceBinding>(
            layoutResId = R.layout.item_choice,
            bindingVariableId = BR.choiceItem
        ) {}

        when(selectedValue) {
            "head" -> adapter.replaceAll(getChoiceHead())
            "kind" -> adapter.replaceAll(getChoiceData())
        }

        adapter.setCallback(object : BaseRecyclerView.Adapter.ACallback {
            override fun onClick(position: Int) {
                callback?.getChoiceItem(position, selectedValue)
                cancel()
            }
        })

        getBinding().choiceRv.adapter = adapter
    }
}

