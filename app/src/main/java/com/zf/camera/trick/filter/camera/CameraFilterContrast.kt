package com.zf.camera.trick.filter.camera

import android.content.res.Resources


// 片段着色器代码
const val FRAGMENT_SHADER_CONTRAST =
        "#extension GL_OES_EGL_image_external : require\n" +
        "precision mediump float;\n" +
        "uniform samplerExternalOES vTexture;\n" +
        "uniform lowp float contrast;\n" +
        "varying vec2 vTexCoordinate;\n" +
        "void main() {\n" +
        "   vec4 textureColor = texture2D(vTexture, vTexCoordinate);\n" +
        "   gl_FragColor = vec4((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5), textureColor.w);\n" +
        "}\n"

const val CONTRAST_MIN = 0F
const val CONTRAST_MAX = 2F
const val CONTRAST_DEFAULT = 1F

class CameraFilterContrast(res: Resources): CameraFilterBase(res, NO_FILTER_VERTEX_SHADER, FRAGMENT_SHADER_CONTRAST) {

        private var uContrastHandle = -1
        override fun onSurfaceCreated() {
                super.onSurfaceCreated()
                uContrastHandle = getUniformLocation("contrast")
                setUniformLocation(uContrastHandle, CONTRAST_DEFAULT)
        }

        public fun setContrast(contrast: Float) {
                setUniformLocation(uContrastHandle, contrast)
        }
}