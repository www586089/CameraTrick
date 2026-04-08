package com.zf.camera.trick.filter.camera

import android.content.res.Resources

class TFilterExposureFilter(
    res: Resources,
    vertexShader: String,
    fragmentShader: String
) : TFilterBase(res, vertexShader, fragmentShader) {

    companion object {
        // 片段着色器代码
        const val vertexShader = NO_FILTER_VERTEX_SHADER
        const val fragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES uTexture;\n" +
                    "uniform lowp float contrast;\n" +
                    "varying vec2 vTexCoordinate;\n" +
                    "void main() {\n" +
                    "   vec4 textureColor = texture2D(uTexture, vTexCoordinate);\n" +
                    "   gl_FragColor = vec4((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5), textureColor.w);\n" +
                    "}\n"
        const val fShader = "" +
                "#extension GL_OES_EGL_image_external : require\n" +
                " varying highp vec2 vTexCoordinate;\n" +
                " \n" +
                " uniform samplerExternalOES uTexture;\n" +
                " uniform highp float exposure;\n" +
                " \n" +
                " void main()\n" +
                " {\n" +
                "     highp vec4 textureColor = texture2D(uTexture, vTexCoordinate);\n" +
                "     \n" +
                "     gl_FragColor = vec4(textureColor.rgb * pow(2.0, exposure), textureColor.w);\n" +
                " } ";

        const val CONTRAST_MIN = -10F
        const val CONTRAST_MAX = 10F
        const val CONTRAST_DEFAULT = 0F
    }

    constructor(res: Resources) : this(res, vertexShader, fShader)

    private var exposureLocation = -1
    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        exposureLocation = getUniformLocation("exposure")
        setUniformLocation(exposureLocation, CONTRAST_DEFAULT)
    }

    public fun setExposure(contrast: Float) {
        setUniformLocation(exposureLocation, contrast)
    }

    override fun createAdjuster(): IAdjuster {
        return object : IAdjuster {
            override fun adjust(percentage: Float) {
                val contrast = range(CONTRAST_MIN, CONTRAST_MAX, percentage = percentage)
                setExposure(contrast)
            }

            override fun getDefaultProgress(): Float {
                return (((CONTRAST_DEFAULT - CONTRAST_MIN) / (CONTRAST_MAX - CONTRAST_MIN)) * 100)
            }
        }
    }
}