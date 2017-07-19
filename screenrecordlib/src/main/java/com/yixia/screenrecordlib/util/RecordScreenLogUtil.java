package com.yixia.screenrecordlib.util;

import android.util.Log;

/**
 * Created by zhaoxiaopo on 2017/7/17.
 */

public class RecordScreenLogUtil {
    public static boolean isLog = true;

    public static void i(String tag, String msg) {
        if (isLog)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (isLog)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isLog)
            Log.e(tag, msg);
    }
}
