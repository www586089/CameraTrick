package com.zf.camera.trick.filter


interface IFilter {
    fun setTextureSize(width: Int, height: Int)
    var frameBuffer: Int
    val offscreenTexture: Int

    fun setBindFBO(bindFBO: Boolean)
    fun doBindFBO()
    fun unBindFBO()
    fun surfaceCreated()
    fun surfaceChanged(width: Int, height: Int)
    fun draw(textureId: Int, matrix: FloatArray?): Int
    fun onDraw(textureId: Int, matrix: FloatArray?)
    fun release()
}

