package com.zf.camera.trick.filter.camera

import android.content.res.Resources


// 片段着色器代码
const val FRAGMENT_SHADER_HUE =
        "#extension GL_OES_EGL_image_external : require\n" +
        "precision mediump float;\n" +
        "uniform samplerExternalOES vTexture;\n" +
        "varying vec2 vTexCoordinate;\n" +

        "uniform mediump float hueAdjust;\n" +
        "const highp vec4 kRGBToYPrime = vec4 (0.299, 0.587, 0.114, 0.0);\n" +
        "const highp vec4 kRGBToI = vec4 (0.595716, -0.274453, -0.321263, 0.0);\n" +
        "const highp vec4 kRGBToQ = vec4 (0.211456, -0.522591, 0.31135, 0.0);\n" +
        "\n" +
        "const highp vec4 kYIQToR = vec4 (1.0, 0.9563, 0.6210, 0.0);\n" +
        "const highp vec4 kYIQToG = vec4 (1.0, -0.2721, -0.6474, 0.0);\n" +
        "const highp vec4 kYIQToB = vec4 (1.0, -1.1070, 1.7046, 0.0);\n" +
        "\n" +
        "void main() {\n" +
        "    // Sample the input pixel\n" +
        "    highp vec4 color = texture2D(vTexture, vTexCoordinate);\n" +
        "\n" +
        "    // Convert to YIQ\n" +
        "    highp float YPrime = dot (color, kRGBToYPrime);\n" +
        "    highp float I = dot (color, kRGBToI);\n" +
        "    highp float Q = dot (color, kRGBToQ);\n" +
        "\n" +
        "    // Calculate the hue and chroma\n" +
        "    highp float hue = atan (Q, I);\n" +
        "    highp float chroma = sqrt (I * I + Q * Q);\n" +
        "\n" +
        "    // Make the user's adjustments\n" +
        "    hue += (-hueAdjust); //why negative rotation?\n" +
        "\n" +
        "    // Convert back to YIQ\n" +
        "    Q = chroma * sin (hue);\n" +
        "    I = chroma * cos (hue);\n" +
        "\n" +
        "    // Convert back to RGB\n" +
        "    highp vec4 yIQ = vec4 (YPrime, I, Q, 0.0);\n" +
        "    color.r = dot (yIQ, kYIQToR);\n" +
        "    color.g = dot (yIQ, kYIQToG);\n" +
        "    color.b = dot (yIQ, kYIQToB);\n" +
        "\n" +
        "    // Save the result\n" +
        "    gl_FragColor = color;\n" +
        "}\n"

const val MIN_HUE = 0f
const val MAX_HUE = 360f
const val DEFAULT_HUE = 90f
class CameraFilterHue(res: Resources): CameraFilterBase(res, NO_FILTER_VERTEX_SHADER, FRAGMENT_SHADER_HUE) {

        private var uHueAdjustHandle = -1

        override fun onSurfaceCreated() {
                super.onSurfaceCreated()
                uHueAdjustHandle = getUniformLocation("hueAdjust")
                setHue(DEFAULT_HUE)
        }

        fun setHue(hue: Float) {
                val hueAdjust = ((hue % MAX_HUE) / 180f) * Math.PI.toFloat()
                setUniformLocation(uHueAdjustHandle, hueAdjust)
        }

}