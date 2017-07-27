package com.yixia.screenrecordlib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.yixia.screenrecordlib.record.TotalController;
import com.yixia.screenrecordlib.record.callback.IRecordShotCallback;

/**
 *
 * Created by zhaoxiaopo on 2017/7/27.
 */

public class CaptureScreenImageView extends ImageView {

    public static final int REQUEST_CODE_CAPTURE_SCREEN = 0x304;

    //系统提供的录屏工具
    private MediaProjectionManager mMediaProjectionManager;

    private String mFilePath;
    private IRecordShotCallback mCallback;

    public CaptureScreenImageView(Context context) {
        this(context, null);
    }

    public CaptureScreenImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptureScreenImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CaptureScreenImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 设置开始后的ActivityResult回掉
     */
    public void setActivityResult(int resultCode, Intent data, String filePath) {
        if (mMediaProjectionManager == null) {
            return;
        }
        MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        TotalController.getInstance().captureScreenImage(getContext(), mFilePath, mediaProjection, mCallback);
    }

    private void initView(Context context, AttributeSet attrs) {
        setBackgroundResource(R.mipmap.iv_screen_open_default);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    Toast.makeText(v.getContext(), R.string.str_support_capture_screen,
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

    public void setCallback(IRecordShotCallback callback) {
        mCallback = callback;
    }

    public void setFilePath(String path) {
        mFilePath = path;
    }
}
