package com.yixia.screenrecordlib;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.projection.MediaProjection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yixia.screenrecordlib.record.TotalController;
import com.yixia.screenrecordlib.record.audio.AudioDataBean;
import com.yixia.screenrecordlib.record.callback.IRecordShotCallback;
import com.yixia.screenrecordlib.util.RecordScreenLogUtil;
import com.yixia.screenrecordlib.view.RecordTipsView;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhaoxiaopo on 2017/7/27.
 */

public class YZBRecordScreenDialog extends Dialog implements View.OnClickListener {

    /**
     * 视频的总时长
     */
    private static final int TOTAL_TIME = 180;
    private static final int MIN_TIME = 6;

    private TotalController mTotalController;

    private int mCurrentTime = 0;
    private Timer mTimer;
    private Handler mHandler = new Handler();
    private IRecordShotCallback mCallback;
    private File mFile;
    private String mFilePath;

    private MediaProjection mMediaProjection;
    private boolean isAudience;
    private boolean isStartRecord = false;
    private RecordScreenDialog.IShareLivePlayer mIShareLivePlayer;

    private ProgressBar mProgressBar;
    private ImageView mRecordStateImg;
    private ImageView mRestartImg;
    private ImageView mCancelImg;
    private TextView mRecordTime;

    private RecordTipsView mRecordTipsView;

    public YZBRecordScreenDialog(Context context, int theme) {
        super(context, theme);

        setContentView(R.layout.dialog_yzb_record_screen_view);
        mRecordStateImg = (ImageView) findViewById(R.id.yzb_record_state_btn);
        mRestartImg = (ImageView) findViewById(R.id.yzb_record_restart_img);
        mCancelImg = (ImageView) findViewById(R.id.yzb_record_cancel_img);
        mRecordTime = (TextView) findViewById(R.id.yzb_record_time);
        mProgressBar = (ProgressBar) findViewById(R.id.yzb_record_progress_bar);
        mRecordTipsView = (RecordTipsView) findViewById(R.id.record_tips_view);

        mRecordStateImg.setOnClickListener(this);
        mRestartImg.setOnClickListener(this);
        mCancelImg.setOnClickListener(this);

        initData();
    }

    @Override
    public void show() {
        super.show();
        isShowOtherViews(false);
        hideSystemUI();
        mHandler.postDelayed(new TimerTask() {
            @Override
            public void run() {
                setTipsDialog("至少录制6秒, 点击开始");
            }
        }, 1000);
    }

