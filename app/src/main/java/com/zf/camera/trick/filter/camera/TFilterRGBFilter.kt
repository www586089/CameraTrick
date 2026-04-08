package com.zf.camera.trick.filter.camera

import android.content.res.Resources

class TFilterRGBFilter(res: Resources) :
    TFilterBase(res, NO_FILTER_VERTEX_SHADER, fShader) {

    companion object {
        // 片段着色器代码
        const val fShader = "" +
                "  #extension GL_OES_EGL_image_external : require\n" +
                "  varying highp vec2 vTexCoordinate;\n" +
                "  \n" +
                "  uniform samplerExternalOES uTexture;\n" +
                "  uniform highp float red;\n" +
                "  uniform highp float green;\n" +
                "  uniform highp float blue;\n" +
                "  \n" +
                "  void main()\n" +
                "  {\n" +
                "      highp vec4 textureColor = texture2D(uTexture, vTexCoordinate);\n" +
                "      \n" +
                "      gl_FragColor = vec4(textureColor.r * red, textureColor.g * green, textureColor.b * blue, 1.0);\n" +
                "  }\n";
    }

    private var redLocation = 0
    private var red = 0f
    private var greenLocation = 0
    private var green = 0f
    private var blueLocation = 0
    private var blue = 0f

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        redLocation = getUniformLocation("red")
        greenLocation = getUniformLocation("green")
        blueLocation = getUniformLocation("blue")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        super.onSurfaceChanged(width, height)
        setRed(1.0f)
        setGreen(1.0f)
        setBlue(1.0f)
    }

    fun setRed(red: Float) {
        this.red = red
        setUniformLocation(redLocation, this.red)
    }

    fun setGreen(green: Float) {
        this.green = green
        setUniformLocation(greenLocation, this.green)
    }

    fun setBlue(blue: Float) {
        this.blue = blue
        setUniformLocation(blueLocation, this.blue)
    }
    override fun createAdjuster(): IAdjuster {
        return object : IAdjuster {
            override fun adjust(percentage: Float) {
                setRed(range(0.0f, 1.0f, percentage))
            }

            override fun getDefaultProgress(): Float {
                return 0f
            }
        }
    }
}