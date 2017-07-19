package com.yixia.screenrecordlib.record.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.nio.ByteBuffer;

/**
 * Created by zhaoxiaopo on 2017/7/11.
 */

public class YXMicRecorder {

    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int SAMPLES_PER_FRAME = 1024;
    private static YXMicRecorder sYxMicRecorder = null;
    private AudioRecord mAudioRecord;
    private int bufferSizeInBytes;
    private boolean isRecording = false;
    private YXAudioEncoder yixiaAudioEncoder;

    private YXMicRecorder() {
    }

    public static synchronized YXMicRecorder getInstance() {
        if (sYxMicRecorder == null) {
            sYxMicRecorder = YXMicRecorderHolder.INSTANCE;
        }
        return sYxMicRecorder;
    }

    public void prepar(YXAudioEncoder encoder) {
        yixiaAudioEncoder = encoder;
        bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT);
        mAudioRecord = new AudioRecord(AUDIO_SOURCE,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSizeInBytes);
    }

    public void start() {
        mAudioRecord.startRecording();
        isRecording = true;
    }

    public void record() {
        final ByteBuffer bytebuffer = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
        int bufferReadResult;

        while (isRecording) {
            bytebuffer.clear();
            bufferReadResult = mAudioRecord.read(bytebuffer, SAMPLES_PER_FRAME);

            if (bufferReadResult == AudioRecord.ERROR_INVALID_OPERATION || bufferReadResult == AudioRecord.ERROR_BAD_VALUE) {

            } else if (bufferReadResult >= 0) {
                //LogUtil.d(TAG, "bytes read "+bufferReadResult);
                // todo send this byte array to an audio encoder

                bytebuffer.position(bufferReadResult);
                bytebuffer.flip();
                byte[] bytes = new byte[bytebuffer.remaining()];
                bytebuffer.get(bytes);

                bytebuffer.position(bufferReadResult);
                bytebuffer.flip();
                yixiaAudioEncoder.encode(bytebuffer, bufferReadResult, yixiaAudioEncoder.getPTSUs());
            }
        }
    }

    public void sendEOS() {

        final ByteBuffer bytebuffer = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
        int bufferReadResult;

        bufferReadResult = mAudioRecord.read(bytebuffer, SAMPLES_PER_FRAME);

        yixiaAudioEncoder.encode(bytebuffer, bufferReadResult, yixiaAudioEncoder.getPTSUs());

    }

    public void stopRecording() {
        mAudioRecord.stop();
        mAudioRecord.release();
        sendEOS();
        yixiaAudioEncoder.stop();
        //mAudioRecord = null;
    }

    public void setIsRecordingFalse() {
        isRecording = false;
    }

    private static class YXMicRecorderHolder {
        private static final YXMicRecorder INSTANCE = new YXMicRecorder();
    }
}
