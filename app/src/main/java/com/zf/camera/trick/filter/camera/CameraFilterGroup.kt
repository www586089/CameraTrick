package com.zf.camera.trick.filter.camera

import android.content.res.Resources
import android.opengl.GLES20
import com.zf.camera.trick.filter.utils.Rotation
import com.zf.camera.trick.filter.utils.TextureRotationUtil
import com.zf.camera.trick.filter.utils.TextureRotationUtil.CUBE
import com.zf.camera.trick.filter.utils.TextureRotationUtil.TEXTURE_NO_ROTATION
import com.zf.camera.trick.utils.TrickLog
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

open class CameraFilterGroup(res: Resources) :
    CameraFilterBase(res) {

    private lateinit var filters: MutableList<CameraFilterBase>
    private lateinit var mergedFilters: MutableList<CameraFilterBase>
    private var frameBuffers: IntArray = intArrayOf()
    private var frameBufferTextures: IntArray = intArrayOf()

    private val glCubeBuffer: FloatBuffer
        get() {
            return ByteBuffer.allocateDirect(CUBE.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer().apply { put(CUBE).position(0) }
        }
    private val glTextureBuffer: FloatBuffer
        get() {
            return ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .apply { put(TEXTURE_NO_ROTATION).position(0) }
        }
    private val glTextureFlipBuffer: FloatBuffer
        get() {
            val flipTexture: FloatArray =
                TextureRotationUtil.getRotation(Rotation.NORMAL, false, true)
            return ByteBuffer.allocateDirect(flipTexture.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer().apply { put(flipTexture).position(0) }
        }

    constructor(res: Resources, filters: MutableList<CameraFilterBase>?) : this(res) {
        if (null == filters) {
            this.filters = mutableListOf()
        } else {
            this.filters = filters
        }

        updateMergedFilters()
    }

    fun addFilter(aFilter: CameraFilterBase?) {
        if (aFilter == null) {
            return
        }
        if (!::filters.isInitialized) {
            filters = mutableListOf()
        }
        filters.add(aFilter)
        updateMergedFilters()
    }

    fun updateMergedFilters() {
        if (filters.isEmpty()) {
            return
        }
        mergedFilters = mutableListOf()
        var filters: List<CameraFilterBase>
        for (filter in this.filters) {
            if (filter is CameraFilterGroup) {
                filter.updateMergedFilters()
                filters = filter.getMergedFilters()
                if (filters.isEmpty()) continue
                mergedFilters.addAll(filters)
                continue
            }
            mergedFilters.add(filter)
        }
    }

    private fun getMergedFilters(): List<CameraFilterBase> {
        return mergedFilters
    }

    fun getFilters(): List<CameraFilterBase> {
        return filters
    }

    private fun destroyFramebuffers() {
        GLES20.glDeleteTextures(frameBufferTextures.size, frameBufferTextures, 0)
        frameBufferTextures = intArrayOf()

        GLES20.glDeleteFramebuffers(frameBuffers.size, frameBuffers, 0)
        frameBuffers = intArrayOf()
    }

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        val size = filters.size
        for (i in 0 until size) {
            filters[i].onSurfaceCreated()
        }
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        super.onSurfaceChanged(width, height)

        if (frameBuffers.isNotEmpty()) {
            destroyFramebuffers()
        }

        var size = filters.size
        for (i in 0 until size) {
            filters[i].onSurfaceChanged(width, height)
        }

        //创建帧缓存
        if (mergedFilters.isNotEmpty()) {
            size = mergedFilters.size
            frameBuffers = IntArray(size - 1)
            frameBufferTextures = IntArray(size - 1)
            var fboId = 0
            var offscreenTextureId = 0 // 绑定到 FBO 的颜色附件纹理
            for (i in 0 until size - 1) {
                // 1. 生成 FBO ID
                GLES20.glGenFramebuffers(1, frameBuffers, i)
                fboId = frameBuffers[i]

                // 2. 生成离屏纹理（用于存储 FBO 的颜色数据）
                GLES20.glGenTextures(1, frameBufferTextures, i)
                offscreenTextureId = frameBufferTextures[i]
                // 配置纹理参数（2D 纹理、过滤方式等）
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, offscreenTextureId)
                // 创建纹理存储空间（宽高对应离屏渲染的尺寸）
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())

                // 3. 绑定 FBO，并将纹理绑定为 FBO 的颜色附件
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId)
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, offscreenTextureId, 0)

                // 4. 检查 FBO 是否创建成功
                val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
                if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                    TrickLog.e("FBO 创建失败: $status")
                }

                // 5. 解绑 FBO 和纹理，恢复默认状态
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
            }
        }
    }

    override fun drawFrame(texMatrix: FloatArray?) {
        if (frameBuffers.isEmpty() || frameBufferTextures.isEmpty()) {
            return
        }
        //todo 这里好像有点问题，但是看不出哪里有问题
        if (mergedFilters.isNotEmpty()) {
            val size = mergedFilters.size
            var previousTexture = textureId
            for (i in 0 until size) {
                val filter: CameraFilterBase = mergedFilters[i]
                val isNotLast = i < size - 1
                if (isNotLast) {
                    // 绑定 FBO，后续渲染都会到这个 FBO 的颜色附件（纹理）
                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[i])
                    GLES20.glClearColor(0f, 0f, 0f, 0f)
                }
                filter.textureId = previousTexture
                if (i == 0) {
//                    filter.drawFrame(previousTexture, cubeBuffer, textureBuffer)
                    filter.drawFrame(texMatrix)
                } else if (i == size - 1) {
//                    this.textureId = if (size % 2 == 0) glTextureFlipBuffer else glTextureBuffer
//                    filter.onDraw(previousTexture, glCubeBuffer, if (size % 2 == 0) glTextureFlipBuffer else glTextureBuffer)
                    filter.textureId = textureId
                    filter.drawFrame(texMatrix)
                } else {
//                    filter.onDraw(previousTexture, glCubeBuffer, glTextureBuffer)
                    filter.drawFrame(texMatrix)
                }
                if (isNotLast) {
                    // 解绑 FBO，恢复到屏幕帧缓冲区
                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
                    previousTexture = frameBufferTextures[i]
                }
            }
        }
    }

    override fun onSurfaceDestroyed() {
        destroyFramebuffers()
        for (filter in filters) {
            filter.onSurfaceDestroyed()
        }
        super.onSurfaceDestroyed()
    }

}