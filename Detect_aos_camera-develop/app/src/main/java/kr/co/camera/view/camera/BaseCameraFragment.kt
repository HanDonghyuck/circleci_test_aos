package kr.co.camera.view.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.camera2.*
import android.hardware.camera2.params.MeteringRectangle
import android.os.*
import android.util.DisplayMetrics
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kr.co.camera.R
import kr.co.camera.base.BaseFragment
import kr.co.camera.databinding.FragmentCameraBinding
import kr.co.camera.etc.Const
import kr.co.camera.etc.Dlog
import kr.co.camera.etc.extension.getColorById
import kr.co.camera.etc.extension.showToast
import kr.co.camera.etc.extension.showToastLong
import kr.co.camera.view.SearchModeActivity
import kr.co.camera.view.dialog.UtilDialog
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.io.File
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

abstract class BaseCameraFragment : BaseFragment<FragmentCameraBinding>(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    override val layoutRes: Int = R.layout.fragment_camera

    companion object {

        /** Conversion from screen rotation to JPEG orientation. */
        val ORIENTATIONS = SparseIntArray()
        const val FRAGMENT_DIALOG = "dialog"

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        const val STATE_PREVIEW = 0                     // Camera state: Showing camera preview.
        const val STATE_WAITING_LOCK = 1                // Camera state: Waiting for the focus to be locked.
        const val STATE_WAITING_PRECAPTURE =
            2          // Camera state: Waiting for the exposure to be precapture state.
        const val STATE_WAITING_NON_PRECAPTURE =
            3      // Camera state: Waiting for the exposure state to be something other than precapture.
        const val STATE_PICTURE_TAKEN = 4               // Camera state: Picture was taken.
        const val MAX_PREVIEW_WIDTH = 1920              // Max preview width that is guaranteed by Camera2 API
        const val MAX_PREVIEW_HEIGHT = 1080             // Max preview height that is guaranteed by Camera2 API

        /**
         * Given `choices` of `Size`s supported by a camera, choose the smallest one that
         * is at least as large as the respective texture view size, and that is at most as large as
         * the respective max size, and whose aspect ratio matches with the specified value. If such
         * size doesn't exist, choose the largest one that is at most as large as the respective max
         * size, and whose aspect ratio matches with the specified value.
         *
         * @param choices           The list of sizes that the camera supports for the intended
         *                          output class
         * @param textureViewWidth  The width of the texture view relative to sensor coordinate
         * @param textureViewHeight The height of the texture view relative to sensor coordinate
         * @param maxWidth          The maximum width that can be chosen
         * @param maxHeight         The maximum height that can be chosen
         * @param aspectRatio       The aspect ratio
         * @return The optimal `Size`, or an arbitrary one if none were big enough
         */
        @JvmStatic
        fun chooseOptimalSize(
            choices: Array<Size>,
            fittedWidth: Int,
            fittedHeight: Int,
            aspectRatio: Size
        ): Size {
            val widthEnough = ArrayList<Size>()
            val notEnough = ArrayList<Size>()

            val w = aspectRatio.width
            val h = aspectRatio.height

            val searchWidth = Math.min(w, fittedWidth)
            val searchHeight = Math.min(h, fittedHeight)

            for (option in choices) {
                if (option.height == searchHeight && option.width == searchWidth) {
                    return option
                } else if (option.height == searchHeight) {
                    widthEnough.add(option)
                } else if (option.height >= searchHeight) {
                    notEnough.add(option)
                }
            }
            var optSize: Size? = null
            var minRatio = 1.0f
            for (option in widthEnough) {
                val ratio = w / h.toFloat() - option.width / option.height.toFloat()
                if (ratio <= minRatio) {
                    minRatio = ratio
                    optSize = option
                }
            }
            if (optSize == null) {
                minRatio = 1.0f
                for (option in notEnough) {
                    val ratio = w / h.toFloat() - option.width / option.height.toFloat()
                    if (ratio <= minRatio) {
                        minRatio = ratio
                        optSize = option
                    }
                }
            }
            if (optSize == null) {
                optSize = choices[0]
            }
            return optSize
        }
    }

    // region 기존 변수
    /** ID of the current [CameraDevice]. */
    protected lateinit var mCameraId: String

    /** An [AutoFitTextureView] for camera preview. */
    protected lateinit var textureView: AutoFitTextureView

    /** A [CameraCaptureSession] for camera preview. */
    protected var mCaptureSession: CameraCaptureSession? = null

    /** A reference to the opened [CameraDevice]. */
    protected var mCameraDevice: CameraDevice? = null

    /** An additional thread for running tasks that shouldn't block the UI. */
    private var mBackgroundThread: HandlerThread? = null

    /** A [Handler] for running tasks in the background. */
    protected var mBackgroundHandler: Handler? = null

    /** This is the output file for our picture. */
    protected lateinit var mFile: File

    /** [CaptureRequest.Builder] for the camera preview */
    protected lateinit var mPreviewRequestBuilder: CaptureRequest.Builder

    /** The current state of camera state for taking pictures. */
    protected var state = STATE_PREVIEW

    /** A [Semaphore] to prevent the app from exiting before closing the camera. */
    private val cameraOpenCloseLock = Semaphore(1)
    // endregion

    // region callback and listener
    private val mLoaderCallback = object : BaseLoaderCallback(context) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Dlog.i("OpenCV loaded successfully")
                    matVerify = Mat()
                    contourImage = Mat()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    /** [CameraDevice.StateCallback] is called when [CameraDevice] changes its state. */
    private val mStateCallback = object : CameraDevice.StateCallback() {

        // [찰스님 블로그] 카메라가 열리고 나면 onOpend(cameraDevice)가 호출됩니다.
        // 카메라가 정상적으로 열렸으므로 이제 프리뷰세션을 만들어 보도록 하겠습니다.
        override fun onOpened(cameraDevice: CameraDevice) {
            Dlog.w("mStateCallback onOpened")
            cameraOpenCloseLock.release()
            mCameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            Dlog.w("mStateCallback onDisconnected")
            cameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            Dlog.w("mStateCallback onError")
            onDisconnected(cameraDevice)
            activity?.finish()
        }
    }

    /** A [CameraCaptureSession.CaptureCallback] that handles events related to JPEG capture. */
    abstract val mCaptureCallback: CameraCaptureSession.CaptureCallback

    /**
     * [찰스님 블로그] SurfaceTexture 는 OpenGL ES 텍스쳐로써 이미지 스트림으로부터 프레임을 캡쳐.
     * [TextureView.SurfaceTextureListener] handles several lifecycle events on a [TextureView].
     */
    abstract val mSurfaceTextureListener: TextureView.SurfaceTextureListener
    // endregion

    // region 추가 변수
    protected lateinit var manager: CameraManager
    protected val cameraActivity: Activity by lazy { activity as Camera2Activity }
    private val currentTime: Calendar by lazy { Calendar.getInstance() }       // 파일 저장과 전송에 필요한 부분

    private var contourImage: Mat? = null
    private var matVerify: Mat? = null

    protected var startTime: Long = 0L

    protected var currentDate: String? = null

    private var isManualFocusEngaged: Boolean = false

    private var hdHideNoti: Handler? = null

    private var bmpRotate: Bitmap? = null       // 원본 이미지

    private val mDialog: UtilDialog by lazy { UtilDialog() }

    private var mStartTime: Long = 0
    private var hdCountDown: Handler? = null
    private var isActive: Boolean = true
    // endregion

    // region life cycle 생명주기
    abstract fun setPetInfo(arguments: Bundle?)

    override fun setView(view: View?, savedInstanceState: Bundle?, arguments: Bundle?) {
        Dlog.i("setView")
        textureView = getBinding().texture

        getBinding().includeToolbar.toolbar.setBackgroundColor(context!!.getColorById(android.R.color.black))
        getBinding().includeToolbar.toolbarTvTitle.text = "촬영 모드"
        getBinding().includeToolbar.toolbarLl.visibility = View.VISIBLE
        getBinding().includeToolbar.toolbarLl.setOnClickListener { activity?.finish() }

        setPetInfo(arguments)

        initView()

        getBinding().fragment = this

        setManualFocus(view)
    }

    override fun onResume() {
        super.onResume()
        Dlog.i("onResume")
        isActive = true

        if (!OpenCVLoader.initDebug()) OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, context, mLoaderCallback)
        else mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)

        startBackgroundThread()

        if (textureView.isAvailable) openCamera(textureView.width, textureView.height)
        else textureView.surfaceTextureListener = mSurfaceTextureListener
    }

    // 카메라 장치는 싱글톤 인스턴스이므로 사용후 반드시 시스템에 반환하여 다른 프로세스(앱)가 이용할 수 있도록 합니다.
    override fun onPause() {
        super.onPause()
        Dlog.i("onPause")

        isActive = false

        setOnPause(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        Dlog.i("onDestroy")

        if (hdCountDown != null) {
            hdCountDown?.removeMessages(0)
            hdCountDown = null
        }

        if (hdHideNoti != null) {
            hdHideNoti?.removeMessages(1000)
            hdHideNoti = null
        }

        closeCamera()
    }
    // endregion

    // 권한
    private fun requestCameraPermission() {
        val isPermissionCamera = shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
        if (isPermissionCamera) ConfirmationDialog().show(childFragmentManager, FRAGMENT_DIALOG)
        else requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                    .show(childFragmentManager, FRAGMENT_DIALOG)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * Sets up member variables related to camera.
     *
     * [찰스님 블로그] - setUpCameraOutputs()가 하는일
     * 후면 카메라 선택
     * 캡쳐된 사진(이미지리더)의 해상도, 포맷 선택
     * 이미지의 방향
     * 적합한 프리뷰 사이즈 선택
     * 들어오는 영상의 비율에 맞춰 TextureView의 비율 변경(이 부분은 예제에 포함된 AutoFitTextureView 커스텀 뷰입니다)
     * 플래시 지원 여부
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    abstract fun setUpCameraOutputs(width: Int, height: Int)

    /**
     * Opens the camera specified by [CameraFragment.mCameraId].
     *
     * [찰스님 블로그]
     * TextureView의 사이즈를 입력받아 여러가지작업들을 수행하게 됩니다.
     * 첫번째로 카메라 런타임 퍼미션이 있는지 확인하게 되고, 퍼미션을 얻었다면 setUpCameraOutputs()를 수행하게 됩니다.
     */
    protected fun openCamera(width: Int, height: Int) {
        val permission = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
            return
        }
        setUpCameraOutputs(width, height)
        try {
            // Wait for camera to open - 2.5 seconds is sufficient
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }

    }

    /** Closes the current [CameraDevice]. */
    private fun closeCamera() {
        Dlog.i("closeCamera")

        try {
            cameraOpenCloseLock.acquire()
            if (mCaptureSession != null) {
                mCaptureSession?.close()
                mCaptureSession = null
            }
            if (mCameraDevice != null) {
                mCameraDevice?.close()
                mCameraDevice = null
            }

            closeImageReader()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    open fun closeImageReader() {}

    /** Creates a new [CameraCaptureSession] for camera preview. */
    abstract fun createCameraPreviewSession()

    /** Starts a background thread and its [Handler]. */
    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground").also { it.start() }
        mBackgroundHandler = Handler(mBackgroundThread?.looper)
    }

    /** Stops the background thread and its [Handler]. */
    private fun stopBackgroundThread() {
        Dlog.i("stopBackgroundThread")
        mBackgroundThread?.quitSafely()
        try {
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    // region 추가 fun
    private fun initView() {
        val metrics = DisplayMetrics()
        cameraActivity.windowManager?.defaultDisplay?.getMetrics(metrics)
    }

    private fun setManualFocus(view: View?) {
        view?.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(touchView: View?, motionEvent: MotionEvent?): Boolean {
                if (touchView == null) {
                    return false
                }

                motionEvent?.let { motionEvent ->
                    val actionMasked = motionEvent.actionMasked
                    if (actionMasked != MotionEvent.ACTION_DOWN) {
                        return false
                    }
                    if (isManualFocusEngaged) {
                        Dlog.d("Manual focus already engaged")
                        return true
                    }

//                    mPreviewRequestBuilder?.let {
                    val characteristics = manager.getCameraCharacteristics(mCameraId)
                    val sensorArraySize =
                        characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)

                    val y =
                        ((motionEvent.x / touchView.width.toFloat()) * sensorArraySize.height().toFloat()).toInt()
                    val x =
                        ((motionEvent.y / touchView.height.toFloat()) * sensorArraySize.width().toFloat()).toInt()
                    val halfTouchWidth = motionEvent.touchMajor.toInt()
                    val halfTouchHeight = motionEvent.touchMinor.toInt()

                    val focusAreaTouch = MeteringRectangle(
                        Math.max(x - halfTouchWidth, 0),
                        Math.max(y - halfTouchHeight, 0),
                        halfTouchWidth * 2,
                        halfTouchHeight * 2,
                        MeteringRectangle.METERING_WEIGHT_MAX - 1
                    )

                    val captureCallbackHandler = object : CameraCaptureSession.CaptureCallback() {
                        override fun onCaptureCompleted(
                            session: CameraCaptureSession,
                            request: CaptureRequest,
                            result: TotalCaptureResult
                        ) {
                            super.onCaptureCompleted(session, request, result)
                            isManualFocusEngaged = false

                            if (request.tag == "FOCUS_TAG") {
                                //the focus trigger is complete -
                                //resume repeating (preview surface will get frames), clear AF trigger
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null)
                                mCaptureSession?.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null)
                            }
                        }

                        override fun onCaptureFailed(
                            session: CameraCaptureSession,
                            request: CaptureRequest,
                            failure: CaptureFailure
                        ) {
                            super.onCaptureFailed(session, request, failure)
                            Dlog.e("Manual AF failure: $failure")
                            isManualFocusEngaged = false
                        }
                    }

                    //first stop the existing repeating request
                    mCaptureSession?.stopRepeating()

                    //cancel any existing AF trigger (repeated touches, etc.)
                    mPreviewRequestBuilder.set(
                        CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
                    )
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                    mCaptureSession?.capture(mPreviewRequestBuilder.build(), captureCallbackHandler, mBackgroundHandler)

                    //Now add a new AF trigger with focus region
                    if (isMeteringAreaAFSupported()) {
                        mPreviewRequestBuilder.set(
                            CaptureRequest.CONTROL_AF_REGIONS,
                            arrayOf(focusAreaTouch)
                        )
                    }
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
                    mPreviewRequestBuilder.set(
                        CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_START
                    )
                    mPreviewRequestBuilder.setTag("FOCUS_TAG") //we'll capture this later for resuming the preview

                    //then we ask for a single request (not repeating!)
                    mCaptureSession?.capture(mPreviewRequestBuilder.build(), captureCallbackHandler, mBackgroundHandler)
                    isManualFocusEngaged = true
//                    }
                }
                return true
            }
        })
    }

    private fun isMeteringAreaAFSupported(): Boolean {
        val characteristics = manager.getCameraCharacteristics(mCameraId)
        return characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) >= 1
    }

    private fun setOnPause(isStopBackground: Boolean) {
        closeCamera()
        if (isStopBackground) stopBackgroundThread()
    }

    // bitmap resize and set bmpRotate
    protected fun getByteArray(bitmap: Bitmap) {
        var resultBitmap = bitmap
        val mat = Matrix()
        mat.postRotate(0f)

        resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, mat, true)

        val resizeBitmap = Bitmap.createBitmap(
            bitmap,
            resultBitmap.width / 2 - 400,
            resultBitmap.height / 2 - 400,
            800,
            800,
            mat,
            true
        )

        Const.IMAGE_BITMAP = resizeBitmap
    }

    fun onClickTakePicture() {
        startActivity(Intent(context, SearchModeActivity::class.java))
    }

    fun onClickFocus(view: View) {
        setManualFocus(view)
    }
    // endregion
}