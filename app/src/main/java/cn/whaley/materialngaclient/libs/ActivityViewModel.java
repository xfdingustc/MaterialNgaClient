package cn.whaley.materialngaclient.libs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by whaley on 2017/5/16.
 */

public class ActivityViewModel<ViewType extends ActivityLifecycleType> {
    private final PublishSubject<ViewType> viewChange = PublishSubject.create();
    private final PublishSubject<Intent> intent = PublishSubject.create();
    private final CompositeSubscription subscription = new CompositeSubscription();

    public ActivityViewModel(final @NonNull Environment environment) {

    }

    public void intent(final @NonNull Intent intent) {
        this.intent.onNext(intent);
    }

    @CallSuper
    protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
        Timber.d("onCreate %s", this.toString());
        dropView();
    }

    @CallSuper
    protected void onPause() {
        Timber.d("onPause %s", this.toString());
        dropView();
    }

    public void onDestroy() {
        Timber.d("onDestroy %s", this.toString());
        subscription.clear();
        viewChange.onCompleted();
    }

    private void dropView() {
        Timber.d("dropView %s", this.toString());
        viewChange.onNext(null);
    }


}
