package com.kylin.datingmoments.dao;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.SaveCallback;
import com.kylin.datingmoments.entity.DMUser;

import cn.sharesdk.framework.Platform;

/**
 * 操作远程数据行为接口
 * Created by kylin on 16-5-24.
 */
public interface DAO {

    public void attemptLoginByThirdParty(Platform platform,LoginCallback loginCallback);

    public DMUser register(DMUser user );

    /**
     * 通过第三方平台实现注册
     * @param platform 平台操作类
     * @return 是否从平台成功获取信息
     */
    public DMUser registerByThirdParty(Platform platform) throws AVException;

    interface LoginCallback{
        public void onSuccess(DMUser user);
        public void onError(AVException exception);
    }
}
