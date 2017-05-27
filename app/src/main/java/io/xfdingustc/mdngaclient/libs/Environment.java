package io.xfdingustc.mdngaclient.libs;

import android.os.Parcelable;

import auto.parcel.AutoParcel;

/**
 * Created by whaley on 2017/5/17.
 */

@AutoParcel
public abstract class Environment implements Parcelable {

    @AutoParcel.Builder
    public abstract static class Builder {
        public abstract Environment build();
    }

    public static Builder builder() {
        return new AutoParcel_Environment.Builder();
    }

}
