package com.kylin.datingmoments.activity;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.facebook.drawee.view.SimpleDraweeView;
import com.kylin.datingmoments.R;
import com.kylin.datingmoments.adapter.CommentAdapter;
import com.kylin.datingmoments.adapter.EndlessRecyclerOnScrollListener;
import com.kylin.datingmoments.app.DMApplication;
import com.kylin.datingmoments.dao.DAO;
import com.kylin.datingmoments.dao.DAOFactory;
import com.kylin.datingmoments.entity.Comment;
import com.kylin.datingmoments.entity.DMUser;
import com.kylin.datingmoments.util.Constant;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.yixia.weibo.sdk.util.StringUtils;

import org.vitamio.vitamiorecorderlibrary.view.SurfaceVideoView;

import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.widget.ShortVideoView;

/**
 * 视频播放界面
 */
public class VideoPlayerActivity extends BaseActivity {

    private boolean mIsFavoriting;
    /**
     * 播放路径
     */
    private String mPath;
    /**
     * 视频截图路径
     */
    private String mCoverPath;
    private String mVideoId;
    private DMUser mUser;
    private boolean mIsFavorite;

    private List<Comment> mListComment;

    private SimpleDraweeView mSDVIcon;
    private TextView mTvNickName;
    private TextView mTvDesc;
    private TextView mTvFavorite;
    private ShortVideoView mVideoView;
    private EditText mEtComment;
    private Button mBtnSendComment;
    private ImageView mIvFavorite;
    private RecyclerView mRvComment;

    private CommentAdapter mAdapterComment;

    private DAO mDAO = DAOFactory.getDAO();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 防止锁屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPath = getIntent().getStringExtra(Constant.RECORD_VIDEO_PATH);
        mCoverPath = getIntent().getStringExtra(Constant.RECORD_VIDEO_CAPTURE);
        mVideoId = getIntent().getStringExtra(Constant.RECORD_VIDEO_ID);
        if (StringUtils.isEmpty(mPath)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_video_player);
        initDate();
        initView();
    }

    private void initDate() {
        mUser = ((DMApplication)getApplicationContext()).getUser();
        mListComment = new ArrayList<Comment>();
        mAdapterComment = new CommentAdapter(getApplicationContext(), mListComment);
    }

    private void initView() {
        mSDVIcon = (SimpleDraweeView) findViewById(R.id.act_video_player_sd_user_icon);
        mSDVIcon.setImageURI(Uri.parse(mUser.getUserIcon()));
        mTvNickName = (TextView) findViewById(R.id.act_video_player_tv_nick_name);
        mTvNickName.setText(mUser.getNickName());
        mTvDesc = (TextView) findViewById(R.id.act_video_player_tv_desc);
        mTvFavorite = (TextView) findViewById(R.id.act_video_player_tv_favorite);
        mVideoView = (ShortVideoView) findViewById(R.id.act_video_player_video_view);
        mEtComment = (EditText) findViewById(R.id.act_video_player_et_msg);
        mIvFavorite = (ImageView) findViewById(R.id.act_video_player_iv_favorite);
        mIvFavorite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsFavoriting)
                    return;
                new FavoriteTask().execute(!mIsFavorite);
            }
        });
        mBtnSendComment = (Button) findViewById(R.id.act_video_player_btn_send);
        mBtnSendComment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUser==null){
                    Toast.makeText(getApplicationContext(),"请先登录！",Toast.LENGTH_LONG).show();
                    return;
                }
                String msg = mEtComment.getText().toString();
                if (msg.equals(""))
                    return;
                Comment comment = new Comment();
                comment.setVideo(mVideoId);
                comment.setComment(msg);
                comment.setUser(mUser.getObjectId());
                new SendCommentTask().execute(comment);
                mEtComment.setText("");
            }
        });
        mRvComment = (RecyclerView) findViewById(R.id.act_video_player_rv_comment);
        mRvComment.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRvComment.setItemAnimator(new DefaultItemAnimator());
        mRvComment.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelOffset(R.dimen.video_cover_padding)));
//        mRvComment.addOnScrollListener(new EndlessRecyclerOnScrollListener(staggeredGridLayoutManager) {
//            @Override
//            public void onLoadMore(int currentPage) {
//                loadMoreDate();
//            }
//        });
        mRvComment.setAdapter(mAdapterComment);
        new GetCommentTask().execute(mVideoId);
        new PlayRecordTask().execute(mVideoId,mUser.getObjectId());
        new GetPlayNumTask().execute(mVideoId);
        new GetFavoriteTask().execute();
        new GetFavoriteNumTask().execute();
        playFunction();
    }

    private void playFunction() {
        mVideoView.setVideoPath(mPath);
        mVideoView.requestFocus();

    }

    private class PlayRecordTask extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... params) {
            String videoId = params[0];
            String userId = params[1];
            mDAO.tryRecordPlayNum(videoId,userId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private class GetPlayNumTask extends AsyncTask<String,Void,Integer>{
        @Override
        protected Integer doInBackground(String... params) {

            return mDAO.getPalyNum(params[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            mTvDesc.setText(integer+"人观看");
        }
    }

    private class GetFavoriteTask extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected Boolean doInBackground(Void... params) {
            AVObject result = mDAO.isFavoriteTheVideo(mVideoId,mUser.getObjectId());
            return result==null?false:true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mIsFavorite = aBoolean;
            mIvFavorite.setImageResource(mIsFavorite?R.drawable.img_feed_like_checked:R.drawable.img_feed_like_default);
        }

    }

    private class FavoriteTask extends AsyncTask<Boolean,Void,Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsFavoriting = true;

        }

        @Override
        protected Boolean doInBackground(Boolean... params) {

            return mDAO.favoriteVideo(mVideoId, mUser.getObjectId(),params[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mIsFavoriting = false;
            if (result) {
                mIsFavorite=!mIsFavorite;
                mIvFavorite.setImageResource(mIsFavorite?R.drawable.img_feed_like_checked:R.drawable.img_feed_like_default);
            }
            new GetFavoriteNumTask().execute();
        }
    }

    private class GetFavoriteNumTask extends AsyncTask<Void,Void,Integer>{

        @Override
        protected Integer doInBackground(Void... params) {
            return mDAO.getFavoriteNum(mVideoId);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            mTvFavorite.setText(integer.toString());
        }
    }

    private class GetCommentTask extends AsyncTask<String, Void, List<Comment>> {

        @Override
        protected List<Comment> doInBackground(String... params) {
            return mDAO.getCommentList(params[0]);
        }

        @Override
        protected void onPostExecute(List<Comment> comments) {
            super.onPostExecute(comments);
            if (comments==null)
                return;
            mListComment.clear();
            mListComment.addAll(comments);
            mAdapterComment.notifyDataSetChanged();
        }
    }

    private class SendCommentTask extends AsyncTask<Comment, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Comment... params) {
            return mDAO.sendComment(params[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean){
                Toast.makeText(getApplicationContext(), "发送成功！", Toast.LENGTH_LONG).show();
                new GetCommentTask().execute(mVideoId);
            }
            else
                Toast.makeText(getApplicationContext(), "发送失败！", Toast.LENGTH_LONG).show();


        }
    }

    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//            outRect.left = space;
//            outRect.top = space * 2;
//            outRect.right = space;
        }
    }


}
