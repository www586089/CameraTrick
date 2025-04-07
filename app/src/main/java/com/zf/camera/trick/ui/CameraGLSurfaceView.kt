package com.zf.camera.trick.ui

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.WindowManager
import com.zf.camera.trick.App
import com.zf.camera.trick.callback.PictureBufferCallback
import com.zf.camera.trick.filter.CameraFilter
import com.zf.camera.trick.utils.CameraSizeComparator
import com.zf.camera.trick.utils.TrickLog
import java.lang.ref.WeakReference
import java.util.Collections
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs


class CameraGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs), SurfaceTexture.OnFrameAvailableListener {

    private val TAG = "CameraGLSurfaceView"
    private val DEBUG = true

    init {
        init(context)
    }

//    private var mSurfaceHolder: SurfaceHolder? = null
    private var mContext: Context? = null

    private var mCameraId = 0;
    private var mCamera: Camera? = null
    private var mDisplayOrientation = -1     //预览方向
    private var mOrientation = -1            //拍照方向

    private var viewWidth = -1
    private var viewHeight = -1

    private var mSurfaceTexture: SurfaceTexture? = null
    private var mCameraHandler: CameraHandler? = null
    private var render: MyRenderer? = null

    private fun init(context: Context) {
        Log.d(TAG, "init: ")
        mContext = context

//        mSurfaceHolder = holder.apply {
//            setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
//            addCallback(this@CameraGLSurfaceView)
//        }
        render = MyRenderer(this)
        mCameraHandler = CameraHandler(this);
        setEGLContextClientVersion(2)
        setRenderer(render)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = measuredWidth
        viewHeight = measuredHeight
    }

//    override fun surfaceCreated(holder: SurfaceHolder) {
//        Log.d(TAG, "surfaceCreated: ")
//        openCamera()
//    }

//    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//        Log.d(TAG, "surfaceChanged: width = $width, height = $height")
//    }

    override fun onResume() {
        super.onResume()
        if (null != mSurfaceTexture) {
            openCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        closeCamera()
    }
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        super.surfaceDestroyed(holder)
        Log.d(TAG, "surfaceDestroyed: ")
        closeCamera()
    }

    fun openCamera() {
        if (null != mCamera || (-1 == viewWidth || -1 == viewHeight)) {
            return
        }
        try {
            mCamera = Camera.open(mCameraId).apply {
                val cameraParams = parameters

                setCameraDisplayOrientation(mContext, mCameraId, this)
                if (-1 != mOrientation) {//需要设置方向否则拍出来的照片角度不对
                    cameraParams.setRotation(mOrientation)
                }
                setAutoFocus(cameraParams)
                setPreViewSize(cameraParams)

                setParameters(cameraParams)
                startCameraPreview(this)
            }
        } catch (e: Throwable) {
            Log.d(TAG, "openCamera: error:\n" + Log.getStackTraceString(e))
        } finally {
        }
    }

    private fun startCameraPreview(camera: Camera?) {
        camera?.apply {
            setPreviewTexture(mSurfaceTexture)
            startPreview()
        }
    }

    private fun setPreViewSize(parameters: Camera.Parameters) {
        val previewSize = parameters.supportedPreviewSizes
        if (DEBUG) {
            Log.d(TAG, "openCamera: Before sort")
            logCameraSize(previewSize)
            val sizeComparator = CameraSizeComparator(false)
            Log.d(TAG, "openCamera: After sort")
            Collections.sort(previewSize, sizeComparator)
            logCameraSize(previewSize)
        }

        /**
         * previewSize 里面的width height，与viewHeight, viewWidth是相反的，所以
         * 这里传递参数的时候也反过来了，这样才能对应起来
         */
        getOptimalPreviewSizeAspect(previewSize, viewHeight, viewWidth)?.also {
            parameters.setPreviewSize(it.width, it.height)
        }
    }

    private fun getOptimalPreviewSizeAspect(sizes: List<Camera.Size>?, w: Int, h: Int): Camera.Size? {
        if (sizes.isNullOrEmpty()) {
            return null
        }
        val viewAreaSize = w * h
        val minArea = viewAreaSize / 3
        val targetRatio = w.toDouble() / h.toDouble()
        var optimalSize: Camera.Size? = null
        Collections.sort(sizes, CameraSizeComparator(false))
        var size: Camera.Size

        var minDiff = Double.MAX_VALUE


        //1 根据宽高比进行过滤
        for (sizeTmp in sizes) {
            val ratio = sizeTmp.width.toDouble() / sizeTmp.height.toDouble()
            if (abs(ratio - targetRatio) < minDiff) {
//                if (null != maxSize && (sizeTmp.height > maxSize.height || sizeTmp.width > maxSize.width)) {
//                    continue
//                }
                if (sizeTmp.width * sizeTmp.height < minArea) { //面积太小[导致预览模糊]，不再考虑后面的
                    break
                }
                minDiff = Math.abs(ratio - targetRatio)
                optimalSize = sizeTmp
            }
        }
        var minDiffSize = Int.MAX_VALUE
        var diff: Int
        if (null == optimalSize) { //2 兜底 找到面积最接近的一个[这里选出的尺寸在UI上可能比较丑]
            for (value in sizes) {
//                if (null != maxSize && (value.height > maxSize.height || value.width > maxSize.width)) {
//                    continue
//                }
                size = value
                diff = abs(size.height * size.width - viewAreaSize)
                if (diff < minDiffSize) {
                    minDiffSize = diff
                    optimalSize = size
                }
            }
            if (null == optimalSize) {
                optimalSize = sizes[0]
            }
        }
        Log.d(TAG, "getOptimalPreviewSizeAspect-> size ${optimalSize.width} x ${optimalSize.height}")
        return optimalSize
    }

