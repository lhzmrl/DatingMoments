package com.kylin.datingmoments.util;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;

public class NetWorkUtils {
	private Context mContext;
	public State wifiState = null;
	public State mobileState = null;

	public NetWorkUtils(Context context) {
		mContext = context;
	}

	public enum NetWorkState {
		WIFI, MOBILE, NONE;

	}

	public NetWorkState getConnectState() {
		ConnectivityManager manager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		manager.getActiveNetworkInfo();
		wifiState = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		try {
			mobileState = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
					.getState();
		} catch (Exception e) {
			mobileState = wifiState;
		}
		if (wifiState != null && mobileState != null
				&& State.CONNECTED != wifiState
				&& State.CONNECTED == mobileState) {
			return NetWorkState.MOBILE;
		} else if (wifiState != null && mobileState != null
				&& State.CONNECTED != wifiState
				&& State.CONNECTED != mobileState) {
			return NetWorkState.NONE;
		} else if (wifiState != null && State.CONNECTED == wifiState) {
			return NetWorkState.WIFI;
		}
		return NetWorkState.NONE;
	}

}
