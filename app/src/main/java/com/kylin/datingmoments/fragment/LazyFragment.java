package com.kylin.datingmoments.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

/**
 * <h1>懒加载Fragment</h1> 只有创建并显示的时候才会调用onCreateViewLazy方法<br>
 * <br>
 * 
 * 懒加载的原理onCreateView的时候Fragment有可能没有显示出来<br>
 * 但是调用到setUserVisibleHint(boolean isVisibleToUser),isVisibleToUser =
 * true的时候就说明有显示出<br>
 * 但是要考虑onCreateView和setUserVisibleHint的先后问题所以才有了下面的代码
 * 
 * 注意<br>
 * 1》原先的Fragment的回调方法名字后面要加个Lazy，比如Fragment的onCreateView方法就写成onCreateViewLazy <br>
 * 2》使用该LazyFragment会导致多一层布局深度
 *
 */
public class LazyFragment extends BaseFragment {
	/**标志初始化数据是否完成*/
	private boolean isInit = false;
	private Bundle savedInstanceState;
	public static final String INTENT_BOOLEAN_LAZYLOAD = "intent_boolean_lazLoad";
	private boolean isLazyLoad = true;
	private FrameLayout layout;
	private boolean isStart = false;

	@Deprecated
	protected final void onCreateView(Bundle savedInstanceState) {
		super.onCreateView(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle != null) {
			isLazyLoad = bundle.getBoolean(INTENT_BOOLEAN_LAZYLOAD, isLazyLoad);
		}
		if (isLazyLoad) {
			//如果可见并且是第一次加载，加载布局，否则使用默认布局先返回
			if (getUserVisibleHint() && !isInit) {
				isInit = true;
				this.savedInstanceState = savedInstanceState;
				onCreateViewLazy(savedInstanceState);
			} else {
				layout = new FrameLayout(getApplicationContext());
				layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				super.setContentView(layout);
			}
		} else {
			isInit = true;
			onCreateViewLazy(savedInstanceState);
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		//如果用户可见并且数据没有被初始化完成，并且布局不为空，加载数据
		if (isVisibleToUser && !isInit && getContentView() != null) {
			isInit = true;
			onCreateViewLazy(savedInstanceState);
			onResumeLazy();
		}
		//如果初始化完成了并且布局不为空，并且用户可见，执行onFragmentStartLazy更新数据？否则保存数据？
		if (isInit && getContentView() != null) {
			if (isVisibleToUser) {
				isStart = true;
				onFragmentStartLazy();
			} else {
				isStart = false;
				onFragmentStopLazy();
			}
		}
	}

	@Deprecated
	@Override
	public final void onStart() {
		super.onStart();
		if (isInit && !isStart && getUserVisibleHint()) {
			isStart = true;
			onFragmentStartLazy();
		}
	}

	@Deprecated
	@Override
	public final void onStop() {
		super.onStop();
		if (isInit && isStart && getUserVisibleHint()) {
			isStart = false;
			onFragmentStopLazy();
		}
	}

	/**初始化完成之后重新显示时获得更新数据*/
	protected void onFragmentStartLazy() {

	}

	/**初始化完成之后失去显示保存数据*/
	protected void onFragmentStopLazy() {

	}

	/**
	 * 初始化数据，生成视图
	 * @param savedInstanceState
	 */
	protected void onCreateViewLazy(Bundle savedInstanceState) {

	}

	protected void onResumeLazy() {

	}

	protected void onPauseLazy() {

	}

	protected void onDestroyViewLazy() {

	}

	@Override
	public void setContentView(int layoutResID) {
		if (isLazyLoad && getContentView() != null && getContentView().getParent() != null) {
			layout.removeAllViews();
			View view = inflater.inflate(layoutResID, layout, false);
			layout.addView(view);
		} else {
			super.setContentView(layoutResID);
		}
	}

	@Override
	public void setContentView(View view) {
		if (isLazyLoad && getContentView() != null && getContentView().getParent() != null) {
			layout.removeAllViews();
			layout.addView(view);
		} else {
			super.setContentView(view);
		}
	}

	@Override
	@Deprecated
	public final void onResume() {
		super.onResume();
		if (isInit) {
			onResumeLazy();
		}
	}

	@Override
	@Deprecated
	public final void onPause() {
		super.onPause();
		if (isInit) {
			onPauseLazy();
		}
	}

	@Override
	@Deprecated
	public final void onDestroyView() {
		super.onDestroyView();
		if (isInit) {
			onDestroyViewLazy();
		}
		isInit = false;
	}
	
	public boolean isInit(){
		return isInit;
	}
}
