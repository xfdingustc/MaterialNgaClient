package io.xfdingustc.mdngaclient.libs.rx.transformers;

import android.support.annotation.NonNull;

import io.xfdingustc.mdngaclient.services.apiresponses.ErrorEnvelope;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by whaley on 2017/6/5.
 */

public class NeverApiErrorTransformer<T> implements Observable.Transformer<T, T> {
    private final Action1<ErrorEnvelope> errorAction;

    protected  NeverApiErrorTransformer(final @NonNull Action1<ErrorEnvelope> errorAction) {
        this.errorAction = errorAction;
    }

    @Override
    public Observable<T> call(Observable<T> source) {
        return source
            .doOnError(new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    final ErrorEnvelope env = ErrorEnvelope.fromThrowable(throwable);
                    if (env != null && errorAction != null) {
                        errorAction.call(env);
                    }
                }
            })
            .onErrorResumeNext(new Func1<Throwable, Observable<? extends T>>() {
                @Override
                public Observable<? extends T> call(Throwable throwable) {
                    if (ErrorEnvelope.fromThrowable(throwable) == null) {
                        return Observable.error(throwable);
                    } else {
                        return Observable.empty();
                    }
                }
            });
    }
}
