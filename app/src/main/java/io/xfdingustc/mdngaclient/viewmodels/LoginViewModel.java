package io.xfdingustc.mdngaclient.viewmodels;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.orhanobut.logger.Logger;

import java.util.List;

import io.xfdingustc.mdngaclient.activities.LoginActivity;
import io.xfdingustc.mdngaclient.libs.ActivityViewModel;
import io.xfdingustc.mdngaclient.libs.Environment;
import io.xfdingustc.mdngaclient.libs.rx.transformers.Transformers;
import io.xfdingustc.mdngaclient.services.NgaApiClient;
import io.xfdingustc.mdngaclient.services.NgaApiClientType;
import io.xfdingustc.mdngaclient.services.apiresponses.VCodeEnvelope;
import io.xfdingustc.mdngaclient.viewmodels.errors.LoginViewModelErrors;
import io.xfdingustc.mdngaclient.viewmodels.inputs.LoginViewModelInputs;
import io.xfdingustc.mdngaclient.viewmodels.outputs.LoginViewModelOutputs;
import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

/**
 * Created by whaley on 2017/5/27.
 */

public class LoginViewModel extends ActivityViewModel<LoginActivity> implements LoginViewModelInputs, LoginViewModelOutputs, LoginViewModelErrors {
    private static final String TAG = LoginViewModel.class.getSimpleName();

    private NgaApiClientType client;

    private String authcodeCookie;

    public LoginViewModelInputs inputs = this;
    public LoginViewModelOutputs outputs = this;
    public LoginViewModelErrors errors = this;

    public LoginViewModel(@NonNull Environment environment) {
        super(environment);
        client = environment.apiClient();

        final Observable<AuthInfoEnvelope> authInfoEnvelope = Observable.combineLatest(username, password, vcode, new Func3<String, String, String, AuthInfoEnvelope>() {
            @Override
            public AuthInfoEnvelope call(String username, String password, String vcode) {
                return new AuthInfoEnvelope(username, password, vcode);
            }
        });

        final Observable<Boolean> isValid = authInfoEnvelope
            .map(new Func1<AuthInfoEnvelope, Boolean>() {
                @Override
                public Boolean call(AuthInfoEnvelope authInfoEnvelope) {
                    Logger.t(TAG).d(authInfoEnvelope.toString());
                    return authInfoEnvelope.isValid();
                }
            });

        isValid
            .compose(this.<Boolean>bindToLifecycle())
            .subscribe(setLoginButtonIsEnabled);

        authInfoEnvelope
            .compose(Transformers.<AuthInfoEnvelope, View>takeWhen(loginClick))
            .switchMap(new Func1<AuthInfoEnvelope, Observable<Void>>() {
                @Override
                public Observable<Void> call(AuthInfoEnvelope authInfoEnvelope) {
                    return submit(authInfoEnvelope);
                }
            });

        client.fetchVerificationCode()
            .compose(this.<VCodeEnvelope>bindToLifecycle())
            .map(new Func1<VCodeEnvelope, Bitmap>() {
                @Override
                public Bitmap call(VCodeEnvelope vCodeEnvelope) {
                    authcodeCookie = vCodeEnvelope.vCode;
                    return vCodeEnvelope.vCodeImage;
                }
            })
            .subscribe(setVerificationCode);
    }


    private final PublishSubject<String> username = PublishSubject.create();
    private final PublishSubject<String> password = PublishSubject.create();
    private final PublishSubject<String> vcode = PublishSubject.create();
    private final PublishSubject<View> loginClick = PublishSubject.create();

    private final BehaviorSubject<Bitmap> setVerificationCode = BehaviorSubject.create();

    private final BehaviorSubject<Boolean> setLoginButtonIsEnabled = BehaviorSubject.create();

    private Observable<Void> submit(AuthInfoEnvelope authInfoEnvelope) {
        return null;
    }

    @Override
    public Observable<Boolean> setLoginButtonIsEnabled() {
        return setLoginButtonIsEnabled;
    }

    @Override
    public Observable<Bitmap> setVerificationCode() {
        return setVerificationCode;
    }

    @Override
    public void username(String un) {
        username.onNext(un);
    }

    @Override
    public void password(String p) {
        password.onNext(p);
    }

    @Override
    public void vcode(String v) {
        vcode.onNext(v);
    }

    @Override
    public void loginClick() {
        loginClick.onNext(null);
    }



    class AuthInfoEnvelope {
        final String username;
        final String password;
        final String vcode;

        AuthInfoEnvelope(String username, String password, String vcode) {
            this.username = username;
            this.password = password;
            this.vcode = vcode;
        }

        boolean isValid() {
            return !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(vcode);
        }

        @Override
        public String toString() {
            return new StringBuilder("Username: ")
                .append(username)
                .append(" Password: ")
                .append(password)
                .append(" Vcode: ")
                .append(vcode)
                .toString();
        }
    }
}
