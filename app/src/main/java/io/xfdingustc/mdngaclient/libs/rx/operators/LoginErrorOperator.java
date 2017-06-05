package io.xfdingustc.mdngaclient.libs.rx.operators;

import android.text.TextUtils;

import com.orhanobut.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import gov.anzong.androidnga.R;
import io.xfdingustc.mdngaclient.services.LoginException;
import io.xfdingustc.mdngaclient.services.apiresponses.AccessTokenEnvelope;

import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import sp.phone.utils.StringUtil;

/**
 * Created by whaley on 2017/6/5.
 */

public class LoginErrorOperator implements Observable.Operator<AccessTokenEnvelope, Response<ResponseBody>> {

    public LoginErrorOperator() {

    }


    @Override
    public Subscriber<? super Response<ResponseBody>> call(final Subscriber<? super AccessTokenEnvelope> subscriber) {

        return new Subscriber<Response<ResponseBody>>() {
            @Override
            public void onCompleted() {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }
            }

            @Override
            public void onError(Throwable e) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e);
                }
            }

            @Override
            public void onNext(Response<ResponseBody> response) {
                if (subscriber.isUnsubscribed()) {
                    return;
                }

                String key, value;
                Headers headers = response.headers();
                String cid = "", uid = "";
                for (int i = 0; i < headers.size(); i++) {
                    key = headers.name(i);
                    value = headers.value(i);
                    if (key.equalsIgnoreCase("location")) {
                        String re301location = value;
                        if (re301location.indexOf("login_failed") > 0) {
                            if (re301location.indexOf("error_vcode") > 0) {
                                subscriber.onError(LoginException.vcodeError());
                            } else if (re301location.indexOf("e_login") > 0) {
                                subscriber.onError(LoginException.passwordError());
                            } else {
                                subscriber.onError(LoginException.unknownError());
                            }
                        }
                    }
                    if (key.equalsIgnoreCase("set-cookie")) {
                        String cookieVal = value;
                        cookieVal = cookieVal.substring(0, cookieVal.indexOf(';'));
                        if (cookieVal.indexOf("_sid=") == 0) {
                            cid = cookieVal.substring(5);
                        }
                        if (cookieVal.indexOf("_178c=") == 0) {
                            uid = cookieVal.substring(6, cookieVal.indexOf('%'));
                        }

                        if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(cid)) {
                            subscriber.onNext(new AccessTokenEnvelope(uid, cid));
                        }

                    }

                }
            }
        };
    }
}
