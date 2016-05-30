package com.kylin.datingmoments.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ksoap2.serialization.SoapPrimitive;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.kylin.datingmoments.app.DMApplication;
import com.kylin.datingmoments.common.NetConfig;
import com.kylin.datingmoments.util.NetWorkUtils;

public class DataLogic extends WebServiceClient {

	private static final String TAG = "DataLogic";

	private static final String METHOD_GET_CON_SITE_DETAIL = "getConstructionSiteDetail";

	private static final String PARAMETER_ConstructionSite_ID = "id";

	private static DMApplication mbaBaseApplication;
	private static DataLogic instance;

	private DataLogic() {
		super();
	}

	public static DataLogic getInstance() {
		if (instance == null) {
			instance = new DataLogic();
		}
		return instance;
	}

	public static DMApplication getMbaBaseApplication() {
		return mbaBaseApplication;
	}

	public static void setBaseApplication(DMApplication mbaBaseApplication) {
		DataLogic.mbaBaseApplication = mbaBaseApplication;
	}

	@Override
	protected String getServiceUrl() {
		return NetConfig.SERVICE_URL;
	}

	@Override
	protected boolean checkNet() {
		if (new NetWorkUtils(mbaBaseApplication.getApplicationContext())
				.getConnectState() == NetWorkUtils.NetWorkState.NONE) {
			netHandler.sendMessage(new Message());
			return false;
		}
		return true;
	}

	private Handler netHandler = new Handler(mbaBaseApplication
			.getApplicationContext().getMainLooper()) {
		public void handleMessage(Message msg) {
			mbaBaseApplication.showCustomToast("无网络连接！");
		};
	};

	/**
	 * 
	 * @param videoPath
	 * @param thumbnailPath
	 * @return
	 */
	public String uploadVideo(String videoPath, String thumbnailPath) {
		String result = null;
		File videoFile = new File(videoPath);
		File thumbnailFile = new File(thumbnailPath);
		if (!videoFile.exists() || !thumbnailFile.exists())
			return null;
		String videoType = videoPath.subSequence(videoPath.lastIndexOf("."),
				videoPath.length()).toString();
		String thumbnailType = thumbnailPath.subSequence(
				thumbnailPath.lastIndexOf("."), thumbnailPath.length())
				.toString();
		byte[] videoBytes = getBytesFromFile(videoFile);
		byte[] thumbnailBytes = getBytesFromFile(thumbnailFile);

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("videoBytes", videoBytes);
		map.put("videoType", videoType);
		map.put("thumbnailBytes", thumbnailBytes);
		map.put("thumbnailType", thumbnailType);
		SoapPrimitive soap = postData("saveVideo", map);
		result = soap == null ? null : soap.toString();
		if (result != null)
			Log.e(TAG, result);
		return result;
	}

	/**
	 * 返回一个byte数组
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private byte[] getBytesFromFile(File file) {
		byte[] buffer = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			// 创建保存要上传的图像文件内容的字节数组
			buffer = new byte[fis.available()];
			// 将输入流fis中的数据读入字节数组buffer中
			fis.read(buffer);
			// 关闭流
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer;
	}

}
