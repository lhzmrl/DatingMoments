package com.kylin.datingmoments.entity;

import com.avos.avoscloud.AVObject;

import java.util.Date;

/**
 * Created by kylin on 16-6-4.
 */
public class Comment {

    public static final String OBJECT_ID = "objectId";
    public static final String COMMENT = "comment";
    public static final String VIDEO = "video";
    public static final String USER = "user";
    public static final String USER_NAME = "user_name";
    public static final String USER_ICON = "user_icon";
    public static final String CREATE_TIME = "create_time";

    private String objectId;
    private String comment;
    private String video;
    private String user;
    private String userName;
    private String userIcon;
    private Date createTime;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public static Comment parse(AVObject object){
        AVObject user = object.getAVObject(USER);
        Comment comment = new Comment();
        comment.setUserName(user.getString(DMUser.NICK_NAME));
        comment.setUserIcon(user.getString(DMUser.USER_ICON));
        comment.setComment(object.getString(COMMENT));
        comment.setCreateTime(object.getCreatedAt());
        return comment;
    }
}
