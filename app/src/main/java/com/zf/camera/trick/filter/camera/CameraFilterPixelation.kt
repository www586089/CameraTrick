package com.zf.camera.trick.filter.camera

import android.content.res.Resources


// 片段着色器代码
const val FRAGMENT_SHADER_PIXELATION =
        "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES vTexture;\n" +
                "varying vec2 vTexCoordinate;\n" +
                "uniform float pixel;\n" +
                "uniform float widthFactor;\n" +
                "uniform float heightFactor;\n" +

                "void main() {\n" +
                "    vec2 uv  = vTexCoordinate.xy;\n" +
                "    float dx = pixel * widthFactor;\n" +
                "    float dy = pixel * heightFactor;\n" +
                "    vec2 coord = vec2(dx * floor(uv.x / dx), dy * floor(uv.y / dy));\n" +
                "    vec4 textureColor = texture2D(vTexture, coord);\n" +
                "    gl_FragColor = vec4(textureColor.rgb, 1.0);\n" +
                "}\n"
const val PIXELATION_MIN = 1.0f
const val PIXELATION_MAX = 100.0f
const val PIXELATION_DEFAULT = 1.0f
class CameraFilterPixelation(res: Resources): CameraFilterBase(res,
    NO_FILTER_VERTEX_SHADER, FRAGMENT_SHADER_PIXELATION) {

        private var uPixelHandle = -1
        private var uWidthFactorHandle = -1
        private var uHeightFactorHandle = -1

        override fun onSurfaceCreated() {
                super.onSurfaceCreated()

                uPixelHandle = getUniformLocation("pixel")
                uWidthFactorHandle = getUniformLocation("widthFactor")
                uHeightFactorHandle = getUniformLocation("heightFactor")

                setUniformLocation(uPixelHandle, PIXELATION_DEFAULT)
        }

        override fun onSurfaceChanged(width: Int, height: Int) {
                super.onSurfaceChanged(width, height)
                setUniformLocation(uWidthFactorHandle, 1.0f / width)
                setUniformLocation(uHeightFactorHandle, 1.0f / height)
        }

        public fun setPixel(pixel: Float) {
                setUniformLocation(uPixelHandle, pixel)
        }
}