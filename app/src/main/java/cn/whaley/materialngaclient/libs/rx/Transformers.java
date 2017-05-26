package cn.whaley.materialngaclient.libs.rx;

import android.support.annotation.NonNull;

import cn.whaley.materialngaclient.libs.rx.transformers.ObserveForUITransformer;

/**
 * Created by whaley on 2017/5/17.
 */

public final class Transformers {
    private Transformers() {}

    public static @NonNull <T> ObserveForUITransformer<T> observerForUI() {
        return new ObserveForUITransformer<>();
    }
}
