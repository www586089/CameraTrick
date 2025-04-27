package com.zf.camera.trick.filter.sample

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import com.zf.camera.trick.R
import com.zf.camera.trick.gl.GLESUtils.createProgram
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * 平移、旋转、缩放
 */
class TextureQuadTransform(val ctx: Context): IShape {

    private var vertices = floatArrayOf(
        //positions         //colors            // texture coords
         0.5f,  0.5f, 0.0f, 1.0f, 0.0f, 0.0f,   1.0f, 0.0f,   // top right
         0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f,   1.0f, 1.0f,   // bottom right
        -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f,   0.0f, 1.0f,   // bottom left
        -0.5f,  0.5f, 0.0f, 1.0f, 1.0f, 0.0f,   0.0f, 0.0f    // top left
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
        
        attribute vec2 aTextCoord;
        varying vec2 vTextCoord;
        
        void main() {
            vColor = aColor;
            vTextCoord = aTextCoord;
            gl_Position = vec4(aPosition, 1.0);
        }
        """

    // 片段着色器代码
    private val fragmentShaderCode = """
        precision mediump float;
        varying vec3 vColor;
        varying vec2 vTextCoord;
        uniform sampler2D sampler1;
        uniform sampler2D sampler2;
        
        void main() {
            vec4 color1 = texture2D(sampler1, vTextCoord);
            vec4 color2 = texture2D(sampler2, vTextCoord);
            vec4 mixTextureColor = mix(color1, color2, 0.4);
            gl_FragColor = mix(mixTextureColor, vec4(vColor, 1.0), 0.2);
        }
        """

    private var mProgram = -1

    private var aPositionHandle = -1
    private var aColorHandle = -1
    private var aTextCoordHandle = -1
    private var mSampler1Handle = -1
    private var mSampler2Handle = -1

    private var mTextureId = -1
    private var mTextureId2 = -1

    private var mVAO = -1
    private var mVBO = -1
    private var mEBO = -1
    private lateinit var vertexBuffer: Buffer
    private lateinit var indexBuffer: Buffer

    init {

    }

    private fun createTexture(bitmapId: Int): Int {
        val texture = IntArray(1)
        val bitmap = BitmapFactory.decodeResource(ctx.resources, bitmapId)
        if (bitmap != null && !bitmap.isRecycled) {
            //生成纹理
            GLES30.glGenTextures(1, texture, 0)
            //生成纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0])
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST.toFloat())
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat())
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat())
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            // 取消绑定纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
            bitmap.recycle()

            return texture[0]
        }
        return 0
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
        aColorHandle = GLES30.glGetAttribLocation(mProgram, "aColor")
        aTextCoordHandle = GLES30.glGetAttribLocation(mProgram, "aTextCoord")
        mSampler1Handle = GLES30.glGetUniformLocation(mProgram, "sampler1")
        mSampler2Handle = GLES30.glGetUniformLocation(mProgram, "sampler2")

        mTextureId = createTexture(R.drawable.container)
        mTextureId2 = createTexture(R.drawable.awesomeface)


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
        //4.3 bind Texture Coords
        GLES30.glVertexAttribPointer(aTextCoordHandle, 3, GLES30.GL_FLOAT, false, 8 * 4, 6 * 4)
        GLES30.glEnableVertexAttribArray(aTextCoordHandle)


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
        //1绑定纹理
        //1.1 使用mTextureId绑定0号纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId)
        //1.2 使用mTextureId2绑定1号纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId2)
        //1.3 mSampler1Handle使用0号纹理采样
        GLES30.glUniform1i(mSampler1Handle, 0)
        //1.4 mSampler2Handle使用1号纹理采样
        GLES30.glUniform1i(mSampler2Handle, 1)

        // 绑定VAO
        GLES30.glBindVertexArray(mVAO)
        // 绘制三角形
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_INT, 0)

        // 解绑VAO
        GLES30.glBindVertexArray(0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
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