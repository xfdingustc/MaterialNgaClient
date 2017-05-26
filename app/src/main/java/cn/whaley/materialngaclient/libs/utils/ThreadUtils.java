package cn.whaley.materialngaclient.libs.utils;

import android.os.Looper;

/**
 * Created by whaley on 2017/5/17.
 */

public final class ThreadUtils {
    private ThreadUtils() {

    }

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
}
