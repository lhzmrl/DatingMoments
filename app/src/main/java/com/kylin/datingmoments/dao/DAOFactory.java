package com.kylin.datingmoments.dao;

/**
 * Created by kylin on 16-5-24.
 */
public class DAOFactory {

    public static DAO getDAO(){
        return new AVOSCloudDAO();
    }

}
