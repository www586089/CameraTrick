package com.zf.camera.trick.filter

import com.zf.camera.trick.filter.sample.IShape

abstract class AFilter: IShape {
    open fun drawFrame(texMatrix: FloatArray?) {}

    override fun drawFrame() {
    }
}