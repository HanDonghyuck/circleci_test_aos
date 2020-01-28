package kr.co.camera.view.dialog

import android.content.Context
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import kr.co.camera.BR
import kr.co.camera.R
import kr.co.camera.base.BaseDialog
import kr.co.camera.base.BaseRecyclerView
import kr.co.camera.databinding.DialogHeadBinding
import kr.co.camera.databinding.ItemHeadBinding
import kr.co.camera.etc.extension.showToast
import kr.co.camera.view.item.*

class HeadDialog(context: Context): BaseDialog<DialogHeadBinding>(context) {

    interface DCallback {
        fun getChoiceItem(position: Int)
    }

    private var callback: DCallback? = null    //아이템 클릭시 실행 함수
    private var selectePosition: Int = -1

    fun setCallBack(callBack: DCallback) {
        this.callback = callBack
    }

    override val layoutRes: Int
        get() = R.layout.dialog_head

    override fun initView() {
        // Dialog 사이즈 조절 하기
        window?.run {
            val params = this.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            this.attributes = params as WindowManager.LayoutParams
        }

        getBinding().dialog = this
        getBinding().headTvTitle.text = context.getString(R.string.login002)
        getBinding().headBtOk.text = context.getString(R.string.ok)

        val adapter = object : BaseRecyclerView.Adapter<HeadItem, ItemHeadBinding>(
            layoutResId = R.layout.item_head,
            bindingVariableId = BR.headItem
        ) {}

        adapter.replaceAll(context.getHeadData())

        adapter.setCallback(object : BaseRecyclerView.Adapter.ACallback {
            override fun onClick(position: Int) {
                selectePosition = position
                getBinding().headViewLeft.visibility = if(position == 0) View.VISIBLE else View.GONE
                getBinding().headViewRight.visibility = if(position == 0) View.GONE else View.VISIBLE
            }
        })

        getBinding().headRv.run {
            this.adapter = adapter
            this.layoutManager = GridLayoutManager(context, 2)
        }
    }

    fun onClickOk() {
        if(selectePosition == -1) {
            context.showToast("두상을 선택해 주세요.")
            return
        }
        callback?.getChoiceItem(selectePosition)
        cancel()
    }
}

