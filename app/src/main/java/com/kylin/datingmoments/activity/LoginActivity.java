package com.kylin.datingmoments.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.SaveCallback;
import com.kylin.datingmoments.R;
import com.kylin.datingmoments.dao.DAO;
import com.kylin.datingmoments.dao.DAOFactory;
import com.kylin.datingmoments.entity.DMUser;
import com.kylin.datingmoments.utils.Logger;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.favorite.WechatFavorite;
import cn.sharesdk.wechat.moments.WechatMoments;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * 登录界面
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * 登录的异步线程
     */
    private UserLoginTask mAuthTask = null;

    private AutoCompleteTextView mActvEmail;
    private EditText mEtPwd;
    private View mPbLogining;
    private View mSvForm;

    private DAO mDAO = DAOFactory.getDAO();

    /**
     * 点击第三方登录按钮监听器
     */
    private OnClickListener mThirdPartyLoginListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Platform platform = null;
            switch (v.getId()) {
                case R.id.act_login_btn_tencent:
                    platform = ShareSDK.getPlatform(QQ.NAME);
                    break;
                case R.id.act_login_btn_wechat:
                    platform = ShareSDK.getPlatform(WechatMoments.NAME);
                    break;
                case R.id.act_login_btn_weibo:
                    platform = ShareSDK.getPlatform(SinaWeibo.NAME);
                    //移除授权
                    //weibo.removeAccount(true);
                    break;
                default:
                    return;
            }
            // 监听第三方登录结果
            platform.setPlatformActionListener(new PlatformActionListener() {

                @Override
                public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                    mDAO.attemptLoginByThirdParty(platform, new DAO.LoginCallback() {

                        @Override
                        public void onSuccess(DMUser user) {
                            Toast.makeText(getApplicationContext(),"登录成功，用户为："+user.getNickName(),Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(AVException exception) {
                            Toast.makeText(getApplicationContext(),"错误："+exception.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onError(Platform platform, int i, Throwable throwable) {

                }

                @Override
                public void onCancel(Platform platform, int i) {

                }
            });
            platform.authorize();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ShareSDK.initSDK(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        mActvEmail = (AutoCompleteTextView) findViewById(R.id.act_login_actv_email);
        populateAutoComplete();

        TextView mTitle = (TextView) findViewById(R.id.com_actionbar_tv_title);
        mTitle.setText(R.string.title_activity_login);

        mEtPwd = (EditText) findViewById(R.id.act_login_et_pwd);
        mEtPwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.act_login_btn_sign_in).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mSvForm = findViewById(R.id.act_login_sv);
        mPbLogining = findViewById(R.id.act_login_pb);

        findViewById(R.id.act_login_btn_tencent).setOnClickListener(mThirdPartyLoginListener);
        findViewById(R.id.act_login_btn_wechat).setOnClickListener(mThirdPartyLoginListener);
        findViewById(R.id.act_login_btn_weibo).setOnClickListener(mThirdPartyLoginListener);

    }

    /**
     * 填充自动补全信息
     */
    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mActvEmail, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * 尝试登录
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // 重置错误提示信息
        mActvEmail.setError(null);
        mEtPwd.setError(null);

        String email = mActvEmail.getText().toString();
        String password = mEtPwd.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 密码合法性验证
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mEtPwd.setError(getString(R.string.error_invalid_password));
            focusView = mEtPwd;
            cancel = true;
        }

        // 邮箱合法性验证
        if (TextUtils.isEmpty(email)) {
            mActvEmail.setError(getString(R.string.error_field_required));
            focusView = mActvEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mActvEmail.setError(getString(R.string.error_invalid_email));
            focusView = mActvEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: 使用正则表达式进行更精细验证。
        return email.contains("@");
    }

    /**
     * 验证密码是否合法
     *
     * @param password 密码
     * @return true 合法，false 不合法
     */
    private boolean isPasswordValid(String password) {
        //TODO: 根据情况进行更精细验证。
        return password.length() > 4;
    }

    /**
     * 显示或隐藏正在的登录状态
     *
     * @param show 显示或隐藏正在的登录状态
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mSvForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mSvForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSvForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mPbLogining.setVisibility(show ? View.VISIBLE : View.GONE);
            mPbLogining.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPbLogining.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

        } else {
            mPbLogining.setVisibility(show ? View.VISIBLE : View.GONE);
            mSvForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mActvEmail.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * 用户登录的异步线程
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: 联网验证登录

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }
            // TODO 获取并存储用户信息

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mEtPwd.setError(getString(R.string.error_incorrect_password));
                mEtPwd.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShareSDK.stopSDK(this);
    }
}

