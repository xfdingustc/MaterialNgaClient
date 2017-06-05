package io.xfdingustc.mdngaclient.services.apiresponses;

import io.xfdingustc.mdngaclient.libs.rx.operators.LoginErrorOperator;
import io.xfdingustc.mdngaclient.services.LoginException;

/**
 * Created by whaley on 2017/6/5.
 */

public class ErrorEnvelope {
    private final int errorCode;

    public ErrorEnvelope(int errorCode) {
        this.errorCode = errorCode;
    }

    public static ErrorEnvelope fromThrowable(Throwable e) {
        if (e instanceof LoginException) {
            final LoginException exception = (LoginException) e;
            return exception.errorEnvelope();
        }

        return null;
    }
}
