package cn.whaley.materialngaclient.libs.utils;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by whaley on 2017/5/16.
 */

public class BundleUtils {
    private BundleUtils() {}

    public static Bundle maybeGetBundle(final @Nullable Bundle state, final @NonNull String key) {
        if (state == null) {
            return null;
        }
        return state.getBundle(key);
    }
}
