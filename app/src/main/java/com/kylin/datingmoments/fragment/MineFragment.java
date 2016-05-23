package com.kylin.datingmoments.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.kylin.datingmoments.R;
import com.kylin.datingmoments.activity.LoginActivity;

/**
 * Created by kylin on 16-5-22.
 */
public class MineFragment extends LazyFragment implements View.OnClickListener{

    private View mItemMine;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_mine);
        initView();
    }

    private void initView() {
        mItemMine = findViewById(R.id.fra_mine_item_mine);
        mItemMine.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.fra_mine_item_mine:
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
