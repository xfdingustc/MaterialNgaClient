package io.xfdingustc.mdngaclient.libs.rx.operators;

import android.support.annotation.NonNull;

/**
 * Created by whaley on 2017/5/31.
 */

public final class Operators {
    public static @NonNull <T> ApiErrorOperator<T> apiError() {
        return new ApiErrorOperator<>();
    }
}
