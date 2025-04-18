package com.zf.camera.trick.filter.sample

import android.opengl.GLES20
import com.zf.camera.trick.gl.GLESUtils.createProgram
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 正常操作
 */
class NormalTriangle() {

    private var vertices = floatArrayOf(
        -0.5f, -0.5f, 0.0f,
         0.5f, -0.5f, 0.0f,
         0.0f,  0.5f, 0.0f
    )

    // 顶点着色器代码
    private val vertexShaderCode = """
        attribute vec3 aPosition;
        void main() {
            gl_Position = vec4(aPosition, 1.0);
        }
        """

    // 片段着色器代码
    private val fragmentShaderCode = """
        precision mediump float;
        void main() {
            gl_FragColor = vec4(1.0, 0.5, 0.2, 1.0);
        }
        """

    private var mProgram = -1

    private var aPositionHandle = -1
    private lateinit var vertexBuffer: Buffer

    init {

    }


    fun surfaceCreated() {
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
            .position(0)

        mProgram = createProgram(vertexShaderCode, fragmentShaderCode)

        aPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
    }

    fun surfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    fun draw() {

        // 重新绘制背景色为黑色
        GLES20.glClearColor(0.2f, 0.5f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer)
        GLES20.glEnableVertexAttribArray(aPositionHandle)

        // 绘制三角形
        GLES20.glUseProgram(mProgram)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glUseProgram(0)
    }

    fun release() {
        if (0 != mProgram) {
            GLES20.glDeleteProgram(mProgram)
        }
    }
}