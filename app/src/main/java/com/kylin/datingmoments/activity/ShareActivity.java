package com.kylin.datingmoments.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.kylin.datingmoments.R;
import com.kylin.datingmoments.entity.VideoInfo;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.wechat.moments.WechatMoments;

public class ShareActivity extends AppCompatActivity {

    private String mCoverPath;
    private String mVideoPath;
    private String mContent;

    Platform pf = null;
    Platform.ShareParams sp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ShareSDK.initSDK(this);
        setContentView(R.layout.activity_share);
        initDate();
        initView();
    }

    private void initDate() {
        VideoInfo info = (VideoInfo) getIntent().getSerializableExtra("videoinfo");
        mVideoPath = info.getVideoPath();
        mCoverPath = getIntent().getStringExtra("cover");
        mContent = info.getDesc()+"\n 视频地址："+mVideoPath;
    }

    private void initView() {
        findViewById(R.id.act_share_btn_tencent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        findViewById(R.id.act_share_btn_wechat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pf = ShareSDK.getPlatform(WechatMoments.NAME);
                sp = new WechatMoments.ShareParams();
                sp.setText(mContent);
                sp.setImagePath(mCoverPath);
            }
        });
        findViewById(R.id.act_share_btn_weibo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pf = ShareSDK.getPlatform(SinaWeibo.NAME);
                sp = new SinaWeibo.ShareParams();
                sp.setText(mContent);
                sp.setImagePath(mCoverPath);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShareSDK.stopSDK(this);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }
}