    private void initData() {
        initFile();
        mProgressBar.setMax(TOTAL_TIME);
        mTotalController = TotalController.getInstance();
        mTotalController.initEncodeThread();
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                return true;
            }
        });
    }

    private void initFile(){
        File parent = new File(TextUtils.isEmpty(mFilePath) ? Environment.getExternalStorageDirectory() + File.separator
                + Environment.DIRECTORY_DCIM + File.separator + "weibo" : mFilePath);
        if (!parent.exists()) {
            parent.mkdirs();
        }

        mFile = new File(parent.getAbsolutePath() + File.separator + "weibo" + new Date().getTime()
                + ".mp4");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.yzb_record_state_btn == id) {
            if (isStartRecord) {
                if (mCurrentTime < MIN_TIME) {
                    return;
                }
                completeRecord(false);
                showSystemUI();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                },100);
                return;
            }
            mTotalController.prepare(mFile.getAbsolutePath());
            mRecordStateImg.setImageResource(R.mipmap.iv_screen_open_default);
            startRecord();
        } else if (R.id.yzb_record_restart_img == id) {
            completeRecord(true);
            initFile();

            mTotalController.prepare(mFile.getAbsolutePath());
            mRecordStateImg.setImageResource(R.mipmap.iv_screen_open_default);
            startRecord();
        } else if (R.id.yzb_record_cancel_img == id) {
            showSystemUI();
            deleteFile(mFile);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            },200);
        }
    }

    public void setCallback(IRecordShotCallback callback) {
        mCallback = callback;
    }

    private void setTipsDialog(String string) {
        int[] location = new int[2];
        mRecordStateImg.getLocationOnScreen(location);

        mRecordTipsView.setLayoutText(string);
        mRecordTipsView.startLayoutAnim(location[0], mRecordStateImg.getWidth(), getContext(),
                new RecordTipsView.OnBubleDialogListner() {
                    @Override
                    public void animOver() {
                        mRecordTipsView.setVisibility(View.INVISIBLE);
                    }
                });
    }

    public void setFilePath(String path) {
        mFilePath = path;
    }

    /**
     * 设置开始后的ActivityResult回掉
     *
     * @param mediaProjection
     * @param isAudience      是否录制音频数据
     */
    public void setActivityResult(MediaProjection mediaProjection, Boolean isAudience) {
        mMediaProjection = mediaProjection;
        this.isAudience = isAudience;
    }

    public void startRecord() {
        isShowOtherViews(true);
        isStartRecord = true;
        if (mTotalController != null && mTimer == null && mFile != null) {
            mTotalController.start(getContext(), mMediaProjection, isAudience);
            new Thread(new TimerTask() {
                @Override
                public void run() {
                    if (isStartRecord) {
                        if (mIShareLivePlayer != null) {
                            AudioDataBean audioDataBean = mIShareLivePlayer.getAudioData();
                            RecordScreenLogUtil.i("audioInfo", "put audio data : " + audioDataBean);
                            if (audioDataBean != null) {
                                mTotalController.putAudioData(audioDataBean.getRawBuffer(), audioDataBean.getLength());
                            }
                        }
                    }
                }
            }).start();
            if (mIShareLivePlayer != null) {
                mIShareLivePlayer.setIsMediaDataPutOut(true);
            }
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCurrentTime < TOTAL_TIME) {
                                mCurrentTime++;
                                setRecordProgress(mCurrentTime);
//                                checkShowFirstTip();
                            } else {
                                showSystemUI();
                                completeRecord(false);
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismiss();
                                    }
                                }, 200);
                            }
                        }
                    });
                }
            }, 1000, 1000);
        }
    }

    private void isShowOtherViews(boolean isShow) {
        mRecordTime.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
        mProgressBar.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 异常结束录屏
     * 比如，主播离开？用户退到后台？希望可以做到各种情况吧
     */
    public void unusualStopRecord() {
        if (mTimer == null) {
            return;
        }
        if (mIShareLivePlayer != null) {
            mIShareLivePlayer.setIsMediaDataPutOut(false);
        }
        mTotalController.stop();
        stopTimer();
        dismiss();
        checkFile();
        Toast.makeText(getContext(), "精彩时刻已保存至本地相册", Toast.LENGTH_SHORT).show();
    }

    private void completeRecord(boolean isRestart) {
        mRecordStateImg.setImageResource(R.mipmap.iv_screen_close_default);
        stopRecordThread();
        if (mTimer == null) return;
        if (mCallback != null) {
            if(!isRestart) {
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(mFile.getAbsolutePath());
                mCallback.onComplete(1, mFile.getAbsolutePath(), mCurrentTime, mediaMetadataRetriever.getFrameAtTime(0));
            }else{
                deleteFile(mFile);
                mCallback.onRestartRecord();
            }
        }
        stopTimer();
        checkFile();
        isStartRecord = false;
    }

    public void releae() {
        if (mTotalController != null) {
            mTotalController.releaseThread();
        }
    }

    /**
     * 设置录屏时间进度条
     *
     * @param time
     */
    private void setRecordProgress(int time) {
        int seconds = time % 60;
        int minutes = (time / 60) % 60;
        mRecordTime.setText(String.format(Locale.SIMPLIFIED_CHINESE, "%02d:%02d", minutes, seconds));
        mProgressBar.setProgress(time);
    }

    /**
     * 停止录屏线程
     */
    private void stopRecordThread() {
        if (mIShareLivePlayer != null) {
            mIShareLivePlayer.setIsMediaDataPutOut(false);
        }
        mTotalController.stop();
    }

    /**
     * 检查保存的文件并且更新到相册
     */
    private void checkFile() {
        if (mFile.exists()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(mFile));
                getContext().sendBroadcast(mediaScanIntent);
            } else {
                getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(mFile.getPath())));
            }
        }
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mCurrentTime = 0;
            setRecordProgress(0);
        }
    }

    public void deleteFile(File file) {
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                if (children == null) {
                    return;
                }

                File[] var3 = children;
                int var4 = children.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    File f = var3[var5];
                    if (f.isDirectory()) {
                        this.deleteFile(f);
                    }

                    if (!f.delete()) {
                        f.deleteOnExit();
                    }
                }
            } else if (!file.delete()) {
                file.deleteOnExit();
            }
        }
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

    /**
     * <p>设置SO方法回掉, 必须设置</p>
     *
     * @param IShareLivePlayer
     */
    public void setIShareLivePlayer(RecordScreenDialog.IShareLivePlayer IShareLivePlayer) {
        mIShareLivePlayer = IShareLivePlayer;
    }
}
