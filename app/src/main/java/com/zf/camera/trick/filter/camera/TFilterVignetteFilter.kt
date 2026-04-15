package com.zf.camera.trick.filter.camera

import android.content.res.Resources
import android.graphics.PointF

/**
 * Performs a vignetting effect, fading out the image at the edges
 * x:
 * y: The directional intensity of the vignetting, with a default of x = 0.75, y = 0.5
 */
class TFilterVignetteFilter(res: Resources) :
    TFilterBase(res, NO_FILTER_VERTEX_SHADER, fShader) {

    companion object {
        // 片段着色器代码
        const val fShader =  "" +
                "#extension GL_OES_EGL_image_external : require\n" +

                " uniform samplerExternalOES uTexture;\n" +
                " varying highp vec2 vTexCoordinate;\n" +
                " \n" +
                " uniform lowp vec2 vignetteCenter;\n" +
                " uniform lowp vec3 vignetteColor;\n" +
                " uniform highp float vignetteStart;\n" +
                " uniform highp float vignetteEnd;\n" +
                " \n" +
                " void main()\n" +
                " {\n" +
                "     /*\n" +
                "     lowp vec3 rgb = texture2D(uTexture, vTexCoordinate).rgb;\n" +
                "     lowp float d = distance(vTexCoordinate, vec2(0.5,0.5));\n" +
                "     rgb *= (1.0 - smoothstep(vignetteStart, vignetteEnd, d));\n" +
                "     gl_FragColor = vec4(vec3(rgb),1.0);\n" +
                "      */\n" +
                "     \n" +
                "     lowp vec3 rgb = texture2D(uTexture, vTexCoordinate).rgb;\n" +
                "     lowp float d = distance(vTexCoordinate, vec2(vignetteCenter.x, vignetteCenter.y));\n" +
                "     lowp float percent = smoothstep(vignetteStart, vignetteEnd, d);\n" +
                "     gl_FragColor = vec4(mix(rgb.x, vignetteColor.x, percent), mix(rgb.y, vignetteColor.y, percent), mix(rgb.z, vignetteColor.z, percent), 1.0);\n" +
                " }";

        const val MAX = 1f
        const val MIN = 0f
        const val DEFAULT = 0.3f
    }

    private var vignetteCenterLocation = 0
    private lateinit var vignetteCenter: PointF
    private var vignetteColorLocation = 0
    private lateinit var vignetteColor: FloatArray
    private var vignetteStartLocation = 0
    private var vignetteStart = 0f
    private var vignetteEndLocation = 0
    private var vignetteEnd = 0f

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        vignetteCenter = PointF(0.5f, 0.5f)
        vignetteColor = floatArrayOf(0f, 0f, 0f)
        vignetteStart = 0.3f
        vignetteEnd = 0.75f

        vignetteCenterLocation = getUniformLocation("vignetteCenter")
        vignetteColorLocation = getUniformLocation("vignetteColor")
        vignetteStartLocation = getUniformLocation("vignetteStart")
        vignetteEndLocation = getUniformLocation("vignetteEnd")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        super.onSurfaceChanged(width, height)
        setVignetteCenter(vignetteCenter)
        setVignetteColor(vignetteColor)
        setVignetteStart(vignetteStart)
        setVignetteEnd(vignetteEnd)
    }


    fun setVignetteCenter(vignetteCenter: PointF) {
        this.vignetteCenter = vignetteCenter
        setUniformLocation2fv(vignetteCenterLocation, this.vignetteCenter)
    }

    fun setVignetteColor(vignetteColor: FloatArray?) {
        this.vignetteColor = vignetteColor!!
        setUniformLocation3fv(vignetteColorLocation, this.vignetteColor)
    }

    fun setVignetteStart(vignetteStart: Float) {
        this.vignetteStart = vignetteStart
        setUniformLocation(vignetteStartLocation, this.vignetteStart)
    }

    fun setVignetteEnd(vignetteEnd: Float) {
        this.vignetteEnd = vignetteEnd
        setUniformLocation(vignetteEndLocation, this.vignetteEnd)
    }
    override fun createAdjuster(): IAdjuster {
        return object : IAdjuster {
            override fun adjust(percentage: Float) {
                setVignetteStart(range(MIN, MAX, percentage))
            }

            override fun getDefaultProgress(): Float {
                return (DEFAULT - MIN / (MAX - MIN) * 100f)
            }
        }
    }
}