package com.kylin.datingmoments.util;

import android.content.Context;

/**
 * Created by kylin on 16-5-29.
 */
public class BitmapUtils {
    public static int dipTopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
