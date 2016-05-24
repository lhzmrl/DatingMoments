package com.kylin.datingmoments;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;

/**
 * Created by kylin on 16-5-23.
 */
public class DMApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        AVOSCloud.initialize(this, "tCFkvg36UcFIdkOOjsLRn7Un-gzGzoHsz", "E5BAiKtIltczgpxdvTLgO6zQ");
    }
}
