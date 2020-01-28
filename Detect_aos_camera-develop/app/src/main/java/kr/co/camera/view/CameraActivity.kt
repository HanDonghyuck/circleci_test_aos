package kr.co.camera.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.hardware.*
import android.hardware.Camera
import android.media.ExifInterface
import android.os.*
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import kr.co.camera.base.BaseActivity
import kr.co.camera.databinding.ActivityCameraBinding
import kr.co.camera.databinding.ControlBinding
import kr.co.camera.etc.*
import kr.co.camera.etc.extension.getDrawableById
import kr.co.camera.etc.extension.showToast
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class CameraActivity : BaseActivity<ActivityCameraBinding>(),
    SurfaceHolder.Callback {

    override fun getContentView(): Int = kr.co.camera.R.layout.activity_camera

    companion object {
        private const val REQUEST_CODE = 100
        private const val MESSAGE_TIMER_START: Int = 102

        init {
            System.loadLibrary("native-lib")
        }

        /** 안드로이드 디바이스 방향에 맞는 카메라 프리뷰를 화면에 보여주기 위해 계산합니다. */
        fun calculatePreviewOrientation(info: Camera.CameraInfo?, rotation: Int): Int {
            var degrees = 0

            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }

            var result: Int
            if (info!!.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360
                result = (360 - result) % 360  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360
            }

            return result
        }
    }

    // region 변수
    external fun imageEntropy(a: Long): Float
    external fun mctCountBinary(a: Long): FloatArray
    external fun detectStep2(a: Long, b: Long, score: Int): Int

    private var mCamera: Camera? = null
    private var prPreviewSize: Camera.Size? = null
    private var mCameraInfo: Camera.CameraInfo? = null
    private var prSupportedPreviewSizes: List<Camera.Size>? = null
    private val mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK        // 최초 카메라 상태는 후면카메라로 설정
    private var mDisplayOrientation: Int = 0
    private var mSurfaceHolder: SurfaceHolder? = null
    //    private var mSurfaceHolderSecond: SurfaceHolder? = null
    private var surfaceCreatedOrientation: Int = 0

    private var cameraIndex: Int = 0

    private var petName: String? = null
    private var positionKind: Int = 0
    private var positionHead: Int = 0

    private var stringData: String = ""
    private var stringGyro: String = ""
    private var stringLux: String = ""
    private var stringIos: String? = null
    private var currentTime: String? = null
    private var saveFileData: String = ""

    private var entropyAndMctHandler: TimerHandler? = null
    private var gyroHandler: Handler? = null
    private var luxHandler: Handler? = null
    private var isFocus: Boolean = false

    private val myAutoFocusCallback = Camera.AutoFocusCallback { success, _ ->
        controlBinding.controlIvTakePicture.isEnabled = success
        isFocus = success
    }

    private val myPictureCallback_JPG = Camera.PictureCallback { data, camera ->
        val photoFile: File = getOutputMediaFile() ?: return@PictureCallback
        val photoFileS: File = getOutputMediaFileSmall() ?: return@PictureCallback
        try {

            Const.ORIGIN_FILE = photoFile
            Const.SMALL_FILE = photoFileS
            Const.PICTURE_BYTE_ARRAY = data

            // region 저장
//            // 본 이미지 저장
//            val fos = FileOutputStream(photoFile)
//            fos.write(data)
//            fos.close()

            // 작게 저장
            // 이미지의 너비와 높이 결정
            val width = camera.parameters.pictureSize.width
            val height = camera.parameters.pictureSize.height
            val orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation)

            // byte array를 bitmap으로 변환
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            var bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options)

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
                bitmap.width / 2 - 400,
                bitmap.height / 2 - 400,
                800,
                800,
                matrix2,
                true
            )

            Const.IMAGE_BITMAP = bitmap
//            // bitmap을 byte array로 변환
//            val stream = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//            val currentData = stream.toByteArray()
//
//            // 파일로 저장
//            val outStream = FileOutputStream(photoFileS)
//            outStream.write(currentData)
////            outStream.flush()
//            outStream.close()
//            // endregion
//
//            setExifData(photoFile)
//            // region 파일 데이터 불러오기
//            val exif = ExifInterface(photoFile.absolutePath)
//            // Now you can extract any Exif tag you want
//            // Assuming the image is a JPEG or supported raw format
//
//            val exifData = getExifData(exif)

