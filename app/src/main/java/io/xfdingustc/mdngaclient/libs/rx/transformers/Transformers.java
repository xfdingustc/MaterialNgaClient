package io.xfdingustc.mdngaclient.libs.rx.transformers;

import android.support.annotation.NonNull;

import io.xfdingustc.mdngaclient.libs.rx.transformers.ObserveForUITransformer;
import io.xfdingustc.mdngaclient.libs.rx.transformers.TakeWhenTransformer;
import io.xfdingustc.mdngaclient.services.apiresponses.ErrorEnvelope;
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

/**
 * Created by whaley on 2017/5/17.
 */

public final class Transformers {
    private Transformers() {}

    public static <T> NeverApiErrorTransformer<T> pipeApiErrorsTo(final @NonNull PublishSubject<ErrorEnvelope> errorSubject) {
        return new NeverApiErrorTransformer<>(new Action1<ErrorEnvelope>() {
            @Override
            public void call(ErrorEnvelope errorEnvelope) {
                errorSubject.onNext(errorEnvelope);
            }
        });
    }

    public static <T> NeverErrorTransformer<T> neverError() {
        return new NeverErrorTransformer<>();
    }

    public static <S, T> TakeWhenTransformer<S, T> takeWhen(final @NonNull Observable<T> when) {
        return new TakeWhenTransformer<>(when);
    }

    public static @NonNull <T> ObserveForUITransformer<T> observerForUI() {
        return new ObserveForUITransformer<>();
    }
}
