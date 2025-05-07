package com.zf.camera.trick.filter.camera

import android.content.res.Resources


class CameraFilterContrast(
    res: Resources,
    vertexShader: String,
    fragmentShader: String
) : CameraFilterBase(res, vertexShader, fragmentShader) {

    companion object {
        // 片段着色器代码
        const val vertexShader = NO_FILTER_VERTEX_SHADER
        const val fragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES vTexture;\n" +
                "uniform lowp float contrast;\n" +
                "varying vec2 vTexCoordinate;\n" +
                "void main() {\n" +
                "   vec4 textureColor = texture2D(vTexture, vTexCoordinate);\n" +
                "   gl_FragColor = vec4((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5), textureColor.w);\n" +
                "}\n"

        const val CONTRAST_MIN = 0F
        const val CONTRAST_MAX = 2F
        const val CONTRAST_DEFAULT = 1F
    }

    constructor(res: Resources) : this(res, vertexShader, fragmentShader)

    private var uContrastHandle = -1
    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        uContrastHandle = getUniformLocation("contrast")
        setUniformLocation(uContrastHandle, CONTRAST_DEFAULT)
    }

    public fun setContrast(contrast: Float) {
        setUniformLocation(uContrastHandle, contrast)
    }

    override fun createAdjuster(): IAdjuster {
        return object : IAdjuster {
            override fun adjust(percentage: Float) {
                val contrast = range(CONTRAST_MIN, CONTRAST_MAX, percentage = percentage)
                setContrast(contrast)
            }

            override fun getDefaultProgress(): Float {
                return ((CONTRAST_DEFAULT / CONTRAST_MAX) * 100)
            }
        }
    }
}