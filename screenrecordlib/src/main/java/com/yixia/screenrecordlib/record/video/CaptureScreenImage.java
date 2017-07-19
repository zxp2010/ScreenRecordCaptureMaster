package com.yixia.screenrecordlib.record.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.view.Surface;

import com.yixia.screenrecordlib.record.callback.IRecordShotCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 屏幕截屏功能
 * <p>
 * Created by zhaoxiaopo on 2017/7/11.
 */

public class CaptureScreenImage {

    public static CaptureScreenImage mCaptureScreenImage = null;

    private VirtualDisplay mVirtualDisplay;
    private Surface mSurface;
    private ImageReader mImageReader;
    private HandlerThread mCheckThread;
    private Handler mCheckHandler;

    private final Lock mImageReaderLock = new ReentrantLock(true /*fair*/);

    private CaptureScreenImage() {
    }

    public static CaptureScreenImage getInstance() {
        if (mCaptureScreenImage == null) {
            mCaptureScreenImage = new CaptureScreenImage();
        }
        return mCaptureScreenImage;
    }

    public void initCapture(Context context, MediaProjection mediaProjection, final String path, final IRecordShotCallback callback){
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        // thread for image checking
        mCheckThread = new HandlerThread("CheckHandler");
        mCheckThread.start();
        mCheckHandler = new Handler(mCheckThread.getLooper());

        try {
            mImageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2);
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    imageAvailable(reader, path, callback);
                }
            }, mCheckHandler);
            mSurface = mImageReader.getSurface();
        }finally {

        }
        mVirtualDisplay = mediaProjection.createVirtualDisplay("mediaprojection", screenWidth, screenHeight,
                1, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, null, null);
    }

    /**
     * 图像可用时截屏
     *
     * @param reader
     * @param path
     *@param callback @return
     */
    private void imageAvailable(ImageReader reader, String path, IRecordShotCallback callback) {
        mImageReaderLock.lock();
        try{
            Image image = reader.acquireLatestImage();

            if(image == null) return;
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            image.close();
            //需要在这里释放图片否则会截取很多图片
            release();

            saveBitmap(path, bitmap, callback);
        }finally {
            mImageReaderLock.unlock();
        }
    }

    private void saveBitmap(String path, Bitmap bm, IRecordShotCallback callback) {
        File parent = new File(TextUtils.isEmpty(path) ? Environment.getExternalStorageDirectory() + "/" + Environment
                .DIRECTORY_DCIM + "/weibo" : path);
        if (!parent.exists()) {
            parent.mkdirs();
        }
        File f = new File(parent.getAbsolutePath() + File.separator + "pic" + new Date().getTime() + ".jpg");
        if (f.exists()) {
            f.delete();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            if (callback != null) {
                //截屏文件，保存结束通知应用
                callback.onComplete(3, f.getAbsolutePath(), 0, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.failed();
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void release() {
        if(mImageReader != null){
            mImageReader.close();
            mImageReader = null;
            mSurface = null;
        }
        mCheckThread.quit();
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
    }
}
