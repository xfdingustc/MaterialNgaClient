package io.xfdingustc.mdngaclient.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.xfdingustc.mdngaclient.app.MdNgaApplication;
import io.xfdingustc.mdngaclient.libs.BaseActivity;
import io.xfdingustc.mdngaclient.libs.qualifiers.RequiresActivityViewModel;
import io.xfdingustc.mdngaclient.libs.rx.transformers.Transformers;
import io.xfdingustc.mdngaclient.services.LoginException;
import gov.anzong.androidnga.R;
import io.xfdingustc.mdngaclient.services.apiresponses.ErrorEnvelope;
import io.xfdingustc.mdngaclient.viewmodels.LoginViewModel;

import rx.functions.Action1;
import sp.phone.adapter.UserListAdapter;
import sp.phone.bean.PerferenceConstant;
import sp.phone.utils.PhoneConfiguration;
import sp.phone.utils.StringUtil;

@RequiresActivityViewModel(LoginViewModel.class)
public class LoginActivity extends BaseActivity<LoginViewModel> implements PerferenceConstant {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private String name;

    private String authcodeCookie;

    private String action, messagemode;
    private String tid;
    private int fid;
    private boolean needtopost = false;
    private String prefix, to;
    private String pid;
    private String title;
    private int mid;
    private boolean alreadylogin = false;



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

    @BindString(R.string.user_name_pwd_error)
    String pwdError;

    @BindString(R.string.vcode_error)
    String vcodeError;

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
        viewModel.inputs.reloadVCodeClick();
    }

    @OnClick(R.id.login_button)
    public void onBtnLoginClicked() {
        viewModel.inputs.loginClick();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();

        viewModel.outputs.setLoginButtonIsEnabled()
            .compose(this.<Boolean>bindToLifecycle())
            .compose(Transformers.<Boolean>observerForUI())
            .subscribe(new Action1<Boolean>() {
                @Override
                public void call(Boolean enabled) {
                    loginButton.setEnabled(enabled);
                }
            });

        viewModel.outputs.setVerificationCode()
            .compose(this.<Bitmap>bindToLifecycle())
            .compose(Transformers.<Bitmap>observerForUI())
            .subscribe(new Action1<Bitmap>() {
                @Override
                public void call(Bitmap bitmap) {
                    authcodeImg.setImageBitmap(bitmap);
                }
            });

        viewModel.outputs.loginSuccess()
            .compose(this.<Void>bindToLifecycle())
            .compose(Transformers.<Void>observerForUI())
            .subscribe(new Action1<Void>() {
                @Override
                public void call(Void aVoid) {
                    finish();
                }
            });

        viewModel.errors.invalidLoginError()
            .compose(this.<ErrorEnvelope>bindToLifecycle())
            .compose(Transformers.<ErrorEnvelope>observerForUI())
            .subscribe(new Action1<ErrorEnvelope>() {
                @Override
                public void call(ErrorEnvelope errorEnvelope) {
                    loginError(errorEnvelope);

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

    private void loginError(ErrorEnvelope errorEnvelope) {
        switch (errorEnvelope.errorCode()) {
            case LoginException.ERROR_PASSWORD:
                passwordText.setError(pwdError);
                break;
            case LoginException.ERROR_VCODE:
                authcodeText.setError(vcodeError);
                break;
        }
    }

    private void initViews() {
        setContentView(R.layout.activity_login);
        getToolbar().setTitle(R.string.login);
    }


    private void reloadAuthCode() {
        authcodeCookie = "";
        authcodeText.setText("");

        authcodeImg.setImageDrawable(getResources().getDrawable(R.drawable.q_vcode));
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
