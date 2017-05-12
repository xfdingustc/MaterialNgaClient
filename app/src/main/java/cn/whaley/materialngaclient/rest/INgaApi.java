package cn.whaley.materialngaclient.rest;

import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by whaley on 2017/5/11.
 */

public interface INgaApi {
    @GET("q_vcode.php")
    Observable<ResponseBody> getRegCode(@Query("_act") String action);


}