//            saveFileData += exifData
//            Dlog.i("결과 : exifData = $exifData")
//            // endregion
//
////            saveTextFile()
//            saveFileData += controlBinding.controlTv.text.toString().split("초")[1].split("iso-values : ")[0]
//            Dlog.i("결과 : saveFileData = $saveFileData")

//            val mat = Mat()
//            Utils.bitmapToMat(bitmap, mat)
//
//            val mat2 = Mat()
//            Utils.bitmapToMat(bitmap, mat2)
//
//            var score = -1
//            score = detectStep2(mat.nativeObj, mat2.nativeObj, score)
//            Dlog.i("결과 : score = $score")

            val fileName = "${petName}_${cameraIndex}_${currentTime}_${stringLux.split(":")[1].trim()}_${Build.MODEL}"
            val imageData = controlBinding.controlTv.text.toString().split("초")[1].split("iso-values : ")[0]
            val lux = controlBinding.controlTv.text.toString().split("LUX :")[1].split("iso-values : ")[0].split(".")[0] + "/1"

            saveTextFile(orientation, width, height, lux, imageData, fileName)

//            startActivityForResult(Intent(this, ResultActivity::class.java).apply {
//                putExtra(Const.INTENT.EXTRA_IMAGE_FILE_DATA, saveFileData)
//                putExtra(Const.INTENT.EXTRA_IMAGE_FILE_NAME, fileName)
//                putExtra(Const.INTENT.EXTRA_IMAGE_DATA, imageData)
//                putExtra(Const.INTENT.EXTRA_LUX, lux)
//                putExtra(Const.INTENT.EXTRA_WIDTH, width)
//                putExtra(Const.INTENT.EXTRA_HEIGHT, height)
//                putExtra(Const.INTENT.EXTRA_ORIENTATION, orientation)
//            }, REQUEST_CODE)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //Using the Lux
    private var sensorManagerLight: SensorManager? = null
    private var sensorLightListener: SensorEventListener? = null
    private var sensorLight: Sensor? = null

    //Using the Accelometer & Gyroscoper
    private var mSensorManager: SensorManager? = null
    //Using the Gyroscope
    private var mGyroLis: SensorEventListener? = null
    private var mGyroSensor: Sensor? = null

    //Roll and Pitch
    private var roll: Double = 0.0     // x
    private var pitch: Double = 0.0    // y
    private var yaw: Double = 0.0      // z

    // 단위 시간을 구하기 위한 변수
    private var timestamp: Double = 0.toDouble()
    private var dt: Double = 0.toDouble()

    // 회전각을 구하기 위한 변수
    private var RAD2DGR = 180 / Math.PI
    private var NS2S = 1.0f / 1000000000.0f

    private var countOfPictures = 0
    // endregion

    private lateinit var controlBinding: ControlBinding

    override fun initView() {
        window.setFormat(PixelFormat.UNKNOWN)

        petName = intent.getStringExtra(Const.INTENT.EXTRA_PET_NAME)
//        calledUser = intent.getStringExtra(Const.INTENT.EXTRA_CALLED_USER)
        positionHead = intent.getIntExtra(Const.INTENT.EXTRA_POSITION_HEAD, 0)

        getBinding().activity = this
        getBinding().includeDk.activity = this
//        getBinding().includeJu.activity = this

        getBinding().cameraIvWhiteLine.bringToFront()
        getBinding().includeDk.cameraViewBackground.bringToFront()
        getBinding().cameraTvCountOfPicture.text = "$countOfPictures/3"

        Dlog.i("결과 : positionHead = $positionHead")
        if (positionHead == 1) {
            getBinding().includeDk.cameraIvShortHead.visibility = View.VISIBLE
            getBinding().includeDk.cameraIvLongHead.visibility = View.GONE
        } else {
            getBinding().includeDk.cameraIvShortHead.visibility = View.GONE
            getBinding().includeDk.cameraIvLongHead.visibility = View.VISIBLE
        }
//        getBinding().includeDk.cameraIvHead.background =
//            if (positionHead == 1) getDrawableById(R.drawable.dog_01_guide) else getDrawableById(R.drawable.dog_02_guide)

        getBinding().cameraClPreview.visibility = View.GONE
        getBinding().cameraViewPreview.visibility = View.VISIBLE
        getBinding().cameraSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getBinding().cameraClPreview.visibility = View.GONE
                getBinding().cameraViewPreview.visibility = View.VISIBLE
            } else {
                getBinding().cameraClPreview.visibility = View.VISIBLE
                getBinding().cameraViewPreview.visibility = View.INVISIBLE
            }
        }

