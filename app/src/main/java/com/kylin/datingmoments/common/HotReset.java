package com.kylin.datingmoments.common;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.kylin.datingmoments.dao.DAO;
import com.kylin.datingmoments.dao.DAOFactory;
import com.kylin.datingmoments.entity.VideoInfo;

import java.util.Date;
import java.util.List;

import io.vov.vitamio.provider.MediaStore;

/**
 * Created by kylin on 16-6-6.
 */
public class HotReset {

    private static final float G = 1.8f;

    DAO mDAO = DAOFactory.getDAO();

    public HotReset(){

    }

    public void resetHot(){
        List<AVObject> videoList = null;
        AVQuery videoQuery = new AVQuery<>(TableName.VIDEO);
        try {
            videoList = videoQuery.find();
        } catch (AVException e) {
            e.printStackTrace();
            return;
        }
        for(AVObject video:videoList){
            setHot(video);
        }
    }

    private void setHot(AVObject video) {
        List<AVObject> favoriteList = null;
        Date date = video.getCreatedAt();
        Date currentTime = new Date();
        // 时间
        int T = (int) ((currentTime.getTime() - date.getTime())/(3600000));
        AVQuery favoriteQuery = new AVQuery<>(TableName.FAVORITE_RELATIONSHIP);
        try {
            favoriteList = favoriteQuery.find();
        } catch (AVException e) {
            e.printStackTrace();
            return;
        }
        // 好评度
        int P = favoriteList.size();
        List<AVObject> wathcList = null;
        AVQuery wathcQuery = new AVQuery<>(TableName.WATCH_RELATIONSHIP);
        try {
            wathcList = favoriteQuery.find();
        } catch (AVException e) {
            e.printStackTrace();
            return;
        }
        // 观看度
        int W = wathcList.size();
        double F = Math.pow(2+T,G);
        float Hot = (float) ((1.0f*P*P/W+1000)/F);
        video.put(VideoInfo.HOT,Hot);
        try {
            video.save();
        } catch (AVException e) {
            e.printStackTrace();
        }
    }

}
