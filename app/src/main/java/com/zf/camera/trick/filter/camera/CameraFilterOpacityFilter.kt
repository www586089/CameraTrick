package com.zf.camera.trick.filter.camera

import android.content.res.Resources

/**
 * todo// opacity的修改无效，很奇怪
 * Adjusts the alpha channel of the incoming image
 * opacity: The value to multiply the incoming alpha channel for each pixel by (0.0 - 1.0, with 1.0 as the default)
 */
class CameraFilterOpacityFilter(res: Resources) :
    CameraFilterBase(res, NO_FILTER_VERTEX_SHADER, fShader) {

    companion object {
        // 片段着色器代码
        const val fShader =  "" +
                "  #extension GL_OES_EGL_image_external : require\n" +
                "  varying highp vec2 vTexCoordinate;\n" +
                "  \n" +
                "  uniform samplerExternalOES uTexture;\n" +
                "  uniform lowp float opacity;\n" +
                "  \n" +
                "  void main()\n" +
                "  {\n" +
                "      lowp vec4 textureColor = texture2D(uTexture, vTexCoordinate);\n" +
                "      \n" +
                "      gl_FragColor = vec4(textureColor.rgb, textureColor.a * opacity);\n" +
                "  }\n";

        const val MIN = 0f
        const val MAX = 1f
        const val DEFAULT = 0.5f
    }

    private var opacityLocation = 0
    private var opacity = 0f

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        opacityLocation = getUniformLocation("opacity")
        setOpacity(1.0f)
    }

    fun setOpacity(opacity: Float) {
        this.opacity = opacity
//        Log.d("CameraFilterOpacityFilter", "setOpacity: opacity=$opacity")
        setUniformLocation(opacityLocation, this.opacity)
    }

    override fun createAdjuster(): IAdjuster {
        return object : IAdjuster {
            override fun adjust(percentage: Float) {
                setOpacity(range(MIN, MAX, percentage))
            }

            override fun getDefaultProgress(): Float {
                return ((DEFAULT - MIN) / (MAX - MIN)) * 100
            }
        }
    }

}