package com.zf.camera.trick.ui

import android.content.Context
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager

class CameraSurfaceView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs),
    SurfaceHolder.Callback {

    private val TAG = "CameraSurfaceView"

    init {
        init(context)
    }

    private var mSurfaceHolder: SurfaceHolder? = null
    private var mContext: Context? = null

    private var mCameraId = 0;
    private var mCamera: Camera? = null
    private var mDisplayOrientation = -1     //预览方向
    private var mOrientation = -1            //拍照方向


    private fun init(context: Context) {
        Log.d(TAG, "init: ")
        mContext = context

        mSurfaceHolder = holder.apply {
            setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
            addCallback(this@CameraSurfaceView)
        }
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

    private fun openCamera() {
        if (null != mCamera) {
            return
        }
        try {
            mCamera = Camera.open(mCameraId).apply {
                setCameraDisplayOrientation(mContext, mCameraId, this)
                setPreviewDisplay(mSurfaceHolder)
                startPreview()
            }
        } catch (e: Throwable) {
            Log.d(TAG, "openCamera: error:\n" + Log.getStackTraceString(e))
        } finally {
        }
    }

    private fun setCameraDisplayOrientation(context: Context?, cameraId: Int, camera: Camera) {
        if (context == null) return
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

    private fun closeCamera() {
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
    }
}