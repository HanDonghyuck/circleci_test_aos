package kr.co.camera.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.view.MenuItem
import kr.co.camera.R
import kr.co.camera.base.BaseActivity
import kr.co.camera.databinding.ActivitySearchModeBinding
import kr.co.camera.etc.Const
import kr.co.camera.etc.extension.setActionBarHome
import kr.co.camera.etc.extension.showToast
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.sql.Time



class SearchModeActivity: BaseActivity<ActivitySearchModeBinding>() {
    override fun getContentView(): Int = R.layout.activity_search_mode

    external fun vervice(a:Long, b:Long):Int

    init {
        System.loadLibrary("native-lib")
    }

    override fun initView() {
        getBinding().activity = this
        setActionBarHome(getBinding().includeToolbar.toolbar, R.drawable.btn_back)

        getBinding().includeToolbar.toolbarTvTitle.text = getString(R.string.search_mode)
        getBinding().searchModeIv.setImageBitmap(Const.IMAGE_BITMAP)

        //TODO : 이미지 서치 부분 정의
    }
    fun onClickSearch(){

//        val am1 = resources.assets
//        val inputstream1 = am1.open("hukja2.png")
//        val size1 = inputstream1.available()
//        val buffer1 = ByteArray(size1)
//        inputstream1.read(buffer1)
//
//        val bitmap2 = BitmapFactory.decodeByteArray(buffer1,0, buffer1.size)


        // Picture just taken
        val tar = Const.IMAGE_BITMAP//bitmap2

        //Const.saveBitmapAsFile(tar,Environment.DIRECTORY_DOCUMENTS+"/image1.png")
        val now = System.currentTimeMillis()

        Const.saveBitmapAsFile(tar, Environment.getExternalStorageDirectory().toString()+"/"+now.toString()+"_device.png")

        val tarMat = Mat()

        // Saved reference image
        val am = resources.assets
        val inputstream = am.open("1566217753951_device.png")
        val size = inputstream.available()
        val buffer = ByteArray(size)
        inputstream.read(buffer)

        val bitmap = BitmapFactory.decodeByteArray(buffer,0, buffer.size)

        val ref = bitmap//Const.IMAGE_BITMAP


        val croppedTar = Bitmap.createBitmap(tar,378,185,60,137)
        val croppedRef = Bitmap.createBitmap(ref,378,185,60,137)


        // change bitmap to tar
        //Utils.bitmapToMat(tar, tarMat)
        Utils.bitmapToMat(croppedTar, tarMat)
        val refMat = Mat()
        //Utils.bitmapToMat(ref,refMat)
        Utils.bitmapToMat(croppedRef,refMat)

        //showToast("여기다가 해당 이미지 서치하면 될거같아요." + refMat.nativeObj + "\n" + tarMat.nativeObj)

        val result = vervice(refMat.nativeObj, tarMat.nativeObj)

        showToast("여기다가 해당 이미지 서치하면 될거같아요." + result)

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