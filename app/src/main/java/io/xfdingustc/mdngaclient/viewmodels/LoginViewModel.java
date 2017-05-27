package io.xfdingustc.mdngaclient.viewmodels;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.orhanobut.logger.Logger;

import io.xfdingustc.mdngaclient.activities.LoginActivity;
import io.xfdingustc.mdngaclient.libs.ActivityViewModel;
import io.xfdingustc.mdngaclient.libs.Environment;
import io.xfdingustc.mdngaclient.viewmodels.errors.LoginViewModelErrors;
import io.xfdingustc.mdngaclient.viewmodels.inputs.LoginViewModelInputs;
import io.xfdingustc.mdngaclient.viewmodels.outputs.LoginViewModelOutputs;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

/**
 * Created by whaley on 2017/5/27.
 */

public class LoginViewModel extends ActivityViewModel<LoginActivity> implements LoginViewModelInputs, LoginViewModelOutputs, LoginViewModelErrors {
    private static final String TAG = LoginViewModel.class.getSimpleName();

    public LoginViewModelInputs inputs = this;
    public LoginViewModelOutputs outputs = this;
    public LoginViewModelErrors errors = this;

    public LoginViewModel(@NonNull Environment environment) {
        super(environment);

//        final Observable<AuthInfoEnvelope> authInfoEnvelope = username.compose(new Tr)username, password, vcode, new Func3<String, String, String, AuthInfoEnvelope>() {
//            @Override
//            public AuthInfoEnvelope call(String username, String password, String vcode) {
//                Logger.t(TAG).d("FFFFFFFFFFFFFFFF ");
//                return new AuthInfoEnvelope(username, password, vcode);
//            }
//        });
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
//            .compose(this.<Boolean>bindToLifecycle())
            .subscribe(setLoginButtonIsEnabled);
    }


    private final PublishSubject<String> username = PublishSubject.create();
    private final PublishSubject<String> password = PublishSubject.create();
    private final PublishSubject<String> vcode = PublishSubject.create();

    private final BehaviorSubject<Boolean> setLoginButtonIsEnabled = BehaviorSubject.create();

    @Override
    public Observable<Boolean> setLoginButtonIsEnabled() {
        return setLoginButtonIsEnabled;
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
