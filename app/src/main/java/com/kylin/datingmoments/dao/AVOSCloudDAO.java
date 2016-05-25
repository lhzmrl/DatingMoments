package com.kylin.datingmoments.dao;

import android.os.AsyncTask;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.SaveCallback;
import com.kylin.datingmoments.entity.DMUser;
import com.kylin.datingmoments.utils.Logger;

import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * 由AVOSCloud实现的云端DAO操作。
 * Created by kylin on 16-5-24.
 */
public class AVOSCloudDAO implements DAO{


    @Override
    public DMUser register(DMUser user) {
        AVObject avUser = new AVObject("DMUser");
        avUser.put(DMUser.NICK_NAME,user.getNickName());
        avUser.put(DMUser.EMAIL,user.getEmail());
        avUser.put(DMUser.QQ_USER_ID,user.getQQUserId());
        avUser.put(DMUser.WECHAT_USER_ID,user.getWechatUserId());
        avUser.put(DMUser.WEIBO_USER_ID,user.getWeiboUserId());
        try {
            avUser.save();
        } catch (AVException e) {
            e.printStackTrace();
            return null;
        }
        return user;
    }

    @Override
    public void attemptLoginByThirdParty(Platform platform,LoginCallback loginCallback) {
        ThirdPartyLoginTask thirdPartyLoginTask = new ThirdPartyLoginTask(loginCallback);
        thirdPartyLoginTask.execute(platform);
    }

    @Override
    public DMUser registerByThirdParty(Platform platform) {
        String openId = platform.getDb().getUserId(); // 获取用户在此平台的ID
        String nickname = platform.getDb().get("nickname"); // 获取用户昵称
        DMUser user = new DMUser();
        user.setNickName(nickname);
        String platformName = platform.getName();
        if (QQ.NAME.equals(platformName)){
            user.setQQUserId(openId);
        }else if (WechatMoments.NAME.equals(platformName)){
            user.setWechatUserId(openId);
        }else if (SinaWeibo.NAME.equals(platformName)){
            user.setWeiboUserId(openId);
        }else{
            return null;
        }
        return register(user);

    }

    private class ThirdPartyLoginTask extends AsyncTask<Platform,Void,DMUser>{

        LoginCallback mLoginCallback;

        protected ThirdPartyLoginTask(LoginCallback loginCallback){
            mLoginCallback = loginCallback;
        }

        @Override
        protected DMUser doInBackground(Platform... params) {
            String platformName = params[0].getName();
            String openId =  params[0].getDb().getUserId();
            List<AVObject> listAVObject = null;
            AVQuery<AVObject> query = new AVQuery<>("DMUser");
            if (QQ.NAME.equals(platformName)){
                query.whereEqualTo(DMUser.QQ_USER_ID, openId);
            }else if (WechatMoments.NAME.equals(platformName)){
                query.whereEqualTo(DMUser.WECHAT_USER_ID, openId);
            }else if (SinaWeibo.NAME.equals(platformName)){
                query.whereEqualTo(DMUser.WEIBO_USER_ID, openId);
            }else{
            }
            try {
                listAVObject = query.find();
            } catch (AVException e) {
                e.printStackTrace();
            }
            if (listAVObject!=null && listAVObject.size()>0){
                AVObject avObject = listAVObject.get(0);
                return DMUser.parse(avObject);
            }

            return registerByThirdParty(params[0]);
        }

        @Override
        protected void onPostExecute(DMUser dmUser) {
            if(dmUser!=null)
                mLoginCallback.onSuccess(dmUser);
            else
                mLoginCallback.onError(new AVException(0,"登录失败！"));
        }
    }
}
