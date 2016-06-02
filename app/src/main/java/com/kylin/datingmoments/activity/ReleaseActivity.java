package com.kylin.datingmoments.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

    private int MAX_WORD_NUM;

    /** 播放路径 */
    private String mVideoPath;
    /** 视频截图路径 */
    private String mCoverPath;

    private SimpleDraweeView mSDCover;
    private EditText mEtDesc;
    private TextView mTvLeftWordNum;
    private RelativeLayout mLayoutRelease;
    private ProgressBar mPb;
    private View mViewVoid;

    private DAO mDAO = DAOFactory.getDAO();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release);
        initData();
        intiView();
    }

    private void initData() {
        MAX_WORD_NUM = getResources().getInteger(R.integer.max_desc_word_num);
        mVideoPath = getIntent().getStringExtra(Constant.RECORD_VIDEO_PATH);
        mCoverPath = getIntent().getStringExtra(Constant.RECORD_VIDEO_CAPTURE);
    }

    private void intiView() {
        TextView title = (TextView) findViewById(R.id.com_actionbar_tv_title);
        title.setText(R.string.release);
        mViewVoid = findViewById(R.id.act_release_view_void);
        mTvLeftWordNum = (TextView) findViewById(R.id.act_release_tv_left_word_num);
        mTvLeftWordNum.setText(MAX_WORD_NUM+"");
        mSDCover = (SimpleDraweeView) findViewById(R.id.act_release_iv_cover);
        mEtDesc = (EditText) findViewById(R.id.act_release_et_desc);
        mEtDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mTvLeftWordNum.setText((MAX_WORD_NUM-mEtDesc.getText().toString().length())+"");
            }
        });
        mEtDesc.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_WORD_NUM)});
        mLayoutRelease = (RelativeLayout) findViewById(R.id.act_release_layout_release);
        mLayoutRelease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String desc = mEtDesc.getText().toString();
                if ("".equals(desc)){
                    Toast.makeText(getApplicationContext(),R.string.please_type_video_describe,Toast.LENGTH_LONG).show();
                    return;
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
                        Toast.makeText(getApplicationContext(),R.string.upload_error,Toast.LENGTH_LONG).show();
                        showProgressBar(false);
                    }
                });
                showProgressBar(true);
            }
        });
        Uri uri = Uri.parse("file://"+mCoverPath);
        mSDCover.setImageURI(uri);

        mPb = (ProgressBar) findViewById(R.id.act_release_pb);
    }

    private void showProgressBar(boolean show){
        mViewVoid.setVisibility(show?View.VISIBLE:View.GONE);
        mPb.setVisibility(show?View.VISIBLE:View.GONE);
    }


}
