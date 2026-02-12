package com.zf.camera.trick.filter.camera

import android.content.res.Resources
import com.zf.camera.trick.utils.TrickLog

/**
 * Applies sobel edge detection on the image.
 */
class CameraFilterThresholdEdgeDetectionFilter(res: Resources): CameraFilterGroup(res) {

    init {
        addFilter(CameraFilterGrayScale(res))
        addFilter(CameraFilterSobelThresholdFilter(res))
    }

    fun setLineSize(size: Float) {
        TrickLog.d("setLineSize: $size")
        (getFilters()[1] as CameraFilter3x3TextureSamplingFilter).setLineSize(size)
    }

    override fun createAdjuster(): IAdjuster {
        val min = 0.0f
        val max = 5.0f
        val default = CameraFilter3x3TextureSamplingFilter.DEFAULT
        return object : IAdjuster {
            override fun adjust(percentage: Float) {
                val value = range(min, max, percentage)
                setLineSize(value)
            }

            override fun getDefaultProgress(): Float {
                return ((default - min) / (max - min)) * 100
            }
        }
    }
}