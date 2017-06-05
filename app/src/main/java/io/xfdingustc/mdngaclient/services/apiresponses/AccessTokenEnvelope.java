package io.xfdingustc.mdngaclient.services.apiresponses;

/**
 * Created by whaley on 2017/6/5.
 */

public class AccessTokenEnvelope {
    public String cid;
    public String uid;

    public AccessTokenEnvelope(String uid, String cid) {
        this.uid = uid;
        this.cid = cid;
    }
}
