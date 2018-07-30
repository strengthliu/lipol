package com.yixinintl.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yixinintl.model.StarBaseVO;

public interface IRedisService {	
	
	public String getUserCode(String phone);
	public String setUserCode(String phone,String code);
	public void delUserCode(String phone);
	public Map<String, String> getUserInfo(long user_Id);		
	public int setUserInfo(StarBaseVO starBaseVO);
	public int setUserInfo(long user_Id, String nickname, String picURL);
	
////  ================================================================
//
//	
//	/***************用户*******************/	
//	public String getUserField(long user_id,String fieldName);
//	public long setUserField(long user_id,String fieldName,String fieldValue);	
//	
//	/***************其他*******************/	
//	public long setErrorMsg(int errorId,String fieldValue);
//	public String getErrorMsg(int errorId);		
//	public int setErrorMsgNew(String msg);
//	public String getErrorMsgNew();		
//	public String setUserToken(long user_id,String token);
//	public String getUserToken(long user_id);	
//	public String getAuthString(String appKey);
//	public String setAuthString(String appKey,String fieldValue);
//	public String setDLurl(String video_id,String urlJson);
//
//	/****************登录*****************/
//	public void setLoginToken(String token, String userinfo);
//	public String getLoginToken(String userinfo);
//
//
//	/**
//	 * 用户token分端逻辑，app的ios和android端与h5端可同时登录
//	 * by 小芊 
//	 * 2017年7月23日下午12:59:47
//	 * @param user_id
//	 * @param token 欲设置的token值
//	 * @param end 终端appkey识别字符串 可选值为
//	 * （iosApp：ios_6c0ed5b15c21f77b，androidApp:android_f89c88a914e2d69c,androidH5:androidM_f89c88a914e2d69c,
//	 * iosH5:iosM_6c0ed5b15c21f77b,未知设备的H5:M_4d37f18c9cab1ec6）
//	 * @return
//	 */
//	public Long setUserToken(long user_id,String token, String end);
//	public String getUserToken(long user_id, String end);
//
//	/**
//	 * 用户登录成功后从token中分离openid并放入缓存，方便第三方登录后微信支付使用openid
//	 * by 黑羽
//	 * 2017年07月24日18:48:19
//	 */
//	public String getOpenId(long user_id);
//	public void setOpenId(String open_id ,long user_id);
//
//	/**
//	 * 2017年09月01日
//	 * by GZY
//	 * @return
//	 */
//	public String getDefaultProfit();
}
