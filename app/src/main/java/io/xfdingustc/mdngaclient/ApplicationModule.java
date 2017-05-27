package io.xfdingustc.mdngaclient;

import android.app.Application;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import io.xfdingustc.mdngaclient.libs.Environment;
import dagger.Module;
import dagger.Provides;

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
    Environment provideEnvironment() {
        return Environment.builder().build();
    }
}
