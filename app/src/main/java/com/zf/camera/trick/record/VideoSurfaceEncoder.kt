package com.zf.camera.trick.record

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.opengl.EGL14
import android.opengl.EGLContext
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import com.zf.camera.trick.App
import com.zf.camera.trick.filter.camera.CONTRAST_MAX
import com.zf.camera.trick.filter.camera.CONTRAST_MIN
import com.zf.camera.trick.filter.camera.CameraFilterBase
import com.zf.camera.trick.filter.camera.CameraFilterContrast
import com.zf.camera.trick.filter.camera.CameraFilterPixelation
import com.zf.camera.trick.filter.camera.PIXELATION_MAX
import com.zf.camera.trick.filter.camera.PIXELATION_MIN
import com.zf.camera.trick.gl.egl.EglCore
import com.zf.camera.trick.gl.egl.WindowSurface
import com.zf.camera.trick.utils.TrickLog

class VideoSurfaceEncoder : Runnable, ISurfaceVideoRecorder {

    companion object {
        const val TAG = "A-VideoSurfaceEncoder"
        const val MIME_TYPE = "video/avc"
    }
    private lateinit var mMuxer: MediaMuxer
    private lateinit var mMediaCodec: MediaCodec
    private lateinit var mFrameData: ByteArray
    private lateinit var mHandler: Handler

    private lateinit var mShareContext: EGLContext
    private lateinit var mInputWindowSurface: WindowSurface
    private lateinit var mEglCore: EglCore
    private lateinit var mCameraFilter: CameraFilterBase
    private lateinit var mEncodeHandler: EncodeHandler
    private var mTextureId = 0
    private val mReadyFence = Object()
    private var mReady = false
    private var mRunning = false
    private var isEndOfStream = false

    private val mFrameDeque = ArrayDeque<ByteArray>()
    private var mIndexDeque = ArrayDeque<Int>()
    private var mTrackIndex = -1;
    private var width = 0
    private var height = 0
    private var shaderType: Int = CameraFilterBase.NO_FILTER

    @Volatile
    private var isEncoderStarted = false

    private var mListener: VideoRecordListener? = null

    override fun startRecord(videoPath: String, width: Int, height: Int, listener: VideoRecordListener) {
        TrickLog.d(TAG, "startMuxer: $videoPath")
        this.mListener = listener
        this.mMuxer = MediaMuxer(videoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        //相机传输过来的是yuv数据，为当前预览分辨率宽高*1.5
        this.mFrameData = ByteArray(width * height * 3 / 2)
        /**
         * 交换宽高
         */
        val tmp = width
        this.width = height
        this.height = tmp

        this.isEndOfStream = false
        this.isEncoderStarted = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val handlerThread = HandlerThread("VideoEncoder").apply { start() }
            this.mHandler = Handler(handlerThread.looper)
        }

        mShareContext = getEGLShareContext()
        synchronized(mReadyFence) {
            if (mRunning) {
                TrickLog.w(TAG, "Encoder thread already running")
                return
            }
            mRunning = true
            Thread(this, "VideoSurfaceEncoder").start()
            while (!mReady) {
                try {
                    mReadyFence.wait()
                } catch (ie: InterruptedException) {
                    // ignore
                }
            }
        }

        mEncodeHandler.sendEmptyMessage(mEncodeHandler.MSG_START_RECORD)
    }

    override fun run() {
        Looper.prepare()
        synchronized(mReadyFence) {
            mEncodeHandler = EncodeHandler(Looper.myLooper()!!)
            mReady = true
            mReadyFence.notify()
        }
        Looper.loop()

        TrickLog.d(TAG, "encoder thread--->end")
    }

    override fun stopRecord() {
        mEncodeHandler.sendEmptyMessage(mEncodeHandler.MSG_STOP_RECORD)
    }

    private fun stopMuxer() {
        TrickLog.d(TAG, "stopMuxer: ")
        /**
         * 置标识位，在正在编码器检测到结束后（EOS标识）再
         * 停止编码器
         */
        isEndOfStream = true
        signalEndOfStream()
    }

    private fun signalEndOfStream() {
        TrickLog.d(TAG, "signalEndOfStream: ")
        mMediaCodec.signalEndOfInputStream()
    }

    private fun doStopMuxer() {
        TrickLog.d(TAG, "doStopMuxer: ")
        mMuxer.apply {
            stop()
            release()
        }
        isEndOfStream = false
        mFrameDeque.clear()
        mIndexDeque.clear()
    }

