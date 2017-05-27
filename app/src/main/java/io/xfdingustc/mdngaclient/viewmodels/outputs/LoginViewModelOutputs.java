package io.xfdingustc.mdngaclient.viewmodels.outputs;

import rx.Observable;

/**
 * Created by whaley on 2017/5/27.
 */

public interface LoginViewModelOutputs {
    Observable<Boolean> setLoginButtonIsEnabled();
}
