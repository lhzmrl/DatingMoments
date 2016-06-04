package com.kylin.datingmoments.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.kylin.datingmoments.R;
import com.kylin.datingmoments.entity.Comment;

import java.util.Date;
import java.util.List;

/**
 * Created by kylin on 16-6-4.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.RecyclerViewHolder>{

    private Context mContext;
    private List<Comment> mListComment;

    public CommentAdapter(Context context,List<Comment> comments){
        mContext = context;
        mListComment = comments;
    }

    @Override
    public CommentAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.rvi_comment,null);
        return new RecyclerViewHolder(contentView);
    }

    @Override
    public void onBindViewHolder(CommentAdapter.RecyclerViewHolder holder, int position) {
        Comment comment = mListComment.get(position);
        holder.mSDUserIcon.setImageURI(Uri.parse(comment.getUserIcon()));
        holder.mTvNickName.setText(comment.getUserName());
        holder.mTvComment.setText(comment.getComment());
        holder.mTvTime.setText(convertTime(comment.getCreateTime()));
    }

    @Override
    public int getItemCount() {
        return mListComment.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private SimpleDraweeView mSDUserIcon;
        private TextView mTvNickName;
        private TextView mTvComment;
        private TextView mTvTime;

        public RecyclerViewHolder(View view) {
            super(view);
            mSDUserIcon = (SimpleDraweeView) view.findViewById(R.id.rvi_comment_sd_user_icon);
            mTvNickName = (TextView) view.findViewById(R.id.rvi_comment_tv_nick_name);
            mTvComment = (TextView) view.findViewById(R.id.rvi_comment_tv_comment);
            mTvTime = (TextView) view.findViewById(R.id.rvi_comment_tv_time);
        }
    }

    private String convertTime(Date date){
        Date currentTime = new Date();
        long interval = currentTime.getTime() - date.getTime();
        interval /=1000;
        if (interval==0){
            return "刚刚";
        }else if (interval<60){
            return (interval)+"秒前";
        }else if (interval/60 < 60){
            return (interval/60)+"分钟前";
        }else if (interval/3600 <24){
            return (interval/3600)+"小时前";
        }else if (interval/86400<30){
            return (interval/86400)+"天前";
        }else if (interval/2592000<12){
            return (interval/2592000)+"月前";
        }else{
            return (interval/31104000)+"年前";
        }
    }
}
