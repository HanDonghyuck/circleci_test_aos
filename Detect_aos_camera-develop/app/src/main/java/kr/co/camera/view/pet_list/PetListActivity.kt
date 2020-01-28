package kr.co.camera.view.pet_list

import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.camera.BR
import kr.co.camera.R
import kr.co.camera.base.BaseActivity
import kr.co.camera.base.BaseRecyclerView
import kr.co.camera.databinding.ActivityPetListBinding
import kr.co.camera.databinding.ItemPetListBinding
import kr.co.camera.etc.extension.setActionBarHome
import kr.co.camera.view.item.*

class PetListActivity: BaseActivity<ActivityPetListBinding>() {
    override fun getContentView(): Int = R.layout.activity_pet_list

    override fun initView() {
        getBinding().includeToolbar.toolbarTvTitle.text = getString(R.string.login003)

        setActionBarHome(getBinding().includeToolbar.toolbar, R.drawable.btn_back)

        setRecyclerView()
    }

    private fun setRecyclerView() {
        val adapter = object : BaseRecyclerView.Adapter<PetListItem, ItemPetListBinding>(
            layoutResId = R.layout.item_pet_list,
            bindingVariableId = BR.petListItem
        ) {}

        adapter.run {
            replaceAll(getPetList())
            setCallback(object : BaseRecyclerView.Adapter.ACallback {
                override fun onClick(position: Int) {
//                    callback?.getChoiceItem(position, selectedValue)
//                    cancel()
                }
            })
        }

        getBinding().petListRv.run {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
    }

    // region menu 관련 버튼(홈)
    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    // endregion
}