package com.kylin.datingmoments.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

public abstract class EndlessRecyclerOnScrollListener extends
        RecyclerView.OnScrollListener {

    private int previousTotal = 0;
    private boolean loading = true;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    private int currentPage = 1;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;

    public EndlessRecyclerOnScrollListener(
            StaggeredGridLayoutManager staggeredGridLayoutManager) {
        this.mStaggeredGridLayoutManager = staggeredGridLayoutManager;
    }

    @Override

    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);


//        visibleItemCount = recyclerView.getChildCount();
//
//        totalItemCount = mLinearLayoutManager.getItemCount();
//
//        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
//
//
//        if (loading) {
//
//            if (totalItemCount > previousTotal) {
//
//                loading = false;
//
//                previousTotal = totalItemCount;
//
//            }
//
//        }
//
//        if (!loading
//
//                && (totalItemCount - visibleItemCount) <= firstVisibleItem) {
//
//            currentPage++;
//
//            onLoadMore(currentPage);
//
//            loading = true;
//
//        }

    }


    public abstract void onLoadMore(int currentPage);

}
