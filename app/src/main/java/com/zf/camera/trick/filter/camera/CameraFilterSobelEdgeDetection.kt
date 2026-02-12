package com.zf.camera.trick.filter.camera

import android.content.res.Resources
import com.zf.camera.trick.utils.TrickLog

/**
 * Applies sobel edge detection on the image.
 */
class CameraFilterSobelEdgeDetection(res: Resources) : CameraFilterGroup(res) {

        companion object {
            const val SOBEL_EDGE_DETECTION = "" +
                    "#extension GL_OES_EGL_image_external : require\n" +

                    "precision mediump float;\n" +
                    "\n" +
                    "varying vec2 textureCoordinate;\n" +
                    "varying vec2 leftTextureCoordinate;\n" +
                    "varying vec2 rightTextureCoordinate;\n" +
                    "\n" +
                    "varying vec2 topTextureCoordinate;\n" +
                    "varying vec2 topLeftTextureCoordinate;\n" +
                    "varying vec2 topRightTextureCoordinate;\n" +
                    "\n" +
                    "varying vec2 bottomTextureCoordinate;\n" +
                    "varying vec2 bottomLeftTextureCoordinate;\n" +
                    "varying vec2 bottomRightTextureCoordinate;\n" +
                    "\n" +
                    "uniform samplerExternalOES vTexture;\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "    float bottomLeftIntensity = texture2D(vTexture, bottomLeftTextureCoordinate).r;\n" +
                    "    float topRightIntensity = texture2D(vTexture, topRightTextureCoordinate).r;\n" +
                    "    float topLeftIntensity = texture2D(vTexture, topLeftTextureCoordinate).r;\n" +
                    "    float bottomRightIntensity = texture2D(vTexture, bottomRightTextureCoordinate).r;\n" +
                    "    float leftIntensity = texture2D(vTexture, leftTextureCoordinate).r;\n" +
                    "    float rightIntensity = texture2D(vTexture, rightTextureCoordinate).r;\n" +
                    "    float bottomIntensity = texture2D(vTexture, bottomTextureCoordinate).r;\n" +
                    "    float topIntensity = texture2D(vTexture, topTextureCoordinate).r;\n" +
                    "    float h = -topLeftIntensity - 2.0 * topIntensity - topRightIntensity + bottomLeftIntensity + 2.0 * bottomIntensity + bottomRightIntensity;\n" +
                    "    float v = -bottomLeftIntensity - 2.0 * leftIntensity - topLeftIntensity + bottomRightIntensity + 2.0 * rightIntensity + topRightIntensity;\n" +
                    "\n" +
                    "    float mag = length(vec2(h, v));\n" +
                    "\n" +
                    "    gl_FragColor = vec4(vec3(mag), 1.0);\n" +
                    "}"
        }

    init {
        addFilter(CameraFilterGrayScale(res));
        addFilter(CameraFilter3x3TextureSamplingFilter(res, SOBEL_EDGE_DETECTION));
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