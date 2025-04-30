package com.zf.camera.trick.filter.camera

import android.content.res.Resources


// 片段着色器代码
const val FRAGMENT_SHADER_GAMMA =
        "#extension GL_OES_EGL_image_external : require\n" +
        "precision mediump float;\n" +
        "uniform samplerExternalOES vTexture;\n" +
        "varying vec2 vTexCoordinate;\n" +
        "uniform lowp float gamma;\n" +
        "void main() {\n" +
        "   vec4 textureColor = texture2D(vTexture, vTexCoordinate);\n" +
        "   gl_FragColor = vec4(pow(textureColor.rgb, vec3(gamma)), textureColor.w);\n" +
        "}\n"

const val MAX_GAMMA = 3f
const val MIN_GAMMA = 0f
const val DEFAULT_GAMMA = 1.3f
class CameraFilterGamma(res: Resources): CameraFilterBase(res, NO_FILTER_VERTEX_SHADER, FRAGMENT_SHADER_GAMMA) {
        var uGammaHandle = -1

        override fun onSurfaceCreated() {
                super.onSurfaceCreated()
                uGammaHandle = getUniformLocation("gamma")
                setGamma(DEFAULT_GAMMA)
        }

        fun setGamma(gamma: Float) {
                setUniformLocation(uGammaHandle, gamma)
        }
}