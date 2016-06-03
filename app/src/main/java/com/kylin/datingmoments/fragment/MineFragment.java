package com.kylin.datingmoments.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.kylin.datingmoments.R;
import com.kylin.datingmoments.activity.LoginActivity;
import com.kylin.datingmoments.app.DMApplication;
import com.kylin.datingmoments.common.SPOperater;
import com.kylin.datingmoments.entity.DMUser;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;

/**
 * 个人信息界面
 * Created by kylin on 16-5-22.
 */
public class MineFragment extends LazyFragment implements View.OnClickListener{

    public static final int REQUEST_CODE_LOGIN = 0X1001;

    private View mItemMine;
    private TextView mTvNickName;
    private TextView mTvIntroduce;

    private DMUser mUser;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_mine);
        initView();
    }

    private void initView() {
        TextView title = (TextView) findViewById(R.id.com_actionbar_tv_title);
        title.setText(R.string.tab_mine);
        mItemMine = findViewById(R.id.fra_mine_item_mine);
        mItemMine.setOnClickListener(this);

        mTvNickName = (TextView) findViewById(R.id.fra_mine_tv_nickname);
        mTvIntroduce = (TextView) findViewById(R.id.fra_mine_tv_introduce);
        findViewById(R.id.fra_mine_item_log_off).setOnClickListener(this);
        tryFillUserInfo();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.fra_mine_item_mine:
                if (mUser == null) {
                    intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_LOGIN);
                }
                break;
            case R.id.fra_mine_item_log_off:
                ((DMApplication)getApplicationContext()).setUser(null);
                String loginType = SPOperater.getLoginInfo(getApplicationContext());
                if (!loginType.equals("") && !loginType.equals("email")){
                    ShareSDK.initSDK(getApplicationContext());
                    Platform platform = ShareSDK.getPlatform(loginType);
                    platform.removeAccount(true);
                    ShareSDK.stopSDK(getApplicationContext());
                }
                SPOperater.setLoginInfo(getApplicationContext(),"");
                tryFillUserInfo();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CODE_LOGIN:
                tryFillUserInfo();
                break;
            default:
                break;
        }
    }

    private void tryFillUserInfo(){
        mUser = ((DMApplication)getApplicationContext()).getUser();
        if (mUser==null){
            mTvNickName.setText(R.string.please_login);
            mTvIntroduce.setText(R.string.login_to_see_more_creditable_content);
            return;
        }
        mTvNickName.setText(mUser.getNickName());
        mTvIntroduce.setText("");
    }
}
