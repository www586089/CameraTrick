package com.zf.camera.trick.filter.sample

interface IShape {
    fun onSurfaceCreated()
    fun onSurfaceChanged(width: Int, height: Int)
    fun onSurfaceDestroyed()
    fun drawFrame()
}