package com.yixinintl.utils;


import java.lang.Exception;
import java.lang.System;
import java.lang.Thread;
/**
 * 生成唯一ID
 * 
 * @author chenchun
 * 
 */
public class IDHelper {

	public static int SERVER_ID  = 1; // 默认为1
	public static long ID_BEGIN_TIME = 1309449600000L;
	public static int DB_COUNT = 1;
	/**
	 * 获取主键ID
	 * 
	 * @return
	 * @throws Exception
	 */
	public static synchronized long getUniqueID() throws Exception {
		if (SERVER_ID <= 0) {
			throw new Exception("server id error, please check config file!");
		}

		long destID = System.currentTimeMillis() - ID_BEGIN_TIME;
		destID = (destID << 8) | SERVER_ID;
		Thread.sleep(1);
		return destID;
	}

	/**
	 * 生成唯一ID,该ID的dbIndex与sourceID一至 注：最大支持库 ：512个 最大支持时间：4240-01-01
	 * 
	 * @param sourceID
	 * @return
	 * @throws Exception
	 */
	public static synchronized long getUniqueID(long sourceID) throws Exception {
		if (SERVER_ID <= 0) {
			throw new Exception("server id error, please check config file!");
		}

		int sourceIndex = getDBIndex(sourceID);
		long destID = System.currentTimeMillis() - ID_BEGIN_TIME;
		destID = (destID << 9) | sourceIndex;
		destID = (destID << 8) | SERVER_ID;
		Thread.sleep(1);
		return destID;
	}

	/**
	 * 获取ID所对应该的数据库编号
	 * 
	 * @param ID
	 * @return 数据库
	 */
	public static int getDBIndex(long id) {
		return (int) ((id >> 8) & (DB_COUNT - 1));
	}
}