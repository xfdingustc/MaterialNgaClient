package cn.whaley.materialngaclient.libs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.trello.rxlifecycle.android.ActivityEvent;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by whaley on 2017/5/16.
 */

public class ActivityViewModel<ViewType extends ActivityLifecycleType> {
    private final PublishSubject<ViewType> viewChange = PublishSubject.create();
    private final Observable<ViewType> view = viewChange.filter(new Func1<ViewType, Boolean>() {
        @Override
        public Boolean call(ViewType viewType) {
            return viewType != null;
        }
    });

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

    protected void onResume(final @NonNull ViewType view) {
        Timber.d("onResume %s", this.toString());
        onTakeView(view);
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

    private void onTakeView(@NonNull ViewType view) {
        Timber.d("onTakeView %s %s", this.toString(), view.toString());
        viewChange.onNext(view);
    }

    private void dropView() {
        Timber.d("dropView %s", this.toString());
        viewChange.onNext(null);
    }


    public @NonNull <T> Observable.Transformer<T, T> bindToLifecycle() {
        final Observable<Pair<ViewType, ActivityEvent>> observable = view.switchMap(new Func1<ViewType, Observable<Pair<ViewType, ActivityEvent>>>() {
            @Override
            public Observable<Pair<ViewType, ActivityEvent>> call(final ViewType viewType) {
                return viewType.lifecycle().map(new Func1<ActivityEvent, Pair<ViewType, ActivityEvent>>() {
                    @Override
                    public Pair<ViewType, ActivityEvent> call(ActivityEvent event) {
                        return Pair.create(viewType, event);
                    }
                }).filter(new Func1<Pair<ViewType, ActivityEvent>, Boolean>() {
                    @Override
                    public Boolean call(Pair<ViewType, ActivityEvent> viewTypeActivityEventPair) {
                        return isFinished(viewTypeActivityEventPair.first, viewTypeActivityEventPair.second);
                    }
                });
            }
        });
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> source) {
                return source.takeUntil(observable);
            }
        };
    }

    private boolean isFinished(@NonNull ViewType view, @NonNull ActivityEvent event) {
        if (view instanceof BaseActivity) {
            return event == ActivityEvent.DESTROY && ((BaseActivity)view).isFinishing();
        }

        return event == ActivityEvent.DESTROY;
    }


}
