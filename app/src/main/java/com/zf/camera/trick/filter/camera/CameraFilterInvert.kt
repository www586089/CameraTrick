package com.zf.camera.trick.filter.camera

import android.content.res.Resources


class CameraFilterInvert(res: Resources) :
    CameraFilterBase(res, NO_FILTER_VERTEX_SHADER, FRAGMENT_SHADER_INVERT) {
    companion object {
        // 片段着色器代码
        const val FRAGMENT_SHADER_INVERT =
            "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES vTexture;\n" +
                "varying vec2 vTexCoordinate;\n" +
                "void main() {\n" +
                "   vec4 textureColor = texture2D(vTexture, vTexCoordinate);\n" +
                "   gl_FragColor = vec4(1.0 - textureColor.rgb, textureColor.w);\n" +
                "}\n"
    }
}