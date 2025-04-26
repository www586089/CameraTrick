package com.zf.camera.trick.filter.sample

import android.content.Context
import android.opengl.GLES30
import com.zf.camera.trick.gl.GLESUtils.createProgram
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * 纹理的使用
 */
class TextureQuad(val ctx: Context): IShape {

    private var vertices = floatArrayOf(
        //positions         //colors            // texture coords
         0.5f,  0.5f, 0.0f, 1.0f, 0.0f, 0.0f,   1.0f, 1.0f,   // top right
         0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f,   1.0f, 0.0f,   // bottom right
        -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f,   0.0f, 0.0f,   // bottom left
        -0.5f,  0.5f, 0.0f, 1.0f, 1.0f, 0.0f,   0.0f, 1.0f    // top left
    )

    private var indices = intArrayOf(
        0, 1, 3,
        1, 2, 3
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
    private var mEBO = -1
    private lateinit var vertexBuffer: Buffer
    private lateinit var indexBuffer: Buffer

    init {

    }


    override fun onSurfaceCreated() {
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
            .position(0)

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
            .put(indices)
            .position(0)

        mProgram = createProgram(vertexShaderCode, fragmentShaderCode)

        aPositionHandle = GLES30.glGetAttribLocation(mProgram, "aPosition")
        aColorHandle = GLES30.glGetAttribLocation(mProgram, "aColor");


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

        // 创建EBO
        val eboArray = ByteBuffer.allocateDirect(4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer().apply { position(0) }
        GLES30.glGenBuffers(1, eboArray)
        mEBO = eboArray[0]

        // :: Initialization code (done once (unless your object frequently changes)) :: ..
        // 1. bind Vertex Array Object
        GLES30.glBindVertexArray(mVAO)

        // 2. copy our vertices array in a buffer for OpenGL to use
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBO)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.size * 4, vertexBuffer, GLES30.GL_STATIC_DRAW)

        // 3. copy our index array in a buffer for OpenGL to use
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mEBO)
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.size * 4, indexBuffer, GLES30.GL_STATIC_DRAW)

        // 4. then set our vertex attributes pointers
        //4.1 bind position
        GLES30.glVertexAttribPointer(aPositionHandle, 3, GLES30.GL_FLOAT, false, 8 * 4, 0)
        GLES30.glEnableVertexAttribArray(aPositionHandle)
        //4.2 bind color
        GLES30.glVertexAttribPointer(aColorHandle, 3, GLES30.GL_FLOAT, false, 8 * 4, 3 * 4)
        GLES30.glEnableVertexAttribArray(aColorHandle)


        // 5. You can unbind the VAO now
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
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_INT, 0)

        // 解绑VAO
        GLES30.glBindVertexArray(0)
        GLES30.glUseProgram(0)
    }

    override fun onSurfaceDestroyed() {
        //释放EBO
        if (0 != mEBO) {
            GLES30.glDeleteBuffers(1, intArrayOf(mEBO), 0)
        }
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