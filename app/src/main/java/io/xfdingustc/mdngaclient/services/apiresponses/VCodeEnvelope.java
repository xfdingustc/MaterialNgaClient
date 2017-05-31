package io.xfdingustc.mdngaclient.services.apiresponses;

import android.graphics.Bitmap;

import auto.parcel.AutoParcel;

/**
 * Created by whaley on 2017/5/31.
 */

public class VCodeEnvelope {
    public String vCode;
    public Bitmap vCodeImage;

    public VCodeEnvelope(String vCode, Bitmap bitmap) {
        this.vCode = vCode;
        this.vCodeImage = bitmap;
    }
}
