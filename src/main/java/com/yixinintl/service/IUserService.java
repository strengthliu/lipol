package com.yixinintl.service;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

public interface IUserService {
	
	JSONObject checkInputParamsNull(String ...params);
	
	JSONObject checkPassword(String account,String password);
	
	JSONObject getUserList(int offset);
	
	JSONObject getUserListByCondition(int offset,String condition,String timestart,String timeend);
	
	JSONObject updateUserInfo(String nickname,String id,String sex,String age,String phone, String pwd, String sign, String picUrl, String location);
	
	JSONObject getVIPUserList(int offset);
	
	JSONObject getVIPUserListByCondition(int offset,String condition,String timestart,String timeend);
	
	void checkToken(JSONObject json);

	Charge getCharge4Shop(String uid, ChargeVo chargeVo, String ctype) throws Exception;
	
	StarOrderInfo checkNormalOrder(ChargingMode chargingMode , CheckVo checkVo, String uid);

	StarOrderInfo generateVideoOrder(ChargeVo chargeVo, String uid, String ctype) throws Exception;

	JSONObject accountLogin(LoginVO loginVO) throws ErrorMsgException;

	JSONObject checkRealName(String url, Map<String, Object> params) throws Exception;

	List<FollowView> getFollowing(int pageNum, String uid);

	List<Follow> getFollowingAll(String uid);

	JSONObject thirdLogin(ThirdPartyLoginVo loginVo,int type) throws ErrorMsgException;

	void bindRegistrationId(Long userId, String rId , Byte rType);

	JSONObject accountLogin(AccountLoginVo loginVo) throws ErrorMsgException;

	JSONObject commonThirdRegister(Integer type, String id, String nickname, String avatar, byte device_type, String device_token, String appKey);

	Charge charge4Shop(ChargeVo chargeVo, StarUser starUser, String paymentAmount) throws Exception;
	
	//返回品牌logo和月份和关注数
	List<StarInfo> findBrandLogoAndMonthAndFollowing(Long starId);

	Charge charge4Crowdfunding(ChargeVo chargeVo, StarUser starUser, String paymentAmount) throws Exception;
	
//	获取用户历史代言人列表，按时间倒排，可分页
//	List<StarRepresent> userRepresents();
}
