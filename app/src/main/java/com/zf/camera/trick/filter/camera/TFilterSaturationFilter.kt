package com.zf.camera.trick.filter.camera

import android.content.res.Resources

class TFilterSaturationFilter(res: Resources) :
    TFilterBase(res, NO_FILTER_VERTEX_SHADER, fShader) {

    companion object {
        // 片段着色器代码
        const val fShader = "" +
                "#extension GL_OES_EGL_image_external : require\n" +

                " varying highp vec2 vTexCoordinate;\n" +
                " \n" +
                " uniform samplerExternalOES uTexture;\n" +
                " uniform lowp float saturation;\n" +
                " \n" +
                " // Values from \"Graphics Shaders: Theory and Practice\" by Bailey and Cunningham\n" +
                " const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);\n" +
                " \n" +
                " void main()\n" +
                " {\n" +
                "    lowp vec4 textureColor = texture2D(uTexture, vTexCoordinate);\n" +
                "    lowp float luminance = dot(textureColor.rgb, luminanceWeighting);\n" +
                "    lowp vec3 greyScaleColor = vec3(luminance);\n" +
                "    \n" +
                "    gl_FragColor = vec4(mix(greyScaleColor, textureColor.rgb, saturation), textureColor.w);\n" +
                "     \n" +
                " }";
        const val START = 0.0F
        const val END = 2.0F
        const val DEFAULT = 1.0F
    }

    var saturationLocation = -1

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        saturationLocation = getUniformLocation("saturation")
        setSaturation(1.0f)
    }

    fun setSaturation(saturation: Float) {
        setUniformLocation(saturationLocation, saturation)
    }

    override fun createAdjuster(): IAdjuster {
        return object : IAdjuster {
            override fun adjust(percentage: Float) {
                val gamma = range(START, END, percentage)
                setSaturation(gamma)
            }

            override fun getDefaultProgress(): Float {
                return (DEFAULT / (END - START)) * 100f
            }
        }
    }
}