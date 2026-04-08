package com.zf.camera.trick.filter.camera

import android.content.res.Resources

class CameraFilterMonochromeFilter(res: Resources) :
    CameraFilterBase(res, NO_FILTER_VERTEX_SHADER, fShader) {

    companion object {
        // 片段着色器代码
        const val fShader = "" +
                "#extension GL_OES_EGL_image_external : require\n" +
                " precision lowp float;\n" +
                "  \n" +
                "  varying highp vec2 vTexCoordinate;\n" +
                "  \n" +
                "  uniform samplerExternalOES uTexture;\n" +
                "  uniform float intensity;\n" +
                "  uniform vec3 filterColor;\n" +
                "  \n" +
                "  const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);\n" +
                "  \n" +
                "  void main()\n" +
                "  {\n" +
                " 	//desat, then apply overlay blend\n" +
                " 	lowp vec4 textureColor = texture2D(uTexture, vTexCoordinate);\n" +
                " 	float luminance = dot(textureColor.rgb, luminanceWeighting);\n" +
                " 	\n" +
                " 	lowp vec4 desat = vec4(vec3(luminance), 1.0);\n" +
                " 	\n" +
                " 	//overlay\n" +
                " 	lowp vec4 outputColor = vec4(\n" +
                "                                  (desat.r < 0.5 ? (2.0 * desat.r * filterColor.r) : (1.0 - 2.0 * (1.0 - desat.r) * (1.0 - filterColor.r))),\n" +
                "                                  (desat.g < 0.5 ? (2.0 * desat.g * filterColor.g) : (1.0 - 2.0 * (1.0 - desat.g) * (1.0 - filterColor.g))),\n" +
                "                                  (desat.b < 0.5 ? (2.0 * desat.b * filterColor.b) : (1.0 - 2.0 * (1.0 - desat.b) * (1.0 - filterColor.b))),\n" +
                "                                  1.0\n" +
                "                                  );\n" +
                " 	\n" +
                " 	//which is better, or are they equal?\n" +
                " 	gl_FragColor = vec4( mix(textureColor.rgb, outputColor.rgb, intensity), textureColor.a);\n" +
                "  }";
    }


    private var intensityLocation = 0
    private var intensity = 1.0f
    private var filterColorLocation = 0
    //RGBA
    private var color: FloatArray = floatArrayOf(0.2617f, 0.45f, 0.9f, 1.0f)

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()

        intensityLocation = getUniformLocation("intensity")
        filterColorLocation = getUniformLocation("filterColor")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        super.onSurfaceChanged(width, height)
        setColor(color)
    }

    fun setIntensity(intensity: Float) {
        this.intensity = intensity
        setUniformLocation(intensityLocation, this.intensity)
    }

    fun setColor(color: FloatArray?) {
        this.color = color!!
        setColor(this.color[0], this.color[1], this.color[2])
    }

    fun setColor(red: Float, green: Float, blue: Float) {
        setUniformLocation3fv(filterColorLocation, floatArrayOf(red, green, blue))
    }
    override fun createAdjuster(): IAdjuster {
        return object : IAdjuster {
            override fun adjust(percentage: Float) {
                setIntensity(range(0.0f, 1.0f, percentage))
            }

            override fun getDefaultProgress(): Float {
                return 100f
            }
        }
    }
}