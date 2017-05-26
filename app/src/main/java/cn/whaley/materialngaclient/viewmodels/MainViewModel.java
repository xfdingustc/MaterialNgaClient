package cn.whaley.materialngaclient.viewmodels;

import android.support.annotation.NonNull;

import cn.whaley.materialngaclient.libs.ActivityViewModel;
import cn.whaley.materialngaclient.libs.Environment;
import cn.whaley.materialngaclient.ui.activities.MainActivity;
import cn.whaley.materialngaclient.viewmodels.outputs.MainViewModelInputs;
import cn.whaley.materialngaclient.viewmodels.outputs.MainViewModelOutputs;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

/**
 * Created by whaley on 2017/5/17.
 */

public class MainViewModel extends ActivityViewModel<MainActivity> implements MainViewModelInputs, MainViewModelOutputs {
    public MainViewModel(final @NonNull Environment environment) {
        super(environment);
        openDrawer
            .distinctUntilChanged()
            .compose(this.<Boolean>bindToLifecycle())
            .subscribe(drawerIsOpen);

        openDrawer
            .filter(new Func1<Boolean, Boolean>() {
                @Override
                public Boolean call(Boolean aBoolean) {
                    return aBoolean;
                }
            })
            .compose(this.<Boolean>bindToLifecycle())
            .subscribe(new Action1<Boolean>() {
                @Override
                public void call(Boolean aBoolean) {

                }
            });
    }

    private final PublishSubject<Boolean> openDrawer = PublishSubject.create();
    private final BehaviorSubject<Boolean> drawerIsOpen = BehaviorSubject.create();

    public final MainViewModelInputs inputs = this;
    public final MainViewModelOutputs outputs = this;

    @Override
    public Observable<Boolean> drawerIsOpen() {
        return drawerIsOpen;
    }

    @Override
    public void openDrawer(boolean open) {
        openDrawer.onNext(open);
    }
}
