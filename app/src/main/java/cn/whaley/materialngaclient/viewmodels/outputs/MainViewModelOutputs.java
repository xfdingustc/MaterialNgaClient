package cn.whaley.materialngaclient.viewmodels.outputs;

import rx.Observable;

/**
 * Created by whaley on 2017/5/17.
 */

public interface MainViewModelOutputs {
    Observable<Boolean> drawerIsOpen();
}
