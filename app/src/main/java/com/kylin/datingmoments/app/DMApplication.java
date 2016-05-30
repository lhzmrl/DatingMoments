package com.kylin.datingmoments.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

import com.avos.avoscloud.AVOSCloud;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.kylin.datingmoments.R;
import com.kylin.datingmoments.dao.DataLogic;
import com.kylin.datingmoments.entity.DMUser;
import com.yixia.camera.demo.service.AssertService;
import com.yixia.weibo.sdk.VCamera;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.yixia.weibo.sdk.util.FileUtils;
import com.yixia.weibo.sdk.util.ToastUtils;

import java.io.File;

/**
 * 应用Application
 * Created by kylin on 16-5-23.
 */
public class DMApplication extends Application {

    private static DMApplication application;

    private DMUser mUser;

    public static DMApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AVOSCloud.initialize(this, "tCFkvg36UcFIdkOOjsLRn7Un-gzGzoHsz", "E5BAiKtIltczgpxdvTLgO6zQ");

        application = this;

        // 设置拍摄视频缓存路径
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (DeviceUtils.isZte()) {
            if (dcim.exists()) {
                VCamera.setVideoCachePath(dcim + "/Camera/VCameraDemo/");
            } else {
                VCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/", "/sdcard-ext/") + "/Camera/VCameraDemo/");
            }
        } else {
            VCamera.setVideoCachePath(dcim + "/Camera/VCameraDemo/");
        }
        // 开启log输出,ffmpeg输出到logcat
        VCamera.setDebugMode(true);
        // 初始化拍摄SDK，必须
        VCamera.initialize(this);
        Fresco.initialize(this);

        //解压assert里面的文件
        startService(new Intent(this, AssertService.class));
        DataLogic.setBaseApplication(this);
    }


    public static Context getContext() {
        return application;
    }

    public static File getGifCacheDirectory() {
        if (application != null)
            return FileUtils.getCacheDiskPath(application, "gif");//vineApplication.getExternalCacheDir() + "/cache/gif/";
        return null;
    }


    public final static int AVAILABLE_SPACE = 200;//M
    /**
     * 检测用户手机是否剩余可用空间200M以上
     * @return
     */
    public static boolean isAvailableSpace() {
        if (application == null) {
            return false;
        }
        //检测磁盘空间
        if (FileUtils.showFileAvailable(application) < AVAILABLE_SPACE) {
            ToastUtils.showToast(application, application.getString(R.string.record_check_available_faild, AVAILABLE_SPACE));
            return false;
        }

        return true;
    }

    /** 视频截图目录 */
    public static File getThumbCacheDirectory() {
        if (application != null)
            return FileUtils.getCacheDiskPath(application, "thumbs");//vineApplication.getExternalCacheDir() + "/cache/thumbs/";
        return null;
    }

    public void setUser(DMUser user){
        mUser = user;
    }

    public DMUser getUser(){
        return mUser;
    }

    public void showCustomToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    public void showCustomToast(String res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }
}
