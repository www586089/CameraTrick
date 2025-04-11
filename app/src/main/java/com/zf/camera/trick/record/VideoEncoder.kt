package com.zf.camera.trick.record

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import java.nio.ByteBuffer

class VideoEncoder {

    companion object {
        const val MIME_TYPE = "video/avc"
    }
    private var mOutputFormat: MediaFormat? = null
    private var mMediaCodec: MediaCodec? = null
    private var isEndOfStream = false

    private val mFrameDeque = ArrayDeque<ByteBuffer>()
    private var mMuxer: MediaMuxer? = null
    private var mTrackIndex = -1;

    fun startMuxer(videoPath: String) {
        mMuxer = MediaMuxer(videoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4).apply { start() }
    }

    fun startMediaCodec() {
        val mediaFormat = MediaFormat().apply {
            setString(MediaFormat.KEY_MIME, MIME_TYPE)
            setInteger(MediaFormat.KEY_WIDTH, 600)
            setInteger(MediaFormat.KEY_HEIGHT, 800)

            setInteger(MediaFormat.KEY_COLOR_FORMAT, 21)
            setInteger(MediaFormat.KEY_FRAME_RATE, 25)

            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)
            setInteger(MediaFormat.KEY_BIT_RATE, 2_000_000)//2Mbps
        }
        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE).apply {
            setCallback(object : MediaCodec.Callback() {
                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                    val inputBuffer = codec.getInputBuffer(index) ?: return
                    val size = 0
                    //fill inputBuffer
                    if (mFrameDeque.size > 0) {
                        mFrameDeque.firstOrNull()?.apply {
                            inputBuffer.put(this)
                            var flag = 0
                            if (isEndOfStream) {
                                flag = MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            }
                            codec.queueInputBuffer(index, 0, size, System.currentTimeMillis(), flag)
                        }
                    }
                }

                override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                    val outputBuffer = codec.getOutputBuffer(index)
                    val outputFormat = codec.getOutputFormat(index)

                    outputBuffer?.apply {
                        mMuxer!!.writeSampleData(mTrackIndex, outputBuffer, info)
                    }

                    codec.releaseOutputBuffer(index, false)
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {

                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                    mOutputFormat = format
                    mTrackIndex = mMuxer!!.addTrack(format)
                }
            })
            configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            start()
        }
    }
}