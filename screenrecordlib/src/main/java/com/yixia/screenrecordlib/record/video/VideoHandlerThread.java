package com.yixia.screenrecordlib.record.video;

import android.content.Context;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.yixia.screenrecordlib.record.YXMuxerWrapper;
import com.yixia.screenrecordlib.util.RecordScreenLogUtil;


/**
 * 视频编码线程
 * <p>
 * Created by zhaoxiaopo on 2017/7/11.
 */

public class VideoHandlerThread extends HandlerThread implements Handler.Callback {

    private static final String TAG = VideoHandlerThread.class.getSimpleName();

    private static final int MSG_RECORDING_START = 100;
    private static final int MSG_RECORDING_STOP = 101;

    //线程内部的调用
    private Handler mHandler;

    private YixiaScreenEncoder mYixiaScreenEncoder;

    public VideoHandlerThread(String name, int priority) {
        super(name, priority);
        mYixiaScreenEncoder = YixiaScreenEncoder.getInstance();
    }

    public void prepar(Context context, MediaProjection mp, YXMuxerWrapper muxer) {
        mYixiaScreenEncoder.prepar(context, mp, muxer);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        RecordScreenLogUtil.i("loop", "initial the handler");
        mHandler = new Handler(getLooper(), this);
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_RECORDING_START:
                mYixiaScreenEncoder.startRecordVideo();
                break;
            case MSG_RECORDING_STOP:
                mYixiaScreenEncoder.stopRecordVideo();
                break;
        }
        return true;
    }


    public void startRecording() {
        Message msg = Message.obtain(null, MSG_RECORDING_START);
        mHandler.sendMessage(msg);
    }

    public void stopRecording() {
        mYixiaScreenEncoder.setIsStartFalse();
        Message msg = Message.obtain(null, MSG_RECORDING_STOP);
        mHandler.sendMessage(msg);
    }
}
