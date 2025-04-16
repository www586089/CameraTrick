package com.zf.camera.trick.manager

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.view.SurfaceHolder
import com.zf.camera.trick.callback.PictureBufferCallback

interface ICameraManager {

    fun addCallback(cb: Camera.PreviewCallback)
    fun removeCallback(cb: Camera.PreviewCallback)
    /**
     * 打开相机
     */
    fun openCamera()

    /**
     * 关闭相机
     */
    fun closeCamera()

    /**
     * 前后摄像头切换
     */
    fun switchCamera()

    /**
     * 拍照
     */
    fun takePicture(callback: PictureBufferCallback)

    fun setPreviewSize(width: Int, height: Int)

    /**
     * 开始预览
     */
    fun startPreview(surfaceTexture: SurfaceTexture)
    fun startPreview(holder: SurfaceHolder)

    /**
     * 结束预览
     */
    fun stopPreview()
}