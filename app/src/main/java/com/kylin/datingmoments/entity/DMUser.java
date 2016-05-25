package com.kylin.datingmoments.entity;

import com.avos.avoscloud.AVObject;

import java.io.Serializable;

import cn.sharesdk.tencent.qq.QQ;

/**
 * 用户信息实体类</br>
 * Created by kylin on 16-5-24.
 */
public class DMUser implements Serializable{

    private long serialVersionUID = 0L;

    public static final String OBJECT_ID = "objectId";
    public static final String NICK_NAME = "nickName";
    public static final String EMAIL = "eMail";
    public static final String WEIBO_USER_ID = "weiboUserId";
    public static final String QQ_USER_ID = "qqUserId";
    public static final String WECHAT_USER_ID = "wechatUserId";

    private String objectId;
    private String nickName;
    private String email;
    private String weiboUserId;
    private String qqUserId;
    private String wechatUserId;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWeiboUserId() {
        return weiboUserId;
    }

    public void setWeiboUserId(String weiboUserId) {
        this.weiboUserId = weiboUserId;
    }

    public String getQQUserId() {
        return qqUserId;
    }

    public void setQQUserId(String qqUserId) {
        this.qqUserId = qqUserId;
    }

    public String getWechatUserId() {
        return wechatUserId;
    }

    public void setWechatUserId(String wechatUserId) {
        this.wechatUserId = wechatUserId;
    }

    public static DMUser parse(AVObject avObject){
        DMUser user = new DMUser();
        user.setObjectId(avObject.getObjectId());
        user.setNickName(avObject.getString(NICK_NAME));
        user.setEmail(avObject.getString(EMAIL));
        user.setQQUserId(avObject.getString(QQ_USER_ID));
        user.setWechatUserId(avObject.getString(WECHAT_USER_ID));
        user.setWeiboUserId(avObject.getString(WEIBO_USER_ID));
        return user;
    }
}
