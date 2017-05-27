package io.xfdingustc.mdngaclient.rest;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by whaley on 2017/5/11.
 */

public interface INgaApi {
    @GET("q_vcode.php")
    Observable<ResponseBody> getRegCode(@Query("_act") String action);


    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("q_account.php?_act=login&print=login")
    Observable<ResponseBody> login(@Body String postBody);
}
