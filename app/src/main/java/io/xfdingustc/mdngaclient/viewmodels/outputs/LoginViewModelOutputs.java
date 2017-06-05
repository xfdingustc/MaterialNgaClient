package io.xfdingustc.mdngaclient.viewmodels.outputs;

import android.graphics.Bitmap;

import rx.Observable;

/**
 * Created by whaley on 2017/5/27.
 */

public interface LoginViewModelOutputs {
    Observable<Void> loginSuccess();

    Observable<Boolean> setLoginButtonIsEnabled();

    Observable<Bitmap> setVerificationCode();
}
