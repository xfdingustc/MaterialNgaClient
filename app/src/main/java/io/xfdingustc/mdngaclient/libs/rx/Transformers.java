package io.xfdingustc.mdngaclient.libs.rx;

import android.support.annotation.NonNull;

import io.xfdingustc.mdngaclient.libs.rx.transformers.ObserveForUITransformer;

/**
 * Created by whaley on 2017/5/17.
 */

public final class Transformers {
    private Transformers() {}

    public static @NonNull <T> ObserveForUITransformer<T> observerForUI() {
        return new ObserveForUITransformer<>();
    }
}
