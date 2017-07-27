package com.yixia.screenrecordmaster;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.yixia.screenrecordlib.RecordScreenDialog;
import com.yixia.screenrecordlib.RecordSyntheticView;
import com.yixia.screenrecordlib.record.audio.AudioDataBean;
import com.yixia.screenrecordlib.record.callback.IRecordShotCallback;

public class MainActivity extends Activity {

    private RecordSyntheticView mRecordSyntheticView;
    private ImageView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecordSyntheticView = (RecordSyntheticView) findViewById(R.id.record_view);
        mImageView = (ImageView) findViewById(R.id.imageview);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasLocationPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            }
        }

        mRecordSyntheticView.setCallback(new IRecordShotCallback() {
            @Override
            public void onComplete(int type, String path, int videoTime, final Bitmap firstFrame) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(firstFrame);
                    }
                });
            }

            @Override
            public void failed() {

            }

            @Override
            public void notSaveFile() {

            }

            @Override
            public void onRestartRecord() {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mRecordSyntheticView.onActivityResult(requestCode, resultCode, data, "", false);
        mRecordSyntheticView.setIShareLivePlayer(new RecordScreenDialog.IShareLivePlayer() {
            @Override
            public void setIsMediaDataPutOut(boolean isCallback) {
                //SharedLivePlayer.getSharedInstance().setIsMediaDataPutOut(isCallback);
            }

            @Override
            public AudioDataBean getAudioData() {
                //从so中获取的直播音频信息
                //sample
                return null;
            }
        });
    }

    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
