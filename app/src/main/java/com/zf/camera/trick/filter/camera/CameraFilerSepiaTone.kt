package com.zf.camera.trick.filter.camera

import android.content.res.Resources

class CameraFilerSepiaTone(val res: Resources): ColorMatrixFilter(res) {


    override fun onSurfaceCreated() {
        colorMatrix = floatArrayOf(
            0.3588f, 0.7044f, 0.1368f, 0.0f,
            0.2990f, 0.5870f, 0.1140f, 0.0f,
            0.2392f, 0.4696f, 0.0912f, 0.0f,
            0f,           0f,      0f, 1.0f
        )
        super.onSurfaceCreated()
    }

    
    override fun createAdjuster(): IAdjuster {
        return object : IAdjuster {
            override fun adjust(percentage: Float) {
                val intensity = range(0.0f, 2.0f, percentage = percentage)
                setIntensity(intensity)
            }

            override fun getDefaultProgress(): Float {
                return ((1.0f / 2.0f) * 100)
            }
        }
    }
}