    private fun setAutoFocus(parameters: Camera.Parameters) {
        parameters.supportedFocusModes?.apply {
            if (contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            }
        }
    }

    fun takePicture(callback: PictureBufferCallback) {
        mCamera?.apply {
            takePicture(null, null, object : Camera.PictureCallback {
                override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
                    startCameraPreview(mCamera)//拍照后重新开始预览
                    callback.onPictureToken(data)
                }
            })
        }
    }

    /**
     * 切换摄像头
     */
    fun switchCamera() {
        Log.d(TAG, "switchCamera: ")
        // 先改变摄像头方向
        mCameraId = mCameraId xor 1
        mDisplayOrientation = -1;
        closeCamera(isRelease = false)
        openCamera()
    }

    private fun setCameraDisplayOrientation(context: Context?, cameraId: Int, camera: Camera) {
        if (context == null) {
            return
        }
        val info = CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation = windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(result)
        mDisplayOrientation = result
        mOrientation = info.orientation
        Log.d(TAG, "displayOrientation:$mDisplayOrientation, orientation:$mOrientation")
    }

    private fun closeCamera(isRelease: Boolean = true) {
        mCamera?.also {
            try {
                it.stopPreview()
                it.release()
            } catch (e: Throwable) {
                Log.e(TAG, "closeCamera: error" + Log.getStackTraceString(e))
            } finally {
                mCamera = null
            }
        }
        if (isRelease) {
            queueEvent {
                render?.release()
            }
            mSurfaceTexture = null
        }
    }

    private fun logCameraSize(sizes: List<Camera.Size>) {
        val sizeInfo = StringBuffer()
        var i = 0;
        for (size: Camera.Size in sizes) {
            sizeInfo.append("i = ${++i}-> ${size.width} x ${size.height} ")
        }
        Log.d(TAG, "logCameraSize: $sizeInfo")
    }

    /**
     * Connects the SurfaceTexture to the Camera preview output, and starts the preview.
     */
    private fun handleSetSurfaceTexture(st: SurfaceTexture) {
        TrickLog.i(TAG, "handleSetSurfaceTexture.")
        mSurfaceTexture = st
        mSurfaceTexture?.setOnFrameAvailableListener(this)
        openCamera()
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        requestRender()
    }

    /**
     * @param width
     * @param height
     */
    private fun handleSurfaceChanged(width: Int, height: Int) {
        TrickLog.i(TAG, "handleSurfaceChanged.")
//        mGLSurfaceWidth = width
//        mGLSurfaceHeight = height
//        setAspectRatio()
    }


    internal class CameraHandler(view: CameraGLSurfaceView) :
        Handler() {
        private val mWeakGLSurfaceView: WeakReference<CameraGLSurfaceView>

        init {
            mWeakGLSurfaceView = WeakReference(view)
        }

        /**
         * Drop the reference to the activity.  Useful as a paranoid measure to ensure that
         * attempts to access a stale Activity through a handler are caught.
         */
        fun invalidateHandler() {
            mWeakGLSurfaceView.clear()
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val what = msg.what
            val view: CameraGLSurfaceView = mWeakGLSurfaceView.get() ?: return
            when (what) {
                MSG_SET_SURFACE_TEXTURE -> view.handleSetSurfaceTexture(msg.obj as SurfaceTexture)
                MSG_SURFACE_CHANGED -> view.handleSurfaceChanged(msg.arg1, msg.arg2)
                else -> throw RuntimeException("unknown msg $what")
            }
        }

        companion object {
            const val MSG_SET_SURFACE_TEXTURE = 0
            const val MSG_SURFACE_CHANGED = 1
        }
    }


    internal class MyRenderer(private val mView: CameraGLSurfaceView) : Renderer {
        private val mCameraFilter: CameraFilter =
            CameraFilter(App.get().resources)
        private var mTextureId = 0
        private var mSurfaceTexture: SurfaceTexture? = null
        private val mDisplayProjectionMatrix = FloatArray(16)

        public fun release() {
            mSurfaceTexture?.release();
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            Log.d("CameraGLSurfaceView", "Render->onSurfaceCreated: ")
            mCameraFilter.surfaceCreated()
            mTextureId = mCameraFilter.textureId
            mSurfaceTexture = SurfaceTexture(mTextureId)
            mView.mCameraHandler?.post { mView.handleSetSurfaceTexture(mSurfaceTexture!!) }
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            Log.d("CameraGLSurfaceView", "Render-> onSurfaceChanged: ")
            mCameraFilter.surfaceChanged(width, height)
        }

        override fun onDrawFrame(gl: GL10) {
            mSurfaceTexture?.apply {
                // 更新最新纹理
                updateTexImage()
                // 获取SurfaceTexture变换矩阵
                getTransformMatrix(mDisplayProjectionMatrix)
                // 将SurfaceTexture绘制到GLSurfaceView上
                mCameraFilter.draw(mDisplayProjectionMatrix)
            }
        }
    }




}