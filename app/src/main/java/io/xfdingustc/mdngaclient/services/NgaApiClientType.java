package io.xfdingustc.mdngaclient.services;

import android.support.annotation.NonNull;

import io.xfdingustc.mdngaclient.services.apiresponses.AccessTokenEnvelope;
import io.xfdingustc.mdngaclient.services.apiresponses.VCodeEnvelope;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by whaley on 2017/5/31.
 */

public interface NgaApiClientType {
    Observable<VCodeEnvelope> fetchVerificationCode();

    Observable<AccessTokenEnvelope> login(@NonNull String authCodeCookie, @NonNull String username, @NonNull String password, @NonNull String vcode);
}