    private fun stopMediaCodec() {
        TrickLog.d(TAG, "stopMediaCodec: ")
        mMediaCodec.apply {
            stop()
            release()
        }
    }

    private fun startMediaCodec() {
        val mediaFormat = MediaFormat().apply {
            setString(MediaFormat.KEY_MIME, MIME_TYPE)
            setInteger(MediaFormat.KEY_WIDTH, width)
            setInteger(MediaFormat.KEY_HEIGHT, height)

            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_FRAME_RATE, 25)

            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)
            setInteger(MediaFormat.KEY_BIT_RATE, 2_000_000)//2Mbps
        }
        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE).apply {
            val callback = object : MediaCodec.Callback() {
                /**
                 * 在使用MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface的情况下，这里不会有回调。在
                 * swapBuffers之后会自动提交数据到MediaCodec，之后MediaCodec会自动回调onOutputBufferAvailable，
                 * 所以这里不需处理了
                 */
                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                    this@VideoSurfaceEncoder.onInputBufferAvailable(codec, index)
                }

                override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                    this@VideoSurfaceEncoder.onOutputBufferAvailable(index, codec, info)
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                    this@VideoSurfaceEncoder.onError(codec, e)
                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                    this@VideoSurfaceEncoder.onOutputFormatChanged(format)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setCallback(callback, mHandler)
            } else {
                setCallback(callback)
            }

            configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

            mEglCore = EglCore(mShareContext, EglCore.FLAG_RECORDABLE)
            mInputWindowSurface = WindowSurface(mEglCore, createInputSurface(), true)
            mInputWindowSurface.makeCurrent()
            mCameraFilter = CameraFilterBase.getFilter(App.get().resources, shaderType)
            mCameraFilter.onSurfaceCreated()
            mCameraFilter.onSurfaceChanged(width, height)

            start()
        }
    }

    private fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
        TrickLog.d(TAG, "onError:${Log.getStackTraceString(e)}")
    }

    private fun onOutputFormatChanged(format: MediaFormat) {
        mTrackIndex = mMuxer.addTrack(format)

        mMuxer.start()
        mListener?.apply { onRecordStart() }
        TrickLog.d(TAG, "onOutputFormatChanged: $mTrackIndex")
    }

    private fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        TrickLog.d(TAG, "onInputBufferAvailable： index = ${index}, isEndOfStream = $isEndOfStream")

        val endOfStreamFlag = MediaCodec.BUFFER_FLAG_END_OF_STREAM
        if (mFrameDeque.size > 0) {
            val inputBuffer = codec.getInputBuffer(index) ?: return
            //fill inputBuffer
            mFrameDeque.removeFirstOrNull()?.apply {

                //Transform to codec format begin
                System.arraycopy(this, 0, mFrameData, 0, width * height)

                for (i in (width * height) until this.size step 2) {
                    mFrameData[i + 1] = this[i]
                    mFrameData[i] = this[i + 1]
                }
                //Transform to codec format end

                inputBuffer.put(mFrameData)
                var flag = 0
                if (isEndOfStream) {
                    TrickLog.d(TAG, "put EOS of Stream")
                    flag = endOfStreamFlag
                    isEncoderStarted = false
                }
                codec.queueInputBuffer(index, 0, inputBuffer.limit(), getPTU(), flag)
            }
        } else {
            if (isEndOfStream) {
                TrickLog.d(TAG, "put EOS no Stream")
                codec.queueInputBuffer(index, 0, 0, getPTU(), endOfStreamFlag)
            } else {
                mIndexDeque.addLast(index)
            }
        }
    }


    private fun onOutputBufferAvailable(index: Int, codec: MediaCodec, info: MediaCodec.BufferInfo) {
        TrickLog.d(TAG, "onOutputBufferAvailable: $index")
        val outputBuffer = codec.getOutputBuffer(index)
        info.presentationTimeUs = getPTU()

        val isCodecConfig = 0 != (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG)
        val isEndOfStream = 0 != (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM)
        if (isCodecConfig) {
            info.size = 0
        }
        outputBuffer?.apply {
            outputBuffer.position(info.offset)
            outputBuffer.limit(info.offset + info.size)
            mMuxer.writeSampleData(mTrackIndex, outputBuffer, info)
        }

        codec.releaseOutputBuffer(index, false)
        if (isEndOfStream) {
            release()
            mListener?.apply { onRecordStop() }
        }
    }

    private fun release() {
        TrickLog.d(TAG, "release: detect EOS")
        stopMediaCodec()
        doStopMuxer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TrickLog.d(TAG, "release: quit looper")
            mHandler.looper.quitSafely()
            mHandler.removeCallbacksAndMessages(null)
        }
        mEncodeHandler.removeCallbacksAndMessages(null)
        mEncodeHandler.looper.quitSafely()
        mRunning = false
        mReady = false

        releaseEGLContext()
    }

    private fun getPTU(): Long {
        return System.nanoTime() / 1000
    }

    fun encode(data: ByteArray) {
//        TrickLog.d(TAG, "encode: $isEncoderStarted")
        if (!isEncoderStarted) {
            return
        }

        val copyData = ByteArray(data.size)
        System.arraycopy(data, 0, copyData, 0, data.size)
        mFrameDeque.addLast(copyData)
        checkIndexBuffer()
    }

    override fun updateShaderType(shaderType: Int) {
        this.shaderType = shaderType
    }

    override fun updateValue(percentage: Float) {
        mEncodeHandler.sendMessage(Message.obtain().apply {
            what = mEncodeHandler.MSG_SET_VALUE
            obj = percentage
        })
    }

    private fun range(start: Float, end: Float, percentage: Float): Float {
        return start + ((end - start) * percentage) / 100f
    }

    fun setValue(value: Float) {
        if (mCameraFilter is CameraFilterContrast) {
            val contrast = range(CONTRAST_MIN, CONTRAST_MAX, percentage = value)
            (mCameraFilter as CameraFilterContrast).setContrast(contrast)
        } else if (mCameraFilter is CameraFilterPixelation) {
            val pixel = range(PIXELATION_MIN, PIXELATION_MAX, value)
            (mCameraFilter as CameraFilterPixelation).setPixel(pixel)
        }
    }

    override fun willComingAFrame(textureId: Int, st: SurfaceTexture) {
        if (!::mCameraFilter.isInitialized || isEndOfStream) {
            return
        }
        mTextureId = textureId
        Message.obtain().apply {
            what = mEncodeHandler.MSG_DRAW_FRAME
            arg1 = textureId
            obj = st
            mEncodeHandler.sendMessage(this)
        }
    }

    private fun drawFrame(st: SurfaceTexture) {
        val transform = FloatArray(16)
        st.getTransformMatrix(transform)
        mCameraFilter.textureId = mTextureId
        mCameraFilter.drawFrame(transform)
        mInputWindowSurface.setPresentationTime(getPTU() * 1000L)
        mInputWindowSurface.swapBuffers()
    }

    override fun onUpdatedSharedContext() {
        mEncodeHandler.sendMessage(Message.obtain().apply {
            what = mEncodeHandler.MSG_UPDATE_SHARE_CONTEXT
            obj = getEGLShareContext()
        })
    }

    private fun getEGLShareContext(): EGLContext {
        return EGL14.eglGetCurrentContext()
    }

    private fun releaseEGLContext() {
        // Release the EGLSurface and EGLContext.
        mInputWindowSurface.releaseEglSurface()
        mCameraFilter.onSurfaceDestroyed()
        mEglCore.release()
    }


    private fun handleUpdateSharedContext(newSharedContext: EGLContext) {
        TrickLog.d(TAG, "handleUpdatedSharedContext $newSharedContext")
        releaseEGLContext()

        // Create a new EGLContext and recreate the window surface.
        mEglCore = EglCore(newSharedContext, EglCore.FLAG_RECORDABLE)
        mInputWindowSurface.recreate(mEglCore)
        mInputWindowSurface.makeCurrent()

        // Create new programs and such for the new context.
        mCameraFilter.onSurfaceCreated()
        mCameraFilter.onSurfaceChanged(width, height)
    }

    private fun checkIndexBuffer() {
        if (mIndexDeque.size > 0) {
            onInputBufferAvailable(mMediaCodec, mIndexDeque.removeFirst())
        }
    }

    inner class EncodeHandler(looper: Looper): Handler(looper) {
        val MSG_START_RECORD = 0
        val MSG_STOP_RECORD = 1
        val MSG_DRAW_FRAME = 2
        val MSG_UPDATE_SHARE_CONTEXT = 3
        val MSG_SET_VALUE = 4

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_START_RECORD -> {
                    startMediaCodec()
                }
                MSG_STOP_RECORD -> {
                    stopMuxer()
                }
                MSG_DRAW_FRAME -> {
                    drawFrame(msg.obj as SurfaceTexture)
                }
                MSG_UPDATE_SHARE_CONTEXT -> {
                    handleUpdateSharedContext(msg.obj as EGLContext)
                }
                MSG_SET_VALUE -> {
                    setValue(msg.obj as Float)
                }
            }
        }
    }
}