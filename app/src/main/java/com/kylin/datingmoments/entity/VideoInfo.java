package com.kylin.datingmoments.entity;

import com.avos.avoscloud.AVObject;
import com.kylin.datingmoments.common.NetConfig;

/**
 * Created by kylin on 16-5-29.
 */
public class VideoInfo {

    private static final String VIDEO_SERVER_ADDRESS = "http://"+NetConfig.VIDEO_SERVER;

    public static final String USER_NAME = "userName";
    public static final String VIDEO_PATH = "videoPath";
    public static final String  COVER_PATH= "coverPath";
    public static final String DESC = "desc";
    public static final String USER_ICON = "userIcon";

    private String userName;
    private String videoPath;
    private String coverPath;
    private String desc;
    private String userIcon;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getVideoPath() {
        return VIDEO_SERVER_ADDRESS+"video/"+videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getCoverPath() {
        return VIDEO_SERVER_ADDRESS+"thumbnail/"+coverPath;
    }

    public void setCoverlPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static VideoInfo parse(AVObject object){
        VideoInfo info = new VideoInfo();
        AVObject user = object.getAVObject("user");
        info.setUserName(user.getString(DMUser.NICK_NAME));
        info.setUserIcon(user.getString(DMUser.USER_ICON));
        info.setVideoPath(object.getString(VIDEO_PATH));
        info.setCoverlPath(object.getString(COVER_PATH));
        info.setDesc(object.getString(DESC));
        return info;
    }
}
