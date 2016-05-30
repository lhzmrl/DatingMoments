package com.kylin.datingmoments.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.kylin.datingmoments.R;
import com.kylin.datingmoments.app.DMApplication;
import com.kylin.datingmoments.dao.DAO;
import com.kylin.datingmoments.dao.DAOFactory;
import com.kylin.datingmoments.dao.DataLogic;
import com.kylin.datingmoments.util.Constant;

import java.io.File;

public class ReleaseActivity extends AppCompatActivity {

    /** 播放路径 */
    private String mVideoPath;
    /** 视频截图路径 */
    private String mCoverPath;

    private SimpleDraweeView mSDCover;
    private EditText mEtDesc;
    private Button mBtnRelease;
    private ProgressBar mPb;

    private DAO mDAO = DAOFactory.getDAO();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release);
        initData();
        intiView();
    }

    private void initData() {
        mVideoPath = getIntent().getStringExtra(Constant.RECORD_VIDEO_PATH);
        mCoverPath = getIntent().getStringExtra(Constant.RECORD_VIDEO_CAPTURE);
    }

    private void intiView() {
        TextView title = (TextView) findViewById(R.id.com_actionbar_tv_title);
        title.setText(R.string.release);
        mSDCover = (SimpleDraweeView) findViewById(R.id.act_release_iv_cover);
        mEtDesc = (EditText) findViewById(R.id.act_release_et_desc);
        mBtnRelease = (Button) findViewById(R.id.act_release_btn_release);
        mBtnRelease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar(true);
                String desc = mEtDesc.getText().toString();
                if ("".equals(desc)){
                    Toast.makeText(getApplicationContext(),R.string.please_type_video_describe,Toast.LENGTH_LONG).show();
                }
                mDAO.uploadVideo(mVideoPath, mCoverPath, desc,((DMApplication)getApplicationContext()).getUser() , new DAO.UploadVideoCallback() {
                    @Override
                    public void onSuccess() {
                        showProgressBar(false);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onError() {
                        showProgressBar(false);
                    }
                });
            }
        });
        Uri uri = Uri.parse("file://"+mCoverPath);
        mSDCover.setImageURI(uri);

        mPb = (ProgressBar) findViewById(R.id.act_release_pb);
    }

    private void showProgressBar(boolean show){
        mSDCover.setVisibility(show?View.INVISIBLE:View.VISIBLE);
        mEtDesc.setVisibility(show?View.INVISIBLE:View.VISIBLE);
        mBtnRelease.setVisibility(show?View.INVISIBLE:View.VISIBLE);
        mPb.setVisibility(show?View.VISIBLE:View.GONE);
    }


}
