package io.xfdingustc.mdngaclient.libs.rx.transformers;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.functions.Func2;

/**
 * Created by whaley on 2017/5/31.
 */

public class TakeWhenTransformer<S, T> implements Observable.Transformer<S, S> {
    private final Observable<T> when;

    public TakeWhenTransformer(final @NonNull Observable<T> when) {
        this.when = when;
    }

    @Override
    public Observable<S> call(Observable<S> source) {
        return when.withLatestFrom(source, new Func2<T, S, S>() {
            @Override
            public S call(T t, S s) {
                return s;
            }
        });
    }
}
