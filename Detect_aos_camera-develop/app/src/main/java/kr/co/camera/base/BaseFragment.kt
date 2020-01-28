package kr.co.camera.base

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import kr.co.camera.view.dialog.UtilDialog

abstract class BaseFragment<T: ViewDataBinding>: androidx.fragment.app.Fragment() {

    private var viewBinding: T? = null
    private var utilDialog: UtilDialog? = null

    abstract val layoutRes: Int

    abstract fun setView(view: View?, savedInstanceState: Bundle?, arguments: Bundle?)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = DataBindingUtil.inflate(inflater, layoutRes, container, false)

        return viewBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setView(view, savedInstanceState, arguments)

        utilDialog = UtilDialog()
    }

    fun getBinding(): T = viewBinding!!

    fun checkActive(): Boolean = isAdded

    fun showProgress() {
        utilDialog?.showCustomProgressDialogAboutServer(context)
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun hideProgress() {
        utilDialog?.closeCustomProgressDialog()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}