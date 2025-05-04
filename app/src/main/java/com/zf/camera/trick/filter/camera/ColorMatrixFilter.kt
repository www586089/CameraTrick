package com.zf.camera.trick.filter.camera

import android.content.res.Resources


// 片段着色器代码
const val FRAGMENT_SHADER_COLOR_MATRIX =
    "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES vTexture;\n" +
            "varying vec2 vTexCoordinate;\n" +

            "uniform lowp float intensity;\n" +
            "uniform lowp mat4 colorMatrix;\n" +

            "void main() {\n" +
            "   vec4 textureColor = texture2D(vTexture, vTexCoordinate);\n" +
            "   vec4 outColor = textureColor * colorMatrix;\n" +
            "   gl_FragColor = outColor * intensity + (1.0 - intensity) * textureColor;\n" +
            "}\n"

open class ColorMatrixFilter(res: Resources): CameraFilterBase(res, NO_FILTER_VERTEX_SHADER, FRAGMENT_SHADER_COLOR_MATRIX) {

    private var uIntensityHandle = -1
    private var uColorMatHandle = -1

    protected open var colorMatrix = floatArrayOf(
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    )

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        uIntensityHandle = getUniformLocation("intensity")
        uColorMatHandle = getUniformLocation("colorMatrix")
        setUniformLocation(uIntensityHandle, 1.0f)
        setUniformMat4fv(uColorMatHandle, colorMatrix)
    }

    public fun setIntensity(intensity: Float) {
        setUniformLocation(uIntensityHandle, intensity)
    }

}