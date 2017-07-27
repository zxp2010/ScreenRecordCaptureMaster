package com.yixia.screenrecordlib.record.callback;

import android.graphics.Bitmap;

/**
 * Created by zhaoxiaopo on 2017/7/17.
 */

public interface IRecordShotCallback {
    /**
     * 结束录制，截屏结果
     *
     * @param type       录屏视频参数(0: 异常结束 1: 正常结束  3: 截屏结果)
     * @param path       文件路径（保存的视频和截图文件）
     * @param videoTime  视频时长
     * @param firstFrame
     */
    void onComplete(int type, String path, int videoTime, Bitmap firstFrame);

    void failed();

    void notSaveFile();

    void onRestart();
}
