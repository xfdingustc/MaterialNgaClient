package io.xfdingustc.mdngaclient;

import android.app.Application;

import android.support.annotation.NonNull;


import java.net.CookieManager;

import javax.inject.Singleton;

import io.xfdingustc.mdngaclient.libs.ApiEndpoint;
import io.xfdingustc.mdngaclient.libs.Build;
import io.xfdingustc.mdngaclient.libs.Environment;
import dagger.Module;
import dagger.Provides;
import io.xfdingustc.mdngaclient.libs.qualifiers.ApiRetrofit;
import io.xfdingustc.mdngaclient.services.NgaApiService;
import okhttp3.CookieJar;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by whaley on 2017/5/16.
 */

@Module
public class ApplicationModule {
    private final Application application;

    public ApplicationModule(final @NonNull Application application) {
        this.application = application;
    }


    @Provides
    @Singleton
    Environment provideEnvironment(final @NonNull NgaApiService apiClient) {
        return Environment.builder()
            .apiClient(apiClient)
            .build();
    }


    @Provides
    @Singleton
    @ApiRetrofit
    @NonNull
    Retrofit provideApiRetrofit(final @NonNull ApiEndpoint apiEndpoint, final @NonNull OkHttpClient okHttpClient) {
        return createRetrofit(apiEndpoint.url(), okHttpClient);
    }

    private Retrofit createRetrofit(String url, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build();
    }

    @Provides
    @Singleton
    @NonNull
    NgaApiService provideNgaApiService(final @ApiRetrofit @NonNull Retrofit retrofit) {
        return retrofit.create(NgaApiService.class);
    }

    @Provides
    @Singleton
    @NonNull
    OkHttpClient provideOkHttpClient(final @NonNull CookieJar cookieJar,
                                     final @NonNull HttpLoggingInterceptor httpLoggingInterceptor, final @NonNull Build build) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (build.isDebug()) {
            builder.addInterceptor(httpLoggingInterceptor);
        }

        return builder
            .cookieJar(cookieJar)
            .build();
    }


    @Provides
    @Singleton
    @NonNull
    HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        return interceptor;
    }


    @Provides
    @Singleton
    @NonNull
    Build provideBuild() {
        return new Build();
    }

    @Provides
    @Singleton
    CookieJar provideCookieJar(final @NonNull CookieManager cookieManager) {
        return new JavaNetCookieJar(cookieManager);
    }

    @Provides
    @Singleton
    CookieManager provideCookieManager() {
        return new CookieManager();
    }

}
