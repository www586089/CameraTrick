package com.zf.camera.trick.filter.camera

import android.content.res.Resources

class CameraFilterPosterize(
    res: Resources,
    vertexShader: String,
    fragmentShader: String
) : CameraFilterBase(res, vertexShader, fragmentShader) {

    companion object {
        // 片段着色器代码
        const val vertexShader = NO_FILTER_VERTEX_SHADER
        const val fragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "varying highp vec2 vTexCoordinate;\n" +
                    "\n" +
                    "uniform samplerExternalOES vTexture;\n" +
                    "uniform highp float colorLevels;\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "   highp vec4 textureColor = texture2D(vTexture, vTexCoordinate);\n" +
                    "   \n" +
                    "   gl_FragColor = floor((textureColor * colorLevels) + vec4(0.5)) / colorLevels;\n" +
                    "}";
    }

    private var glUniformColorLevels = -1
    private var colorLevels = 1f

    constructor(res: Resources) : this(res, vertexShader, fragmentShader)

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        glUniformColorLevels = getUniformLocation("colorLevels")
        setColorLevels(1f)
    }

    fun setColorLevels(levels: Float) {
        this.colorLevels = levels
        setUniformLocation(glUniformColorLevels, levels)
    }

    override fun createAdjuster(): IAdjuster {
        return object : IAdjuster {
            override fun adjust(percentage: Float) {
                // In theorie to 256, but only first 50 are interesting
                setColorLevels(range(1f, 50f, percentage))
            }

            override fun getDefaultProgress(): Float {
                return 0f
            }
        }
    }
}