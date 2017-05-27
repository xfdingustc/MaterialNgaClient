package io.xfdingustc.mdngaclient.libs;

import com.trello.rxlifecycle.android.ActivityEvent;

import rx.Observable;

/**
 * Created by whaley on 2017/5/16.
 */

public interface ActivityLifecycleType {
    Observable<ActivityEvent> lifecycle();
}
