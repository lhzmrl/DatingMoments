package com.kylin.datingmoments.dao;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.kobjects.base64.Base64;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.kylin.datingmoments.common.NetConfig;

public abstract class WebServiceClient {

	/**
	 * 获取WSDL文档文档地址
	 * @return
	 */
	protected abstract String getServiceUrl();

	protected abstract boolean checkNet();

	public SoapPrimitive getData(String methodName, Map<String, String> params) {
		if (checkNet() == false) {
			return null;
		}

		SoapPrimitive response = null;
		try {
			String strNameSpaceString = NetConfig.NAME_SPACE;
			String strMethodNameString = methodName;
			String strUrl = getServiceUrl();
			String saopActionString = strMethodNameString.endsWith("/") ? (strNameSpaceString + strMethodNameString)
					: (strNameSpaceString + "/" + strMethodNameString);

			SoapObject request = buildRequest(strNameSpaceString, methodName,
					params);

			
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.dotNet = false;
			envelope.setOutputSoapObject(request);
			envelope.bodyOut = request;
			envelope.encodingStyle = SoapSerializationEnvelope.ENC;

			// 是wsdl文档地址
			HttpTransportSE ht = new HttpTransportSE(strUrl);
			// 请求地址及方法   请求
			ht.call(saopActionString, envelope);

			if (envelope.getResponse() != null) {
				response = (SoapPrimitive) envelope.getResponse();
				Log.i("response.toString()", response.toString());
			}
		} catch (SoapFault e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} 

		return response;
	}

	public SoapPrimitive postData(String methodName, Map<String, Object> params) {
		if (checkNet() == false) {
			return null;
		}

		SoapPrimitive response = null;
		try {
			String strNameSpaceString = NetConfig.NAME_SPACE;
			String strMethodNameString = methodName;
			String strUrl = getServiceUrl();
			String saopActionString = strMethodNameString.endsWith("/") ? (strNameSpaceString + strMethodNameString)
					: (strNameSpaceString + "/" + strMethodNameString);

			SoapObject request = new SoapObject(strNameSpaceString, methodName);
			Iterator<Entry<String, Object>> iterator = params.entrySet()
					.iterator();
			while (iterator.hasNext()) {
				Entry<?, ?> entry = iterator.next();
				String key = (String) entry.getKey();
				Object value = (Object) entry.getValue();

				if (value instanceof String) {
					request.addProperty(key, value);
				} else {
					String strData = new String(Base64.encode((byte[]) value));
					request.addProperty(key, new SoapPrimitive(
							SoapEnvelope.ENC, "base64Binary", strData));
				}

				Log.d("webservice-getdata" + key, String.valueOf(value));
			}

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.dotNet = false;
			envelope.setOutputSoapObject(request);
			envelope.bodyOut = request;
			envelope.encodingStyle = SoapSerializationEnvelope.ENC;

			HttpTransportSE ht = new HttpTransportSE(strUrl);
			ht.call(saopActionString, envelope);

			if (envelope.getResponse() != null) {
				response = (SoapPrimitive) envelope.getResponse();
				Log.i("response.toString()", response.toString());
			}
		} catch (SoapFault e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

		return response;
	}

	/**
	 * 建立请求
	 * @param strNameSpaceString 
	 * @param methodName 方法名
	 * @param params 参数键值对
	 * @return
	 */
	private SoapObject buildRequest(String strNameSpaceString,
			String methodName, Map<String, String> params) {
		SoapObject soapObject = new SoapObject(strNameSpaceString, methodName);
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<?, ?> entry = iterator.next();
			String key = (String) entry.getKey();
			Object value = (Object) entry.getValue();
			String s=null;
			s = Base64.encode(new byte[9]);
			soapObject.addProperty(key, value);

			Log.d("webservice-getdata" + key, String.valueOf(value));
		}
		return soapObject;
	}
}
