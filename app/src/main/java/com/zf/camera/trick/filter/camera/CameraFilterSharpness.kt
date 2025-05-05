package com.zf.camera.trick.filter.camera

import android.content.res.Resources


class CameraFilterSharpness(res: Resources, vertexShader: String, fragmentShader: String): CameraFilterBase(res, vertexShader, fragmentShader) {

    companion object {
        private const val DEFAULT_SHARPNESS = 0.0f
        private const val MAX_SHARPNESS = 4.0f
        private const val MIN_SHARPNESS = -4.0f

        private const val vertexShader =
                "uniform mat4 uMVPMatrix;\n" +
                // 顶点坐标
                "attribute vec4 aPosition;\n" +
                "uniform mat4 uTexPMatrix;\n" +
                // 纹理坐标
                "attribute vec4 aTexCoordinate;\n" +

                "\n" +
                "uniform float uWidthFactor; \n" +
                "uniform float uHeightFactor; \n" +
                "uniform float uSharpness;\n" +
                "\n" +
                "varying vec2 vTexCoordinate;\n" +
                "varying vec2 vLeftTextureCoordinate;\n" +
                "varying vec2 vRightTextureCoordinate; \n" +
                "varying vec2 vTopTextureCoordinate;\n" +
                "varying vec2 vBottomTextureCoordinate;\n" +
                "\n" +
                "varying float vCenterMultiplier;\n" +
                "varying float vEdgeMultiplier;\n" +
                "\n" +

                "void main() {\n" +
                "    gl_Position = uMVPMatrix * aPosition;\n" +
                "    vTexCoordinate = (uTexPMatrix * aTexCoordinate).xy;\n" +

                "    \n" +
                "    mediump vec2 widthStep = vec2(uWidthFactor, 0.0);\n" +
                "    mediump vec2 heightStep = vec2(0.0, uHeightFactor);\n" +
                "    \n" +
                "    vLeftTextureCoordinate = vTexCoordinate.xy - widthStep;\n" +
                "    vRightTextureCoordinate = vTexCoordinate.xy + widthStep;\n" +
                "    vTopTextureCoordinate = vTexCoordinate.xy + heightStep;     \n" +
                "    vBottomTextureCoordinate = vTexCoordinate.xy - heightStep;\n" +
                "    \n" +
                "    vCenterMultiplier = 1.0 + 4.0 * uSharpness;\n" +
                "    vEdgeMultiplier = uSharpness;\n" +
                "}"

        private const val fragmentShader =
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES vTexture;\n" +
                "varying vec2 vTexCoordinate;\n" +
                "\n" +
                "varying highp vec2 vLeftTextureCoordinate;\n" +
                "varying highp vec2 vRightTextureCoordinate; \n" +
                "varying highp vec2 vTopTextureCoordinate;\n" +
                "varying highp vec2 vBottomTextureCoordinate;\n" +
                "\n" +
                "varying highp float vCenterMultiplier;\n" +
                "varying highp float vEdgeMultiplier;\n" +
                "\n" +
                "void main() {\n" +
                "    mediump vec3 textureColor = texture2D(vTexture, vTexCoordinate).rgb;\n" +
                "    mediump vec3 leftTextureColor = texture2D(vTexture, vLeftTextureCoordinate).rgb;\n" +
                "    mediump vec3 rightTextureColor = texture2D(vTexture, vRightTextureCoordinate).rgb;\n" +
                "    mediump vec3 topTextureColor = texture2D(vTexture, vTopTextureCoordinate).rgb;\n" +
                "    mediump vec3 bottomTextureColor = texture2D(vTexture, vBottomTextureCoordinate).rgb;\n" +
                "\n" +
                "    gl_FragColor = vec4((textureColor * vCenterMultiplier - (leftTextureColor * vEdgeMultiplier + rightTextureColor * vEdgeMultiplier + topTextureColor * vEdgeMultiplier + bottomTextureColor * vEdgeMultiplier)), texture2D(vTexture, vBottomTextureCoordinate).w);\n" +
                "}\n"
    }

    constructor(res: Resources) : this(res, vertexShader, fragmentShader)

    private var uWidthFactorHandle = -1
    private var uHeightFactorHandle = -1
    private var uSharpnessHandle = -1

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()

        uWidthFactorHandle = getUniformLocation("uWidthFactor")
        uHeightFactorHandle = getUniformLocation("uHeightFactor")
        uSharpnessHandle = getUniformLocation("uSharpness")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        super.onSurfaceChanged(width, height)
        setUniformLocation(uWidthFactorHandle, 1.0f / width.toFloat())
        setUniformLocation(uHeightFactorHandle, 1.0f / height.toFloat())
        setUniformLocation(uSharpnessHandle, DEFAULT_SHARPNESS)
    }

    private fun setSharpness(sharpness: Float) {
        setUniformLocation(uSharpnessHandle, sharpness)
    }

    override fun createAdjuster(): IAdjuster {
        return object : IAdjuster {
            override fun adjust(percentage: Float) {
                val sharpness = range(MIN_SHARPNESS, MAX_SHARPNESS, percentage)
                setSharpness(sharpness)
            }

            override fun getDefaultProgress(): Float {
                return 100F * (DEFAULT_SHARPNESS - MIN_SHARPNESS) / (MAX_SHARPNESS - MIN_SHARPNESS)
            }
        }
    }
}