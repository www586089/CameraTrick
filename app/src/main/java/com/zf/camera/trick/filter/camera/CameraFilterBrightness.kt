package com.zf.camera.trick.filter.camera

import android.content.res.Resources


// 片段着色器代码
const val FRAGMENT_SHADER_BRIGHTNESS =
        "#extension GL_OES_EGL_image_external : require\n" +
        "precision mediump float;\n" +
        "uniform samplerExternalOES vTexture;\n" +
        "varying vec2 vTexCoordinate;\n" +
        "uniform lowp float brightness;\n" +
        "void main() {\n" +
        "   vec4 textureColor = texture2D(vTexture, vTexCoordinate);\n" +
        "   gl_FragColor = vec4(textureColor.rgb + vec3(brightness), textureColor.w);\n" +
        "}\n"

const val MAX_BRIGHTNESS = 1.0f
const val MIN_BRIGHTNESS = -1.0f
const val DEFAULT_BRIGHTNESS = 0f
class CameraFilterBrightness(res: Resources): CameraFilterBase(res, NO_FILTER_VERTEX_SHADER, FRAGMENT_SHADER_BRIGHTNESS) {
        var uBrightness = -1

        override fun onSurfaceCreated() {
                super.onSurfaceCreated()
                uBrightness = getUniformLocation("brightness")
                setBrightness(DEFAULT_BRIGHTNESS)
        }

        fun setBrightness(gamma: Float) {
                setUniformLocation(uBrightness, gamma)
        }

        override fun createAdjuster(): IAdjuster {
                return object : IAdjuster {
                        override fun adjust(percentage: Float) {
                                val gamma = range(MIN_BRIGHTNESS, MAX_BRIGHTNESS, percentage)
                                setBrightness(gamma)
                        }

                        override fun getDefaultProgress(): Float {
                                return ((DEFAULT_BRIGHTNESS - MIN_BRIGHTNESS) / (MAX_BRIGHTNESS - MIN_BRIGHTNESS)) * 100
                        }
                }
        }
}