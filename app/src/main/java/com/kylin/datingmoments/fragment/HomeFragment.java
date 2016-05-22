package com.kylin.datingmoments.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.kylin.datingmoments.R;

/**
 * Created by kylin on 16-5-22.
 */
public class HomeFragment extends LazyFragment{

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_home);
    }
}
