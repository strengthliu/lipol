package com.yixinintl.model;

/**
 * 客户端提交时，服务器收到的安全信息。
 * 已经转为接口，这个不用了。
 * @Deprecated
 * 
 */
public class SecretInfo {

	public String app_key = "";

	public String sign = "";

	public long time = 0;

	public UserMeta user_meta = null;

}
