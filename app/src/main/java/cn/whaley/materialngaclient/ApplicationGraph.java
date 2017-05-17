package cn.whaley.materialngaclient;


import cn.whaley.materialngaclient.app.MdNgaApplication;
import cn.whaley.materialngaclient.libs.Environment;
import cn.whaley.materialngaclient.ui.activities.MainActivity;

/**
 * Created by whaley on 2017/5/17.
 */

public interface ApplicationGraph {
    Environment environment();
    void inject(MainActivity __);
    void inject(MdNgaApplication __);
}
