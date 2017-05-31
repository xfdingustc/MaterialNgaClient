package io.xfdingustc.mdngaclient.libs;

import android.support.annotation.NonNull;

/**
 * Created by whaley on 2017/5/16.
 */

public class ApiEndpoint {
    private String url;

    public ApiEndpoint() {
        this.url = "http://account.178.com/";
    }

    public @NonNull String url() {
        return url;
    }
}
