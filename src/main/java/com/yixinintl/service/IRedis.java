package com.yixinintl.service;

import java.util.Map;

import redis.clients.jedis.Jedis;


public interface IRedis {
	
	
	public Jedis openRedis();
	public void closeRedis();
	
	public String getErrorMsg(int errorId);
	public long setErrorMsg(int errorId, String fieldValue);	
	/*
	 * zilong
	 * 2015.10.30
	 * */
	public int setHashLive(Map<String, String> hm);	
	public int delLiveKey(String video_id);

}
