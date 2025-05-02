package com.zf.camera.trick.filter.camera

interface IAdjuster {
    fun adjust(percentage: Float)
    fun getDefaultProgress(): Float

    fun range(start: Float, end: Float, percentage: Float): Float {
        return start + ((end - start) * percentage) / 100f
    }
}