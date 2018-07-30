package com.heysound.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.heysound.service.ISmsService;
import com.heysound.util.HttpSend;
import com.heysound.util.MsgUtil;
import com.heysound.util.TestinMsgUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class SmsServiceImpl implements ISmsService {
	
	@Value("${zucp.sn}")
    private   String zucpSn;
	@Value("${zucp.pwd}")
    private   String zucpPwd;
	
	@Value("${zucp.msg}")
    private   String zucpMsg;
	
	@Value("${zucp.msg.send.order}")
	private String zucpMsgSend;//已支付订单发货提示用户短信内容
	
	//testin smsmsg
	@Value("${testin.apiKey}")
    private   String apiKey;
	
	@Value("${testin.apiSecretkey}")
    private   String apiSecretkey;
	
	@Value("${testin.templateId}")
    private   String templateId;
	
	private static Logger logger = Logger
			.getLogger(SmsServiceImpl.class.getName());
	@Override
	public String sendMsgWithValidateCode(String phone) throws Exception{
		String sn =zucpSn; //"SDK-WSS-010-08482";// 北京漫道科技
		String pwd =zucpPwd; //"512f-0Ec";
		int mobile_code = 0;

		MsgUtil msgUtil;
		try {
			msgUtil = new MsgUtil(sn, pwd);
			mobile_code = (int) ((Math.random() * 9 + 1) * 1000);// 4位
			String content = zucpMsg.replaceAll("label", mobile_code+"");
					//new String("您的验证码是：" + mobile_code
					//+ "。请不要把验证码泄露给其他人。【杭州海笙乐科技】");
			// 短信发送
			content = java.net.URLEncoder.encode(content, "utf-8");
			String returnId = msgUtil.mdsmssend(phone, content, "", "", "", "");
			logger.info("短信返回code==" + returnId);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error("短信异常信息==" + e.getMessage());
			throw e;
		}
		return mobile_code + "";
	}

	@Override
	public String sendMsgWithValidateCodeByHuaXin(String phone) throws Exception{
		String strReg = "101100-WEB-HUAX-204117"; // 注册号（由华兴软通提供）
		String strPwd = "LUWRMPVT";
		String strSourceAdd = ""; // 子通道号，可为空（预留参数一般为空）
		String strSmsUrl = "http://www.stongnet.com/sdkhttp/sendsms.aspx";
		int mobile_code = (int) ((Math.random() * 9 + 1) * 1000);
		try {
			String strContent = HttpSend.paraTo16(new String("您的验证码是："
					+ mobile_code + "。请不要把验证码泄露给其他人。【杭州海笙乐科技】"));
			String strSmsParam = "reg=" + strReg + "&pwd=" + strPwd
					+ "&sourceadd=" + strSourceAdd + "&phone=" + phone
					+ "&content=" + strContent;
			String returnId = HttpSend.postSend(strSmsUrl, strSmsParam);
			logger.info("短信返回code==" + returnId);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error("短信异常信息==" + e.getMessage());
			throw e;
		}
		return mobile_code + "";
	}

	
	
	@Override
	public String sendMsgWithValidateCodeByTestIn(String phone)
			throws Exception {
		  JSONObject jsonObj = new JSONObject();
		  int mobile_code = (int) ((Math.random() * 9 + 1) * 1000);// 4位
          jsonObj.put("op", "Sms.send");
          jsonObj.put("apiKey", apiKey);
          jsonObj.put("ts", System.currentTimeMillis()+"");
          jsonObj.put("phone", phone);
          jsonObj.put("templateId", templateId);
          jsonObj.put("content", mobile_code+"");
          jsonObj.put("sig", TestinMsgUtil.getSig(jsonObj, apiSecretkey));
          String url = "http://api.sms.testin.cn/sms";
          String result = TestinMsgUtil.transmessage(url, jsonObj.toString());
          logger.info("短信返回code==" + result);
		  return mobile_code+"";
	}

	@Override
	public String sendMsgOfPaidOrder(String phone) throws Exception {
		String sn =zucpSn; //"SDK-WSS-010-08482";// 北京漫道科技
		String pwd =zucpPwd; //"512f-0Ec";
		int mobile_code = 0;

		MsgUtil msgUtil;
		try {
			msgUtil = new MsgUtil(sn, pwd);
			String content = zucpMsgSend;
					//new String("您的验证码是：" + mobile_code
					//+ "。请不要把验证码泄露给其他人。【杭州海笙乐科技】");
			// 短信发送
			content = java.net.URLEncoder.encode(content, "utf-8");
			String returnId = msgUtil.mdsmssend(phone, content, "", "", "", "");
			logger.info("提示发货短信返回code==" + returnId);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error("提示发货短信异常信息==" + e.getMessage());
			throw e;
		}
		return mobile_code + "";
	}

}
