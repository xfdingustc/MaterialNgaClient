package gov.anzong.androidnga.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import cn.whaley.materialngaclient.app.Consts;
import cn.whaley.materialngaclient.app.MyApp;
import cn.whaley.materialngaclient.rest.INgaApi;
import cn.whaley.materialngaclient.ui.activities.MainActivity;
import gov.anzong.androidnga.R;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import sp.phone.adapter.UserListAdapter;
import sp.phone.bean.PerferenceConstant;
import sp.phone.forumoperation.HttpPostClient;
import sp.phone.utils.PhoneConfiguration;
import sp.phone.utils.ReflectionUtil;
import sp.phone.utils.StringUtil;
import sp.phone.utils.ThemeManager;

public class LoginActivity extends SwipeBackAppCompatActivity implements PerferenceConstant {
    private static final String TAG = LoginActivity.class.getSimpleName();

    Object commit_lock = new Object();
    String name;


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
    private boolean loading = false;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
    }

    @BindView(R.id.login_button)
    Button buttonLogin;

    @BindView(R.id.authcode_img)
    ImageView authcodeImg;

    @BindView(R.id.authcode_refresh)
    ImageButton authcodeimgRefresh;

    @BindView(R.id.login_user_edittext)
    EditText userText;

    @BindView(R.id.login_password_edittext)
    EditText passwordText;

    @BindView(R.id.login_authcode_edittext)
    EditText authcodeText;

    @BindView(R.id.user_list)
    ListView userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
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
        ThemeManager.SetContextTheme(this);


        initViews();


        userList.setAdapter(new UserListAdapter(this, userText));


        String postUrl = "http://account.178.com/q_account.php?_act=login&print=login";

        String userName = PhoneConfiguration.getInstance().userName;
        if (userName != "") {
            userText.setText(userName);
            userText.selectAll();
        }

        LoginButtonListener listener = new LoginButtonListener(postUrl);
        buttonLogin.setOnClickListener(listener);
        updateThemeUI();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                InputMethodManager inputManager = (InputMethodManager) userText
                        .getContext().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(userText, 0);
            }

        }, 500);
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
                if (action.equals("new") || action.equals("reply")
                        || action.equals("modify")) {
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
        authcodeimgRefresh.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                reloadauthcode();
            }

        });
        authcodeImg.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                reloadauthcode();
            }

        });
        reloadauthcode();
    }

    private void initViews() {
        setContentView(R.layout.login);
        toolbar.setTitle(R.string.login);
    }


    private String encodeCookie(List<String> cookies) {
        StringBuilder sb = new StringBuilder();
        Set<String> set = new HashSet<>();
        for (String cookie : cookies) {
            String[] arr = cookie.split(";");
            for (String s : arr) {
                if (set.contains(s)) continue;
                set.add(s);

            }
        }

        Iterator<String> ite = set.iterator();
        while (ite.hasNext()) {
            String cookie = ite.next();
            sb.append(cookie).append(";");
        }

        int last = sb.lastIndexOf(";");
        if (sb.length() - 1 == last) {
            sb.deleteCharAt(last);
        }

        return sb.toString();
    }

    private void reloadauthcode() {
        authcodeCookie = "";
        authcodeText.setText("");

        authcodeImg.setImageDrawable(getResources().getDrawable(R.drawable.q_vcode));

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        clientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = chain.proceed(request);
                if (!response.headers("set-cookie").isEmpty()) {
                    List<String> cookies = response.headers("set-cookie");
                    for (String cookie : cookies) {
                        Logger.t(TAG).d("one cookie: " + cookie);
                        cookie = cookie.substring(0, cookie.indexOf(';'));
                        Logger.t(TAG).d("cookie:" + cookie);
                        if (cookie.indexOf("reg_vcode=") == 0 && cookie.indexOf("deleted") < 0) {
                            authcodeCookie = cookie.substring(10);
                            Logger.t(TAG).d("authcodeCookie:" + authcodeCookie);
                        }
                    }


                }

                return response;
            }
        })
                .readTimeout(15000, TimeUnit.MILLISECONDS)
                .connectTimeout(15000, TimeUnit.MILLISECONDS);

        new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(Consts.BASE_URL)
                .client(clientBuilder.build())
                .build()
                .create(INgaApi.class)
                .getRegCode("gen_reg")
                .map(new Func1<ResponseBody, Bitmap>() {
                    @Override
                    public Bitmap call(ResponseBody responseBody) {

                        return BitmapFactory.decodeStream(responseBody.byteStream());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {
                        Logger.t(TAG).d("onComplete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.t(TAG).d("on error " + e.toString());
                        authcodefinishLoadError();
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        authcodeImg.setImageBitmap(bitmap);
                    }
                });

    }

    private void reloadauthcode(String error) {
        if (!StringUtil.isEmpty(error)) {
            showToast(error);
        }
        reloadauthcode();
    }

    private void updateThemeUI() {
//        ThemeManager tm = ThemeManager.getInstance();
//        if (tm.getMode() == ThemeManager.MODE_NIGHT) {
//            view.setBackgroundResource(ThemeManager.getInstance()
//                    .getBackgroundColor());
//        } else {
//            setbackgroundbitmap();
//        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int flags = 15;
        ReflectionUtil.actionBar_setDisplayOption(this, flags);
        return super.onCreateOptionsMenu(menu);
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


    public void authcodefinishLoad(Bitmap authimg, String authcode) {
        this.authcodeCookie = authcode;
        authcodeImg.setImageBitmap(authimg);
    }


    public void authcodefinishLoadError() {
        showToast("载入验证码失败，请点击刷新重新加载");
        authcodeImg.setImageDrawable(getResources().getDrawable(R.drawable.q_vcode_retry));
        authcodeCookie = "";
        authcodeText.setText("");
        authcodeText.setSelected(true);
    }

    class LoginButtonListener implements OnClickListener {
        final private String loginUrl;
        private final String LOG_TAG = LoginButtonListener.class
                .getSimpleName();

        public LoginButtonListener(String loginUrl) {
            super();
            this.loginUrl = loginUrl;
        }

        @Override
        public void onClick(View v) {
            synchronized (commit_lock) {
                if (loading == true) {
                    showToast(R.string.avoidWindfury);
                    return;
                } else {
                    StringBuffer bodyBuffer = new StringBuffer();
                    bodyBuffer.append("email=");
                    if (StringUtil.isEmpty(authcodeCookie)) {
                        showToast("验证码信息错误，请重试");
                        reloadauthcode();
                        return;
                    }
                    name = userText.getText().toString();
                    if (StringUtil.isEmpty(name) ||
                            StringUtil.isEmpty(passwordText.getText().toString()) ||
                            StringUtil.isEmpty(authcodeText.getText().toString())) {
                        showToast("内容缺少，请检查后再试");
                        reloadauthcode();
                        return;
                    }
                    try {
                        bodyBuffer.append(URLEncoder.encode(userText.getText()
                                .toString(), "utf-8"));
                        bodyBuffer.append("&password=");
                        bodyBuffer.append(URLEncoder.encode(passwordText
                                .getText().toString(), "utf-8"));
                        bodyBuffer.append("&vcode=");
                        bodyBuffer.append(URLEncoder.encode(authcodeText
                                .getText().toString(), "utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    new LoginTask(v).execute(loginUrl, bodyBuffer.toString());

                }
                loading = true;
            }

        }

        private class LoginTask extends AsyncTask<String, Integer, Boolean> {
            final View v;
            private String uid = null;
            private String cid = null;
            private String errorstr = "";

            public LoginTask(View v) {
                super();
                this.v = v;
            }

            @Override
            protected Boolean doInBackground(String... params) {
                String url = params[0];
                String body = params[1];
                String cookie = "reg_vcode=" + authcodeCookie;
                HttpURLConnection conn = new HttpPostClient(url, cookie).post_body(body);
                return validate(conn);

            }

            private boolean validate(HttpURLConnection conn) {
                if (conn == null)
                    return false;

                String cookieVal = null;
                String key = null;

                String uid = "";
                String cid = "";
                String location = "";

                for (int i = 1; (key = conn.getHeaderFieldKey(i)) != null; i++) {
                    Log.d(LOG_TAG,
                            conn.getHeaderFieldKey(i) + ":"
                                    + conn.getHeaderField(i));
                    if (key.equalsIgnoreCase("location")) {
                        String re301location = conn.getHeaderField(i);
                        if (re301location.indexOf("login_failed") > 0) {
                            if (re301location.indexOf("error_vcode") > 0) {
                                errorstr = ("验证码错误");
                            }
                            if (re301location.indexOf("e_login") > 0) {
                                errorstr = ("用户名或密码错误");
                            }
                            errorstr = "未知错误";
                            return false;
                        }
                    }
                    if (key.equalsIgnoreCase("set-cookie")) {
                        cookieVal = conn.getHeaderField(i);
                        cookieVal = cookieVal.substring(0,
                                cookieVal.indexOf(';'));
                        if (cookieVal.indexOf("_sid=") == 0)
                            cid = cookieVal.substring(5);
                        if (cookieVal.indexOf("_178c=") == 0) {
                            uid = cookieVal.substring(6, cookieVal.indexOf('%'));
                            if (StringUtil.isEmail(name)) {
                                try {
                                    String nametmp = cookieVal
                                            .substring(cookieVal.indexOf("%23") + 3);
                                    nametmp = URLDecoder.decode(nametmp,
                                            "utf-8");
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

                    }
                    if (key.equalsIgnoreCase("Location")) {
                        location = conn.getHeaderField(i);

                    }
                }
                if (cid != "" && uid != ""
                        && location.indexOf("login_success&error=0") != -1) {
                    this.uid = uid;
                    this.cid = cid;
                    Log.i(LOG_TAG, "uid =" + uid + ",csid=" + cid);
                    return true;
                }

                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                synchronized (commit_lock) {
                    loading = false;
                }
                if (!StringUtil.isEmpty(errorstr)) {
                    reloadauthcode(errorstr);
                    super.onPostExecute(result);
                } else {
                    if (result.booleanValue()) {
                        showToast(R.string.login_successfully);
                        SharedPreferences share = LoginActivity.this
                                .getSharedPreferences(PERFERENCE,
                                        MODE_MULTI_PROCESS);
                        Editor editor = share.edit().putString(UID, uid)
                                .putString(CID, cid).putString(PENDING_REPLYS, "")
                                .putString(REPLYTOTALNUM, "0")
                                .putString(USER_NAME, name)
                                .putString(BLACK_LIST, "");
                        editor.apply();
                        MyApp app = (MyApp) LoginActivity.this.getApplication();
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
                                    intent.setClass(
                                            v.getContext(),
                                            PhoneConfiguration.getInstance().topicActivityClass);
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
                                    intent.setClass(
                                            v.getContext(),
                                            PhoneConfiguration.getInstance().postActivityClass);
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
                                    intent.setClass(
                                            v.getContext(),
                                            PhoneConfiguration.getInstance().messagePostActivityClass);
                                    startActivity(intent);
                                }
                            }
                        } else {
                            intent.setClass(v.getContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                        super.onPostExecute(result);
                    } else {
                        showToast(R.string.login_failed);
                    }
                }
            }

        }

    }

}
