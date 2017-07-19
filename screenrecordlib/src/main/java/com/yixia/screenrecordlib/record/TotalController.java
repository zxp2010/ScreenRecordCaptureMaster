package com.yixia.screenrecordlib.record;

import android.content.Context;
import android.media.projection.MediaProjection;

import com.yixia.screenrecordlib.record.audio.AudioHandlerThread;
import com.yixia.screenrecordlib.record.callback.IRecordShotCallback;
import com.yixia.screenrecordlib.record.video.CaptureScreenImage;
import com.yixia.screenrecordlib.record.video.VideoHandlerThread;

import java.nio.ByteBuffer;

import static android.os.Process.THREAD_PRIORITY_AUDIO;

/**
 * Created by zhaoxiaopo on 2017/7/11.
 */

public class TotalController {
    private static TotalController mTotalController = null;
    private YXMuxerWrapper mYixiaMuxerWrapper;
    private AudioHandlerThread mAudioHandlerThread;
    private VideoHandlerThread mVideoHandlerThread;
    private Boolean isRecording = false;

    private TotalController() {
    }

    public static TotalController getInstance() {
        synchronized (TotalController.class) {
            if (mTotalController == null) {
                mTotalController = new TotalController();
            }
        }
        return mTotalController;
    }

    public void initEncodeThread(){
        mVideoHandlerThread = new VideoHandlerThread("Video Recorder Thread", THREAD_PRIORITY_AUDIO);
        mVideoHandlerThread.start();
        mAudioHandlerThread = new AudioHandlerThread("Audio Recorder Thread", THREAD_PRIORITY_AUDIO);
        mAudioHandlerThread.start();
    }

    /**
     * 准备-》》》此时就该开启
     * Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
     * startActivityForResult(captureIntent, REQUEST_CODE);
     */
    public void prepare(String filePath) {
        mYixiaMuxerWrapper = YXMuxerWrapper.getInstance();
        mYixiaMuxerWrapper.prepar(filePath);
    }

    /**
     * onActivityResult(XXXX)回掉之后就该调用这个方法
     *
     * @param context
     * @param mediaProjection
     * @param isAudience
     */
    public void start(Context context, MediaProjection mediaProjection, Boolean isAudience) {
        if (mYixiaMuxerWrapper != null) {
            mYixiaMuxerWrapper.setIsRecordAudio(isAudience);
        }
        isRecording = true;
        mVideoHandlerThread.prepar(context, mediaProjection, mYixiaMuxerWrapper);
        mVideoHandlerThread.startRecording();
        mAudioHandlerThread.prepar(mYixiaMuxerWrapper);
        mAudioHandlerThread.startRecording();
    }


    /**
     * 停止
     * 关于重新录制，这个问题。。。那就再次执行上面这个start的方法吧
     */
    public void stop() {
        if (!isRecording) return;
        isRecording = false;
        mVideoHandlerThread.stopRecording();
        mAudioHandlerThread.stopRecording();
        mYixiaMuxerWrapper.stopMuxer();
        mYixiaMuxerWrapper = null;
    }

    public void releaseThread(){
        if(mVideoHandlerThread != null) {
            mVideoHandlerThread.quit();
            mVideoHandlerThread = null;
        }
        if(mAudioHandlerThread != null) {
            mAudioHandlerThread.quit();
            mAudioHandlerThread = null;
        }
    }

    /**
     * 从外部获取音频源输出
     *
     * @param rawBuffer
     * @param length
     */
    public void putAudioData(ByteBuffer rawBuffer, int length) {
        mAudioHandlerThread.putAudioData(rawBuffer, length);
    }

    /**
     * android 5.0以上屏幕截图
     *
     * @param mediaProjection
     * @param callback
     */
    public void captureScreenImage(Context context, String path, MediaProjection mediaProjection, IRecordShotCallback callback) {
        CaptureScreenImage.getInstance().initCapture(context, mediaProjection, path, callback);
    }

}
