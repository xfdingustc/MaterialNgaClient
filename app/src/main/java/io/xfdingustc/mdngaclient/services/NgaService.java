package io.xfdingustc.mdngaclient.services;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

/**
 * Created by whaley on 2017/5/11.
 */

public class NgaService {
    public static final int TIME_OUT_MILLI_SEC = 15000;
    public static NgaApiService mNgaApiInstance = null;

    private NgaService() {

    }

    public static NgaApiService createNgaApiService() {
        if (mNgaApiInstance == null) {
            synchronized (NgaService.class) {
                if (mNgaApiInstance == null) {
                    Retrofit.Builder builder = new Retrofit.Builder().addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .baseUrl("http://account.178.com/");
                    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

                    clientBuilder.addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request();
                            Request.Builder newReqBuilder = request.newBuilder();


//                            final String token = SessionManager.getInstance().getToken();
//                            if (!TextUtils.isEmpty(token)) {
//                                newReqBuilder.addHeader("X-Auth-Token", token);
//                            }

                            Response response = chain.proceed(newReqBuilder.build());
                            //Logger.t(TAG).d("response:" + response.body().string());
                            return response;
                        }
                    })
                            .readTimeout(TIME_OUT_MILLI_SEC, TimeUnit.MILLISECONDS)
                            .connectTimeout(TIME_OUT_MILLI_SEC, TimeUnit.MILLISECONDS);

                    builder.client(clientBuilder.build());

                    mNgaApiInstance = builder.build().create(NgaApiService.class);
                }
            }
        }

        return mNgaApiInstance;


    }
}
