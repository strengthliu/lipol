package com.heysound.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.heysound.service.IRedis;

@Service
@Scope("prototype")
public class RedisImpl implements IRedis {

	@Autowired
	private JedisPool jedisPool;
	private Jedis jedis = null;
	private static String star_hash_video_info_ = "star_hash_video_info_";
	private static String star_hash_error = "star_hash_error";
	private static Logger logger = Logger.getLogger(RedisImpl.class.getName());

	public Jedis openRedis() {
		return jedis = jedisPool.getResource();
	}

	public void closeRedis() {
		if (jedis != null) {
			jedis.close();
		}
	}

	/*
	 * 设置直播到hash zilong 2015.10.30 json return 1
	 */
	public int setHashLive(Map<String, String> hm) {
		try {
			openRedis();

			String video_id = hm.get("video_id") == null ? "" : hm.get("video_id");
			if (!video_id.equals("")) {
				String video_type = hm.get("video_type") == null ? "" : hm.get("video_type");
				String flag = hm.get("flag") == null ? "" : hm.get("flag");
				String live_id = hm.get("live_id") == null ? "" : hm.get("live_id");

				Map<String, String> hashMap = new LinkedHashMap<String, String>();
				if (!video_type.equals(""))
					hashMap.put("video_type", hm.get("video_type") + ""); // 视频分类 （1视频，2回放，3预告）
				if (!flag.equals(""))
					hashMap.put("flag", hm.get("flag") + ""); 	// 直播视频状态（0 直播中,1 转码中,2  回放）
				if (!live_id.equals(""))
					hashMap.put("live_id", hm.get("live_id") + ""); // 对应的直播ID
								
				hashMap.put("start_time", hm.get("start_time") + ""); //视频对外开放时间
				jedis.hmset(star_hash_video_info_ + video_id, hashMap);

				return 1;
			
			}
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis addHashLive error:" + e.getMessage());
		} finally {
			closeRedis();
		}
		return 0;
	}
	
	//删除直播key
	public int delLiveKey(String video_id) {
		try{		
			openRedis();
			jedis.del(star_hash_video_info_+video_id);
			return 1;
		}	
		catch(Exception e){
			e.printStackTrace();		
		}	
		finally	{
			closeRedis();
		}
		return 0;
	}
	
	// 获取错误信息
	public String getErrorMsg(int errorId) {

		try {
			openRedis();
			return jedis.hget(star_hash_error, errorId + "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis getErrorMsg error:" + e.getMessage());
		} finally {
			closeRedis();
		}
		return null;

	}

	// 设置错误信息
	public long setErrorMsg(int errorId, String fieldValue) {
		try {
			openRedis();
			return jedis.hset(star_hash_error, errorId + "", fieldValue);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis setErrorMsg error:" + e.getMessage());
		} finally {
			closeRedis();
		}
		return -1;

	}


}
