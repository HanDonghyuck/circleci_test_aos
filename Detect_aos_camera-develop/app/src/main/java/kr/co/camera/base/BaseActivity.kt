package kr.co.camera.base

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager
import kr.co.camera.etc.extension.changeStatusBar
import kr.co.camera.view.dialog.UtilDialog

abstract class BaseActivity<T: ViewDataBinding>: AppCompatActivity() {

    private var viewBinding: T? = null

    private var utilDialog: UtilDialog? = null

    abstract fun getContentView(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = DataBindingUtil.setContentView(this, getContentView())

        title = ""
        changeStatusBar(android.R.color.black)

        initView()
        onClickListener()

        utilDialog = UtilDialog()
    }

    abstract fun initView()

    open fun onClickListener() {}

//    fun setBinding(@LayoutRes resId: Int) {
//        if(viewBinding == null) viewBinding = DataBindingUtil.setContentView(this, resId)
//    }

    fun getBinding(): T = viewBinding ?: DataBindingUtil.setContentView(this, getContentView())

    fun checkActive(): Boolean = !isFinishing

    fun showProgress() {
        utilDialog?.showCustomProgressDialogAboutServer(this)
        window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun hideProgress() {
        utilDialog?.closeCustomProgressDialog()
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}