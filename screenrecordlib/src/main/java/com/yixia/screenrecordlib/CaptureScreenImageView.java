package com.yixia.screenrecordlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.yixia.screenrecordlib.record.TotalController;
import com.yixia.screenrecordlib.record.callback.IRecordShotCallback;

/**
 *
 * Created by zhaoxiaopo on 2017/7/27.
 */

public class CaptureScreenImageView extends ImageView {

    private String mFilePath;
    private MediaProjection mMediaProjection;
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
     *
     * @param mediaProjection
     */
    public void setActivityResult(MediaProjection mediaProjection) {
        mMediaProjection = mediaProjection;
    }


    private void initView(Context context, AttributeSet attrs) {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TotalController.getInstance().captureScreenImage(getContext(), mFilePath, mMediaProjection, mCallback);
            }
        });
    }

    public void setCallback(IRecordShotCallback callback) {
        mCallback = callback;
    }

    public void setFilePath(String path) {
        mFilePath = path;
    }

    public void release(){
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }
}
