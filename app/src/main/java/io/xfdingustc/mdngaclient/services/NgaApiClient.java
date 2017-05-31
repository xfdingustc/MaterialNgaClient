package io.xfdingustc.mdngaclient.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.orhanobut.logger.Logger;

import java.util.List;

import io.xfdingustc.mdngaclient.libs.rx.operators.ApiErrorOperator;
import io.xfdingustc.mdngaclient.libs.rx.operators.Operators;
import io.xfdingustc.mdngaclient.services.apiresponses.VCodeEnvelope;
import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by whaley on 2017/5/31.
 */

public class NgaApiClient implements NgaApiClientType {
    private final NgaApiService service;

    public NgaApiClient(NgaApiService service) {
        this.service = service;
    }


    @Override
    public Observable<VCodeEnvelope> fetchVerificationCode() {
        return service.fetchRegCode("gen_reg")
            .map(new Func1<Response<ResponseBody>, VCodeEnvelope>() {
                @Override
                public VCodeEnvelope call(Response<ResponseBody> responseBody) {
                    okhttp3.Response rawResponse = responseBody.raw();
                    String authcodeCookie = null;
                    if (!rawResponse.headers("set-cookie").isEmpty()) {
                        List<String> cookies = rawResponse.headers("set-cookie");
                        for (String cookie : cookies) {
//                                Logger.t(TAG).d("one cookie: " + cookie);
                            cookie = cookie.substring(0, cookie.indexOf(';'));
//                                Logger.t(TAG).d("cookie:" + cookie);
                            if (cookie.indexOf("reg_vcode=") == 0 && cookie.indexOf("deleted") < 0) {
                                authcodeCookie = cookie.substring(10);
                            }
                        }
                    }
                    return new VCodeEnvelope(authcodeCookie, BitmapFactory.decodeStream(responseBody.body().byteStream()));
                }
            })


            .subscribeOn(Schedulers.io());
    }


    @NonNull
    private <T> ApiErrorOperator<T> apiErrorOperator() {
        return Operators.apiError();
    }
}
