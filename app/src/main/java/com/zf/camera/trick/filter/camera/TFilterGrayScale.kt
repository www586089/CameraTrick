package com.zf.camera.trick.filter.camera

import android.content.res.Resources


open class TFilterGrayScale(res: Resources, vertexShader: String, fragmentShader: String) :
    TFilterBase(res, vertexShader, fragmentShader) {

    companion object {
        private const val vertexShader = NO_FILTER_VERTEX_SHADER
        private const val fragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +

            "precision mediump float;\n" +
            "uniform samplerExternalOES uTexture;\n" +
            "varying vec2 vTexCoordinate;\n" +

            "const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);\n" +

            "void main() {\n" +
            "   vec4 textureColor = texture2D(uTexture, vTexCoordinate);\n" +
            "   float luminance = dot(textureColor.rgb, W);\n" +
            "   gl_FragColor = vec4(vec3(luminance), textureColor.a);\n" +
            "}\n"
    }

    constructor(res: Resources) : this(res, vertexShader, fragmentShader)
}