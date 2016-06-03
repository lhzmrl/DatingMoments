package com.kylin.datingmoments.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.kylin.datingmoments.R;
import com.kylin.datingmoments.app.DMApplication;
import com.kylin.datingmoments.common.SPOperater;
import com.kylin.datingmoments.dao.DAO;
import com.kylin.datingmoments.dao.DAOFactory;
import com.kylin.datingmoments.entity.DMUser;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;

public class SplashActivity extends AppCompatActivity {

    private DAO mDAO = DAOFactory.getDAO();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ShareSDK.initSDK(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        jump();
    }

    private void jump() {
        final String loginType = SPOperater.getLoginInfo(getApplicationContext());
        if (loginType.equals("")){
        }else if (loginType.equals("email")){
            DMUser user = SPOperater.getUserInfo(getApplicationContext());
            new UserLoginTask(user.getEmail(),user.getPwd()).execute();

        }else {
            Platform platform = ShareSDK.getPlatform(loginType);
            mDAO.attemptLoginByThirdParty(platform, new DAO.LoginCallback() {

                @Override
                public void onSuccess(DMUser user) {
                    ((DMApplication) getApplicationContext()).setUser(user);
                    Toast.makeText(getApplicationContext(), "欢迎回来，" + user.getNickName(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(AVException exception) {
                    Toast.makeText(getApplicationContext(), "错误：" + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        },3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShareSDK.stopSDK(this);
    }

    /**
     * 用户登录的异步线程
     */
    public class UserLoginTask extends AsyncTask<Void, Void, DMUser> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected DMUser doInBackground(Void... params) {
            DMUser user = mDAO.attemptLoginByEmail(mEmail,mPassword);
            return user;
        }

        @Override
        protected void onPostExecute(DMUser result) {

            if (result!=null) {
                ((DMApplication)getApplicationContext()).setUser(result);
                SPOperater.setLoginInfo(getApplicationContext(),"email");
                Toast.makeText(getApplicationContext(), "欢迎回来，" + result.getNickName(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "错误" , Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
