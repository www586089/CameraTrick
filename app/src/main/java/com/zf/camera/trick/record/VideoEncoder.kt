package com.zf.camera.trick.record

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.zf.camera.trick.utils.TrickLog

class VideoEncoder {

    companion object {
        const val TAG = "A-VideoEncoder"
        const val MIME_TYPE = "video/avc"
    }
    private lateinit var mMuxer: MediaMuxer
    private lateinit var mMediaCodec: MediaCodec
    private lateinit var mFrameData: ByteArray
    private lateinit var mHandler: Handler
    private var isEndOfStream = false

    private val mFrameDeque = ArrayDeque<ByteArray>()
    private var mIndexDeque = ArrayDeque<Int>()
    private var mTrackIndex = -1;
    private var width = 0
    private var height = 0

    @Volatile
    private var isEncoderStarted = false

    private var mListener: VideoRecordListener? = null

    fun startMuxer(videoPath: String, width: Int, height: Int, listener: VideoRecordListener) {
        TrickLog.d(TAG, "startMuxer: $videoPath")
        this.mListener = listener
        this.mMuxer = MediaMuxer(videoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        //相机传输过来的是yuv数据，为当前预览分辨率宽高*1.5
        this.mFrameData = ByteArray(width * height * 3 / 2)
        this.width = width
        this.height = height
        this.isEndOfStream = false
        this.isEncoderStarted = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val handlerThread = HandlerThread("VideoEncoder").apply { start() }
            this.mHandler = Handler(handlerThread.looper)
        }

        startMediaCodec(width, height)
    }


    fun stopMuxer() {
        TrickLog.d(TAG, "stopMuxer: ")
        /**
         * 置标识位，在正在编码器检测到结束后（EOS标识）再
         * 停止编码器
         */
        isEndOfStream = true
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

    private fun startMediaCodec(width: Int, height: Int) {
        val mediaFormat = MediaFormat().apply {
            setString(MediaFormat.KEY_MIME, MIME_TYPE)
            setInteger(MediaFormat.KEY_WIDTH, width)
            setInteger(MediaFormat.KEY_HEIGHT, height)

            setInteger(MediaFormat.KEY_COLOR_FORMAT, 21)
            setInteger(MediaFormat.KEY_FRAME_RATE, 25)

            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)
            setInteger(MediaFormat.KEY_BIT_RATE, 2_000_000)//2Mbps
        }
        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE).apply {
            val callback = object : MediaCodec.Callback() {
                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                    this@VideoEncoder.onInputBufferAvailable(codec, index)
                }

                override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                    this@VideoEncoder.onOutputBufferAvailable(index, codec, info)
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                    this@VideoEncoder.onError(codec, e)
                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                    this@VideoEncoder.onOutputFormatChanged(format)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setCallback(callback, mHandler)
            } else {
                setCallback(callback)
            }

            configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
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
//        TrickLog.d(TAG, "onInputBufferAvailable： index = ${index}, isEndOfStream = $isEndOfStream")

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
//        TrickLog.d(TAG, "onOutputBufferAvailable: $index")
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

    private fun checkIndexBuffer() {
        if (mIndexDeque.size > 0) {
            onInputBufferAvailable(mMediaCodec, mIndexDeque.removeFirst())
        }
    }
}