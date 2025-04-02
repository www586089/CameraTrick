package com.zf.camera.trick.ui

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView

class CameraSurfaceView(context: Context, attrs: AttributeSet): SurfaceView(context, attrs), SurfaceHolder.Callback {

    private val TAG = "CameraSurfaceView"

    init {
        init(context)
    }

    private var mSurfaceHolder: SurfaceHolder? = null
    private var mContext: Context? = null

    private var mCameraId = 0;
    private var mCamera: Camera? = null




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
                setPreviewDisplay(mSurfaceHolder)
                startPreview()
            }
        } catch (e: Throwable) {
            Log.d(TAG, "openCamera: error:\n" + Log.getStackTraceString(e))
        } finally {
        }
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