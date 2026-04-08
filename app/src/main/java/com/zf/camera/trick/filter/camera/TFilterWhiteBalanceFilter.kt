package com.zf.camera.trick.filter.camera

import android.content.res.Resources

/**
 * Adjusts the white balance of incoming image. <br>
 * <br>
 * temperature:
 * tint:
 */
class TFilterWhiteBalanceFilter(res: Resources) :
    TFilterBase(res, NO_FILTER_VERTEX_SHADER, fShader) {

    companion object {
        // 片段着色器代码
        const val fShader =  "" +
                "#extension GL_OES_EGL_image_external : require\n" +

                "uniform samplerExternalOES uTexture;\n" +
                "varying highp vec2 vTexCoordinate;\n" +
                " \n" +
                "uniform lowp float temperature;\n" +
                "uniform lowp float tint;\n" +
                "\n" +
                "const lowp vec3 warmFilter = vec3(0.93, 0.54, 0.0);\n" +
                "\n" +
                "const mediump mat3 RGBtoYIQ = mat3(0.299, 0.587, 0.114, 0.596, -0.274, -0.322, 0.212, -0.523, 0.311);\n" +
                "const mediump mat3 YIQtoRGB = mat3(1.0, 0.956, 0.621, 1.0, -0.272, -0.647, 1.0, -1.105, 1.702);\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "	lowp vec4 source = texture2D(uTexture, vTexCoordinate);\n" +
                "	\n" +
                "	mediump vec3 yiq = RGBtoYIQ * source.rgb; //adjusting tint\n" +
                "	yiq.b = clamp(yiq.b + tint*0.5226*0.1, -0.5226, 0.5226);\n" +
                "	lowp vec3 rgb = YIQtoRGB * yiq;\n" +
                "\n" +
                "	lowp vec3 processed = vec3(\n" +
                "		(rgb.r < 0.5 ? (2.0 * rgb.r * warmFilter.r) : (1.0 - 2.0 * (1.0 - rgb.r) * (1.0 - warmFilter.r))), //adjusting temperature\n" +
                "		(rgb.g < 0.5 ? (2.0 * rgb.g * warmFilter.g) : (1.0 - 2.0 * (1.0 - rgb.g) * (1.0 - warmFilter.g))), \n" +
                "		(rgb.b < 0.5 ? (2.0 * rgb.b * warmFilter.b) : (1.0 - 2.0 * (1.0 - rgb.b) * (1.0 - warmFilter.b))));\n" +
                "\n" +
                "	gl_FragColor = vec4(mix(rgb, processed, temperature), source.a);\n" +
                "}";

        const val MAX = 8000f
        const val MIN = 2000f
        const val DEFAULT = 1000f
    }

    private var temperatureLocation = 0
    private var temperature = 0f
    private var tintLocation = 0
    private var tint = 0f

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        temperatureLocation = getUniformLocation("temperature")
        tintLocation = getUniformLocation("tint")
    }

    fun setTemperature(temperature: Float) {
        this.temperature = temperature
        setUniformLocation(
            temperatureLocation,
            if (this.temperature < 5000) (0.0004 * (this.temperature - 5000.0)).toFloat() else (0.00006 * (this.temperature - 5000.0)).toFloat()
        )
    }

    fun setTint(tint: Float) {
        this.tint = tint
        setUniformLocation(tintLocation, (this.tint / 100.0).toFloat())
    }
    override fun createAdjuster(): IAdjuster {
        return object : IAdjuster {
            override fun adjust(percentage: Float) {
                setTemperature(range(MIN, MAX, percentage))
            }

            override fun getDefaultProgress(): Float {
                return (DEFAULT - MIN / (MAX - MIN) * 100f).toFloat()
            }
        }
    }
}