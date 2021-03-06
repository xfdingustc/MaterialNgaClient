package io.xfdingustc.mdngaclient.libs;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.trello.rxlifecycle.RxLifecycle;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.components.RxActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import gov.anzong.androidnga.R;
import io.xfdingustc.mdngaclient.ApplicationComponent;
import io.xfdingustc.mdngaclient.app.MdNgaApplication;
import io.xfdingustc.mdngaclient.libs.qualifiers.RequiresActivityViewModel;
import io.xfdingustc.mdngaclient.libs.utils.BundleUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by whaley on 2017/5/16.
 */

public abstract class BaseActivity<ViewModelType extends ActivityViewModel> extends RxActivity implements ActivityLifecycleType{
    private final PublishSubject<Void> back = PublishSubject.create();

    private static final String VIEW_MODEL_KEY = "viewModel";
    private final CompositeSubscription subscription = new CompositeSubscription();
    protected ViewModelType viewModel;

    @Nullable
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public ViewModelType viewModel() {
        return viewModel;
    }

    protected Toolbar getToolbar() {
        return toolbar;
    }

    @CallSuper
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }



    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate %s", this.toString());

        assignViewModel(savedInstanceState);
        viewModel.intent(getIntent());
    }


    @CallSuper
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        viewModel.intent(intent);
    }

    @CallSuper
    @Override
    protected void onStart() {
        super.onStart();
        Timber.d("onStart %s", this.toString());


        back
            .compose(bindUntilEvent(ActivityEvent.STOP))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object o) {
                    goBack();
                }
            });
    }

    @CallSuper
    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume %s", this.toString());

        assignViewModel(null);
    }

    @CallSuper
    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("onPause %s", this.toString());


        if (viewModel != null) {
            viewModel.onPause();
        }
    }

    @CallSuper
    @Override
    protected void onStop() {
        super.onStop();
        Timber.d("onStop %s", this.toString());
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy %s", this.toString());

        subscription.clear();
        if (isFinishing()) {
            if (viewModel != null) {
                ActivityViewModelManager.getInstance().destroy(viewModel);
                viewModel = null;
            }
        }
    }

    @NonNull
    protected MdNgaApplication application() {
        return (MdNgaApplication)getApplication();
    }


    @NonNull
    protected ApplicationComponent component() {
        return application().component();
    }

    @NonNull
    protected Environment environment() {
        return component().environment();
    }


    @CallSuper
    @Override
    public void onBackPressed() {
        back();
    }

    public void back() {
        back.onNext(null);
    }

    private void goBack() {
        super.onBackPressed();
    }

    private void assignViewModel(final @Nullable Bundle viewModelEnvelope) {
        if (viewModel == null) {
            final RequiresActivityViewModel annotation = getClass().getAnnotation(RequiresActivityViewModel.class);
            final Class<ViewModelType> viewModelClass = annotation == null ? null : (Class<ViewModelType>)annotation.value();
            if (viewModelClass != null) {
                viewModel = ActivityViewModelManager.getInstance().fetch(this,
                    viewModelClass,
                    BundleUtils.maybeGetBundle(viewModelEnvelope, VIEW_MODEL_KEY));
            }
        }
    }

}
