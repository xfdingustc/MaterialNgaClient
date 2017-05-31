package io.xfdingustc.mdngaclient.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.halcyon.logger.HttpLogInterceptor;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.xfdingustc.mdngaclient.app.Consts;
import io.xfdingustc.mdngaclient.app.MdNgaApplication;
import io.xfdingustc.mdngaclient.libs.BaseActivity;
import io.xfdingustc.mdngaclient.libs.Environment;
import io.xfdingustc.mdngaclient.libs.qualifiers.RequiresActivityViewModel;
import io.xfdingustc.mdngaclient.libs.rx.Transformers;
import io.xfdingustc.mdngaclient.services.NgaApiService;
import io.xfdingustc.mdngaclient.libs.rx.SimpleSubscriber;
import gov.anzong.androidnga.R;
import io.xfdingustc.mdngaclient.viewmodels.LoginViewModel;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import sp.phone.adapter.UserListAdapter;
import sp.phone.bean.PerferenceConstant;
import sp.phone.utils.PhoneConfiguration;
import sp.phone.utils.StringUtil;
import sp.phone.utils.ThemeManager;

@RequiresActivityViewModel(LoginViewModel.class)
public class LoginActivity extends BaseActivity<LoginViewModel> implements PerferenceConstant {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private String name;


    private String action, messagemode;
    private String tid;
    private int fid;
    private boolean needtopost = false;
    private String prefix, to;
    private String pid;
    private String title;
    private int mid;
    private boolean alreadylogin = false;
    private String authcodeCookie;


    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
    }

    @BindView(R.id.login_button)
    Button loginButton;

    @BindView(R.id.authcode_img)
    ImageView authcodeImg;

    @BindView(R.id.authcode_refresh)
    ImageView authcodeimgRefresh;

    @BindView(R.id.login_user_edittext)
    TextInputEditText userText;

    @BindView(R.id.login_password_edittext)
    TextInputEditText passwordText;

    @BindView(R.id.login_authcode_edittext)
    TextInputEditText authcodeText;

    @BindView(R.id.login_authcode_layout)
    TextInputLayout authCodeLayout;


    @BindView(R.id.user_list)
    ListView userList;

    @OnTextChanged(R.id.login_user_edittext)
    void onUsernameTextChanged(CharSequence username) {
        viewModel.inputs.username(username.toString());
    }

    @OnTextChanged(R.id.login_password_edittext)
    void onPasswordTextChanged(CharSequence password) {
        viewModel.inputs.password(password.toString());
    }

    @OnTextChanged(R.id.login_authcode_edittext)
    void onAuthcodeTextChanged(CharSequence vcode) {
        viewModel.inputs.vcode(vcode.toString());
    }

    @OnClick({R.id.authcode_refresh, R.id.authcode_img})
    public void onAuthCodeRefreshClicked() {
        reloadAuthCode();
    }

    @OnClick(R.id.login_button)
    public void onBtnLoginClicked() {
        StringBuffer bodyBuffer = new StringBuffer();
        bodyBuffer.append("email=");
        if (StringUtil.isEmpty(authcodeCookie)) {
            showToast("验证码信息错误，请重试");
            reloadAuthCode();
            return;
        }
        name = userText.getText().toString();
        if (StringUtil.isEmpty(name) ||
            StringUtil.isEmpty(passwordText.getText().toString()) ||
            StringUtil.isEmpty(authcodeText.getText().toString())) {
            showToast("内容缺少，请检查后再试");
            reloadAuthCode();
            return;
        }
        try {
            bodyBuffer.append(URLEncoder.encode(userText.getText().toString(), "utf-8"));
            bodyBuffer.append("&password=");
            bodyBuffer.append(URLEncoder.encode(passwordText.getText().toString(), "utf-8"));
            bodyBuffer.append("&vcode=");
            bodyBuffer.append(URLEncoder.encode(authcodeText.getText().toString(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        doLogin(bodyBuffer.toString());

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onCreate(savedInstanceState);
        int orentation = ThemeManager.getInstance().screenOrentation;
        if (orentation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            || orentation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(orentation);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
//        ThemeManager.SetContextTheme(this);


        initViews();

        viewModel.outputs.setLoginButtonIsEnabled()
            .compose(this.<Boolean>bindToLifecycle())
            .compose(Transformers.<Boolean>observerForUI())
            .subscribe(new Action1<Boolean>() {
                @Override
                public void call(Boolean enabled) {
                    Logger.t(TAG).d("set login button: " + enabled);
                    loginButton.setEnabled(enabled);
                }
            });

        userList.setAdapter(new UserListAdapter(this, userText));


        String userName = PhoneConfiguration.getInstance().userName;
        if (userName != "") {
            userText.setText(userName);
            userText.selectAll();
        }


        Intent intent = this.getIntent();
        action = intent.getStringExtra("action");
        messagemode = intent.getStringExtra("messagemode");
        if (!StringUtil.isEmpty(action)) {
            showToast("你需要登录才能进行下一步操作");
            if (action.equals("search")) {
                fid = intent.getIntExtra("fid", -7);
                needtopost = true;
            }
            if (StringUtil.isEmpty(messagemode)) {
                if (action.equals("new") || action.equals("reply") || action.equals("modify")) {
                    needtopost = true;
                    prefix = intent.getStringExtra("prefix");
                    tid = intent.getStringExtra("tid");
                    fid = intent.getIntExtra("fid", -7);
                    title = intent.getStringExtra("title");
                    pid = intent.getStringExtra("pid");
                }
            } else {
                if (action.equals("new") || action.equals("reply")) {
                    needtopost = true;
                    to = intent.getStringExtra("to");
                    title = intent.getStringExtra("title");
                    mid = intent.getIntExtra("mid", 0);
                }
            }
        }

        reloadAuthCode();
    }

    private void initViews() {
        setContentView(R.layout.activity_login);
        getToolbar().setTitle(R.string.login);
    }


    private void reloadAuthCode() {
        authcodeCookie = "";
        authcodeText.setText("");

        authcodeImg.setImageDrawable(getResources().getDrawable(R.drawable.q_vcode));


        final Environment environment = environment();
        NgaApiService service = environment.apiClient();


        service.fetchRegCode("gen_reg")
            .map(new Func1<Response<ResponseBody>, Bitmap>() {
                @Override
                public Bitmap call(Response<ResponseBody> response) {
                    okhttp3.Response rawResponse = response.raw();
                    if (!rawResponse.headers("set-cookie").isEmpty()) {
                        List<String> cookies = rawResponse.headers("set-cookie");
                        for (String cookie : cookies) {
//                                Logger.t(TAG).d("one cookie: " + cookie);
                            cookie = cookie.substring(0, cookie.indexOf(';'));
//                                Logger.t(TAG).d("cookie:" + cookie);
                            if (cookie.indexOf("reg_vcode=") == 0 && cookie.indexOf("deleted") < 0) {
                                authcodeCookie = cookie.substring(10);
                                Logger.t(TAG).d("authcodeCookie:" + authcodeCookie);
                            }
                        }
                    }
                    return BitmapFactory.decodeStream(response.body().byteStream());
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new SimpleSubscriber<Bitmap>() {
                @Override
                public void onNext(Bitmap bitmap) {
                    authcodeImg.setImageBitmap(bitmap);
                }
            });


    }


    @Override
    protected void onResume() {
        if (PhoneConfiguration.getInstance().fullscreen) {
            //ActivityUtil.getInstance().setFullScreen(view);
        }
        if (alreadylogin && needtopost) {
            finish();
        }
        super.onResume();
    }

    public void authcodefinishLoadError() {
        showToast("载入验证码失败，请点击刷新重新加载");
        authcodeImg.setImageDrawable(getResources().getDrawable(R.drawable.q_vcode_retry));
        authcodeCookie = "";
        authcodeText.setText("");
        authcodeText.setSelected(true);
    }


    private void doLogin(String postBody) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    Request.Builder requestBuilder = request.newBuilder();
                    requestBuilder.addHeader("Cookie", "reg_vcode=" + authcodeCookie);
                    final okhttp3.Response response = chain.proceed(requestBuilder.build());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!validateLoginInfo(response)) {
                                reloadAuthCode();
                            }
                        }
                    });


                    return response;
                }
            })
            .addInterceptor(new HttpLogInterceptor())
            .followRedirects(false)
            .readTimeout(15000, TimeUnit.MILLISECONDS)
            .connectTimeout(15000, TimeUnit.MILLISECONDS);

        new Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .baseUrl(Consts.BASE_URL)
            .client(clientBuilder.build())
            .build()
            .create(NgaApiService.class)
            .login(postBody)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new SimpleSubscriber<ResponseBody>() {
                @Override
                public void onNext(ResponseBody response) {
//                        Logger.t(TAG).d("Response body: " + response.toString());

                }
            });
    }

    private boolean validateLoginInfo(okhttp3.Response response) {
        String key, value;
        Headers headers = response.headers();
        String cid = "", uid = "";
        for (int i = 0; i < headers.size(); i++) {
            key = headers.name(i);
            value = headers.value(i);
            Logger.t(TAG).d(key + " : " + value);
            if (key.equalsIgnoreCase("location")) {
                String re301location = value;
                if (re301location.indexOf("login_failed") > 0) {
                    if (re301location.indexOf("error_vcode") > 0) {
                        authcodeText.setError(getString(R.string.vcode_error));
                    } else if (re301location.indexOf("e_login") > 0) {
                        passwordText.setError(getString(R.string.user_name_pwd_error));
                    } else {
                        showToast(R.string.unknown_error);
                    }
                    return false;
                }
            }
            if (key.equalsIgnoreCase("set-cookie")) {
                String cookieVal = value;
                cookieVal = cookieVal.substring(0, cookieVal.indexOf(';'));
                if (cookieVal.indexOf("_sid=") == 0) {
                    cid = cookieVal.substring(5);
                }
                if (cookieVal.indexOf("_178c=") == 0) {
                    uid = cookieVal.substring(6, cookieVal.indexOf('%'));
                    if (StringUtil.isEmail(name)) {
                        try {
                            String nametmp = cookieVal.substring(cookieVal.indexOf("%23") + 3);
                            nametmp = URLDecoder.decode(nametmp, "utf-8");
                            String[] stemp = nametmp.split("#");
                            for (int ia = 0; ia < stemp.length; ia++) {
                                if (!StringUtil.isEmail(stemp[ia])) {
                                    name = stemp[ia];
                                    ia = stemp.length;
                                }
                            }
                        } catch (UnsupportedEncodingException e) {
                        }
                    }
                }

                if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(cid)) {
                    saveCookie(uid, cid);
                    return true;
                }

            }

        }

        return false;

    }

    private void saveCookie(String uid, String cid) {
        showToast(R.string.login_successfully);
        SharedPreferences share = getSharedPreferences(PERFERENCE, MODE_MULTI_PROCESS);
        Editor editor = share.edit().putString(UID, uid)
            .putString(CID, cid).putString(PENDING_REPLYS, "")
            .putString(REPLYTOTALNUM, "0")
            .putString(USER_NAME, name)
            .putString(BLACK_LIST, "");
        editor.apply();
        MdNgaApplication app = (MdNgaApplication) getApplication();
        app.addToUserList(uid, cid, name, "", 0, "");

        PhoneConfiguration.getInstance().setUid(uid);
        PhoneConfiguration.getInstance().setCid(cid);
        PhoneConfiguration.getInstance().userName = name;
        PhoneConfiguration.getInstance().setReplyTotalNum(0);
        PhoneConfiguration.getInstance().setReplyString("");
        PhoneConfiguration.getInstance().blacklist = StringUtil
            .blackliststringtolisttohashset("");
        alreadylogin = true;
        Intent intent = new Intent();
        if (needtopost) {
            if (StringUtil.isEmpty(to)) {
                if (action.equals("search")) {
                    intent.putExtra("fid", fid);
                    intent.putExtra("searchmode", "true");
                    intent.setClass(this, PhoneConfiguration.getInstance().topicActivityClass);
                    startActivity(intent);
                } else {
                    if (action.equals("new")) {
                        intent.putExtra("fid", fid);
                        intent.putExtra("action", "new");
                    } else if (action.equals("reply")) {
                        intent.putExtra("prefix", "");
                        intent.putExtra("tid", tid);
                        intent.putExtra("action", "reply");
                    } else if (action.equals("modify")) {
                        intent.putExtra("prefix", prefix);
                        intent.putExtra("tid", tid);
                        intent.putExtra("pid", pid);
                        intent.putExtra("title", title);
                        intent.putExtra("action", "modify");
                    }
                    intent.setClass(this, PhoneConfiguration.getInstance().postActivityClass);
                    startActivity(intent);
                }
            } else {
                if (to.equals(name)) {
                    showToast(R.string.not_to_send_to_self);
                    finish();
                } else {
                    if (action.equals("new")) {
                        intent.putExtra("to", to);
                        intent.putExtra("action", "new");
                    } else if (action.equals("reply")) {
                        intent.putExtra("mid", mid);
                        intent.putExtra("title", title);
                        intent.putExtra("to", to);
                        intent.putExtra("action", "reply");
                    }
                    intent.setClass(this, PhoneConfiguration.getInstance().messagePostActivityClass);
                    startActivity(intent);
                }
            }
        } else {
            intent.setClass(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    private void showToast(String toast) {

    }

    private void showToast(int toast) {

    }


}
