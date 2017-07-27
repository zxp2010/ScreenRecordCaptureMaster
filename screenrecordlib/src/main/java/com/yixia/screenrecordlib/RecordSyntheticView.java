package com.yixia.screenrecordlib;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.yixia.screenrecordlib.record.audio.AudioDataBean;
import com.yixia.screenrecordlib.record.callback.IRecordShotCallback;
import com.yixia.screenrecordlib.util.RecordScreenLogUtil;

import java.util.TimerTask;

/**
 * 录屏按钮以及视频上传进度条
 * <p>
 * Created by zhaoxiaopo on 2017/7/11.
 */
public class RecordSyntheticView extends FrameLayout {

    public static final int REQUEST_CODE_CAPTURE_SCREEN = 0x303;
    private ImageView mRecordImg;

    //系统提供的录屏工具
    private MediaProjectionManager mMediaProjectionManager;
    private RecordScreenDialog mRecordDialog;
    private YZBRecordScreenDialog mYZBRecordScreenDialog;

    private IShareLivePlayer mIShareLivePlayer;
    private IRecordShotCallback mCallback;

    private boolean isYizhibo = true;

    public RecordSyntheticView(Context context) {
        this(context, null);
    }

    public RecordSyntheticView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordSyntheticView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mRecordDialog != null) {
            mRecordDialog.releae();
        }
    }

    private void init(Context context, AttributeSet attrs) {
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        mRecordImg = new ImageView(context);
        mRecordImg.setBackgroundResource(R.mipmap.iv_screen_open_default);
        mRecordImg.setLayoutParams(new LayoutParams(dip2px(context.getApplicationContext(), 35),
                dip2px(context.getApplicationContext(), 35)));

        addView(mRecordImg);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    Toast.makeText(view.getContext(), R.string.str_support_capture_screen,
                            Toast.LENGTH_SHORT).show();
                }
                requestRecordPermission();
            }
        });
    }

    /**
     * 标志是否显示一直播的弹窗
     *
     * @param isYizhibo
     */
    public void setYizhibo(boolean isYizhibo){
        this.isYizhibo = isYizhibo;
    }

    private void requestRecordPermission() {
        if (mMediaProjectionManager == null) {
            mMediaProjectionManager = (MediaProjectionManager) getContext().getSystemService(Service.MEDIA_PROJECTION_SERVICE);
        }
        if (getContext() instanceof Activity) {
            Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
            ((Activity) getContext()).startActivityForResult(captureIntent, REQUEST_CODE_CAPTURE_SCREEN);
        }
    }

    /**
     * 接受录屏权限请求结果，授权后开始录屏
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @param isRecordAudio 是否录制音频数据
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data, String filePath, boolean isRecordAudio) {
        if (requestCode == REQUEST_CODE_CAPTURE_SCREEN) {
            MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            if (mediaProjection == null) {
                return;
            }
            if(isYizhibo){
                showYZBRecordDialog(mediaProjection, filePath, isRecordAudio);
                return;
            }
            showRecordDialog(mediaProjection, filePath, isRecordAudio);
        }
    }

    private void showYZBRecordDialog(MediaProjection mediaProjection, String path, boolean isRecordAudio) {
        if (mYZBRecordScreenDialog == null) {
            mYZBRecordScreenDialog = new YZBRecordScreenDialog(getContext(), R.style.record_dialog);
            mYZBRecordScreenDialog.setFilePath(path);
            Window window = mYZBRecordScreenDialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
            mYZBRecordScreenDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
//                    showSystemUI();
                }
            });
        }
        //第二个参数表述是否录制音频数据
        mYZBRecordScreenDialog.setActivityResult(mediaProjection, isRecordAudio);
        mYZBRecordScreenDialog.setIShareLivePlayer(mIShareLivePlayer);
        mYZBRecordScreenDialog.setCallback(mCallback);
        mYZBRecordScreenDialog.show();
    }

    private void showRecordDialog(MediaProjection mediaProjection, String filePath, boolean isRecordAudio) {
        if (mRecordDialog == null) {
            mRecordDialog = new RecordScreenDialog(getContext(), R.style.record_dialog);
            mRecordDialog.setFilePath(filePath);
            Window window = mRecordDialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
            mRecordDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
//                    showSystemUI();
                }
            });
        }
        //第二个参数表述是否录制音频数据
        mRecordDialog.setActivityResult(mediaProjection, isRecordAudio);
        mRecordDialog.setIShareLivePlayer(mIShareLivePlayer);
        mRecordDialog.setCallback(mCallback);
        mRecordDialog.show();
    }

    /**
     * 设置音频数据
     * <p>从so中获取的数据封装成{@link AudioDataBean}对象</p>
     *
     * @param audioDataBean
     */
    public void putAudioData(final AudioDataBean audioDataBean){
        if (mYZBRecordScreenDialog == null) {
            mYZBRecordScreenDialog.putAudioData(audioDataBean);
        }
        if (mRecordDialog == null) {
            mRecordDialog.putAudioData(audioDataBean);
        }
    }

    public void setCallback(IRecordShotCallback callback) {
        mCallback = callback;
    }

    /**
     * 设置so音视频信息回掉接口
     *
     * @param IShareLivePlayer
     */
    public void setIShareLivePlayer(IShareLivePlayer IShareLivePlayer) {
        mIShareLivePlayer = IShareLivePlayer;
    }

    private int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5F);
    }
}
