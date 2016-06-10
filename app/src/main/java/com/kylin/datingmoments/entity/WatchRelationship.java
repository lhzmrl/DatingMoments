package com.kylin.datingmoments.entity;

/**
 * Created by kylin on 16-6-5.
 */
public class WatchRelationship {

    public static final String VIDEO = "video";
    public static final String USER = "user";

    private String video;
    private String user;

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
}
