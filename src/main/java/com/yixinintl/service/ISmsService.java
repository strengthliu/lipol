package com.yixinintl.service;

public interface ISmsService {
	public  String sendMsgWithValidateCode(String phone) throws Exception;
	
	public  String sendMsgOfPaidOrder(String phone) throws Exception;
	
	public  String sendMsgWithValidateCodeByHuaXin(String phone) throws Exception;
	
	public String sendMsgWithValidateCodeByTestIn(String phone) throws Exception;
	
}