//        getBinding().includeDk.cameraViewLeft.visibility = View.VISIBLE
//        getBinding().includeDk.cameraViewRight.visibility = View.VISIBLE

        val controlInflater = LayoutInflater.from(baseContext)
        controlBinding = DataBindingUtil.inflate(controlInflater, kr.co.camera.R.layout.control, null, false)
        val layoutParamsControl =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        this.addContentView(controlBinding.root, layoutParamsControl)
        controlBinding.activity = this
//        controlBinding.controlTv.visibility = if (GlobalApplication.DEBUG) View.VISIBLE else View.GONE
        controlBinding.controlTv.visibility = View.GONE

        mSurfaceHolder = getBinding().includeDk.cameraSv.holder?.apply {
            addCallback(this@CameraActivity)
            setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }

        settingGyroSensor()
        settingLuxSensor()

        val nr = NewRunnable()
        val mThread = Thread(nr)
        mThread.start()
    }

    fun onClickTakePicture() {
        val lux = stringLux.split(":")[1].trim()
//        if(lux.toFloat() >= 8000f)
            if(isFocus) mCamera?.takePicture(null, null, myPictureCallback_JPG)
            else showToast("포커스를 맞추세요.")
//        else showToast("광량을 맞추세요.")
    }

    fun onClickFocus() {
        controlBinding.controlIvTakePicture.isEnabled = false
        mCamera?.autoFocus(myAutoFocusCallback)
    }

    // region lifecycle
    override fun onResume() {
        super.onResume()
        Dlog.i("onResume")

        mSensorManager?.registerListener(mGyroLis, mGyroSensor, SensorManager.SENSOR_DELAY_UI)
        sensorManagerLight?.registerListener(sensorLightListener, sensorLight, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        Dlog.i("onPause")
        mSensorManager?.unregisterListener(mGyroLis)
        sensorManagerLight?.unregisterListener(sensorLightListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        Dlog.i("onDestroy")

        if (entropyAndMctHandler != null) {
            entropyAndMctHandler?.removeMessages(MESSAGE_TIMER_START)
            entropyAndMctHandler = null
        }

        if (gyroHandler != null) {
            gyroHandler?.removeMessages(MESSAGE_TIMER_START)
            gyroHandler = null
        }

        if (luxHandler != null) {
            luxHandler?.removeMessages(MESSAGE_TIMER_START)
            luxHandler = null
        }
    }
    // endregion

    // Surface가 생성되었을 때 어디에 화면에 프리뷰를 출력할지 알려줘야 한다.
    override fun surfaceCreated(holder: SurfaceHolder?) {
        // Surface가 생성되었으니 프리뷰를 어디에 띄울지 지정해준다. (holder 로 받은 SurfaceHolder에 뿌려준다.
        try {
            mCamera = Camera.open(mCameraID) // attempt to get a Camera instance

            // retrieve camera's info.
            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(mCameraID, cameraInfo)

            mCameraInfo = cameraInfo
            mDisplayOrientation = windowManager.defaultDisplay.rotation

            // 방향 잡기
            surfaceCreatedOrientation = calculatePreviewOrientation(
                mCameraInfo,
                mDisplayOrientation
            )



            mCamera?.run {
                setDisplayOrientation(surfaceCreatedOrientation)
                prSupportedPreviewSizes = this.parameters.supportedPreviewSizes

                val parameters = this.parameters
                if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    parameters?.set("orientation", "portrait")
                    this.setDisplayOrientation(90)
                    parameters?.setRotation(90)
                } else {
                    parameters?.set("orientation", "landscape")
                    this.setDisplayOrientation(0)
                    parameters?.setRotation(0)
                }

                this.parameters = parameters

                setPreviewDisplay(mSurfaceHolder)
                startPreview()
            }
        } catch (e: IOException) {
            Dlog.e("Error setting camera preview: " + e.message)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, w: Int, h: Int) {
        Dlog.i("결과 surfaceChanged width = $w, height = $h")
        // 프리뷰를 회전시키거나 변경시 처리를 여기서 해준다. 프리뷰 변경시에는 먼저 프리뷰를 멈춘다음 변경해야한다.
        if (holder?.surface == null) {
            // 프리뷰가 존재하지 않을때
            return
        }

        // 우선 멈춘다
        try {
            mCamera?.stopPreview()
        } catch (e: Exception) {
            // 프리뷰가 존재조차 하지 않는 경우다
        }

        // 프리뷰 변경, 처리 등을 여기서 해준다.
        val parameters = mCamera?.parameters
        val focusModes = parameters?.supportedFocusModes
        focusModes?.let {
            if (it.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            }
        }

        setCameraPreview()

        entropyAndMctHandler = TimerHandler()
        entropyAndMctHandler?.sendEmptyMessage(MESSAGE_TIMER_START)

        val size = getBestPreviewSize(w, h)
        parameters?.setPreviewSize(size!!.width, size.height)
        // 새로 변경된 설정으로 프리뷰를 재생성한다
        try {
            mCamera?.apply {
                this.parameters = parameters
                setPreviewDisplay(mSurfaceHolder)
//                setPreviewDisplay(mSurfaceHolderSecond)
                startPreview()
            }
        } catch (e: Exception) {
            Dlog.e("Error starting camera preview: " + e.message)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Dlog.i("결과 : surfaceDestroyed ")
        // 프리뷰 제거시 카메라 사용도 끝났다고 간주하여 리소스를 전부 반환한다
        if (mCamera != null) {
            mCamera?.apply {
                stopPreview()
                setPreviewCallback(null)
                release()
            }
            mCamera = null
        }
    }

    private fun setCameraPreview() {
        mCamera?.setPreviewCallback { data, camera ->
            val params = mCamera!!.parameters
            val width = params.previewSize.width
            val height = params.previewSize.height
            val preViewFormat = params.previewFormat
            val image = YuvImage(data, preViewFormat, width, height, null)

            val out = ByteArrayOutputStream()
            val area = Rect(0, 0, width, height)
            image.compressToJpeg(area, 50, out)

            val captureImg = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())

            // region 이미지를 디바이스 방향으로 회전
            val matrix = Matrix()
            matrix.postRotate(surfaceCreatedOrientation.toFloat())
            val bitmap = Bitmap.createBitmap(
                captureImg,
                0, 0,
                width,
                height,
                matrix,
                true
            )
            // endregion

            // region 확대경
            // 전체 중 큰 빨간 사각형 안에 있는 것만 잘라내기
            val redRect = Bitmap.createBitmap(bitmap, bitmap.width / 2 - 102, bitmap.height / 2 - 102, 204, 204)
            getBinding().cameraIv.setImageBitmap(redRect)
            // endregion

            getCameraData(bitmap, camera)
        }
    }

    private fun getCameraData(bitmap: Bitmap, camera: Camera) {
        var floatArrayMct: FloatArray = floatArrayOf()
        var entropy: Float = 0.0f
        thread(name = "backprocess") {
            // region currentData -> cvmat 으로 바꾸고, 바꾼걸 allowc, mct
            // 전체 중 큰 초록 영역만 잘라내기
            val roi = Bitmap.createBitmap(bitmap, bitmap.width / 2 - 27, bitmap.height / 2 - 61, 54, 108)

            val matLong = Mat()
            Utils.bitmapToMat(roi, matLong)
            entropy = imageEntropy(matLong.nativeObjAddr)
            floatArrayMct = mctCountBinary(matLong.nativeObjAddr)
            // endregion

            stringData = ""
            stringData += "imageEntropy : $entropy\n"
            for (i in floatArrayMct.indices) {
                stringData += "mctCountBinary[$i] : ${floatArrayMct[i]}\n"
            }

            // region iso 가져오기
            val flat = camera.parameters.flatten()
            var isoValues: Array<String>? = null
            var values_keyword: String = ""
            var iso_keyword: String? = null// iso not supported in a known way

            // flatten contains the iso key!!
            when {
                flat.contains("iso-values") -> {
                    // most used keywords
                    values_keyword = "iso-values"
                    iso_keyword = "iso"
                }
                flat.contains("iso-mode-values") -> {
                    // google galaxy nexus keywords
                    values_keyword = "iso-mode-values"
                    iso_keyword = "iso"
                }
                flat.contains("iso-speed-values") -> {
                    // micromax a101 keywords
                    values_keyword = "iso-speed-values"
                    iso_keyword = "iso-speed"
                }
                flat.contains("nv-picture-iso-values") -> {
                    // LG dual p990 keywords
                    values_keyword = "nv-picture-iso-values"
                    iso_keyword = "nv-picture-iso"
                }

                // add other eventual keywords here...
            }

            // add other eventual keywords here...
            if (iso_keyword != null) {
                // flatten contains the iso key!!
                var iso = flat.substring(flat.indexOf(values_keyword))
                iso = iso.substring(iso.indexOf("=") + 1)

                if (iso.contains(";")) iso = iso.substring(0, iso.indexOf(";"))

                isoValues = iso.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            } else {
                // iso not supported in a known way
            }

            stringIos = "iso-values : "

            isoValues?.let {
                for (i in it.indices) {
                    stringIos += it[i] + " "
                }
            }
            // endregion
        }
    }

    // region 자이로센서 설정
    private fun settingGyroSensor() {
        // 센서 매니저 설정
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // 자이로스코프 센서를 사용하겠다고 등록
        mGyroSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        mGyroLis = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {        // 센서의 정확도가 변하는 이벤트 감지

            }

            override fun onSensorChanged(event: SensorEvent?) {     // 센서의 값이 변하는 이벤트를 감지
                event?.let {
                    val gyroX = it.values[0]
                    val gyroY = it.values[1]
                    val gyroZ = it.values[2]

                    // 단위 시간 계산
                    dt = (it.timestamp - timestamp) * NS2S
                    timestamp = it.timestamp.toDouble()

                    // 시간이 변화했으면
                    gyroHandler = object : Handler() {
                        override fun handleMessage(msg: Message?) {
                            if ((dt - (timestamp * NS2S)) != 0.0) {
                                pitch += gyroY * dt
                                roll += gyroX * dt
                                yaw += gyroZ * dt

                                stringGyro = ""
                                stringGyro += "roll(x) = ${String.format(
                                    "%.1f",
                                    roll * RAD2DGR
                                )}\npitch(y) = ${String.format(
                                    "%.1f",
                                    pitch * RAD2DGR
                                )}\nyaw(z) = ${String.format("%.1f", yaw * RAD2DGR)}\n"
                            }
                        }
                    }
                }
            }
        }
    }
    // endregion

    // region lux 설정
    private fun settingLuxSensor() {
        sensorManagerLight = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorLight = sensorManagerLight?.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (sensorLight == null)
            Toast.makeText(this, "센서 없음...", Toast.LENGTH_SHORT).show()

        sensorLightListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (event.sensor.type == Sensor.TYPE_LIGHT) {
                        luxHandler = @SuppressLint("HandlerLeak")
                        object : Handler() {
                            override fun handleMessage(msg: Message?) {
                                stringLux = "LUX : ${it.values[0]}\n"

                                when {
                                    it.values[0] >= 8000f -> {
                                        getBinding().cameraTv.text = "딱 좋은 광량이네요."
                                        controlBinding.controlIvTakePicture.background = getDrawableById(kr.co.camera.R.drawable.circle_background_gray)
                                        controlBinding.controlIvTakePictureMain.background = getDrawableById(kr.co.camera.R.drawable.green_camera_icon)
                                        getBinding().includeDk.cameraViewRect.background = getDrawableById(kr.co.camera.R.drawable.rect_green_2)
                                    }
                                    it.values[0] >= 5000f -> {
                                        getBinding().cameraTv.text = "점점 맞춰가고 있어요"
                                        controlBinding.controlIvTakePicture.background = getDrawableById(kr.co.camera.R.drawable.circle_background_gray)
                                        controlBinding.controlIvTakePictureMain.background = getDrawableById(kr.co.camera.R.drawable.camera_icon)
                                        getBinding().includeDk.cameraViewRect.background = getDrawableById(kr.co.camera.R.drawable.rect_green_1)
                                    }
                                    else -> {
                                        getBinding().cameraTv.text = "더 밝은 곳에서 촬영해주세요."
                                        controlBinding.controlIvTakePicture.background = getDrawableById(kr.co.camera.R.drawable.circle_background_gray)
                                        controlBinding.controlIvTakePictureMain.background = getDrawableById(kr.co.camera.R.drawable.camera_icon)
                                        getBinding().includeDk.cameraViewRect.background = getDrawableById(kr.co.camera.R.drawable.rect_red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // endregion

    private fun getBestPreviewSize(width: Int, height: Int): Camera.Size? {
        var result: Camera.Size? = null
        val p = mCamera!!.parameters
        for (size in p.supportedPreviewSizes) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size
                } else {
                    val resultArea = result.width * result.height
                    val newArea = size.width * size.height

                    if (newArea > resultArea) {
                        result = size
                    }
                }
            }
        }
        return result
    }

    private inner class NewRunnable : Runnable {
        override fun run() {
            while (true) {
                try {
                    Thread.sleep(1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                gyroHandler?.sendEmptyMessage(0)
                luxHandler?.sendEmptyMessage(0)
            }
        }
    }

    private inner class TimerHandler : Handler() {
        var count: Int = 0
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                MESSAGE_TIMER_START -> {
                    count++
                    val textViewData = "${count}초\n" + stringData + stringGyro + stringLux + stringIos
                    controlBinding.controlTv.text = textViewData
                    this.sendEmptyMessageDelayed(MESSAGE_TIMER_START, 1000)
                }
            }
        }
    }

    private fun getOutputMediaFile(): File? {
        val sdcard: File = Environment.getExternalStorageDirectory()
        val mediaStorageDir = File(sdcard.absolutePath + "/DetectData/")
//        val mediaStorageDir = File(sdcard.absolutePath + "/DCIM/Camera/")
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Dlog.i("failed to create directory")
                return null
            }
        }
        // Create a media file name
        currentTime = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        return File(mediaStorageDir.path + File.separator + "IMG_" + currentTime + ".png")
    }

    private fun getOutputMediaFileSmall(): File? {
        val sdcard: File = Environment.getExternalStorageDirectory()
        val mediaStorageDir: File = File(sdcard.absolutePath + "/DetectData/")
//        val mediaStorageDir: File = File(sdcard.absolutePath + "/DCIM/Camera/")
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Dlog.i("failed to create directory")
                return null
            }
        }
        // Create a media file name
        currentTime = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        return File(mediaStorageDir.path + File.separator + "IMG_" + currentTime + "_S.png")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)

        Dlog.i("결과 : requestCode = ${requestCode == REQUEST_CODE}, resultCode = ${resultCode == RESULT_OK}")
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val receiveData = data?.getIntExtra(Const.INTENT.EXTRA_RESULT, 0)
                countOfPictures += receiveData!!
                getBinding().cameraTvCountOfPicture.text = "$countOfPictures/3"
            } else {
                showToast("Failed")
            }
        }
    }




    private fun saveTextFile(orientation: Int, width: Int, height: Int, lux: String, imageData: String, fileName: String) {
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

//            setExifData(photoFile, lux)
//            // region 파일 데이터 불러오기
//            val exif = ExifInterface(photoFile?.absolutePath)
//            // Now you can extract any Exif tag you want
//            // Assuming the image is a JPEG or supported raw format
//
//            val exifData = getExifData(exif)
//
//            saveFileData += exifData
//            // endregion
//
//            saveFileData += imageData
//            Dlog.i("결과 : saveFileData = $saveFileData")
//
//            val state = Environment.getExternalStorageState()
//            if (Environment.MEDIA_MOUNTED == state) {
//                if (checkPermission()) {
//                    val sdcard: File = Environment.getExternalStorageDirectory()
//                    val dir = File(sdcard.absolutePath + "/DetectData/")
////                    val dir = File(sdcard.absolutePath + "/DCIM/Camera/")
//                    if (!dir.exists())
//                        dir.mkdir()
//
//                    val file = File(dir, "$fileName.txt")
//                    var os: FileOutputStream? = null
//                    try {
//                        os = FileOutputStream(file)
//                        os.write(saveFileData?.toByteArray())
//                        os.close()
//
//                        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
//
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                    showToast("저장되었습니다.")
//
//                    setResult(RESULT_OK, Intent().apply {
//                        putExtra(Const.INTENT.EXTRA_RESULT, 1)
//                    })
//                    finish()
//                } else {
//
//                }
//            }
        } ?: run {
            showToast("데이터가 없습니다. 다시 시도해 주세요.")
        }
    }

    private fun setExifData(photo: File?, lux: String) {
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(photo?.canonicalPath)
            Dlog.i("결과 : UserComment = ${exif.getAttribute(ExifInterface.TAG_USER_COMMENT)}, Test = ${exif.getAttribute(
                ExifInterface.TAG_BRIGHTNESS_VALUE)}")

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