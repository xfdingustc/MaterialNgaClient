package cn.whaley.materialngaclient.viewmodels;

import android.support.annotation.NonNull;

import cn.whaley.materialngaclient.libs.ActivityViewModel;
import cn.whaley.materialngaclient.libs.Environment;
import cn.whaley.materialngaclient.ui.activities.MainActivity;

/**
 * Created by whaley on 2017/5/17.
 */

public class MainViewModel extends ActivityViewModel<MainActivity> {
    public MainViewModel(final @NonNull Environment environment) {
        super(environment);
    }
}
