package com.kylin.datingmoments.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by kylin on 16-5-22.
 */
public class FraPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mListFra;

    public FraPagerAdapter(FragmentManager fm, List<Fragment> fragments){
        super(fm);
        this.mListFra = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mListFra.get(position);
    }

    @Override
    public int getCount() {
            return mListFra.size();
    }

}
