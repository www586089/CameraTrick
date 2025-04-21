package com.zf.camera.trick.filter.sample

import android.content.Context
import android.opengl.GLES30
import com.zf.camera.trick.gl.GLESUtils.createProgram
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * 使用VAO绘制 （OpenGL ES3.0才支持）
 */
class VAOTriangle(val ctx: Context): IShape {

    private var vertices = floatArrayOf(
        //vertex             //colors
        -0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, //bottom-left
         0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, //bottom-right
         0.0f,  0.5f, 0.0f, 0.0f, 0.0f, 1.0f  //top-center
    )

    // 顶点着色器代码
    private val vertexShaderCode = """
        attribute vec3 aPosition;
        attribute vec3 aColor;
        varying vec3 vColor;
        
        void main() {
            vColor = aColor;
            gl_Position = vec4(aPosition, 1.0);
        }
        """

    // 片段着色器代码
    private val fragmentShaderCode = """
        precision mediump float;
        varying vec3 vColor;
        
        void main() {
            gl_FragColor = vec4(vColor, 1.0);
        }
        """

    private var mProgram = -1

    private var aPositionHandle = -1
    private var aColorHandle = -1
    private var mVAO = -1
    private var mVBO = -1
    private lateinit var vertexBuffer: Buffer

    init {

    }


    override fun onSurfaceCreated() {
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
            .position(0)


        mProgram = createProgram(vertexShaderCode, fragmentShaderCode)

        aPositionHandle = GLES30.glGetAttribLocation(mProgram, "aPosition")
        aColorHandle = GLES30.glGetAttribLocation(mProgram, "aColor")


        //创建VAO
        val vaoArray = ByteBuffer.allocateDirect(4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer().apply { position(0) }
        GLES30.glGenVertexArrays(1, vaoArray)
        mVAO = vaoArray[0]

        // 创建VBO
        val vboArray = ByteBuffer.allocateDirect(4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer().apply { position(0) }
        GLES30.glGenBuffers(1, vboArray)
        mVBO = vboArray[0]

        // :: Initialization code (done once (unless your object frequently changes)) :: ..
        // 1. bind Vertex Array Object
        GLES30.glBindVertexArray(mVAO)

        // 2. copy our vertices array in a buffer for OpenGL to use
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBO)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.size * 4, vertexBuffer, GLES30.GL_STATIC_DRAW)

        // 3. then set our vertex attributes pointers
        GLES30.glVertexAttribPointer(aPositionHandle, 3, GLES30.GL_FLOAT, false, (3 + 3) * 4, 0)
        GLES30.glEnableVertexAttribArray(aPositionHandle)
        GLES30.glVertexAttribPointer(aColorHandle, 3, GLES30.GL_FLOAT, false, (3 + 3) * 4, 3 * 4)
        GLES30.glEnableVertexAttribArray(aColorHandle)


        // 4. You can unbind the VAO now
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindVertexArray(0)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun drawFrame() {

        // 重新绘制背景色为黑色
        GLES30.glClearColor(0.2f, 0.5f, 0.0f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glUseProgram(mProgram)
        // 绑定VAO
        GLES30.glBindVertexArray(mVAO)
        // 绘制三角形
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)

        // 解绑VAO
        GLES30.glBindVertexArray(0)
        GLES30.glUseProgram(0)
    }

    override fun onSurfaceDestroyed() {
        //释放VBO
        if (0 != mVBO) {
            GLES30.glDeleteBuffers(1, intArrayOf(mVBO), 0)
        }
        //释放VAO
        if (0 != mVAO) {
            GLES30.glDeleteVertexArrays(1, intArrayOf(mVAO), 0)
        }

        if (0 != mProgram) {
            GLES30.glDeleteProgram(mProgram)
        }
    }
}