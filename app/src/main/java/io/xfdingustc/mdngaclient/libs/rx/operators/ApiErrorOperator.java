package io.xfdingustc.mdngaclient.libs.rx.operators;

import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by whaley on 2017/5/31.
 */

public final class ApiErrorOperator<T> implements Observable.Operator<T, Response<T>> {

    @Override
    public Subscriber<? super Response<T>> call(final Subscriber<? super T> subscriber) {
        return new Subscriber<Response<T>>() {
            @Override
            public void onCompleted() {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }
            }

            @Override
            public void onError(Throwable e) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e);
                }
            }

            @Override
            public void onNext(Response<T> response) {
                if (subscriber.isUnsubscribed()) {
                    return;
                }

                if (!response.isSuccessful()) {

                } else {
                    subscriber.onNext(response.body());
                    subscriber.onCompleted();
                }
            }
        };
    }
}
