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
import com.kylin.datingmoments.entity.VideoInfo;
import com.kylin.datingmoments.util.Logger;

import java.util.List;

/**
 * Created by Kylin_admin on 2016/4/6.
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.RecyclerViewHolder>{

    private Context mContext;
    private List<VideoInfo> mListVideo;

    private OnCoverClickListener mOnCoverClickListener;

    public VideoAdapter(Context context, List<VideoInfo> listImageDetail, OnCoverClickListener onCoverClickListener){
        this.mContext = context;
        this.mListVideo = listImageDetail;
        mOnCoverClickListener = onCoverClickListener;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.rvi_video_info,null);
        return new RecyclerViewHolder(contentView);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, final int position) {
        VideoInfo videoInfo = mListVideo.get(position);
        String path = Uri.parse(videoInfo.getCoverPath()).toString();
        Logger.e(path);
        holder.mSDVCover.setImageURI(Uri.parse(videoInfo.getCoverPath()));
        holder.mSDVIcon.setImageURI(Uri.parse(videoInfo.getUserIcon()));
        holder.mTvNickName.setText(videoInfo.getUserName());
        if ("".equals(videoInfo.getDesc()))
            holder.mTvDesc.setVisibility(View.GONE);
        else {
            holder.mTvDesc.setVisibility(View.VISIBLE);
            holder.mTvDesc.setText(videoInfo.getDesc());
        }
        if (mOnCoverClickListener!=null) {
            holder.mSDVCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnCoverClickListener.onClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mListVideo.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private SimpleDraweeView mSDVCover;
        private SimpleDraweeView mSDVIcon;
        private TextView mTvNickName;
        private TextView mTvDesc;

        public RecyclerViewHolder(View view) {
            super(view);
            mSDVCover = (SimpleDraweeView) view.findViewById(R.id.rvi_video_info_sdv_cover);
            mSDVIcon = (SimpleDraweeView) view.findViewById(R.id.rvi_video_info_sdv_icon);
            mTvNickName = (TextView) view.findViewById(R.id.rvi_video_info_tv_nickname);
            mTvDesc = (TextView) view.findViewById(R.id.rvi_video_info_tv_desc);
        }
    }

    public interface OnCoverClickListener{
        public void onClick(int position);
    }
}
