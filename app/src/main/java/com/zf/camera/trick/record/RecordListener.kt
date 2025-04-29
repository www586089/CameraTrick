package com.zf.camera.trick.record

import android.graphics.SurfaceTexture

/**
 * 通用录制接口
 */
interface IMediaRecorder {
    fun startRecord(videoPath: String, width: Int, height: Int, listener: VideoRecordListener)
    fun stopRecord()
}

/**
 * Camera视频录制接口
 */
interface ICameraVideoRecorder : IMediaRecorder {
    fun encode(data: ByteArray)
}

/**
 * Surface视频录制接口
 */
interface ISurfaceVideoRecorder : IMediaRecorder {
    fun onUpdatedSharedContext()
    fun willComingAFrame(textureId: Int, st: SurfaceTexture)
    fun updateShaderType(shaderType: Int)
}