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

import com.yixia.screenrecordlib.record.callback.IRecordShotCallback;

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

    private RecordScreenDialog.IShareLivePlayer mIShareLivePlayer;
    private IRecordShotCallback mCallback;

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
            showRecordDialog(mediaProjection, filePath, isRecordAudio);
        }
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

    public void setCallback(IRecordShotCallback callback) {
        mCallback = callback;
    }

    /**
     * 设置so音视频信息回掉接口
     *
     * @param IShareLivePlayer
     */
    public void setIShareLivePlayer(RecordScreenDialog.IShareLivePlayer IShareLivePlayer) {
        mIShareLivePlayer = IShareLivePlayer;
    }

    private int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5F);
    }
}
