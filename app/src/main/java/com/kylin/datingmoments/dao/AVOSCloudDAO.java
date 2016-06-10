package com.kylin.datingmoments.dao;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.kylin.datingmoments.activity.MainActivity;
import com.kylin.datingmoments.common.TableName;
import com.kylin.datingmoments.entity.Comment;
import com.kylin.datingmoments.entity.DMUser;
import com.kylin.datingmoments.entity.FavoriteRelationship;
import com.kylin.datingmoments.entity.UploadVideoResult;
import com.kylin.datingmoments.entity.VideoInfo;
import com.kylin.datingmoments.entity.WatchRelationship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.moments.WechatMoments;
import io.vov.vitamio.provider.MediaStore;

/**
 * 由AVOSCloud实现的云端DAO操作。
 * Created by kylin on 16-5-24.
 */
public class AVOSCloudDAO implements DAO {

    @Override
    public DMUser register(DMUser user) {
        AVObject avUser = new AVObject("DMUser");
        avUser.put(DMUser.NICK_NAME, user.getNickName());
        avUser.put(DMUser.EMAIL, user.getEmail());
        avUser.put(DMUser.QQ_USER_ID, user.getQQUserId());
        avUser.put(DMUser.WECHAT_USER_ID, user.getWechatUserId());
        avUser.put(DMUser.WEIBO_USER_ID, user.getWeiboUserId());
        avUser.put(DMUser.USER_ICON, user.getUserIcon());
        avUser.put(DMUser.PWD, user.getPwd());
        try {
            avUser.save();
        } catch (AVException e) {
            e.printStackTrace();
            return null;
        }
        return DMUser.parse(avUser);
    }

    @Override
    public DMUser attemptLoginByEmail(String email, String pwd) {
        List<AVObject> listAVObject = null;
        final AVQuery<AVObject> priorityQuery = new AVQuery<>(TableName.USER);
        priorityQuery.whereEqualTo(DMUser.EMAIL, email);
        final AVQuery<AVObject> statusQuery = new AVQuery<>(TableName.USER);
        statusQuery.whereEqualTo(DMUser.PWD, pwd);
        AVQuery<AVObject> query = AVQuery.and(Arrays.asList(priorityQuery, statusQuery));
        try {
            listAVObject = query.find();
        } catch (AVException e) {
            e.printStackTrace();
        }
        if (listAVObject != null && listAVObject.size() > 0) {
            AVObject avObject = listAVObject.get(0);
            return DMUser.parse(avObject);
        } else {
            return null;
        }

    }

    @Override
    public void attemptLoginByThirdParty(Platform platform, LoginCallback loginCallback) {
        new ThirdPartyLoginTask(loginCallback).execute(platform);
    }

