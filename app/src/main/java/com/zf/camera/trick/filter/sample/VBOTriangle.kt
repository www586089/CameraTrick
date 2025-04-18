package com.zf.camera.trick.filter.sample

import android.opengl.GLES20
import com.zf.camera.trick.gl.GLESUtils.createProgram
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * 使用VBO绘制
 */
class VBOTriangle() {

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
    private var mVBO = -1
    private lateinit var vertexBuffer: Buffer

    init {

    }


    fun surfaceCreated() {
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
            .position(0)

        // 创建VBO
        val vboArray = ByteBuffer.allocateDirect(4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer().apply { position(0) }
        GLES20.glGenBuffers(1, vboArray)
        mVBO = vboArray[0]

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVBO)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.size * 4, vertexBuffer, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)


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

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVBO)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, 0)
        GLES20.glEnableVertexAttribArray(aPositionHandle)

        // 绘制三角形
        GLES20.glUseProgram(mProgram)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)


        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glUseProgram(0)
    }

    fun release() {
        if (0 != mProgram) {
            GLES20.glDeleteProgram(mProgram)
        }
        if (0 != mVBO) {
            GLES20.glDeleteBuffers(1, intArrayOf(mVBO), 0)
        }
    }
}