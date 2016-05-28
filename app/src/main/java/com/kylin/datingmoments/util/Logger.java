package com.kylin.datingmoments.util;

import android.util.Log;

/**
 * Created by kylin on 16-5-24.
 */
public class Logger {
    private static final String APP_NAME = "DatingMoments";

    private static final int VERBOSE = 0;
    private static final int DEBUG= 1;
    private static final int INFO = 2;
    private static final int WARN = 3;
    private static final int ERROR = 4;

    private static final int MIN_SHOW_LEVEL = 0;

    private static boolean canLogMsg(int level){
        if (level>=MIN_SHOW_LEVEL)
            return true;
        return false;
    }

    public static void v(String tag,String msg){
        if (canLogMsg(VERBOSE))
            Log.v(tag, msg);
    }

    public static void d(String tag,String msg){
        if (canLogMsg(DEBUG))
            Log.d(tag, msg);
    }

    public static void i(String tag,String msg){
        if (canLogMsg(INFO))
            Log.i(tag, msg);
    }

    public static void w(String tag,String msg){
        if (canLogMsg(WARN))
            Log.w(tag, msg);
    }

    public static void e(String tag,String msg){
        if (canLogMsg(ERROR))
            Log.e(tag, msg);
    }

    public static void a(String tag,String msg){
            int a = Log.ASSERT;
    }

    public static void v(String msg){
        v(APP_NAME, msg);
    }

    public static void d(String msg){
        d(APP_NAME, msg);
    }

    public static void i(String msg){
        i(APP_NAME, msg);
    }

    public static void w(String msg){
        w(APP_NAME, msg);
    }

    public static void e(String msg){
        e(APP_NAME, msg);
    }

}
