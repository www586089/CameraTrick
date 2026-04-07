package com.zf.camera.trick.filter.camera

import android.content.res.Resources

class CameraFilterEmbossFilter(res: Resources): CameraFilter3x3ConvolutionFilter(res) {
    private var intensity = 0f
    init {
        this.intensity = 1.0f
    }

    fun setIntensity(intensity: Float) {
        this.intensity = intensity
        setConvolutionKernel(
            floatArrayOf(
                intensity * -2.0f, -intensity, 0.0f,
                -intensity, 1.0f, intensity,
                0.0f, intensity, intensity * 2.0f
            )
        )
    }

    override fun createAdjuster(): IAdjuster {
        return object : IAdjuster {
            override fun adjust(percentage: Float) {
                setIntensity(range(0.0f, 4.0f, percentage))
            }

            override fun getDefaultProgress(): Float {
                return 0f
            }
        }
    }
}