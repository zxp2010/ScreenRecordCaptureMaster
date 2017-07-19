package com.yixia.screenrecordlib.record.video;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.view.Surface;

import com.yixia.screenrecordlib.record.YXMuxerWrapper;
import com.yixia.screenrecordlib.util.RecordScreenLogUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 视频编码器，因为数据源只有一个 so获取数据源和视频编码放在一起了
 * <p>
 * Created by zhaoxiaopo on 2017/7/11.
 */
public class YixiaScreenEncoder {
    private static final String TAG = "YixiaScreenEncoder";
    // parameters for the encoder
    private static final String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC; // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 15; // 15 fps
    private static final int IFRAME_INTERVAL = 1; // 10 seconds between I-frames
    private static final int TIMEOUT_US = 10000;
    public static YixiaScreenEncoder mYixiaScreenEncoder = null;
    protected YXMuxerWrapper mWeakMuxer;
    private int mWidth = 1280;
    private int mHeight = 720;
    private int mBitRate = 6000000;//4000000
    private int mDpi = 1;
    private String mDstPath;
    private MediaProjection mMediaProjection;
    private MediaCodec mEncoder;
    private Surface mSurface;
    private boolean mMuxerStarted = false;
    private int mVideoTrackIndex = -1;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private VirtualDisplay mVirtualDisplay;
    private boolean isStart = true;

    private YixiaScreenEncoder() {
    }

    public static YixiaScreenEncoder getInstance() {
        synchronized (YixiaScreenEncoder.class) {
            if (mYixiaScreenEncoder == null)
                mYixiaScreenEncoder = new YixiaScreenEncoder();
        }
        return mYixiaScreenEncoder;
    }


    public void prepar(Context context, MediaProjection mp, YXMuxerWrapper muxer) {
        mDpi = context.getResources().getDisplayMetrics().densityDpi;
        mWeakMuxer = muxer;
        mMediaProjection = mp;
    }

    public void startRecordVideo() {
        try {
            isStart = true;
            prepareEncoder();
            mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG + "-display",
                    mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    mSurface, null, null);
            recordVirtualDisplay();
        } catch (Exception e) {
            stopRecordVideo();
        }
    }

    private void recordVirtualDisplay() {
        while (isStart) {
            int index = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_US);
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                resetOutputFormat();

            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                try {
                    // wait 10ms
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            } else if (index >= 0) {
                if (!mMuxerStarted) {
                    throw new IllegalStateException("MediaMuxer dose not call addTrack(format) ");
                }
                encodeToVideoTrack(index);

                mEncoder.releaseOutputBuffer(index, false);
            }
        }
    }

    private void encodeToVideoTrack(int index) {
        ByteBuffer encodedData = mEncoder.getOutputBuffer(index);

        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // The codec config data was pulled out and fed to the muxer when we got
            // the INFO_OUTPUT_FORMAT_CHANGED status.
            // Ignore it.
            mBufferInfo.size = 0;
        }
        if (mBufferInfo.size == 0) {
            encodedData = null;
        } else {

        }
        if (encodedData != null) {
            encodedData.position(mBufferInfo.offset);
            encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

            mWeakMuxer.writeMediaData(mVideoTrackIndex, encodedData, mBufferInfo);
        }
    }

    private void resetOutputFormat() {
        // should happen before receiving buffers, and should only happen once
        if (mMuxerStarted) {
            throw new IllegalStateException("output format already changed!");
        }
        MediaFormat newFormat = mEncoder.getOutputFormat();

        mVideoTrackIndex = mWeakMuxer.addMediaTrack(newFormat);
        RecordScreenLogUtil.i(TAG, "mVideoTrackIndex ; " + mVideoTrackIndex);
        if (mVideoTrackIndex == -1)
            return;

        mMuxerStarted = true;
    }

    private void prepareEncoder() throws IOException {
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        RecordScreenLogUtil.i(TAG, "created video format: " + format);
        mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface = mEncoder.createInputSurface();
        mEncoder.start();
    }


    public void setIsStartFalse() {
        isStart = false;
        mMuxerStarted = false;
    }

    public void stopRecordVideo() {
        isStart = false;
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
    }
}
