package com.yixinintl.util;

import com.alibaba.fastjson.JSONObject;
import com.yixinintl.exception.ErrorMsgException;
import com.yixinintl.service.IRedisService;
import com.yixinintl.util.Constants;
import com.yixinintl.util.MD5;
import com.yixinintl.util.MyComparator;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

@Component
public class PublicVerify {

	@Autowired
	private IRedisService redisService;
	@Autowired
	private ApplicationContext context;
    private static Logger logger = Logger.getLogger(PublicVerify.class.getName());
	private static volatile PublicVerify instance = null;

	public Map transObjToMap(Object obj) throws IllegalAccessException {
		Map map = new HashMap();
		if(obj instanceof HashMap){
			return (Map) obj;
		}
		for(Class<?> clazz = obj.getClass() ; clazz != Object.class ; clazz = clazz.getSuperclass()){
			Field[] fs = clazz.getDeclaredFields();
			for(int i = 0 ; i < fs.length; i++) {
				Field f = fs[i];
				f.setAccessible(true);
				Object val = f.get(obj);//得到此属性的值
				map.put(f.getName(),f.get(obj));
//                String type = f.getType().toString();//得到此属性的类型
			}
		}
		return map;
	}

	public boolean newCheckSign(Object obj, long time){
		try {
			Map<String, String> params = transObjToMap(obj);
			System.out.println(params);
			String appkey = params.get(Constants.APPKEY);
			String app_secret=redisService.getAuthString(appkey);
			//secret是否存在
			if (app_secret == null || app_secret.equals("")) {
				throw new ErrorMsgException(ActionResult.SYS_9006.getValue());
			}
			//请求是否超时
			if (StringHelper.isGreaterThan(time)) {
				throw new ErrorMsgException(ActionResult.SYS_9004.getValue());
			}
			String sign = params.get(Constants.SIGN);
			params.remove(Constants.APPKEY);
			params.remove("time");
			params.remove(Constants.SIGN);
			List<Map.Entry<String,?>> mappingList = new ArrayList<Map.Entry<String,?>>(params.entrySet());
			Collections.sort(mappingList, new MyComparator(mappingList.get(0)));
			StringBuilder sb = new StringBuilder();
			sb.append(appkey);
			sb.append(time + "");
			Map newObj = new LinkedHashMap();
			for(Map.Entry e : mappingList){
				if(e.getValue()!=null && !e.getKey().equals("app_key")
						&& !e.getKey().equals("sign") && !e.getKey().equals("time"))
					e.getValue();
					newObj.put((String)e.getKey(), e.getValue());
			}
			sb.append(JSONObject.toJSONString(newObj));
			sb.append(redisService.getAuthString(appkey));
			System.out.println("签名前：" + sb.toString());
			System.out.println("签名后：" + DigestUtils.md5Hex(sb.toString()));
			System.out.println("参数sign：" + sign);
			if(sign.equals(MD5.GetMD5Code(sb.toString()))){
				return true;
			}else{
				throw new ErrorMsgException(ActionResult.SYS_9005.getValue());
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new ErrorMsgException(ActionResult.SYS_9005.getValue());
		}
	}


	/**
	 *升序参数，进行签名验证
	 * @param appKey
	 * @param sign
	 * @param time
	 * @param params
	 * @return
	 * @throws ErrorMsgException
	 */
	public  boolean checkSignValid(String appKey, String sign, long time,
								   Map<String, String> params) throws ErrorMsgException {
		IRedisService iredis = redisService;
		String app_secret=iredis.getAuthString(appKey);
		//secret是否存在
		if (app_secret == null || app_secret.equals("")) {
			throw new ErrorMsgException(ActionResult.SYS_9006.getValue());
		}

		//请求是否超时
		if (StringHelper.isGreaterThan(time)) {
			throw new ErrorMsgException(ActionResult.SYS_9004.getValue());
		}

		List<Map.Entry<String,String>> mappingList = new ArrayList<Map.Entry<String,String>>(params.entrySet());
		Collections.sort(mappingList, new MyComparator(mappingList.get(0)));

		StringBuilder sb = new StringBuilder();
		sb.append(appKey);
		sb.append(time);
		for(Map.Entry e : mappingList){
			sb.append(e.getValue());//由于JDK版本(或者spring版本)不一样，此处getValue的返回值可以是string和string[]两种。当前项目是string[]
		}
		sb.append(iredis.getAuthString(appKey));
		//签名是否一致
		System.out.println("签名钱：" + sb.toString());
		String serverSign = DigestUtils.md5Hex(sb.toString());
		System.out.println("签名后：" + serverSign);
		System.out.println("客户端签名：" + sign);
		if (sign == null || !serverSign.equals(sign)) {
			throw new ErrorMsgException(ActionResult.SYS_9005.getValue());
		}

		return true;
	}

	// 验证签名
	public  boolean checkSignValid(String appKey, String sign, long time,
			String params) throws ErrorMsgException {
		String app_secret=redisService.getAuthString(appKey);
		//secret是否存在
		if (app_secret == null || app_secret.equals("")) {
			throw new ErrorMsgException(ActionResult.SYS_9006.getValue());
		}

		//请求是否超时
		if (StringHelper.isGreaterThan(time)) {
			throw new ErrorMsgException(ActionResult.SYS_9004.getValue());
		}

		//签名是否一致
		String serverSign = DigestUtils.md5Hex(appKey + time + params + redisService.getAuthString(appKey));
		if (sign == null || !serverSign.equals(sign)) {
			throw new ErrorMsgException(ActionResult.SYS_9005.getValue());
		}

		return true;
	}
	
	//token是否失效
	public  void isUserValid(JSONObject json) throws ErrorMsgException {		
		if (json == null) {
			logger.error("jsonData为空");
			throw new ErrorMsgException(ActionResult.SYS_9001.getValue());
		}
		String token = redisService.getUserToken(json.getLong("user_id"));
		if (token == null || !token.equals(json.getString("token"))) {
			logger.error("用户token失效");
			throw new ErrorMsgException(ActionResult.SYS_9002.getValue());
		}
	}

	public JSONObject generateSign2SimpleJson(Object object){
		return (JSONObject) JSONObject.toJSON(object);
	}

	public StringBuffer generateSign2String(Object object,String appKey, String appSecrt ,String time){
		StringBuffer stringBuffer = new StringBuffer();
		JSONObject jsonObject = (JSONObject) JSONObject.toJSON(object);
		stringBuffer.append(appKey).append(time).append(jsonObject.toJSONString()).append(appSecrt);
		return stringBuffer;
	}
	public JSONObject generateSign2JsonObject(Object object,String appKey, String sign ,String time){
		JSONObject jsonObject = new JSONObject();
		String simpleName = object.getClass().getSimpleName();
		String first = simpleName.substring(0,1).toLowerCase();
		String body = simpleName.substring(1);
		jsonObject.put(first+body,object);
		jsonObject.put("appKey",appKey);
		jsonObject.put("sign",sign);
		jsonObject.put("time",time);

		return jsonObject;
	}

	public Map generateSign2Map(Object object,String appKey, String sign ,String time,String objName){
		Map jsonObject = new HashMap();
		String simpleName = object.getClass().getSimpleName();
		String first = simpleName.substring(0,1).toLowerCase();
		String body = simpleName.substring(1);
		String keyName = first+body;
		if(org.springframework.util.StringUtils.hasText(objName)){
			keyName = objName;
		}
		jsonObject.put(keyName,object);
		jsonObject.put("app_key",appKey);
		jsonObject.put("sign",sign);
		jsonObject.put("time",time);

		return jsonObject;
	}


}
