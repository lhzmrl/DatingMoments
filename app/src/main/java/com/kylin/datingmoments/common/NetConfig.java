package com.kylin.datingmoments.common;

public class NetConfig {
	public static final String SERVICE_IP = "192.168.1.102:8080";
	public static final String SERVICE_URL = "http://" + SERVICE_IP
			+ "/axis2/services/DatingMomentsServer?wsdl";
	/** 命名空间  */
	public static final String NAME_SPACE = "http://server.datingmoments.kylin.com";

	public static final String VIDEO_SERVER = SERVICE_IP +"/DatingMomentsVideoServer/";
}