    @Override
    public boolean isUserInfoExist(DMUser user) {
        final AVQuery<AVObject> priorityQuery = new AVQuery<>(TableName.USER);
        priorityQuery.whereEqualTo(DMUser.NICK_NAME, user.getNickName());
        final AVQuery<AVObject> statusQuery = new AVQuery<>(TableName.USER);
        if (user.getEmail().equals(""))
            statusQuery.whereEqualTo(DMUser.EMAIL, "temp");
        else
            statusQuery.whereEqualTo(DMUser.EMAIL, user.getEmail());

        AVQuery<AVObject> query = AVQuery.or(Arrays.asList(priorityQuery, statusQuery));
        try {
            List<AVObject> list = query.find();
            if (list.size() > 0)
                return true;
            return false;
        } catch (AVException e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public DMUser registerByThirdParty(Platform platform) {
        String openId = platform.getDb().getUserId(); // 获取用户在此平台的ID
        String nickname = platform.getDb().get("nickname"); // 获取用户昵称
        DMUser user = new DMUser();
        user.setNickName(nickname);
        String platformName = platform.getName();
        if (QQ.NAME.equals(platformName)) {
            user.setQQUserId(openId);
        } else if (WechatMoments.NAME.equals(platformName)) {
            user.setWechatUserId(openId);
        } else if (SinaWeibo.NAME.equals(platformName)) {
            user.setWeiboUserId(openId);
        } else {
            return null;
        }
        user.setUserIcon(platform.getDb().get("icon"));
        return register(user);

    }

    @Override
    public boolean sendComment(Comment comment) {
        AVObject video = AVObject.createWithoutData(TableName.VIDEO, comment.getVideo());
        AVObject user = AVObject.createWithoutData(TableName.USER, comment.getUser());
        AVObject avosComment = new AVObject(TableName.COMMENT);// 东莞
        avosComment.put(Comment.COMMENT, comment.getComment());
        avosComment.put(Comment.VIDEO, video);
        avosComment.put(Comment.USER, user);
        try {
            avosComment.save();
        } catch (AVException e) {
            e.printStackTrace();
            return false;
        }
        if (avosComment.getObjectId() != null && avosComment.getObjectId().equals(""))
            return false;
        return true;
    }

    @Override
    public List<Comment> getCommentList(String videoId) {
        List<AVObject> listAVObject = null;
        List<Comment> listComment = new ArrayList<Comment>();
        AVObject video = AVObject.createWithoutData(TableName.VIDEO, videoId);
        AVQuery<AVObject> query = new AVQuery<>(TableName.COMMENT);
        query.whereEqualTo(Comment.VIDEO, video);
        query.include(Comment.USER);
        try {
            listAVObject = query.find();
        } catch (AVException e) {
            e.printStackTrace();
            return null;
        }
        if (listAVObject == null || listAVObject.size() == 0) {
            return null;
        }
        for (AVObject o : listAVObject) {
            listComment.add(Comment.parse(o));
        }
        return listComment;
    }

    @Override
    public void tryRecordPlayNum(String videoId, String userId) {
        AVObject video = AVObject.createWithoutData(TableName.VIDEO, videoId);
        AVObject user = AVObject.createWithoutData(TableName.USER, userId);
        List<AVObject> listAVObject = null;
        final AVQuery<AVObject> videoQuery = new AVQuery<>(TableName.WATCH_RELATIONSHIP);
        videoQuery.whereEqualTo(WatchRelationship.VIDEO, video);
        final AVQuery<AVObject> userQuery = new AVQuery<>(TableName.WATCH_RELATIONSHIP);
        userQuery.whereEqualTo(WatchRelationship.USER, user);
        AVQuery<AVObject> query = AVQuery.and(Arrays.asList(videoQuery, userQuery));
        try {
            listAVObject = query.find();
        } catch (AVException e) {
            e.printStackTrace();
        }
        if (listAVObject != null && listAVObject.size() > 0) {
            return;
        }
        AVObject watchRelationship = new AVObject(TableName.WATCH_RELATIONSHIP);
        watchRelationship.put(WatchRelationship.VIDEO, video);
        watchRelationship.put(WatchRelationship.USER, user);
        try {
            watchRelationship.save();
        } catch (AVException e) {
            e.printStackTrace();
        }
        return;
    }

    @Override
    public int getPalyNum(String videoId) {
        List<AVObject> listAVObject = null;
        AVObject video = AVObject.createWithoutData(TableName.VIDEO, videoId);
        final AVQuery<AVObject> videoQuery = new AVQuery<>(TableName.WATCH_RELATIONSHIP);
        videoQuery.whereEqualTo(WatchRelationship.VIDEO, video);
        try {
            listAVObject = videoQuery.find();
        } catch (AVException e) {
            e.printStackTrace();
            return 0;
        }
        if (listAVObject == null)
            return 0;
        return listAVObject.size();
    }

    @Override
    public AVObject isFavoriteTheVideo(String videoId, String userId) {
        AVObject video = AVObject.createWithoutData(TableName.VIDEO, videoId);
        AVObject user = AVObject.createWithoutData(TableName.USER, userId);
        List<AVObject> listAVObject = null;
        final AVQuery<AVObject> videoQuery = new AVQuery<>(TableName.FAVORITE_RELATIONSHIP);
        videoQuery.whereEqualTo(WatchRelationship.VIDEO, video);
        final AVQuery<AVObject> userQuery = new AVQuery<>(TableName.FAVORITE_RELATIONSHIP);
        userQuery.whereEqualTo(WatchRelationship.USER, user);
        AVQuery<AVObject> query = AVQuery.and(Arrays.asList(videoQuery, userQuery));
        try {
            listAVObject = query.find();
        } catch (AVException e) {
            e.printStackTrace();
            return null;
        }
        if (listAVObject != null && listAVObject.size() > 0) {
            AVObject result = listAVObject.get(0);
            return result;
        }
        return null;
    }

    @Override
    public boolean favoriteVideo(String videoId, String userId, boolean isFavorite) {
        AVObject result = isFavoriteTheVideo(videoId, userId);
        if ((isFavorite && result != null) || (!isFavorite && result == null))
            return true;
        if (!isFavorite) {
            try {
                result.delete();
            } catch (AVException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        AVObject video = AVObject.createWithoutData(TableName.VIDEO, videoId);
        AVObject user = AVObject.createWithoutData(TableName.USER, userId);
        AVObject favoriteRelationship = new AVObject(TableName.FAVORITE_RELATIONSHIP);
        favoriteRelationship.put(FavoriteRelationship.VIDEO, video);
        favoriteRelationship.put(FavoriteRelationship.USER, user);
        try {
            favoriteRelationship.save();
        } catch (AVException e) {
            e.printStackTrace();
            return false;
        }
        if (favoriteRelationship.getObjectId().equals(""))
            return false;
        return true;

    }

    @Override
    public int getFavoriteNum(String videoId) {
        AVObject video = AVObject.createWithoutData(TableName.VIDEO, videoId);
        List<AVObject> listAVObject = null;
        final AVQuery<AVObject> videoQuery = new AVQuery<>(TableName.FAVORITE_RELATIONSHIP);
        videoQuery.whereEqualTo(WatchRelationship.VIDEO, video);
        try {
            listAVObject = videoQuery.find();
        } catch (AVException e) {
            e.printStackTrace();
            return 0;
        }
        if (listAVObject==null)
            return 0;
        return listAVObject.size();
    }

    @Override
    public void uploadVideo(String videoPath, String thumbnailPath, String desc, DMUser user, UploadVideoCallback uploadVideoCallback) {
        new UpLoadTask(videoPath, thumbnailPath, desc, user, uploadVideoCallback).execute();
    }

    @Override
    public void getVideoList(int pageNum, int itemPerPage, GetVideoCallback getVideoCallback) {
        new GetVideoListTask(pageNum, itemPerPage, getVideoCallback).execute();
    }

    private class ThirdPartyLoginTask extends AsyncTask<Platform, Void, DMUser> {

        LoginCallback mLoginCallback;

        protected ThirdPartyLoginTask(LoginCallback loginCallback) {
            mLoginCallback = loginCallback;
        }

        @Override
        protected DMUser doInBackground(Platform... params) {
            String platformName = params[0].getName();
            String openId = params[0].getDb().getUserId();
            List<AVObject> listAVObject = null;
            AVQuery<AVObject> query = new AVQuery<>(TableName.USER);
            if (QQ.NAME.equals(platformName)) {
                query.whereEqualTo(DMUser.QQ_USER_ID, openId);
            } else if (WechatMoments.NAME.equals(platformName)) {
                query.whereEqualTo(DMUser.WECHAT_USER_ID, openId);
            } else if (SinaWeibo.NAME.equals(platformName)) {
                query.whereEqualTo(DMUser.WEIBO_USER_ID, openId);
            } else {
                return null;
            }
            try {
                listAVObject = query.find();
            } catch (AVException e) {
                e.printStackTrace();
                return null;
            }
            if (listAVObject != null && listAVObject.size() > 0) {
                AVObject avObject = listAVObject.get(0);
                return DMUser.parse(avObject);
            }

            return registerByThirdParty(params[0]);
        }

        @Override
        protected void onPostExecute(DMUser dmUser) {
            if (dmUser != null)
                mLoginCallback.onSuccess(dmUser);
            else
                mLoginCallback.onError(new AVException(0, "登录失败！"));
        }
    }

    private class UpLoadTask extends AsyncTask<Void, Void, VideoInfo> {

        UploadVideoCallback mUploadVideoCallback;
        String mVideoPath;
        String mCoverPath;
        String mDesc;
        DMUser mDMUser;

        protected UpLoadTask(String videoPath, String thumbnailPath, String desc, DMUser user, UploadVideoCallback uploadVideoCallback) {
            mVideoPath = videoPath;
            mCoverPath = thumbnailPath;
            mDesc = desc;
            mDMUser = user;
            mUploadVideoCallback = uploadVideoCallback;
        }

        @Override
        protected VideoInfo doInBackground(Void... params) {
            String jsonString = DataLogic.getInstance().uploadVideo(mVideoPath, mCoverPath);
            if (jsonString == null)
                return null;
            UploadVideoResult result = JSONObject.parseArray(jsonString, UploadVideoResult.class).get(0);
            if (result == null)
                return null;
            AVObject user = AVObject.createWithoutData(TableName.USER, mDMUser.getObjectId());
            AVObject video = new AVObject(TableName.VIDEO);
            video.put(VideoInfo.VIDEO_PATH, result.videoPath);
            video.put(VideoInfo.COVER_PATH, result.thumbnailPath);
            video.put(VideoInfo.DESC, mDesc);
            video.put("user", user);
            try {
                video.save();
            } catch (AVException e) {
                e.printStackTrace();
                return null;
            }
            return VideoInfo.parse(video);
        }

        @Override
        protected void onPostExecute(VideoInfo result) {
            if (result!=null && !result.getObjectId().equals(""))
                mUploadVideoCallback.onSuccess(result);
            else {
                mUploadVideoCallback.onError();
            }
        }
    }

    private class GetVideoListTask extends AsyncTask<Void, Void, List<VideoInfo>> {

        int mPageNum;
        int mItemPerPage;
        GetVideoCallback mGetVideoCallback;

        protected GetVideoListTask(int pageNum, int itemPerPage, GetVideoCallback getVideoCallback) {
            mPageNum = pageNum;
            mItemPerPage = itemPerPage;
            mGetVideoCallback = getVideoCallback;
        }

        @Override
        protected List<VideoInfo> doInBackground(Void... params) {
            List<VideoInfo> videoList = new ArrayList<VideoInfo>();
            AVQuery<AVObject> query = new AVQuery<>(TableName.VIDEO);
            query.include("user");
            query.limit(mItemPerPage);
            query.skip(mPageNum * mItemPerPage);
//            query.orderByDescending(VideoInfo.HOT);
            try {
                List<AVObject> list = query.find();
                for (AVObject o : list) {
                    videoList.add(VideoInfo.parse(o));
                }
            } catch (AVException e) {
                e.printStackTrace();
            }
            return videoList;
        }

        @Override
        protected void onPostExecute(List<VideoInfo> videoInfos) {
            if (videoInfos == null) {
                mGetVideoCallback.onError();
                return;
            }
            mGetVideoCallback.onSuccess(videoInfos);
        }
    }

}
