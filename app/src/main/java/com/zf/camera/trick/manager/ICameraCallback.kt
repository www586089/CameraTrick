package com.zf.camera.trick.manager

interface ICameraCallback {
    fun onOpen()
    fun onOpenError(coe: Int, msg: String)
    fun onSetPreviewSize(width: Int, height: Int)
}