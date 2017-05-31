package io.xfdingustc.mdngaclient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.xfdingustc.mdngaclient.libs.ApiEndpoint;

/**
 * Created by whaley on 2017/5/16.
 */
@Module(includes = ApplicationModule.class)
public class ExternalApplicationModule {
    @Provides
    @Singleton
    ApiEndpoint provideApiEndpoint() {
        return new ApiEndpoint();
    }
}
