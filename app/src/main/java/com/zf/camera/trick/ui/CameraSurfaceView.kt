package com.zf.camera.trick.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.zf.camera.trick.callback.PictureBufferCallback
import com.zf.camera.trick.manager.CameraManager
import com.zf.camera.trick.manager.ICameraCallback
import com.zf.camera.trick.manager.ICameraManager
import com.zf.camera.trick.utils.TrickLog

class CameraSurfaceView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs),
    SurfaceHolder.Callback, ICameraCallback {

    private val TAG = "CameraSurfaceView"

    init {
        init(context)
    }

    private lateinit var mSurfaceHolder: SurfaceHolder
    private lateinit var mCameraManager: ICameraManager

    private fun init(context: Context) {
        Log.d(TAG, "init: ")
        mCameraManager = CameraManager(context).apply {
            mCameraCallback = this@CameraSurfaceView
        }

        mSurfaceHolder = holder.apply {
            setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
            addCallback(this@CameraSurfaceView)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mCameraManager.setPreviewSize(measuredWidth, measuredHeight)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated: ")
        openCamera()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged: width = $width, height = $height")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
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

    override fun onOpen() {
        mSurfaceHolder.apply {
            mCameraManager.startPreview(this)
        }
    }

    override fun onOpenError(coe: Int, msg: String) {
        TrickLog.e(TAG, "onOpenError-> code = $coe, msg = $msg")
    }

    override fun onSetPreviewSize(width: Int, height: Int) {

    }

}