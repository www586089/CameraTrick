package com.zf.camera.trick.filter

import android.opengl.GLES20
import com.zf.camera.trick.gl.GLESUtils
import com.zf.camera.trick.utils.TrickLog


/**
 * 滤镜效果抽象类
 */
abstract class BaseFilter : IFilter {
    /**
     * 离屏渲染纹理id
     */
    final override var offscreenTexture = -1
        private set

    /**
     * 帧缓冲区
     */
    override var frameBuffer = -1

    /**
     * 深度缓冲区
     */
    private var mDepthBuffer = -1

    /**
     * 是否使用离屏渲染
     */
    protected var isBindFbo = false
    private var mWidth = 0
    private var mHeight = 0

    override fun setBindFBO(bindFBO: Boolean) {
        this.isBindFbo = bindFBO
    }

    override fun doBindFBO() {
        if (frameBuffer > 0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer)
        }
    }

    override fun unBindFBO() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    fun createFrameBufferV2(width: Int, height: Int) {
        //1 创建帧缓冲区 FBO
        val frameBuffer = intArrayOf(1)
        GLES20.glGenFramebuffers(1, frameBuffer, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])

        //2 创建颜色附着
        val textureColorBuffer = intArrayOf(1)
        GLES20.glGenTextures(GLES20.GL_TEXTURE_2D, textureColorBuffer, 0)
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureColorBuffer[0], 0)

        //3 创建渲染缓冲区附着
        val rbo = intArrayOf(1)
        GLES20.glGenRenderbuffers(1, rbo, 0)
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, rbo[0])
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height)
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0)
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, rbo[0])

        //4 检查配置
        if (GLES20.GL_FRAMEBUFFER_COMPLETE != GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)) {
            TrickLog.e("A", "FrameBuffer config error!!!")
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
            return
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    /**
     * 创建帧缓冲区（FBO）
     *
     * @param width
     * @param height
     */
    fun createFrameBuffers(width: Int, height: Int) {
        if (frameBuffer > 0) {
            destroyFrameBuffers()
        }
        // 1.创建一个纹理对象并绑定它，这将是颜色缓冲区。
        val values = IntArray(1)
        GLES20.glGenTextures(values.size, values, 0)
        GLESUtils.checkGlError("glGenTextures")
        offscreenTexture = values[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, offscreenTexture)
        GLESUtils.checkGlError("glBindTexture " + offscreenTexture)

        // 2.创建纹理存储对象
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
            0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        )

        // 3.设置参数。我们可能正在使用二维的非幂函数，所以某些值可能无法使用。
        // 设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLESUtils.checkGlError("glTexParameter")

        // 4.创建帧缓冲区对象并将其绑定
        GLES20.glGenFramebuffers(values.size, values, 0)
        GLESUtils.checkGlError("glGenFramebuffers")
        frameBuffer = values[0] // expected > 0
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer)
        GLESUtils.checkGlError("glBindFramebuffer " + frameBuffer)

        // 5.创建深度缓冲区并绑定它
        GLES20.glGenRenderbuffers(values.size, values, 0)
        GLESUtils.checkGlError("glGenRenderbuffers")
        mDepthBuffer = values[0] // expected > 0
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mDepthBuffer)
        GLESUtils.checkGlError("glBindRenderbuffer $mDepthBuffer")

        // 为深度缓冲区分配存储空间。
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height)
        GLESUtils.checkGlError("glRenderbufferStorage")

        // 6.将深度缓冲区和纹理（颜色缓冲区）附着到帧缓冲区对象
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, mDepthBuffer)
        GLESUtils.checkGlError("glFramebufferRenderbuffer")
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, offscreenTexture, 0)
        GLESUtils.checkGlError("glFramebufferTexture2D")

        // 检查是否一切正常
        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Framebuffer not complete, status=$status")
        }

        // 解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        // 解绑Frame Buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        // 解绑Render Buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0)
        GLESUtils.checkGlError("prepareFramebuffer done")
    }

    /**
     * 销毁帧缓冲区（FBO）
     */
    fun destroyFrameBuffers() {
        // 删除fbo的纹理
        if (offscreenTexture > 0) {
            GLES20.glDeleteTextures(1, intArrayOf(offscreenTexture), 0)
            offscreenTexture = -1
        }
        if (frameBuffer > 0) {
            GLES20.glDeleteFramebuffers(1, intArrayOf(frameBuffer), 0)
            frameBuffer = -1
        }
        if (mDepthBuffer > 0) {
            GLES20.glDeleteRenderbuffers(1, intArrayOf(mDepthBuffer), 0)
            mDepthBuffer = -1
        }
    }

    override fun surfaceChanged(width: Int, height: Int) {
        mWidth = width
        mHeight = height
        if (isBindFbo) {
            createFrameBuffers(width, height)
        }
    }

    override fun draw(textureId: Int, matrix: FloatArray?): Int {
        GLES20.glViewport(0, 0, mWidth, mHeight)
        if (isBindFbo) {
            // 绑定FBO
            doBindFBO()
        }
        onDraw(textureId, matrix)
        return if (isBindFbo) {
            // 解绑FBO
            unBindFBO()
            //返回fbo的纹理id
            offscreenTexture
        } else {
            textureId
        }
    }



    override fun release() {
        destroyFrameBuffers()
    }
}

