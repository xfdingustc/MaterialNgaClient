package io.xfdingustc.mdngaclient.libs.rx.transformers;

import android.support.annotation.NonNull;

import io.xfdingustc.mdngaclient.libs.rx.transformers.ObserveForUITransformer;
import io.xfdingustc.mdngaclient.libs.rx.transformers.TakeWhenTransformer;
import rx.Observable;

/**
 * Created by whaley on 2017/5/17.
 */

public final class Transformers {
    private Transformers() {}

    public static <S, T> TakeWhenTransformer<S, T> takeWhen(final @NonNull Observable<T> when) {
        return new TakeWhenTransformer<>(when);
    }

    public static @NonNull <T> ObserveForUITransformer<T> observerForUI() {
        return new ObserveForUITransformer<>();
    }
}
