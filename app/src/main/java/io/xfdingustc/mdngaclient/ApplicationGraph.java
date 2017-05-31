package io.xfdingustc.mdngaclient;


import io.xfdingustc.mdngaclient.activities.LoginActivity;
import io.xfdingustc.mdngaclient.app.MdNgaApplication;
import io.xfdingustc.mdngaclient.libs.Environment;
import io.xfdingustc.mdngaclient.activities.MainActivity;

/**
 * Created by whaley on 2017/5/17.
 */

public interface ApplicationGraph {
    Environment environment();
    void inject(MainActivity __);
    void inject(LoginActivity __);
    void inject(MdNgaApplication __);
}
