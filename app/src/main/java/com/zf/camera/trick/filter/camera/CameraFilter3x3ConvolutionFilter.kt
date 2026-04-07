package com.zf.camera.trick.filter.camera

import android.content.res.Resources

/**
 * Runs a 3x3 convolution kernel against the image
 */
open class CameraFilter3x3ConvolutionFilter(res: Resources) :
    CameraFilter3x3TextureSamplingFilter(res, THREE_X_THREE_TEXTURE_SAMPLING_FRAGMENT_SHADER) {
    companion object {
        const val THREE_X_THREE_TEXTURE_SAMPLING_FRAGMENT_SHADER = "" +
                "#extension GL_OES_EGL_image_external : require\n" +

                "precision highp float;\n" +
                "\n" +
                "uniform samplerExternalOES uTexture;\n" +
                "\n" +
                "uniform mediump mat3 convolutionMatrix;\n" +
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
                "void main()\n" +
                "{\n" +
                "    mediump vec4 bottomColor = texture2D(uTexture, bottomTextureCoordinate);\n" +
                "    mediump vec4 bottomLeftColor = texture2D(uTexture, bottomLeftTextureCoordinate);\n" +
                "    mediump vec4 bottomRightColor = texture2D(uTexture, bottomRightTextureCoordinate);\n" +
                "    mediump vec4 centerColor = texture2D(uTexture, textureCoordinate);\n" +
                "    mediump vec4 leftColor = texture2D(uTexture, leftTextureCoordinate);\n" +
                "    mediump vec4 rightColor = texture2D(uTexture, rightTextureCoordinate);\n" +
                "    mediump vec4 topColor = texture2D(uTexture, topTextureCoordinate);\n" +
                "    mediump vec4 topRightColor = texture2D(uTexture, topRightTextureCoordinate);\n" +
                "    mediump vec4 topLeftColor = texture2D(uTexture, topLeftTextureCoordinate);\n" +
                "\n" +
                "    mediump vec4 resultColor = topLeftColor * convolutionMatrix[0][0] + topColor * convolutionMatrix[0][1] + topRightColor * convolutionMatrix[0][2];\n" +
                "    resultColor += leftColor * convolutionMatrix[1][0] + centerColor * convolutionMatrix[1][1] + rightColor * convolutionMatrix[1][2];\n" +
                "    resultColor += bottomLeftColor * convolutionMatrix[2][0] + bottomColor * convolutionMatrix[2][1] + bottomRightColor * convolutionMatrix[2][2];\n" +
                "\n" +
                "    gl_FragColor = resultColor;\n" +
                "}"
    }

    private var convolutionKernel: FloatArray = floatArrayOf(
        0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f
    )
    private var uniformConvolutionMatrix = 0

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        uniformConvolutionMatrix = getUniformLocation("convolutionMatrix")
        setConvolutionKernel(convolutionKernel)
    }

    fun setConvolutionKernel(convolutionKernel: FloatArray) {
        this.convolutionKernel = convolutionKernel
        setUniformMatrix3f(uniformConvolutionMatrix, this.convolutionKernel)
    }

    override fun createAdjuster(): IAdjuster {
        //这里的Adjuster没用到，只是设置了一个convolutionKernel的初值
        return object : IAdjuster {
            override fun adjust(percentage: Float) {

            }

            override fun getDefaultProgress(): Float {
                setConvolutionKernel(floatArrayOf(
                    -1.0f, 0.0f, 1.0f,
                    -2.0f, 0.0f, 2.0f,
                    -1.0f, 0.0f, 1.0f
                ))
                return 0.0f
            }
        }
    }
}