package com.zf.camera.trick.filter.sample

import android.content.Context
import android.opengl.GLES20
import com.zf.camera.trick.gl.GLESUtils.createProgram
import com.zf.camera.trick.utils.TrickLog
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

/**
 * 正常操作
 */
class NormalTriangle(val ctx: Context): IShape {

    companion object {
        const val TAG = "NormalTriangle"
    }
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
        
        uniform vec4 vertexColor;
        
        void main() {
            gl_FragColor = vertexColor;
        }
        """

    private var mProgram = -1

    private var aPositionHandle = -1
    private var uVertexColorHandle = -1
    private lateinit var vertexBuffer: Buffer

    private var curMSC = System.currentTimeMillis()

    init {

    }


    override fun onSurfaceCreated() {
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
            .position(0)
        val nrAttributes = IntArray(1)
        //least 16 4-component vertex attributes available
        GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_ATTRIBS, nrAttributes, 0)
        TrickLog.d(TAG, "onSurfaceCreated-> Maximum nr of vertex attributes supported: ${nrAttributes[0]}")

        mProgram = createProgram(vertexShaderCode, fragmentShaderCode)

        aPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
        uVertexColorHandle = GLES20.glGetUniformLocation(mProgram, "vertexColor")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun drawFrame() {

        // 重新绘制背景色为黑色
        GLES20.glClearColor(0.2f, 0.5f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer)
        GLES20.glEnableVertexAttribArray(aPositionHandle)

        // 绘制三角形
        GLES20.glUseProgram(mProgram)
        val redColor = 0.1f + sin((System.currentTimeMillis() - curMSC) / 1000.0f) / 2.0f
        val greenColor = 0.3f + sin((System.currentTimeMillis() - curMSC) / 1000.0f) / 2.0f
        val blueColor = 0.5f + sin((System.currentTimeMillis() - curMSC) / 1000.0f) / 2.0f
        TrickLog.d(TAG, "drawFrame-> blueColor: $blueColor")
        GLES20.glUniform4f(uVertexColorHandle, redColor, greenColor, blueColor, 1.0f)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glUseProgram(0)
    }

    override fun onSurfaceDestroyed() {
        if (0 != mProgram) {
            GLES20.glDeleteProgram(mProgram)
        }
    }
}