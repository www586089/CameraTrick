package com.zf.camera.trick.ui

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import com.zf.camera.trick.App
import com.zf.camera.trick.callback.PictureBufferCallback
import com.zf.camera.trick.filter.CameraFilter
import com.zf.camera.trick.manager.CameraManager
import com.zf.camera.trick.manager.ICameraCallback
import com.zf.camera.trick.manager.ICameraManager
import com.zf.camera.trick.utils.TrickLog
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class CameraGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs), SurfaceTexture.OnFrameAvailableListener,
    ICameraCallback {

    private val TAG = "CameraGLSurfaceView"

    init {
        init(context)
    }

    private lateinit var mCameraManager: ICameraManager
    private lateinit var render: MyRenderer

    private var mSurfaceTexture: SurfaceTexture? = null
    private var mCameraHandler: CameraHandler? = null

    private fun init(context: Context) {
        Log.d(TAG, "init: ")

        mCameraManager = CameraManager(context).apply {
            mCameraCallback = this@CameraGLSurfaceView
        }
        render = MyRenderer(this)
        mCameraHandler = CameraHandler(this);
        setEGLContextClientVersion(2)
        setRenderer(render)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mCameraManager.setPreviewSize(measuredWidth, measuredHeight)
    }

    override fun onResume() {
        super.onResume()
        if (null != mSurfaceTexture) {
            openCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        closeCamera()
        queueEvent {
            render.release()
        }
        mSurfaceTexture = null
    }
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        super.surfaceDestroyed(holder)
        Log.d(TAG, "surfaceDestroyed: ")
        closeCamera()
    }

    fun openCamera() {
        mCameraManager.openCamera()
    }


    fun takePicture(callback: PictureBufferCallback) {
        mCameraManager.takePicture(callback)
    }

    /**
     * 切换摄像头
     */
    fun switchCamera() {
        mCameraManager.switchCamera()
    }

    private fun closeCamera() {
        mCameraManager.closeCamera()
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

    override fun onOpen() {
        mSurfaceTexture?.apply {
            mCameraManager.startPreview(this)
        }
    }

    override fun onOpenError(code: Int, msg: String) {
        TrickLog.e(TAG, "onOpenError-> code = $code, msg = $msg")
    }


}