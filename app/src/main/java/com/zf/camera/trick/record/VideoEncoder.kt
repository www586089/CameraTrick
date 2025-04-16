package com.zf.camera.trick.record

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Handler
import android.os.HandlerThread
import com.zf.camera.trick.utils.TrickLog

class VideoEncoder: Runnable {

    companion object {
        const val TAG = "A-VideoEncoder"
        const val MIME_TYPE = "video/avc"
    }
    private var mOutputFormat: MediaFormat? = null
    private var mMediaCodec: MediaCodec? = null
    private var isEndOfStream = false

    private val mFrameDeque = ArrayDeque<ByteArray>()
    private var mIndexDeque = ArrayDeque<Int>()
    private var mMuxer: MediaMuxer? = null
    private var mTrackIndex = -1;
    private var mFrameData: ByteArray? = null
    private var width = 0
    private var height = 0
    @Volatile
    private var isExit = true
    private var isMuxerStarted = false
    private var handler: Handler? = null

    fun startMuxer(videoPath: String, width: Int, height: Int) {
        TrickLog.d(TAG, "startMuxer: $videoPath")
        mMuxer = MediaMuxer(videoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        mFrameData = ByteArray(width * height * 3 / 2)
        this.width = width
        this.height = height
        this.isExit = false
        val handlerThread = HandlerThread("VideoEncode")
        handlerThread.start()
        startMediaCodec(width, height)
    }

    override fun run() {
        while (!isExit) {
            if (!isMuxerStarted) {
                startMediaCodec(width, height)
            } else {
//                feedData()
            }
        }
    }

    private fun feedData() {
        mMediaCodec?.apply {
            val index = dequeueInputBuffer(0)
            if (index > 0) {
                val inputBuffer = getInputBuffer(index) ?: return
                if (mFrameDeque.size > 0) {
                    mFrameDeque.removeFirstOrNull()?.apply {
                        //TrickLog.d(TAG, "inputBuffer.size = ${inputBuffer.capacity()}, frame.size = ${this.capacity()}")
                        System.arraycopy(this, 0, mFrameData!!, 0, width * height)

                        for (i in (width * height) until this.size step 2) {
                            mFrameData!![i + 1] = this[i]
                            mFrameData!![i] = this[i + 1]
                        }
                        inputBuffer.put(mFrameData!!)
                        var flag = 0
                        if (isEndOfStream) {
                            TrickLog.d(TAG, "put EOS")
                            flag = MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        }
                        queueInputBuffer(index, 0, inputBuffer.limit(), System.nanoTime() / 1000, 0)
                    }
                }

            }
        }
    }

    fun stopMuxer() {
        TrickLog.d(TAG, "stopMuxer: ")
        isExit = true
        isEndOfStream = true
    }

    private fun doStopMuxer() {
        TrickLog.d(TAG, "doStopMuxer: ")
        mMuxer?.apply {
            stop()
            release()
        }
    }

    private fun stopMediaCodec() {
        TrickLog.d(TAG, "stopMediaCodec: ")
        mMediaCodec?.apply {
            stop()
            release()
        }
    }

    fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        TrickLog.d(TAG, "onInputBufferAvailable: $index")

        if (mFrameDeque.size > 0) {
            val inputBuffer = codec.getInputBuffer(index) ?: return
            val size = 0
            //fill inputBuffer
            mFrameDeque.removeFirstOrNull()?.apply {
                //TrickLog.d(TAG, "inputBuffer.size = ${inputBuffer.capacity()}, frame.size = ${this.capacity()}")
                System.arraycopy(this, 0, mFrameData!!, 0, width * height)

                for (i in (width * height) until this.size step 2) {
                    mFrameData!![i + 1] = this[i]
                    mFrameData!![i] = this[i + 1]
                }
                inputBuffer.put(mFrameData!!)
                var flag = 0
                if (isEndOfStream) {
                    TrickLog.d(TAG, "put EOS")
                    flag = MediaCodec.BUFFER_FLAG_END_OF_STREAM
                }
                codec.queueInputBuffer(index, 0, inputBuffer.limit(), System.nanoTime() / 1000, 0)
            }
        } else {
            mIndexDeque.addLast(index)
        }
    }


    fun startMediaCodec(width: Int, height: Int) {
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
            setCallback(object : MediaCodec.Callback() {
                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                    this@VideoEncoder.onInputBufferAvailable(codec, index)
                }

                override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                    TrickLog.d(TAG, "onOutputBufferAvailable: $index")
                    val outputBuffer = codec.getOutputBuffer(index)
                    val outputFormat = codec.getOutputFormat(index)
                    info.presentationTimeUs = System.nanoTime() / 1000L

                    if (0 != (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG)) {
                        info.size = 0
                    }
                    outputBuffer?.apply {
                        outputBuffer.position(info.offset)
                        outputBuffer.limit(info.offset + info.size)
                        mMuxer!!.writeSampleData(mTrackIndex, outputBuffer, info)
                    }

                    codec.releaseOutputBuffer(index, false)
                    if (isEndOfStream) {
                        TrickLog.d(TAG, "onOutputBufferAvailable: end of stream")
                        stopMediaCodec()
                        doStopMuxer()
                    }
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {

                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                    mOutputFormat = format
                    mTrackIndex = mMuxer!!.addTrack(format)

                    mMuxer!!.start()
                    isMuxerStarted = true
                    TrickLog.d(TAG, "onOutputFormatChanged: $mTrackIndex")
                }
            })

            configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            start()
        }
    }

    fun encode(data: ByteArray) {
//        TrickLog.d(TAG, "encode: isExit = $isExit")
        if (isExit) {
            return
        }
        val copyData = ByteArray(data.size)
        System.arraycopy(data, 0, copyData, 0, data.size)
        mFrameDeque.addLast(copyData)
        checkIndexBuffer()
    }

    private fun checkIndexBuffer() {
        if (mIndexDeque.size > 0) {
            onInputBufferAvailable(mMediaCodec!!, mIndexDeque.removeFirst())
        }
    }
}