package io.xfdingustc.mdngaclient.libs.qualifiers;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.xfdingustc.mdngaclient.libs.ActivityViewModel;

/**
 * Created by whaley on 2017/5/16.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresActivityViewModel {
    Class<? extends ActivityViewModel> value();
}
