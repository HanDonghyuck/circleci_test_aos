package kr.co.camera.view.result_image

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.os.Environment
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.control.*
import kr.co.camera.R
import kr.co.camera.base.BaseActivity
import kr.co.camera.databinding.ActivityResultBinding
import kr.co.camera.etc.Const
import kr.co.camera.etc.Dlog
import kr.co.camera.etc.extension.actionBarHide
import kr.co.camera.etc.extension.showToast
import kr.co.camera.etc.extension.toSpanned
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ResultActivity: BaseActivity<ActivityResultBinding>() {

    override fun getContentView(): Int = R.layout.activity_result

    private lateinit var saveFileData: String
    private lateinit var fileName: String
    private lateinit var imageData: String
    private lateinit var lux: String
    private var textData: String? = null
    private var width: Int = 0
    private var height: Int = 0
    private var orientation: Int = 0

    private var entropy: Float = 0f
    private var mct: MutableList<Float> = mutableListOf()

    override fun initView() {
        actionBarHide()

        getBinding().resultTvReference.bringToFront()

        getBinding().resultTvReference.text = getString(R.string.result003)
        getBinding().resultTvSave.text = getString(R.string.save)
        getBinding().resultTvCancel.text = getString(R.string.cancel)

        getBinding().resultIvImage.setImageBitmap(Const.IMAGE_BITMAP)

        saveFileData = intent.getStringExtra(Const.INTENT.EXTRA_IMAGE_FILE_DATA)
        fileName = intent.getStringExtra(Const.INTENT.EXTRA_IMAGE_FILE_NAME)
        imageData = intent.getStringExtra(Const.INTENT.EXTRA_IMAGE_DATA)
        lux = intent.getStringExtra(Const.INTENT.EXTRA_LUX)
        width = intent.getIntExtra(Const.INTENT.EXTRA_WIDTH, 0)
        height = intent.getIntExtra(Const.INTENT.EXTRA_HEIGHT, 0)
        orientation = intent.getIntExtra(Const.INTENT.EXTRA_ORIENTATION, 0)

        entropy = imageData.split("imageEntropy : ")[1].split("mctCountBinary[0]")[0].toFloat()
        mct.add(imageData.split("mctCountBinary[0] : ")[1].split("mctCountBinary[1]")[0].toFloat())
        mct.add(imageData.split("mctCountBinary[1] : ")[1].split("mctCountBinary[2]")[0].toFloat())
        mct.add(imageData.split("mctCountBinary[2] : ")[1].split("mctCountBinary[3]")[0].toFloat())
        mct.add(imageData.split("mctCountBinary[3] : ")[1].split("roll(x)")[0].toFloat())

        Dlog.i("결과 : entropy = $entropy")
        mct.forEach {
            println("결과 : $it")
        }

        if(entropy < 3) {
            textData = "<font color='#ffff4444'>${getString(R.string.failEntropy)}</font>"
            getBinding().resultTvTitle.text = textData?.toSpanned()
        }

        mct.forEach {
            if(it > 0.05) {
                textData = "<font color='#ffff4444'>${getString(R.string.failMct)}</font>"
                getBinding().resultTvTitle.text = textData?.toSpanned()
            }
        }

        if(entropy >= 3) {
            mct.forEach {
                if (it <= 0.05) {
                    textData = "<font color='#88007700'>${getString(R.string.successImage)}</font>"
                    getBinding().resultTvTitle.text = textData?.toSpanned()
                    getBinding().resultIvImage.visibility = View.GONE
                }
            }
        }
    }

    override fun onClickListener() {
        super.onClickListener()

        // 취소 클릭 시 이벤트
        getBinding().resultTvCancel.setOnClickListener {
            setResult(RESULT_OK, Intent())
            finish()
        }

        // 확인 클릭 시 이벤트
        getBinding().resultTvSave.setOnClickListener {
            if(entropy >= 3) {
                mct.forEach {
                    if (it <= 0.05) {
                        saveTextFile()
                    }
                }
            }

//            saveTextFile()
        }
    }

    override fun onResume() {
        super.onResume()
        Dlog.i("onResume")
    }

    private fun checkPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun saveTextFile() {
        val photoFile = Const.ORIGIN_FILE
        val photoFileS = Const.SMALL_FILE
        val dataByteArray = Const.PICTURE_BYTE_ARRAY

        // 본 이미지 저장
        val fos = FileOutputStream(photoFile)
        fos.write(dataByteArray)
        fos.close()

        // byte array를 bitmap으로 변환
        dataByteArray?.let {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            var bitmap = BitmapFactory.decodeByteArray(it, 0, it.size, options)

            //이미지를 디바이스 방향으로 회전
            val matrix = Matrix()
            matrix.postRotate(orientation.toFloat())
            bitmap = Bitmap.createBitmap(
                bitmap,
                0, 0,
                width,
                height,
                matrix,
                true
            )

            // 이미지 잘라내 저장하기
            val matrix2 = Matrix()
            matrix2.postRotate(0f)

            bitmap = Bitmap.createBitmap(
                bitmap,
                bitmap.width/2-400,
                bitmap.height/2-400,
                800,
                800,
                matrix2,
                true
            )

            // bitmap을 byte array로 변환
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val currentData = stream.toByteArray()

            // 파일로 저장
            val outStream = FileOutputStream(photoFileS)
            outStream.write(currentData)
//            outStream.flush()
            outStream.close()
            // endregion

            setExifData(photoFile, lux)
            // region 파일 데이터 불러오기
            val exif = ExifInterface(photoFile?.absolutePath)
            // Now you can extract any Exif tag you want
            // Assuming the image is a JPEG or supported raw format

            val exifData = getExifData(exif)

            saveFileData += exifData
            // endregion

            saveFileData += imageData
            Dlog.i("결과 : saveFileData = $saveFileData")

            val state = Environment.getExternalStorageState()
            if (Environment.MEDIA_MOUNTED == state) {
                if (checkPermission()) {
                    val sdcard: File = Environment.getExternalStorageDirectory()
                    val dir = File(sdcard.absolutePath + "/DetectData/")
                    if (!dir.exists())
                        dir.mkdir()

                    val file = File(dir, "$fileName.txt")
                    var os: FileOutputStream? = null
                    try {
                        os = FileOutputStream(file)
                        os.write(saveFileData?.toByteArray())
                        os.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    showToast("저장되었습니다.")

                    setResult(RESULT_OK, Intent().apply {
                        putExtra(Const.INTENT.EXTRA_RESULT, 1)
                    })
                    finish()
                } else {

                }
            }
        } ?: run {
            showToast("데이터가 없습니다. 다시 시도해 주세요.")
        }
    }

    private fun setExifData(photo: File?, lux: String) {
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(photo?.canonicalPath)
            Dlog.i("결과 : UserComment = ${exif.getAttribute(ExifInterface.TAG_USER_COMMENT)}, Test = ${exif.getAttribute(ExifInterface.TAG_BRIGHTNESS_VALUE)}")

//            val lux = controlBinding.controlTv.text.toString().split("LUX :")[1].split("iso-values : ")[0].split(".")[0]+"/1"
            Dlog.i("결과 : lux = $lux")

            exif.setAttribute(ExifInterface.TAG_BRIGHTNESS_VALUE, lux)
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, "2019-06-03")
            exif.saveAttributes()

            val UserComment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT)
            val Test = exif.getAttribute(ExifInterface.TAG_BRIGHTNESS_VALUE)
            Dlog.i("결과 : UserComment = $UserComment, Test = $Test")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getExifData(exif: ExifInterface): String = "\nISO = ${exif.getAttribute(ExifInterface.TAG_ISO)}" +
            "\nAPERTURE_VALUE = ${exif.getAttribute(ExifInterface.TAG_APERTURE_VALUE)}" +
            "\nAPERTURE = ${exif.getAttribute(ExifInterface.TAG_APERTURE)}" +
            "\nSHUTTER_SPEED_VALUE = ${exif.getAttribute(ExifInterface.TAG_SHUTTER_SPEED_VALUE)}" +
            "\nLUX = ${exif.getAttribute(ExifInterface.TAG_BRIGHTNESS_VALUE)}\n\n"
}