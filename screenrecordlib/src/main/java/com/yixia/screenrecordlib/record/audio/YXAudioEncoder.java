package com.yixia.screenrecordlib.record.audio;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.yixia.screenrecordlib.record.YXMuxerWrapper;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 音频编码器
 * <p>
 * Created by zhaoxiaopo on 2017/7/11.
 */

public class YXAudioEncoder {
    private static final String TAGSTRING = YXAudioEncoder.class.getSimpleName();

    /**
     * @see MediaFormat#MIMETYPE_AUDIO_AAC
     */
    private static final String MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC;
    private static final int BIT_RATE = 64000;
    private static final int TIMEOUT_US = 10000;
    public static YXAudioEncoder sYxAudioEncoder = null;
    private static int SAMPLE_RATE = 44100;
    private static int CHANNEL = 2;
    private MediaCodec mAudioEncoder;
    private MediaFormat mAudioFormat;
    private MediaCodec.BufferInfo mBufferInfo;
    private YXMuxerWrapper mMuxer;
    private boolean isEncoding;
    private long mPrevOutputPTSUs = 0;
    private int mAudioTrackIndex = -1;

    private YXAudioEncoder() {
    }

    public static synchronized YXAudioEncoder getInstance() {
        if (sYxAudioEncoder == null) {
            sYxAudioEncoder = YXAudioEncoderHolder.YX_AUDIO_ENCODER;
        }
        return sYxAudioEncoder;
    }

    public void setParam(int sampleRate, int channel) {
        SAMPLE_RATE = sampleRate;
        CHANNEL = channel;
    }

    public void prepare(YXMuxerWrapper muxerWrapper) throws IOException {
        mMuxer = muxerWrapper;
        mBufferInfo = new MediaCodec.BufferInfo();

        mAudioEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        mAudioFormat = MediaFormat.createAudioFormat(MIME_TYPE, SAMPLE_RATE, CHANNEL);
        mAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);
        mAudioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_STEREO);
        mAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        mAudioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, CHANNEL);
        mAudioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 8192);
        mAudioEncoder.configure(mAudioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    public void start() {
        mAudioEncoder.start();
        isEncoding = true;
    }

    public void stop() {
        if(mAudioEncoder != null) {
            mAudioEncoder.stop();
            mAudioEncoder.release();
            mAudioEncoder = null;
        }
        isEncoding = false;
        mPrevOutputPTSUs = 0;
    }

    public void encode(ByteBuffer rawBuffer, int length, long presentationTimeUs) {
        if (isEncoding) {
            final ByteBuffer[] inputBuffers = mAudioEncoder.getInputBuffers();
            //dequeue input buffer
            final int inputBufferIndex = mAudioEncoder.dequeueInputBuffer(TIMEOUT_US);
            if (inputBufferIndex >= 0) {
                final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                if (rawBuffer != null) {
                    //copy ByteBuffer to input buffer
                    inputBuffer.put(rawBuffer);
                }
                if (length <= 0) {
                    //enqueue bytebuffer with EOS
                    mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, 0, presentationTimeUs, MediaCodec.BUFFER_FLAG_KEY_FRAME);
                } else {
                    //enqueue bytebuffer
                    mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, length, presentationTimeUs, MediaCodec.BUFFER_FLAG_KEY_FRAME);
                }
            }
        }

        sendToMediaMuxer();
        //get outputByteBuffer
        //take data from outputByteBuffer
        //send to mediamuxer
    }

    private void sendToMediaMuxer() {
        if (mAudioEncoder == null) {
            return;
        }

        final ByteBuffer[] outputBuffers = mAudioEncoder.getOutputBuffers();
        final int outputBufferIndex = mAudioEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_US);
        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            mAudioTrackIndex = mMuxer.addMediaTrack(mAudioEncoder.getOutputFormat());
            if (mAudioTrackIndex == -1) {
                return;
            }
        }
        if (outputBufferIndex >= 0) {
            if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                // You shoud set output format to muxer here when you target Android4.3 or less
                // but MediaCodec#getOutputFormat can not call here(because INFO_OUTPUT_FORMAT_CHANGED don't come yet)
                // therefor we should expand and prepare output format from buffer data.
                // This sample is for API>=18(>=Android 4.3), just ignore this flag here
                mBufferInfo.size = 0;
            }
            final ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            mMuxer.writeMediaData(mAudioTrackIndex, outputBuffer, mBufferInfo);

            mAudioEncoder.releaseOutputBuffer(outputBufferIndex, false);
        }
    }

    public MediaCodec getEncoder() {
        return mAudioEncoder;
    }

    protected long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < mPrevOutputPTSUs) {
            result = (mPrevOutputPTSUs - result) + result;
        }
        mPrevOutputPTSUs = result;
        return result;
    }

    private static class YXAudioEncoderHolder {
        private static final YXAudioEncoder YX_AUDIO_ENCODER = new YXAudioEncoder();
    }
}
