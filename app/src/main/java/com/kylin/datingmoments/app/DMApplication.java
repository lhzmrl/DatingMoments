package com.kylin.datingmoments.app;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.kylin.datingmoments.entity.DMUser;

/**
 * 应用Application
 * Created by kylin on 16-5-23.
 */
public class DMApplication extends Application {

    private DMUser mUser;

    @Override
    public void onCreate() {
        super.onCreate();
        AVOSCloud.initialize(this, "tCFkvg36UcFIdkOOjsLRn7Un-gzGzoHsz", "E5BAiKtIltczgpxdvTLgO6zQ");
    }

    public void setUser(DMUser user){
        mUser = user;
    }

    public DMUser getUser(){
        return mUser;
    }
}
