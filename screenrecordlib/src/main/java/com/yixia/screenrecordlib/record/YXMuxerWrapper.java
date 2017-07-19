package com.yixia.screenrecordlib.record;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import com.yixia.screenrecordlib.util.RecordScreenLogUtil;

import java.nio.ByteBuffer;

/**
 * Created by zhaoxiaopo on 2017/7/11.
 */

public class YXMuxerWrapper {
    private static final String TAG = YXMuxerWrapper.class.getSimpleName();
    public static YXMuxerWrapper sYxMuxerWrapper = null;
    private MediaMuxer mMuxer;
    private boolean isRecordAudio = true;
    private boolean mMuxerStarted = false;
    private String mOutputPath;
    private int mTime = 0;

    private YXMuxerWrapper() {
    }

    public static synchronized YXMuxerWrapper getInstance() {
        if (sYxMuxerWrapper == null) {
            sYxMuxerWrapper = YXMuxerWrapperHolder.INSTANCE;
        }
        return sYxMuxerWrapper;
    }

    public void setIsRecordAudio(Boolean isAudience) {
        this.isRecordAudio = isAudience;
    }

    public void prepar(String mOutputPath) {
        try {
            mMuxer = new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    public synchronized int addMediaTrack(MediaFormat Format) {
        RecordScreenLogUtil.i(TAG, "addMediaTrack ; " + Format);
        int flag = isRecordAudio ? 2 : 1;
        try {
            int i = mMuxer.addTrack(Format);
            RecordScreenLogUtil.i(TAG, "addMediaTrack index : " + i);
            mTime++;
            if (mTime >= flag) {
                mMuxer.start();
                mMuxerStarted = true;
            }
            return i;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public synchronized void writeMediaData(int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        if (mMuxerStarted) {
            RecordScreenLogUtil.e(TAG, "当前线程：：：：" + trackIndex);
            if (mMuxer != null && mMuxerStarted == true)
                mMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
        }
    }

    public synchronized void stopMuxer() {
        try {
            if (mMuxer != null) {
                mMuxer.stop();
                mMuxer.release();
                mMuxer = null;
                mTime = 0;
                mMuxerStarted = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static class YXMuxerWrapperHolder {
        private static final YXMuxerWrapper INSTANCE = new YXMuxerWrapper();
    }
}
