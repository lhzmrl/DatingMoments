package com.kylin.datingmoments.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.kylin.datingmoments.entity.DMUser;

/**
 * Created by kylin on 16-6-3.
 */
public class SPOperater {

    private static final String PREFS_NAME_LOGIN_INFO = "login_info";

    public static void setLoginInfo(Context context,String loginType){
        SharedPreferences mSpSettings = context.getSharedPreferences(PREFS_NAME_LOGIN_INFO,Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = mSpSettings.edit();
        edit.putString("login_type", loginType);
        edit.commit();
    }

    public static String getLoginInfo(Context context){
        SharedPreferences mSpSettings = context.getSharedPreferences(PREFS_NAME_LOGIN_INFO,Context.MODE_PRIVATE);
        return mSpSettings.getString("login_type","");
    }

    public static void saveUserInfo(Context context,String name,String email,String pwd){
        SharedPreferences mSpSettings = context.getSharedPreferences(PREFS_NAME_LOGIN_INFO,Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = mSpSettings.edit();
        edit.putString("name", name);
        edit.putString("email", email);
        edit.putString("pwd", pwd);
        edit.commit();
    }

    public static DMUser getUserInfo(Context context){
        SharedPreferences mSpSettings = context.getSharedPreferences(PREFS_NAME_LOGIN_INFO,Context.MODE_PRIVATE);
        DMUser user = new DMUser();
        user.setNickName(mSpSettings.getString("name",""));
        user.setEmail(mSpSettings.getString("email",""));
        user.setPwd(mSpSettings.getString("pwd",""));
        return user;
    }
}
