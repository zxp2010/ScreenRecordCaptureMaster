package com.yixia.screenrecordlib.record.audio;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.yixia.screenrecordlib.record.YXMuxerWrapper;
import com.yixia.screenrecordlib.util.RecordScreenLogUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by zhaoxiaopo on 2017/7/11.
 */

public class AudioHandlerThread extends HandlerThread implements Handler.Callback {

    private static final String TAG = AudioHandlerThread.class.getSimpleName();

    private static final int MSG_RECORD_START = 0x1000;
    private static final int MSG_RECORD_STOP = 0x1001;

    private Handler mHandler;

    private YXAudioEncoder mAudioEncoder;
    //private YXMicRecorder yixiaMicRecorder;
    private YXMuxerWrapper mWeekMuxerWrapper;

    public AudioHandlerThread(String name, int priority) {
        super(name, priority);
        mAudioEncoder = YXAudioEncoder.getInstance();
    }

    public void prepar(YXMuxerWrapper muxer) {
        mWeekMuxerWrapper = muxer;
        try {
            mAudioEncoder.prepare(mWeekMuxerWrapper);
        } catch (IOException e) {
            RecordScreenLogUtil.e(TAG, e.getMessage());
        }
        // yixiaMicRecorder.prepar(mAudioEncoder);
    }


    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mHandler = new Handler(getLooper(), this);
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_RECORD_START:
                //yixiaMicRecorder.start();
                mAudioEncoder.start();
                //yixiaMicRecorder.record();
                break;
            case MSG_RECORD_STOP:
                //yixiaMicRecorder.stopRecording();
                mAudioEncoder.stop();
                break;
        }
        return true;
    }

    public void startRecording() {
        Message msg = Message.obtain(null, MSG_RECORD_START);
        mHandler.sendMessage(msg);
    }

    public void stopRecording() {
        //yixiaMicRecorder.setIsRecordingFalse();
        Message msg = Message.obtain(null, MSG_RECORD_STOP);
        mHandler.sendMessage(msg);
    }

    public void putAudioData(final ByteBuffer rawBuffer, final int length) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mAudioEncoder != null)
                    mAudioEncoder.encode(rawBuffer, length, mAudioEncoder.getPTSUs());
            }
        });
    }
}
