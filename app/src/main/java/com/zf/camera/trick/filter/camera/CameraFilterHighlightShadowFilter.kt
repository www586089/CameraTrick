package com.zf.camera.trick.filter.camera

import android.content.res.Resources

class CameraFilterHighlightShadowFilter(res: Resources) :
    CameraFilterBase(res, NO_FILTER_VERTEX_SHADER, fShader) {

    companion object {
        // 片段着色器代码
        const val fShader = "" +
                "#extension GL_OES_EGL_image_external : require\n" +
                " uniform samplerExternalOES uTexture;\n" +
                " varying highp vec2 vTexCoordinate;\n" +
                "  \n" +
                " uniform lowp float shadows;\n" +
                " uniform lowp float highlights;\n" +
                " \n" +
                " const mediump vec3 luminanceWeighting = vec3(0.3, 0.3, 0.3);\n" +
                " \n" +
                " void main()\n" +
                " {\n" +
                " 	lowp vec4 source = texture2D(uTexture, vTexCoordinate);\n" +
                " 	mediump float luminance = dot(source.rgb, luminanceWeighting);\n" +
                " \n" +
                " 	mediump float shadow = clamp((pow(luminance, 1.0/(shadows+1.0)) + (-0.76)*pow(luminance, 2.0/(shadows+1.0))) - luminance, 0.0, 1.0);\n" +
                " 	mediump float highlight = clamp((1.0 - (pow(1.0-luminance, 1.0/(2.0-highlights)) + (-0.8)*pow(1.0-luminance, 2.0/(2.0-highlights)))) - luminance, -1.0, 0.0);\n" +
                " 	lowp vec3 result = vec3(0.0, 0.0, 0.0) + ((luminance + shadow + highlight) - 0.0) * ((source.rgb - vec3(0.0, 0.0, 0.0))/(luminance - 0.0));\n" +
                " \n" +
                " 	gl_FragColor = vec4(result.rgb, source.a);\n" +
                " }";
    }

    private var shadowsLocation = 0
    private var shadows = 0f
    private var highlightsLocation = 0
    private var highlights = 1.0f

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        shadowsLocation = getUniformLocation("shadows")
        highlightsLocation = getUniformLocation("highlights")
    }

    fun setHighlights(highlights: Float) {
        this.highlights = highlights
        setUniformLocation(highlightsLocation, this.highlights)
    }

    fun setShadows(shadows: Float) {
        this.shadows = shadows
        setUniformLocation(shadowsLocation, this.shadows)
    }

    override fun createAdjuster(): IAdjuster {
        return object : IAdjuster {
            override fun adjust(percentage: Float) {
                setHighlights(range(0f, 1f, percentage))
                setShadows(range(0f, 1f, percentage))
            }

            override fun getDefaultProgress(): Float {
                return 50f
            }
        }
    }

}