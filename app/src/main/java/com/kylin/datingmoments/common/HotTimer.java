package com.kylin.datingmoments.common;

/**
 * Created by kylin on 16-6-6.
 */
public class HotTimer {

    private HotReset mHotReset ;



    public static void main(String[] args){
        HotReset mHotReset = new HotReset();
        while(true){
            mHotReset.resetHot();
            try {
                Thread.sleep(3600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
