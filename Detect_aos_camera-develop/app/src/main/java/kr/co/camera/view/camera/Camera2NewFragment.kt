package kr.co.camera.view.camera

import android.content.Context
import android.content.res.Configuration
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Size
import android.view.Surface
import android.view.TextureView
import kr.co.camera.R
import kr.co.camera.etc.Const
import kr.co.camera.etc.Dlog
import kr.co.camera.etc.extension.showToast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class Camera2NewFragment: BaseCameraFragment() {

    companion object {
        @JvmStatic fun newInstance() = Camera2NewFragment().apply {
            arguments = Bundle().apply {

            }
        }
    }

    override val mCaptureCallback: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        private fun process(result: CaptureResult) {
            when (state) {
                STATE_PREVIEW -> Unit // Do nothing when the camera preview is working normally.
                STATE_WAITING_LOCK -> capturePicture(result)
                STATE_WAITING_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        state = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        private fun capturePicture(result: CaptureResult) {
            val afState = result.get(CaptureResult.CONTROL_AF_STATE)
            if (afState == null) {
                captureStillPicture()
            } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                // CONTROL_AE_STATE can be null on some devices
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    state = STATE_PICTURE_TAKEN
                    captureStillPicture()
                } else {
                    runPrecaptureSequence()
                }
            }
        }

        override fun onCaptureProgressed(session: CameraCaptureSession,
                                         request: CaptureRequest,
                                         partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(session: CameraCaptureSession,
                                        request: CaptureRequest,
                                        result: TotalCaptureResult
        ) {
            process(result)
        }
    }

    override val mSurfaceTextureListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {

        // [찰스님 블로그] 처음 액티비티가 실행됐다고 가정하면, 다음과 같이 TextureView가 초기화가 완료되고 화면에 텍스쳐를 그릴 준비가 되었을 때,
        // onSurfaceTextureAvailable()을 호출
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)

            startTime = System.currentTimeMillis()
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
//            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture) = true

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit

    }

    private val operator: Operator by lazy { Operator() }

    /** [CaptureRequest] generated by [.previewRequestBuilder] */
    private lateinit var mPreviewRequest: CaptureRequest

    /** Whether the current camera device supports Flash or not. */
    private var isFlashSupported = false

    /** The [android.util.Size] of camera preview. */
    private lateinit var previewSize: Size

    /** Orientation of the camera sensor */
    private var sensorOrientation = 0

    /** An [ImageReader] that handles still image capture. */
    private var imageReader: ImageReader? = null

    /** This a callback object for the [ImageReader]. "onImageAvailable" will be called when a still image is ready to be saved. */
    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener {
//        currentDate = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        mFile = File(cameraActivity.getExternalFilesDir(null), "$currentDate.jpg")

        val sdcard: File = Environment.getExternalStorageDirectory()
        val mediaStorageDir = File(sdcard.absolutePath + "/DCIM/Camera/")
        if (!mediaStorageDir.exists()) {
            val isMakeDirectory = mediaStorageDir.mkdirs()
            Dlog.i("결과 : isMakeDirectory = $isMakeDirectory")
            if (!isMakeDirectory) {
                Dlog.i("failed to create directory")
                return@OnImageAvailableListener
            }
        }

        currentDate = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        mFile = File(mediaStorageDir.path + File.separator + "$currentDate.jpg")

        mBackgroundHandler?.post(ImageSaver(it.acquireNextImage(), mFile))
    }

    /** 이미지 프리뷰 단에서 imageReader를 통해 image를 읽어와 mat -> bitmap 으로 변환시켜주기 위한 것. */
    private val mOnImageAvailableListenerPreviewCallback = ImageReader.OnImageAvailableListener { reader ->
        var image: Image? = null
        try {
            image = reader.acquireLatestImage()

            val bitmap = operator.imageToBitmap(image)
            getByteArray(bitmap)
            image.close()

        } catch (e: Exception) {
            e.printStackTrace()
            image?.close()
        }
    }

    override fun setPetInfo(arguments: Bundle?) {
        arguments?.let {

        }
    }

    override fun setUpCameraOutputs(width: Int, height: Int) {
        // CameraManager - 카메라 시스템 서비스 매니저 리턴.
        // 설명 - CameraManager : 사용가능한 카메라를 나열하고, CameraDevice를 취득하기 위한 Camera2 API의 첫번째 클래스.
        manager = this.activity?.applicationContext?.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // 설명 - 사용가능한 카메라 리스트를 가져와 후면 카메라(LENS_FACING_BACK) 사용하여 해당 cameraId 리턴.
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                if(characteristics[CameraCharacteristics.LENS_FACING] == CameraCharacteristics.LENS_FACING_BACK) {
                    val configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: continue

                    // For still image captures, we use the largest available size.
                    // 캡쳐된 사진(이미지 리더)의 해상도, 포맷 선택
                    val largest = Collections.max(
                        Arrays.asList(*configurationMap.getOutputSizes(ImageFormat.JPEG)),
                        CompareSizesByArea()
                    )
                    imageReader = ImageReader.newInstance(largest.width, largest.height, ImageFormat.JPEG, /*maxImages*/ 2).apply {
                        setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler)
                    }

                    // Find out if we need to swap dimension to get the preview size relative to sensor coordinate.
                    // 이미지의 방향
                    val displayRotation = activity!!.windowManager.defaultDisplay.rotation

                    // noinspection ConstantConditions
                    sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                    val swappedDimensions = areDimensionsSwapped(displayRotation)

                    val displaySize = Point()
                    activity!!.windowManager.defaultDisplay.getSize(displaySize)
                    val rotatedPreviewWidth = if (swappedDimensions) height else width
                    val rotatedPreviewHeight = if (swappedDimensions) width else height
                    var maxPreviewWidth = if (swappedDimensions) displaySize.y else displaySize.x
                    var maxPreviewHeight = if (swappedDimensions) displaySize.x else displaySize.y

                    if (maxPreviewWidth > MAX_PREVIEW_WIDTH) maxPreviewWidth =
                        MAX_PREVIEW_WIDTH
                    if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) maxPreviewHeight =
                        MAX_PREVIEW_HEIGHT

                    // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                    // bus' bandwidth limitation, resulting in gorgeous previews but the storage of garbage capture data.
                    // 적합한 프리뷰 사이즈 선택

                    previewSize = chooseOptimalSize(
                        configurationMap.getOutputSizes(SurfaceTexture::class.java),
                        MAX_PREVIEW_WIDTH,
                        MAX_PREVIEW_HEIGHT,
                        Size(maxPreviewWidth, maxPreviewHeight)
                    )

                    Dlog.i("previewSize : ${previewSize.width}, ${previewSize.height}")

                    // We fit the aspect ratio of TextureView to the size of preview we picked.
                    // 들어오는 영상의 빔율에 맞춰 TextureView의 비율 변경
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        textureView.setAspectRatio(previewSize.width, previewSize.height)
                    } else {
                        textureView.setAspectRatio(previewSize.height, previewSize.width)
                    }

                    // Check if the flash is supported.
                    // 플래시 지원 여부
                    isFlashSupported = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                    mCameraId = cameraId

                    // We've found a viable camera and finished setting up member variables,
                    // so we don't need to iterate through other available cameras.
                    return
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                .show(childFragmentManager, FRAGMENT_DIALOG)
        }
    }

    override fun createCameraPreviewSession() {
        try {
            // Preview 세션을 만들기 위해서 TextureView가 가지고 있는 SurfaceTexture를 가져옵니다.
            val texture = textureView.surfaceTexture

            // We configure the size of default buffer to be the size of camera preview we want.
            // SurfaceTexture에 setUpCameraOutputs()에서 계산한 기본 버퍼 사이즈를 설정
            texture.setDefaultBufferSize(previewSize.width, previewSize.height)

            // This is the output Surface we need to start preview.
            // SufaceTexture를 이용하여 Surface를 만듭니다.
            val surface = Surface(texture)

            val imageProcessingReader: ImageReader = ImageReader.newInstance(previewSize.width, previewSize.height, ImageFormat.YUV_420_888, 2)

            // We set up a CaptureRequest.Builder with the output Surface.
            // 그런 다음 CaptureRequest.Builder에 surface를 타겟으로 지정합니다.
            // 지정된 타겟은 실제 카메라 프레임 버퍼를 받아 처리하게 됩니다.
            // 아직 CaptureRequest를 사용할순 없습니다. 캡쳐세션이 먼저 만들어져야합니다
            mPreviewRequestBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                addTarget(surface)
                addTarget(imageProcessingReader.surface)
            }
            imageProcessingReader.setOnImageAvailableListener(mOnImageAvailableListenerPreviewCallback, Handler())

            // Here, we create a CameraCaptureSession for camera preview.
            // CameraDevice.createCaptureSession()을 통해 세션을 만듭니다.
            // 캡쳐 세션이 만들어졌다면 CameraCaptureSession.StateCallback의 onConfigured()가 호출 되게 됩니다.
            mCameraDevice?.createCaptureSession(
                Arrays.asList(surface, imageReader?.surface, imageProcessingReader.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // The camera is already closed
                        if (mCameraDevice == null) return

                        // When the session is ready, we start displaying the preview.
                        mCaptureSession = cameraCaptureSession
                        try {
                            // Auto focus should be continuous for camera preview.
                            mPreviewRequestBuilder.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                                mPreviewRequestBuilder.set(
                                    CaptureRequest.CONTROL_AF_MODE,
                                    CaptureRequest.CONTROL_AF_MODE_AUTO
                                )
                                mPreviewRequestBuilder.set(
                                    CaptureRequest.CONTROL_AF_TRIGGER,
                                    CaptureRequest.CONTROL_AF_TRIGGER_START
                                )
                            }

//                            // Flash is automatically enabled when necessary.
//                            setAutoFlash(mPreviewRequestBuilder)

                            // Finally, we start displaying the camera preview.
                            // 이곳에서 아까만든 CaptureRequest.Builder를 build하여 CaptureRequest객체를 만들고,
                            // 반복적으로 이미지 버퍼를 얻기 위해 setRepeatingRequest()를 호출합니다.
                            // 이렇게 하면 TextureView에 카메라 영상이 나오는것을 확인할 수 있습니다.
                            mPreviewRequest = mPreviewRequestBuilder.build()
                            mCaptureSession?.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler)
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        context?.showToast("Failed")
                    }
                }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun closeImageReader() {
        super.closeImageReader()

        if(imageReader != null) {
            imageReader?.close()
            imageReader = null
        }
    }

    /**
     * Determines if the dimensions are swapped given the phone's current rotation.
     *
     * @param displayRotation The current rotation of the display
     *
     * @return true if the dimensions are swapped, false otherwise.
     */
    private fun areDimensionsSwapped(displayRotation: Int): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    swappedDimensions = true
                }
            }
            else -> {
                Dlog.e("Display rotation is invalid: $displayRotation")
            }
        }
        return swappedDimensions
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * [.captureCallback] from both [.lockFocus].
     *
     * CaptureSession에 CatureRequest를 넣어 사진을 캡쳐하는데
     * 이때의 캡쳐한 이미지 버퍼를 받을 Surface는 아까 TextureView의 Surface가 아닌 ImageReader의 Surface입니다.
     */
    private fun captureStillPicture() {
        try {
            if (activity == null || mCameraDevice == null) return
            val rotation = activity!!.windowManager.defaultDisplay.rotation

            // This is the CaptureRequest.Builder that we use to take a picture.
            val captureBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)?.apply {
                addTarget(imageReader?.surface)

                // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
                // We have to take that into account and rotate JPEG properly.
                // For devices with orientation of 90, we return our mapping from ORIENTATIONS.
                // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
                set(CaptureRequest.JPEG_ORIENTATION, (ORIENTATIONS.get(rotation) + sensorOrientation + 270) % 360)

                // Use the same AE and AF modes as the preview.
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            } ?.also { setAutoFlash(it) }

            val captureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession,
                                                request: CaptureRequest,
                                                result: TotalCaptureResult
                ) {
                    context?.showToast("Saved: $mFile")
                    Dlog.d("Saved mFile = $mFile")
                    unlockFocus()
                }
            }

            mCaptureSession?.apply {
                stopRepeating()
                abortCaptures()
                capture(captureBuilder?.build(), captureCallback, null)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (isFlashSupported) {
            requestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
        }
    }

    /** Unlock the focus. This method should be called when still image capture sequence is finished. */
    private fun unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            setAutoFlash(mPreviewRequestBuilder)
            mCaptureSession?.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler)
            // After this, the camera will go back to the normal state of preview.
            state = STATE_PREVIEW
            mCaptureSession?.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in [.captureCallback] from [.lockFocus].
     */
    private fun runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            // Tell #captureCallback to wait for the precapture sequence to be set.
            state = STATE_WAITING_PRECAPTURE
            mCaptureSession?.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
}