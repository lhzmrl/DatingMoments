package com.kylin.datingmoments.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVAnalytics;
import com.kylin.datingmoments.R;
import com.kylin.datingmoments.adapter.FraPagerAdapter;
import com.kylin.datingmoments.fragment.HomeFragment;
import com.kylin.datingmoments.fragment.MineFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面
 */
public class MainActivity extends AppCompatActivity {

    private static final int MAX_PAGE_NUM = 4;

    private List<Fragment> mListFra;

    private RelativeLayout mRlHome;
    private RelativeLayout mRlMine;
    private TextView mTvHome;
    private TextView mTvMine;
    private ImageView mIvAdd;
    private ViewPager mViewPager;

    private FraPagerAdapter mAdapterFra;

    private View.OnClickListener mRlOnClickListener = new View.OnClickListener() {

        private int selectItem;

        @Override
        public void onClick(View v) {
            int clickItem = v.getId();
            if (clickItem == selectItem)
                return;
            Drawable drawable;
            int width;
            int height;
            switch (clickItem){
                case R.id.act_main_rl_home:
                    mTvHome.setTextColor(getResources().getColor(R.color.tab_text_press));
                    drawable = getResources().getDrawable(R.drawable.img_tv_home_press);
                    drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                    mTvHome.setCompoundDrawables(null,drawable,null,null);
                    mViewPager.setCurrentItem(0,false);
                    break;
                case R.id.act_main_rl_mine:
                    mTvMine.setTextColor(getResources().getColor(R.color.tab_text_press));
                    drawable = getResources().getDrawable(R.drawable.img_tv_mine_press);
                    drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                    mTvMine.setCompoundDrawables(null,drawable,null,null);
                    mViewPager.setCurrentItem(1,false);
                    break;
                default:
                    break;
            }
            switch (selectItem){
                case R.id.act_main_rl_home:
                    mTvHome.setTextColor(getResources().getColor(R.color.tab_text_normal));
                    drawable = getResources().getDrawable(R.drawable.img_tv_home_normal);
                    drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                    mTvHome.setCompoundDrawables(null,drawable,null,null);
                    break;
                case R.id.act_main_rl_mine:
                    mTvMine.setTextColor(getResources().getColor(R.color.tab_text_normal));
                    drawable = getResources().getDrawable(R.drawable.img_tv_mine_normal);
                    drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                    mTvMine.setCompoundDrawables(null,drawable,null,null);
                    break;
                default:
                    break;
            }
            selectItem = clickItem;
        }

    };

    private View.OnClickListener mIvOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(),RecorderActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AVAnalytics.trackAppOpened(getIntent());
        setContentView(R.layout.activity_main);
        initFragment();
        initView();
    }

    private void initFragment() {
        mListFra = new ArrayList<Fragment>();
        mListFra.add(new HomeFragment());
        mListFra.add(new MineFragment());

    }

    private void initView() {
        mRlHome = (RelativeLayout) findViewById(R.id.act_main_rl_home);
        mRlHome.setOnClickListener(mRlOnClickListener);
        mRlMine = (RelativeLayout) findViewById(R.id.act_main_rl_mine);
        mRlMine.setOnClickListener(mRlOnClickListener);
        mTvHome = (TextView) findViewById(R.id.act_main_tv_home);
        mTvMine = (TextView) findViewById(R.id.act_main_tv_mine);

        mIvAdd = (ImageView) findViewById(R.id.act_main_iv_add);
        mIvAdd.setOnClickListener(mIvOnClickListener);

        mViewPager = (ViewPager) findViewById(R.id.act_main_nrvp);
        mViewPager.setOffscreenPageLimit(MAX_PAGE_NUM);
        mAdapterFra = new FraPagerAdapter(getSupportFragmentManager(), mListFra);
        mViewPager.setAdapter(mAdapterFra);

        mRlOnClickListener.onClick(mRlHome);
    }
}
