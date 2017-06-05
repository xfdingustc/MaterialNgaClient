package io.xfdingustc.mdngaclient.viewmodels.errors;

import io.xfdingustc.mdngaclient.services.apiresponses.ErrorEnvelope;
import rx.Observable;

/**
 * Created by whaley on 2017/5/27.
 */

public interface LoginViewModelErrors {

    Observable<ErrorEnvelope> invalidLoginError();

}
