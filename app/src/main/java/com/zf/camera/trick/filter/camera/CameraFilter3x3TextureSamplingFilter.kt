package com.zf.camera.trick.filter.camera

import android.content.res.Resources
import com.zf.camera.trick.utils.TrickLog

open class CameraFilter3x3TextureSamplingFilter(res: Resources, fragShader: String) :
    CameraFilterBase(res, THREE_X_THREE_TEXTURE_SAMPLING_VERTEX_SHADER, fragShader) {

    companion object {
        const val THREE_X_THREE_TEXTURE_SAMPLING_VERTEX_SHADER = "" +
                "attribute vec4 aPosition;\n" +
                "attribute vec4 aTexCoordinate;\n" +
                "uniform mat4 uTextureMatrix;\n" +

                "\n" +
                "uniform highp float texelWidth; \n" +
                "uniform highp float texelHeight; \n" +
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
                "    gl_Position = aPosition;\n" +
                "\n" +
                "    vec2 widthStep = vec2(texelWidth, 0.0);\n" +
                "    vec2 heightStep = vec2(0.0, texelHeight);\n" +
                "    vec2 widthHeightStep = vec2(texelWidth, texelHeight);\n" +
                "    vec2 widthNegativeHeightStep = vec2(texelWidth, -texelHeight);\n" +
                "\n" +
                "    vec2 textCoordinate = (uTextureMatrix * aTexCoordinate).xy;\n" +
                "    textureCoordinate = textCoordinate.xy;\n" +
                "    leftTextureCoordinate = textCoordinate.xy - widthStep;\n" +
                "    rightTextureCoordinate = textCoordinate.xy + widthStep;\n" +
                "\n" +
                "    topTextureCoordinate = textCoordinate.xy - heightStep;\n" +
                "    topLeftTextureCoordinate = textCoordinate.xy - widthHeightStep;\n" +
                "    topRightTextureCoordinate = textCoordinate.xy + widthNegativeHeightStep;\n" +
                "\n" +
                "    bottomTextureCoordinate = textCoordinate.xy + heightStep;\n" +
                "    bottomLeftTextureCoordinate = textCoordinate.xy - widthNegativeHeightStep;\n" +
                "    bottomRightTextureCoordinate = textCoordinate.xy + widthHeightStep;\n" +
                "}"
        const val DEFAULT = 1.0F
    }


    private var uniformTexelWidthLocation = 0
    private var uniformTexelHeightLocation = 0

    private var hasOverriddenImageSizeFactor = false
    private var texelWidth = 0f
    private var texelHeight = 0f
    private var lineSize = DEFAULT

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        uniformTexelWidthLocation = getUniformLocation("texelWidth")
        uniformTexelHeightLocation = getUniformLocation("texelHeight")
        if (0f != texelWidth) {
            updateTexelValues()
        }
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        super.onSurfaceChanged(width, height)
        if (!hasOverriddenImageSizeFactor) {
            setLineSize(lineSize)
        }
    }


    fun setTexelWidth(texelWidth: Float) {
        hasOverriddenImageSizeFactor = true
        this.texelWidth = texelWidth
        setUniformLocation(uniformTexelWidthLocation, texelWidth)
    }

    fun setTexelHeight(texelHeight: Float) {
        hasOverriddenImageSizeFactor = true
        this.texelHeight = texelHeight
        setUniformLocation(uniformTexelHeightLocation, texelHeight)
    }

    fun setLineSize(size: Float) {
        lineSize = size
        texelWidth = size / width
        texelHeight = size / height
        TrickLog.d("setLineSize: $texelWidth, $texelHeight")
        updateTexelValues()
    }

    private fun updateTexelValues() {
        setUniformLocation(uniformTexelWidthLocation, texelWidth)
        setUniformLocation(uniformTexelHeightLocation, texelHeight)
    }

}