package io.xfdingustc.mdngaclient.libs.rx.transformers;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by whaley on 2017/6/5.
 */

public class NeverErrorTransformer<T> implements Observable.Transformer<T, T> {
    @Override
    public Observable<T> call(Observable<T> source) {
        return source
            .doOnError(new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {

                }
            }).onErrorResumeNext(Observable.<T>empty());
    }
}
