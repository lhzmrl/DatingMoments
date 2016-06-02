package com.kylin.datingmoments.dao;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.kylin.datingmoments.activity.MainActivity;
import com.kylin.datingmoments.common.TableName;
import com.kylin.datingmoments.entity.DMUser;
import com.kylin.datingmoments.entity.UploadVideoResult;
import com.kylin.datingmoments.entity.VideoInfo;

import java.util.ArrayList;
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
        avUser.put(DMUser.USER_ICON,user.getUserIcon());
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
        user.setUserIcon(platform.getDb().get("icon"));
        return register(user);

    }

    @Override
    public void uploadVideo(String videoPath, String thumbnailPath,String desc, DMUser user,UploadVideoCallback uploadVideoCallback) {
        new UpLoadTask(videoPath,thumbnailPath,desc,user,uploadVideoCallback).execute();
    }

    @Override
    public void getVideoList(GetVideoCallback getVideoCallback) {
        new GetVideoListTask(getVideoCallback).execute();
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
            AVQuery<AVObject> query = new AVQuery<>(TableName.USER);
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
                return null;
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

    private class UpLoadTask extends AsyncTask<Void, Void, Boolean> {

        UploadVideoCallback mUploadVideoCallback;
        String mVideoPath;
        String mCoverPath;
        String mDesc;
        DMUser mDMUser;

        protected UpLoadTask(String videoPath, String thumbnailPath, String desc,DMUser user,UploadVideoCallback uploadVideoCallback){
            mVideoPath = videoPath;
            mCoverPath = thumbnailPath;
            mDesc = desc;
            mDMUser = user;
            mUploadVideoCallback = uploadVideoCallback;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String jsonString =  DataLogic.getInstance().uploadVideo(mVideoPath, mCoverPath);
            if (jsonString==null)
                return false;
            UploadVideoResult result = JSONObject.parseArray(jsonString, UploadVideoResult.class).get(0);
            if (result == null)
                return false;
            AVObject user = AVObject.createWithoutData(TableName.USER, mDMUser.getObjectId());
            AVObject video = new AVObject(TableName.VIDEO);
            video.put(VideoInfo.VIDEO_PATH,result.videoPath);
            video.put(VideoInfo.COVER_PATH,result.thumbnailPath);
            video.put(VideoInfo.DESC,mDesc);
            video.put("user", user);
            try {
                video.save();
            } catch (AVException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result)
                mUploadVideoCallback.onSuccess();
            else{
                mUploadVideoCallback.onError();
            }
        }
    }

    private class GetVideoListTask extends AsyncTask<Void,Void,List<VideoInfo>>{

        GetVideoCallback mGetVideoCallback;

        protected GetVideoListTask(GetVideoCallback getVideoCallback){
            mGetVideoCallback = getVideoCallback;
        }

        @Override
        protected List<VideoInfo> doInBackground(Void... params) {
            List<VideoInfo> videoList=new ArrayList<VideoInfo>();
            AVQuery<AVObject> query = new AVQuery<>(TableName.VIDEO);
            query.include("user");
            try {
                List<AVObject> list = query.find();
                for (AVObject o:list){
                    videoList.add(VideoInfo.parse(o));
                }
            } catch (AVException e) {
                e.printStackTrace();
            }
            return videoList;
        }

        @Override
        protected void onPostExecute(List<VideoInfo> videoInfos) {
            if (videoInfos==null){
                mGetVideoCallback.onError();
                return;
            }
            mGetVideoCallback.onSuccess(videoInfos);
        }
    }
}
