package com.kylin.datingmoments.dao;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.SaveCallback;
import com.kylin.datingmoments.entity.Comment;
import com.kylin.datingmoments.entity.DMUser;
import com.kylin.datingmoments.entity.VideoInfo;

import java.util.List;

import cn.sharesdk.framework.Platform;

/**
 * 操作远程数据行为接口
 * Created by kylin on 16-5-24.
 */
public interface DAO {

    /**
     * 通过邮箱登录
     * @param email 邮箱
     * @param pwd 密码
     * @return 用户信息
     */
    public DMUser attemptLoginByEmail(String email,String pwd);

    /**
     * 通过第三方平台登录
     * @param platform 平台
     * @param loginCallback 回调接口
     */
    public void attemptLoginByThirdParty(Platform platform,LoginCallback loginCallback);

    /**
     * 用户是否存在
     * @param user 用户信息
     * @return
     */
    public boolean isUserInfoExist(DMUser user);

    /**
     * 注册
     * @param user 用户信息
     * @return 注册完成后用户的信息
     */
    public DMUser register(DMUser user );

    /**
     * 通过第三方平台实现注册
     * @param platform 平台操作类
     * @return 是否从平台成功获取信息
     */
    public DMUser registerByThirdParty(Platform platform) throws AVException;

    /**
     * 上传视频
     * @param videoPath 视频路径
     * @param thumbnailPath 缩略图路径
     * @param desc 视频描述
     * @param user 用户
     * @param uploadVideoCallback 上传回调
     */
    public void uploadVideo(String videoPath,String thumbnailPath,String desc,DMUser user,UploadVideoCallback uploadVideoCallback);

    /**
     * 获取视频
     * @param getVideoCallback 获取视频毁掉
     */
    public void getVideoList(GetVideoCallback getVideoCallback);

    /**
     * 发送评论
     * @param comment 评论内容
     * @return 是否发送成功
     */
    public boolean sendComment(Comment comment);

    /**
     * 获取评论
     * @return 评论集合
     */
    public List<Comment> getCommentList(String videoId);

    /**
     * 用户登录回调
     */
    interface LoginCallback{
        public void onSuccess(DMUser user);
        public void onError(AVException exception);
    }

    /**
     * 视频上传回调
     */
    interface UploadVideoCallback{
        public void onSuccess();
        public void onError();
    }

    /**
     * 视频获取回调
     */
    interface GetVideoCallback{
        public void onSuccess(List<VideoInfo> result);
        public void onError();
    }
}
