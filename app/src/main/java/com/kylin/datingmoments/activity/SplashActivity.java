package com.kylin.datingmoments.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.kylin.datingmoments.R;
import com.kylin.datingmoments.app.DMApplication;
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
        new Thread(){
            @Override
            public void run() {
                Platform platform = ShareSDK.getPlatform(SinaWeibo.NAME);
                mDAO.attemptLoginByThirdParty(platform, new DAO.LoginCallback() {

                    @Override
                    public void onSuccess(DMUser user) {
                        ((DMApplication)getApplicationContext()).setUser(user);
                        Toast.makeText(getApplicationContext(),"登录成功，用户为："+user.getNickName(),Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(AVException exception) {
                        Toast.makeText(getApplicationContext(),"错误："+exception.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShareSDK.stopSDK(this);
    }
}
