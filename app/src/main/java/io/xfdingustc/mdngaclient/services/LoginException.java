package io.xfdingustc.mdngaclient.services;

import io.xfdingustc.mdngaclient.services.apiresponses.ErrorEnvelope;

/**
 * Created by whaley on 2017/6/5.
 */

public final class LoginException extends RuntimeException {
    public static final int ERROR_PASSWORD = 100;
    public static final int ERROR_VCODE = 101;
    public static final int ERROR_UNKNOWN = 102;

    private final ErrorEnvelope errorEnv;

    public static LoginException passwordError() {
        return new LoginException(ERROR_PASSWORD);
    }

    public static LoginException vcodeError() {
        return new LoginException(ERROR_VCODE);
    }

    public static LoginException unknownError() {
        return new LoginException(ERROR_UNKNOWN);
    }

    private LoginException(int errorCode) {
        this.errorEnv = new ErrorEnvelope(errorCode);
    }

    public ErrorEnvelope errorEnvelope() {
        return errorEnv;
    }
}
