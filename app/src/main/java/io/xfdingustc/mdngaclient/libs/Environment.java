package io.xfdingustc.mdngaclient.libs;

import android.os.Parcelable;

import auto.parcel.AutoParcel;
import io.xfdingustc.mdngaclient.services.NgaApiService;

/**
 * Created by whaley on 2017/5/17.
 */

@AutoParcel
public abstract class Environment implements Parcelable {
    public abstract NgaApiService apiClient();

    @AutoParcel.Builder
    public abstract static class Builder {
        public abstract Builder apiClient(NgaApiService __);
        public abstract Environment build();
    }

    public static Builder builder() {
        return new AutoParcel_Environment.Builder();
    }

    public abstract Builder toBuilder();

}
