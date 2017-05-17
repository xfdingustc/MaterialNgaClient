package cn.whaley.materialngaclient.libs.rx;

import rx.Subscriber;

/**
 * Created by whaley on 2017/5/12.
 */

public abstract class SimpleSubscriber<T> extends Subscriber<T> {
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }
}
