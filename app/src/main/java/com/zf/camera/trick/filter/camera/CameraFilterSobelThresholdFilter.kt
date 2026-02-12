package com.zf.camera.trick.filter.camera

import android.content.res.Resources

class CameraFilterSobelThresholdFilter(res: Resources) :
    CameraFilter3x3TextureSamplingFilter(res, SOBEL_THRESHOLD_EDGE_DETECTION) {
        companion object {
            const val SOBEL_THRESHOLD_EDGE_DETECTION = "" +
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
                    "uniform lowp float threshold;\n" +
                    "\n" +
                    "const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);\n" +
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
                    "    float mag = 1.0 - length(vec2(h, v));\n" +
                    "    mag = step(threshold, mag);\n" +
                    "\n" +
                    "    gl_FragColor = vec4(vec3(mag), 1.0);\n" +
                    "}\n"
        }

    private var uniformThresholdLocation = 0
    private var threshold = 0f

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        uniformThresholdLocation = getUniformLocation("threshold")
        setThreshold(0.9f)
    }

    fun setThreshold(threshold: Float) {
        this.threshold = threshold
        setUniformLocation(uniformThresholdLocation, threshold)
    }
}