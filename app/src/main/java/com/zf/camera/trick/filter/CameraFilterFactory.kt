package com.zf.camera.trick.filter

import android.content.res.Resources
import com.zf.camera.trick.filter.camera.TFilerNoChange
import com.zf.camera.trick.filter.camera.TFilerSepiaTone
import com.zf.camera.trick.filter.camera.TFilter3x3ConvolutionFilter
import com.zf.camera.trick.filter.camera.TFilterBase
import com.zf.camera.trick.filter.camera.TFilterBrightness
import com.zf.camera.trick.filter.camera.TFilterContrast
import com.zf.camera.trick.filter.camera.TFilterDirectionalSobelEdgeDetection
import com.zf.camera.trick.filter.camera.TFilterEmbossFilter
import com.zf.camera.trick.filter.camera.TFilterExposureFilter
import com.zf.camera.trick.filter.camera.TFilterGamma
import com.zf.camera.trick.filter.camera.TFilterGrayScale
import com.zf.camera.trick.filter.camera.TFilterGroup
import com.zf.camera.trick.filter.camera.TFilterHighlightShadowFilter
import com.zf.camera.trick.filter.camera.TFilterHue
import com.zf.camera.trick.filter.camera.TFilterInvert
import com.zf.camera.trick.filter.camera.TFilterMonochromeFilter
import com.zf.camera.trick.filter.camera.TFilterOpacityFilter
import com.zf.camera.trick.filter.camera.TFilterPixelation
import com.zf.camera.trick.filter.camera.TFilterPosterize
import com.zf.camera.trick.filter.camera.TFilterRGBFilter
import com.zf.camera.trick.filter.camera.TFilterSaturationFilter
import com.zf.camera.trick.filter.camera.TFilterSharpness
import com.zf.camera.trick.filter.camera.TFilterSobelEdgeDetection
import com.zf.camera.trick.filter.camera.TFilterThresholdEdgeDetectionFilter
import com.zf.camera.trick.filter.camera.TFilterWhiteBalanceFilter

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
        val FILTER_TYPE_POSTERIZE = NO_FILTER + 14
        val FILTER_TYPE_FILTER_GROUP = NO_FILTER + 15
        val FILTER_TYPE_SATURATION = NO_FILTER + 16
        val FILTER_TYPE_EXPOSURE = NO_FILTER + 17
        val FILTER_TYPE_HIGHLIGHT_SHADOW = NO_FILTER + 18
        val FILTER_TYPE_MONOCHROME = NO_FILTER + 19
        val FILTER_TYPE_OPACITY = NO_FILTER + 20
        val FILTER_TYPE_RGB = NO_FILTER + 21
        val FILTER_TYPE_WHITE_BALANCE = NO_FILTER + 22
        val instance = CameraFilterFactory()
            get
    }
    fun getFilter(resources: Resources?, type: Int): TFilterBase {
        when (type) {
            NO_FILTER -> return TFilerNoChange(resources!!)
            FILTER_TYPE_CONTRAST -> return TFilterContrast(resources!!)
            FILTER_TYPE_INVERT -> return TFilterInvert(resources!!)
            FILTER_TYPE_PIXELATION -> return TFilterPixelation(resources!!)
            FILTER_TYPE_HUE -> return TFilterHue(resources!!)
            FILTER_TYPE_GAMMA -> return TFilterGamma(resources!!)
            FILTER_TYPE_BRIGHTNESS -> return TFilterBrightness(resources!!)
            FILTER_TYPE_SEPIA_TONE -> return TFilerSepiaTone(resources!!)
            FILTER_TYPE_GRAY_SCALE -> return TFilterGrayScale(resources!!)
            FILTER_TYPE_SHARPNESS -> return TFilterSharpness(resources!!)
            FILTER_TYPE_SOBEL_EDGE_DETECTION -> return TFilterSobelEdgeDetection(resources!!)
            FILTER_TYPE_THRESHOLD_EDGE_DETECTION -> return TFilterThresholdEdgeDetectionFilter(resources!!)
            FILTER_TYPE_THREE_X_THREE_CONVOLUTION -> return TFilter3x3ConvolutionFilter(resources!!)
            FILTER_TYPE_EMBOSS -> return TFilterEmbossFilter(resources!!)
            FILTER_TYPE_POSTERIZE -> return TFilterPosterize(resources!!)
            FILTER_TYPE_FILTER_GROUP -> return TFilterGroup(resources!!, mutableListOf(
                //todo 组合起来后，切换到其他filter会崩溃，应该是CameraFilterGroup有bug
                TFilterContrast(resources),
                TFilterDirectionalSobelEdgeDetection(resources),
                TFilterGrayScale(resources))
            )
            FILTER_TYPE_SATURATION -> return TFilterSaturationFilter(resources!!)
            FILTER_TYPE_EXPOSURE -> return TFilterExposureFilter(resources!!)
            FILTER_TYPE_HIGHLIGHT_SHADOW -> return TFilterHighlightShadowFilter(resources!!)
            FILTER_TYPE_MONOCHROME -> return TFilterMonochromeFilter(resources!!)
            FILTER_TYPE_OPACITY -> return TFilterOpacityFilter(resources!!)
            FILTER_TYPE_RGB -> return TFilterRGBFilter(resources!!)
            FILTER_TYPE_WHITE_BALANCE -> return TFilterWhiteBalanceFilter(resources!!)
        }
        return TFilerNoChange(resources!!)
    }
}