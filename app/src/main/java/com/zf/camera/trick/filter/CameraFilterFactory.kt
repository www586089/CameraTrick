package com.zf.camera.trick.filter

import android.content.res.Resources
import com.zf.camera.trick.filter.camera.CameraFilerNoChange
import com.zf.camera.trick.filter.camera.CameraFilerSepiaTone
import com.zf.camera.trick.filter.camera.CameraFilter3x3ConvolutionFilter
import com.zf.camera.trick.filter.camera.CameraFilterBase
import com.zf.camera.trick.filter.camera.CameraFilterBrightness
import com.zf.camera.trick.filter.camera.CameraFilterContrast
import com.zf.camera.trick.filter.camera.CameraFilterEmbossFilter
import com.zf.camera.trick.filter.camera.CameraFilterGamma
import com.zf.camera.trick.filter.camera.CameraFilterGrayScale
import com.zf.camera.trick.filter.camera.CameraFilterHue
import com.zf.camera.trick.filter.camera.CameraFilterInvert
import com.zf.camera.trick.filter.camera.CameraFilterPixelation
import com.zf.camera.trick.filter.camera.CameraFilterSharpness
import com.zf.camera.trick.filter.camera.CameraFilterSobelEdgeDetection
import com.zf.camera.trick.filter.camera.CameraFilterThresholdEdgeDetectionFilter

class CameraFilterFactory {


    companion object {

        val NO_FILTER = 0
        val FILTER_TYPE_CONTRAST = NO_FILTER + 1
        val FILTER_TYPE_INVERT = NO_FILTER + 2
        val FILTER_TYPE_PIXELATION = NO_FILTER + 3
        val FILTER_TYPE_HUE = NO_FILTER + 4
        val FILTER_TYPE_GAMMA = NO_FILTER + 5
        val FILTER_TYPE_BRIGHTNESS = NO_FILTER + 6
        val FILTER_TYPE_SEPIA_TONE = NO_FILTER + 7
        val FILTER_TYPE_GRAY_SCALE = NO_FILTER + 8
        val FILTER_TYPE_SHARPNESS = NO_FILTER + 9
        val FILTER_TYPE_SOBEL_EDGE_DETECTION = NO_FILTER + 10
        val FILTER_TYPE_THRESHOLD_EDGE_DETECTION = NO_FILTER + 11
        val FILTER_TYPE_THREE_X_THREE_CONVOLUTION = NO_FILTER + 12
        val FILTER_TYPE_EMBOSS = NO_FILTER + 13
        val instance = CameraFilterFactory()
            get
    }
    fun getFilter(resources: Resources?, type: Int): CameraFilterBase {
        when (type) {
            NO_FILTER -> return CameraFilerNoChange(resources!!)
            FILTER_TYPE_CONTRAST -> return CameraFilterContrast(resources!!)
            FILTER_TYPE_INVERT -> return CameraFilterInvert(resources!!)
            FILTER_TYPE_PIXELATION -> return CameraFilterPixelation(resources!!)
            FILTER_TYPE_HUE -> return CameraFilterHue(resources!!)
            FILTER_TYPE_GAMMA -> return CameraFilterGamma(resources!!)
            FILTER_TYPE_BRIGHTNESS -> return CameraFilterBrightness(resources!!)
            FILTER_TYPE_SEPIA_TONE -> return CameraFilerSepiaTone(resources!!)
            FILTER_TYPE_GRAY_SCALE -> return CameraFilterGrayScale(resources!!)
            FILTER_TYPE_SHARPNESS -> return CameraFilterSharpness(resources!!)
            FILTER_TYPE_SOBEL_EDGE_DETECTION -> return CameraFilterSobelEdgeDetection(resources!!)
            FILTER_TYPE_THRESHOLD_EDGE_DETECTION -> return CameraFilterThresholdEdgeDetectionFilter(resources!!)
            FILTER_TYPE_THREE_X_THREE_CONVOLUTION -> return CameraFilter3x3ConvolutionFilter(resources!!)
            FILTER_TYPE_EMBOSS -> return CameraFilterEmbossFilter(resources!!)
        }
        return CameraFilerNoChange(resources!!)
    }
}