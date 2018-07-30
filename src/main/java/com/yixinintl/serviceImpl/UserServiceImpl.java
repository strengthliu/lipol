package com.heysound.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.heysound.domain.*;
import com.heysound.integration.qcloud.video.common.MD5;
import com.heysound.mapper.*;
import com.heysound.model.*;
import com.heysound.service.IRedisService;
import com.heysound.service.IUserService;
import com.heysound.util.*;
import com.heysound.util.type.*;
import com.heysound.vo.ChargingMode;
import com.heysound.vo.FollowView;
import com.heysound.vo.LoginVO;
import com.heysound.vo.StarBaseVO;
import com.heysound.vo.StarInfo;
import com.mysql.cj.core.util.StringUtils;
import com.pingplusplus.Pingpp;
import com.pingplusplus.model.Charge;
import com.superstar.hsy.util.UserTypeEnum;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

@Service
public class UserServiceImpl implements IUserService {
	private static Logger logger = Logger.getLogger(UserServiceImpl.class
			.getName());
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private BopsUserMapper bopsusemapper;

	@Autowired
	private StarUserMapper starusermapper;

	@Autowired
	private StarOrderInfoMapper orderinfomapper;

	@Autowired
	private StarUserDeviceMapper starUserDeviceMapper;
	
	@Autowired
	private PublicVerify publicVerify;

	@Autowired
	private IRedisService redisService;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private StarUserMapper starUserMapper;

	@Autowired
	private ShopShopMapper shopShopMapper;

	@Autowired
	private StarPayLogMapper starPayLogMapper;

	@Autowired
	private StarOrderInfoMapper starOrderInfoMapper;
	
	@Value("${add.months}")
	private int months;
	@Value("${oss.user.headurl}")
	private String headUrl;
	@Value("${realnamekey}")
	private String realnamekey;
	@Override
	public JSONObject checkInputParamsNull(String... params) {
		JSONObject ret = new JSONObject();

		for (int i = 0; i < params.length; i++) {
			if (StringUtils.isNullOrEmpty(params[i])) {
				ret.put("error", EActionResult.EMPTY_PARAM.getValue());
				ret.put("msg", EActionResult.EMPTY_PARAM.getStr());
				return ret;
			}
		}

		return ret;
	}

	@Override
	public JSONObject checkPassword(String account, String password) {
		JSONObject ret = new JSONObject();

		BopsUser user = bopsusemapper.selectByAccount(account.trim());
		if (user == null) {
			ret.put("error", EActionResult.ERROR_ACCOUNT.getValue());
			ret.put("msg", EActionResult.ERROR_ACCOUNT.getStr());
			return ret;
		}

		if (!StringHelper.md5ApacheCommonsCodec(password).equals(
				user.getPassword())) {
			ret.put("error", EActionResult.ERROR_PWD.getValue());
			ret.put("msg", EActionResult.ERROR_PWD.getStr());
			return ret;
		}

		ret.put("uid", user.getUid());
		ret.put("uname", user.getAccount());
		ret.put("success", true);

		return ret;
	}

	private JSONArray encodeStarUserList(List<StarUser> userlist) {
		JSONArray arr = new JSONArray();

		if (userlist == null || userlist.size() == 0) {
			return null;
		}

		for (StarUser user : userlist) {
			JSONObject obj = new JSONObject();
			obj.put("nickname", user.getNickname());
			obj.put("ID", user.getUserId());
			obj.put("sex", ESexType.getSexType(user.getSex()));
			obj.put("age", user.getAge());
			obj.put("heroscope", user.getHoroscope());
			obj.put("sign", user.getSign());

			//测试登陆方式是否显示中文
			obj.put("registertime", StringHelper.dateToString(
					user.getCreateTime(), "yyyy-MM-dd HH:mm"));
			obj.put("lastlogintime", StringHelper.dateToString(
					user.getLastLoginTime(), "yyyy-MM-dd HH:mm"));
			obj.put("lastloginway",
					ELoginType.getLoginType(user.getThirdPartyType()));
			obj.put("isvip", false);
			obj.put("vipexpiretime",
					user.getVipValidity() == null ? null : StringHelper
							.dateToString(user.getVipValidity(), "yyyy-MM-dd"));

			// 判断是否是会员
			if (user.getVipValidity() != null
					&& StringHelper.getDaysBetween(new Date(),
							user.getVipValidity()) >= 0) {
				obj.put("isvip", true);
			}

			arr.add(obj);

		}
		return arr;

	}

	@Override
	public JSONObject getUserList(int offset) {
		JSONObject ret = new JSONObject();

		JSONArray arr = new JSONArray();

		List<StarUser> userlist = new ArrayList<StarUser>();

		offset = offset * ConstantType.PAGE_NUM;
		userlist = starusermapper.getUserInfoByPage(offset,
				ConstantType.PAGE_NUM);

		arr = encodeStarUserList(userlist);

		int listsize;
		listsize = starusermapper.calTotalUserCount();

		int mod = listsize % ConstantType.PAGE_NUM;
		int pagenum = listsize / ConstantType.PAGE_NUM;

		ret.put("pagecount", mod == 0 ? pagenum : pagenum + 1);
		ret.put("listsize", listsize);
		ret.put("userlist", arr);

		return ret;
	}

	@Override
	public JSONObject getUserListByCondition(int offset, String condition,
			String timestart, String timeend) {
		JSONObject ret = new JSONObject();

		JSONArray arr = new JSONArray();

		List<StarUser> userlist = new ArrayList<StarUser>();
		int listsize;

		if (StringUtils.isNullOrEmpty(timestart)) {
			timestart = "";
		}

		if (StringUtils.isNullOrEmpty(timeend)) {
			timeend = new Date().toString();
		}

		offset = offset * ConstantType.PAGE_NUM;

		userlist = starusermapper.getUserInfoByCondition(offset,
				ConstantType.PAGE_NUM, condition, timestart, timeend);
		listsize = starusermapper.calUserCountByCondition(condition, timestart,
				timeend);

		arr = encodeStarUserList(userlist);

		int mod = listsize % ConstantType.PAGE_NUM;
		int pagenum = listsize / ConstantType.PAGE_NUM;

		ret.put("pagecount", mod == 0 ? pagenum : pagenum + 1);
		ret.put("listsize", listsize);
		ret.put("userlist", arr);

		return ret;
	}

	@Override
	@Transactional
	public JSONObject updateUserInfo(String nickname, String id, String sex,
			String age, String phone, String pwd, String sign, String picUrl, String location) {
		JSONObject ret = new JSONObject();

		if(age != null){
			boolean isNumber = StringHelper.checkNumber(age);
			if (isNumber) {
				ret.put("error", EActionResult.NOT_NUMBER.getValue());
				ret.put("msg", EActionResult.NOT_NUMBER.getStr());
				return ret;
			}
		}

//		boolean isemoji = StringHelper.checkEmojiString(nickname, sign);
//		if (isemoji) {
//			ret.put("error", EActionResult.EXISTS_EMOJI.getValue());
//			ret.put("msg", EActionResult.EXISTS_EMOJI.getStr());
//			return ret;
//		}

		StarUser user = starusermapper.getStarUserByUid(Long.valueOf(id));
		
		if (user == null) {
			ret.put("error", EActionResult.ERROR_ACCOUNT.getValue());
			ret.put("msg", EActionResult.ERROR_ACCOUNT.getStr());
			return ret;
		}

//		if (nickname.length() > ConstantType.NICKNAME_MAX_LENGTH) {
//			ret.put("error", EActionResult.ERROR_LENGTH.getValue());
//			ret.put("msg", EActionResult.ERROR_LENGTH.getStr());
//			return ret;
//		}

		if (!StringUtils.isNullOrEmpty(age) && age.length() > ConstantType.AGE_MAX_LENGTH) {
			ret.put("error", EActionResult.ERROR_LENGTH.getValue());
			ret.put("msg", EActionResult.ERROR_LENGTH.getStr());
			return ret;
		}

//		if (sign.length() > ConstantType.SIGN_MAX_LENGTH) {
//			ret.put("error", EActionResult.ERROR_LENGTH.getValue());
//			ret.put("msg", EActionResult.ERROR_LENGTH.getStr());
//			return ret;
//		}
		ShopShop shopShop = shopShopMapper.getUserShop(id);
		boolean shopUpdate = (shopShop!=null);
		if(org.apache.commons.lang.StringUtils.isNotEmpty(nickname) && !nickname.equals(user.getNickname())) {
			user.setNickname(nickname);
			//zilong 2015.12.11
			redisService.setUserField(Long.valueOf(id), "nickname", nickname);
			if(shopUpdate){
				shopShop.setName(nickname);
			}
		}
		if(org.apache.commons.lang.StringUtils.isNotEmpty(sex)) {
			user.setSex((byte) (ESexType.setSexValue(sex)));
		}
		if(org.apache.commons.lang.StringUtils.isNotEmpty(age)) {
			user.setAge(age);
		}

		if(org.apache.commons.lang.StringUtils.isNotEmpty(phone)) {
			user.setUserPhone(phone);
		}
		if(org.apache.commons.lang.StringUtils.isNotEmpty(pwd)) {
			user.setUserPwd(StringHelper.md5ApacheCommonsCodec(pwd));
		}
		if(org.apache.commons.lang.StringUtils.isNotEmpty(sign)) {
			user.setSign(sign);
		}
		if(org.apache.commons.lang.StringUtils.isNotEmpty(picUrl)) {
			user.setPicUrl(picUrl);
			redisService.setUserField(Long.valueOf(id), "pic_url", picUrl);
			if(shopUpdate){
				shopShop.setName(nickname);
			}
		}
		if(org.apache.commons.lang.StringUtils.isNotEmpty(location)) {
			user.setLocation(location);
		}
		starusermapper.updateByPrimaryKeySelective(user);
		if(shopUpdate){
			shopShopMapper.updateByPrimaryKeySelective(shopShop);
		}

		return ret;
	}

	private JSONArray encodeVIPTicketsInfo(List<StarOrderInfo> list) {
		JSONArray arr = new JSONArray();

		for (StarOrderInfo ticket : list) {
			JSONObject obj = new JSONObject();

			obj.put("name", "VIP套餐＊" + ticket.getBuyCount());
			obj.put("getway", ETicketSource.getSourceName(ticket.getSource()));
			obj.put("gettime", StringHelper.dateToString(ticket.getStartTime(),
					"yyyy-MM-dd"));
			obj.put("expiretime", StringHelper.dateToString(
					ticket.getExpireTime(), "yyyy-MM-dd"));

			arr.add(obj);
		}

		return arr;
	}


	private JSONArray encodeVIPUserlist(List<Map<String, Object>> list) {
		JSONArray arr = new JSONArray();

		for (Map<String, Object> info : list) {
			JSONObject obj = new JSONObject();
			obj.put("vipname", "VIP套餐＊" + info.get("buy_count"));
			obj.put("nickname", info.get("nickname"));
			obj.put("ID", info.get("user_id"));
			obj.put("buytime", StringHelper.dateToString(
					(Date) info.get("create_time"), "yyyy-MM-dd HH:mm"));
			obj.put("start_time", StringHelper.dateToString(
					(Date) info.get("start_time"), "yyyy-MM-dd"));
			obj.put("expiretime", StringHelper.dateToString(
					(Date) info.get("expire_time"), "yyyy-MM-dd"));
			obj.put("payment",
					EPaymentType.getPaymentTypeName((int) info.get("pay_type")));

			arr.add(obj);
		}
		return arr;
	}

	@Override
	public JSONObject getVIPUserList(int offset) {
		JSONObject ret = new JSONObject();
		JSONArray arr = new JSONArray();

		offset = offset * ConstantType.PAGE_NUM;
		List<Map<String, Object>> list = new ArrayList<>();
		list = orderinfomapper.getVIPUserList(offset, ConstantType.PAGE_NUM);
		arr = encodeVIPUserlist(list);

		int listsize;
		listsize = orderinfomapper.calVIPUserCount();

		int mod = listsize % ConstantType.PAGE_NUM;
		int pagenum = listsize / ConstantType.PAGE_NUM;

		ret.put("pagecount", mod == 0 ? pagenum : pagenum + 1);
		ret.put("listsize", listsize);
		ret.put("viplist", arr);

		return ret;
	}

	@Override
	public JSONObject getVIPUserListByCondition(int offset, String condition,
			String timestart, String timeend) {
		JSONObject ret = new JSONObject();

		JSONArray arr = new JSONArray();

		List<Map<String, Object>> userlist = new ArrayList<Map<String, Object>>();
		int listsize;

		if (StringUtils.isNullOrEmpty(timeend)) {
			timeend = new Date().toString();
		}

		if (StringUtils.isNullOrEmpty(timestart)) {
			timestart = "";
		}

		offset = offset * ConstantType.PAGE_NUM;

		userlist = orderinfomapper.getVIPUserByCondition(offset,
				ConstantType.PAGE_NUM, condition, timestart, timeend);
		listsize = orderinfomapper.calVIPUserCountByCondition(condition,
				timestart, timeend);

		arr = encodeVIPUserlist(userlist);

		int mod = listsize % ConstantType.PAGE_NUM;
		int pagenum = listsize / ConstantType.PAGE_NUM;

		ret.put("pagecount", mod == 0 ? pagenum : pagenum + 1);
		ret.put("listsize", listsize);
		ret.put("viplist", arr);

		return ret;
	}

	private JSONArray encodeStarVideoCountlist(List<Map<String, Object>> vlist, int videotype) {
		JSONArray arr = new JSONArray();
		JSONObject obj = null;
		for (Map<String, Object> map : vlist) {
			obj = new JSONObject();
			obj.put("videoid", map.get("video_id"));
			obj.put("vtitle", map.get("title"));
			obj.put("star_name", map.get("star_name"));
			obj.put("video_type", map.get("video_type"));
			
			if(videotype == EVideoType.live.getValue()){
				obj.put("votenum", map.get("live_vote"));
			}else if(videotype == EVideoType.record.getValue()){
				obj.put("votenum", map.get("vod_vote"));
			}

			obj.put("time", StringHelper.dateToString(new Date((Long)(map.get("start_time"))),null));
			arr.add(obj);
		}
		return arr;
	}

	public void checkToken(JSONObject json){
		if (json == null) {
//			logger.error("jsonData为空");
			System.out.println("jsonData为空");
			throw new ErrorMsgException(ActionResult.SYS_9001.getValue());
		}
		IRedisService iredis = redisService;
		if(iredis != null ){
			logger.info(json.toJSONString());
			logger.info("================================iredis is not null================================");
			String token = iredis.getUserToken(json.getLong("user_id"), json.getString("appKey"));
			if (token == null || !token.equals(json.getString("token"))) {
//			logger.error("用户token失效");
				System.out.println("用户token失效");
				throw new ErrorMsgException(ActionResult.SYS_9002.getValue());
			}
		}

	}

	@Value("${shop.host}")
	private String remotehost;
	
	private static String QUERY_CROWDFUNDING_URL = "http://funding.heysound.com/crowdfunding/getBetOrderAmount";
	
	@Override
	@Transactional
	public Charge getCharge4Shop(String uid, ChargeVo chargeVo, String ctype) throws Exception {
		StarUser starUser = starUserMapper.selectByUserId(Long.parseLong(uid));

		String totalAmount = "";

		Map<String,Object> map = new HashMap<>();
		Map<String,Object> user = new HashMap<>();
		Map<String,Object> order = new HashMap<>();
		user.put("userId",uid);
		String orderIds = chargeVo.getOrderNo();
		if(orderIds.indexOf("M") > 0 ){
			String[] idStrings = orderIds.split("M");
			List<Long> oids = new ArrayList<>(idStrings.length);
			for(int i = 0 ; i < idStrings.length ; i ++){
				oids.add(Long.parseLong(idStrings[i]));
			}
			order.put("id", oids);
		}else{
			order.put("id", Long.parseLong(orderIds));
		}
		map.put("user",user);
		map.put("order",order);
		if(ctype.equals("crowdfunding")){
			totalAmount = getBetOrderAmmount(QUERY_CROWDFUNDING_URL, order);
		}else{
			totalAmount = getOrderAmmount("http://" + remotehost + "/buy/order/getByUser.do",map);
		}
		

		/**
		 * 开放平台即app的unionId兼容openId，但是公众平台不兼容，需要专门获取一下redis缓存的openId
		 */
		if(PayChannelEnum.WX.getName().equals(chargeVo.getChannel().trim()) || PayChannelEnum.WX_PUB.getName().equals(chargeVo.getChannel().trim())){
//            pingPPCharge.setOpenId(starUser.getThirdPartyId());
			String oid = null;
			if(PayChannelEnum.WX_PUB.getName().equals(chargeVo.getChannel().trim())){
				oid = redisService.getOpenId(Long.parseLong(uid));//取微信用户openId的服务
//				oid = starUser.getThirdPartyId();
			}else{
				oid = starUser.getThirdPartyId();
			}
			if(org.apache.commons.lang.StringUtils.isEmpty(oid)){
				List<StarUser> subUser = starUserMapper.getStarUserByBindTo(Long.parseLong(uid));
				if(subUser != null){
					for(int i = 0 ; i < subUser.size(); i++){
						StarUser starUser2 = subUser.get(i);
						/**
						 * 第三方注册类型\n            
						 * 1-qq           
						 * 2-weixin      
						 * 3-weibo
						 */
						if(starUser2.getThirdPartyType() == 2){
							starUser = starUser2;
							oid = starUser2.getThirdPartyId();
							break;
						}
					}
				}
			}
			chargeVo.setOpenId(oid);
//				pingPPCharge.put("openId",starUser.getThirdPartyId());
			String orderNoStr = chargeVo.getOrderNo();
			Date date = new Date();
			long tlong = date.getTime();
			// 针对微信订单,ping++没有每次支付都去获取统一下单,
			// 所以会有bug导致会出现重复下单错误,
			// ping++给出的解决方案是2个小时之内微信订单单号可以重复,
			// 超过两个小时要重新生成订单号去支付,目前没有监控,
			// 只做成微信每次支付都加上毫秒数去做到每次动手新的订单号去支付,
			// 订单相关的处理要处理分割_字符
			chargeVo.setOrderNo(orderNoStr+"T"+tlong);
		}else{
			
		}

		//生成paylog
		StarPayLog starPayLog = new StarPayLog();
		Long id = IDHelper.getUniqueID();
		starPayLog.setLogId(id);
		starPayLog.setOrderNo(chargeVo.getOrderNo());
		starPayLog.setStep((byte) PayPhaseEnum.GET_PAY_TOKEN.getValue());

		JSONObject chargeJson = (JSONObject) JSONObject.toJSON(chargeVo);
		starPayLog.setBody(JSONObject.toJSONString(chargeJson.toJSONString()));
		starPayLog.setCreateTime(new Date());
		starPayLogMapper.insertSelective(starPayLog);
		Charge charge = null;
		//创建ping++支付需要的 charge对象
		if(ctype.equals("crowdfunding")){
			charge = charge4Crowdfunding(chargeVo,starUser,totalAmount);
		}else{
			charge = charge4Shop(chargeVo,starUser,totalAmount);
		}

		return charge;
	}



	@Override
	public StarOrderInfo checkNormalOrder(ChargingMode chargingMode ,CheckVo checkVo, String uid){
		BigDecimal zero = new BigDecimal("0");
		StarOrderInfo starOrderInfo = null;
		//获得videoItemId对应的视频付费模型
//		ChargingMode chargingMode = getChargingMode(checkVo.getVideoItemId());
		//如果权限为1 是vip权限
		if(1 == (chargingMode.role)){
			//vip目前暂时没有失实现业务逻辑
		}else if(0 == chargingMode.role && chargingMode.price.compareTo(zero) != 0){
			//普通用户 并且收费金额不是0 表示是收费视频
			//查询
			starOrderInfo = starOrderInfoMapper.selectByVid(checkVo.getVideoItemId(),Long.parseLong(uid));

		}

		return starOrderInfo;
	}


	@Override
	@Transactional
	public StarOrderInfo generateVideoOrder(ChargeVo chargeVo, String uid, String ctype) throws Exception {
/**
 * 生成订单
 */
		String orderNo = StringHelper.genOrderNO();
//        if(Integer.parseInt(pingPPCharge.get("OrderType").toString()) == PayProductEnum.GET_GUARD_TYPE.getValue()){
//            orderNo = ORDER_PREFIX + orderNo;
//        }
		chargeVo.setOrderNo(orderNo);



		//生成订单
		Long id = IDHelper.getUniqueID();
		StarOrderInfo starOrderInfo = decodeStarOrderInfo(chargeVo,uid,ctype);
		starOrderInfo.setOrderId(id);
//		starOrderInfo.setSeats(pingPPCharge.get("seats") == null ? "" : pingPPCharge.get("seats").toString());
		starOrderInfoMapper.insertSelective(starOrderInfo);
		return starOrderInfo;
	}


	// 订单信息
	private StarOrderInfo decodeStarOrderInfo(ChargeVo chargeVo,String uid,String ctype)
			throws ErrorMsgException {
		StarOrderInfo info = new StarOrderInfo();

		info.setOrderNo(chargeVo.getOrderNo());
//        info.setOrderNo(pingPPCharge.getOrder_no());
		int orderType = Integer.parseInt(ctype);
//        int orderType = pingPPCharge.getOrderType();
//        String[] productArray = this.payProductMap.get(orderType).split("_");
		info.setOrderType(Byte.valueOf(orderType+""));
		info.setUserId(Long.parseLong(uid));
		info.setSource((byte) GetSourceEnum.GET_FROM_ONLINE.getValue());
		info.setBuyCount(1);
		info.setAmountPrice(new BigDecimal(chargeVo.getPrice()).setScale(2,BigDecimal.ROUND_HALF_UP));
		info.setPrice(new BigDecimal(chargeVo.getPrice()).setScale(2,BigDecimal.ROUND_HALF_UP));
		info.setBalancePrice(new BigDecimal(chargeVo.getPrice()).setScale(2,BigDecimal.ROUND_HALF_UP));
		info.setCreateTime(new Date());
		info.setVideoItemId(Long.parseLong(chargeVo.getVideoItemId()));
		//生成订单的时候没有选择支付方式
//        int payType = Integer.parseInt(pingPPCharge.get("payType").toString());
//        if (payType != 1 && payType != 2 && payType != 3) {// 如果不是这三种支付类型，直接抛出异常
//            throw new ErrorMsgException(ActionResult.SYS_9007.getValue());
//        }
//        info.setPayType(Byte.valueOf(payType+""));
		info.setStatus(PayStatusEnum.START_PAY.getValue());
		return info;
	}


//	private ChargingMode getChargingMode(long videoItemId) {
//		ChargingMode chargingMode =  videoService.getChargingMode(videoItemId+"");
//		return chargingMode;
//
//	}


	private String getOrderAmmount(String url,Map<String,Object> paramsMap) throws Exception {
		BigDecimal totalAmount = new BigDecimal("0");
		JSONObject jsonObject1 = new JSONObject();
		jsonObject1.putAll(paramsMap);
		System.out.println(jsonObject1.toJSONString());
		JSONObject result = HttpClient.postForm(url,paramsMap,"utf-8");
		if(null != result){
			if(!result.get("success").toString().equals("true")){
				throw new Exception("查询订单失败");
			}
			Object results = result.get("result");
			if(results instanceof List){
				for(JSONObject o : (List<JSONObject>)results){
					totalAmount = totalAmount.add(new BigDecimal(o.getString("payAmount")));
				}
			}else{
				totalAmount = new BigDecimal(((JSONObject)results).getString("payAmount"));
			}
		}
		return totalAmount.toPlainString();
	}
	
	private String getBetOrderAmmount(String url,Map<String,Object> paramsMap) throws Exception {
		BigDecimal totalAmount = new BigDecimal("0");
		JSONObject result = (JSONObject)JSONObject.parse(HttpUtil.doGet(url, paramsMap));
		if(null != result){
			if(!result.get("status").toString().equals("ok")){
				throw new Exception("查询投注订单失败");
			}else{
				totalAmount = new BigDecimal(result.getString("data"));
			}
		}
		return totalAmount.toPlainString();
	}

	@Transactional
	public JSONObject accountLogin(LoginVO loginVO) throws ErrorMsgException
	{
		JSONObject retJson = new JSONObject();
		retJson.put("status","ok");
		try {
			//如果是新版登录********************************************************************************
//            String params;
//            Map<String,String> paramsMap = new HashMap<>();
//            if (device_token!=null && device_type!=-1 ){
////                paramsMap.put("time",time+"");
//                paramsMap.put("phone",phone);
//                paramsMap.put("pwd",pwd);
//                paramsMap.put("device_type",device_type+"");
//                paramsMap.put("device_token",device_token);
////                params = time + phone + pwd+device_type+device_token;
//            }else {
////                params = time + phone + pwd;
////                paramsMap.put("time", time + "");
//                paramsMap.put("phone", phone + "");
//                paramsMap.put("pwd", pwd + "");
//            }
			publicVerify.newCheckSign(loginVO, loginVO.time);
			//********************************************************************************
			String phone = loginVO.phone;
			String pwd = loginVO.pwd;
			byte device_type = loginVO.device_type;
			String device_token = loginVO.device_token;
			boolean loginCaptcha = false;
			if(pwd.length() > 0 && pwd.length() == 4){//相当于验证码登录
				loginCaptcha = true;
				IRedisService iredis = redisService;
				String validateCode = iredis.getUserCode(phone);
				if (validateCode == null || !pwd.equals(validateCode)) {// 缓存里面没有，直接让用户重新获取验证码
					throw new RuntimeException(ActionResult.LOGIN_1014.getValue());
				}
			}


			StarUser starUser = starusermapper.selectByPhone(phone);
			if (starUser == null && !loginCaptcha) {// 用户不存在,号码没有注册
				throw new RuntimeException(ActionResult.LOGIN_1019.getValue());
			}else if(starUser == null && loginCaptcha){//用户不存在，验证码登录，自动注册
				return commonRegister(phone, pwd, null, 2, null, (byte)-1, null, loginVO.app_key);
			}else if(!loginCaptcha){
				if (!StringHelper.md5ApacheCommonsCodec(pwd).equals(starUser.getUserPwd())) {
					throw new RuntimeException(ActionResult.LOGIN_1015.getValue());
				}
			}else{
			}



			//device_token验证****************************************************************
			if (device_token!=null && device_token.length()>80 )
			throw new RuntimeException(ActionResult.SYS_9003.getValue());
			//********************************************************************************

			starUser.setLastLoginTime(new Date());
			starusermapper.updateByPrimaryKeySelective(starUser);// 更新用户最后登录时间

			String newToken = StringHelper.generateUUID();// 生成用户token

			//********************************************************************************

			if (!StringUtils.isNullOrEmpty(device_token) && device_type!=-1 )
				settingDevice(starUser.getUserId(),device_type,device_token);//更新用户登录的设备信息
			//********************************************************************************

			JSONObject innerJson = new JSONObject();
			innerJson.put("user_id", starUser.getUserId());
			innerJson.put("token", newToken);

			retJson.put("user_meta", innerJson);
			ShopShop shopShop = shopShopMapper.getUserShop(starUser.getUserId() + "");
			if(null != shopShop){
				retJson.put("sid", shopShop.getId());
			}
			// 添加到缓存
			Map<String, String> map = redisService.getUserInfo(starUser.getUserId());
			if (map == null || map.isEmpty()) {
				// 添加到缓存
				List<StarInfo> list=starUserMapper.findBrandLogoAndMonthAndFollowing(starUser.getUserId());
				int mark = 0;
				if(list != null && !list.isEmpty()){
					mark = 1;
				}
				StarBaseVO starBaseVO = new StarBaseVO(starUser.getUserId() + "", starUser.getNickname(), starUser.getPicUrl(), mark);
				redisService.setUserInfo(starBaseVO);
			}
			// add to cache
			redisService.setUserToken(starUser.getUserId(), newToken, loginVO.app_key);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return retJson;
	}

	@Override
	public JSONObject checkRealName(String url, Map<String, Object> params) throws Exception {
		String name = null;
		String idcard = null;
		String tm = System.currentTimeMillis() / 1000 + "";
		if((name = (String)params.get("realname")) == null){
			throw new RealNameException("实名信息有误[姓名]");
		}else if((idcard = (String)params.get("idcard")) == null){
			throw new RealNameException("实名信息有误[身份证号码]");
		}else{
			params.put("mall_id", "110562");
			params.put("tm", tm);
			StringBuilder sb = new StringBuilder().append("110562").append(name).append(idcard).append(tm).append(realnamekey);
			params.put("sign", MD5.stringToMD5(sb.toString()));
		}
		return (JSONObject)JSONObject.parse(HttpUtil.doGet(url, params));
	}

	@Override
	public List<FollowView> getFollowing(int pageNum, String uid){
		List<FollowView> views = new ArrayList<>();
		String followed = "";
		int pageIndex = (pageNum - 1) < 0 ? 0 : (pageNum - 1) * ConstantType.PAGE_SIZE;
		if (org.apache.commons.lang.StringUtils.isNotEmpty(uid) && Long.parseLong(uid) > 0) {
			List<Follow> followList = mongoTemplate.find(Query.query(Criteria.where("userId").is(uid)), Follow.class);
			if(followList != null && followList.size() > 0){
				Follow follow1 = followList.get(0);
				String following = follow1.getFollowing();
				String[] followings0 = following.split(",");
				views = new ArrayList<>();
				if(pageIndex > followings0.length){
					return views;
				}
//					pageIndex = pageIndex > followings0.length ? (followings0.length % ConstantType.PAGE_SIZE > 0 ? followings0.length - followings0.length % ConstantType.PAGE_SIZE : followings0.length - ConstantType.PAGE_SIZE) : pageIndex;
				int endIndex = pageIndex + ConstantType.PAGE_SIZE > followings0.length ? followings0.length : pageIndex + ConstantType.PAGE_SIZE;
				List<String> followings = Arrays.asList(followings0).subList(pageIndex, endIndex);

				for(String followingId : followings){
					Long id  = Long.parseLong(followingId);
					FollowView view = new FollowView();
					StarBaseVO vo = null;
					Map<String, String> map = redisService.getUserInfo(id);
					if (map != null && !map.isEmpty()) {
						vo = new StarBaseVO(id + "", map.get("nickname"), map.get("pic_url"));
					} else {
						StarUser user = starUserMapper.getStarUserByUid(Long.parseLong(id + ""));
						if (user != null) {
							vo = new StarBaseVO(user.getUserId() + "", user.getNickname(), user.getPicUrl());
						} else {//如果数据库也没有，那就是数据有问题，略过
							continue;
						}
					}
					view.star = vo;
					Follow follows = mongoTemplate.findOne(Query.query(Criteria.where("userId").is(followingId)), Follow.class);
					if(follows != null) {
						followed = follows.getFollowd();
						if (org.apache.commons.lang.StringUtils.isNotEmpty(followed)) {
							view.followed = followed.split(",").length;
						}
//							view.isFollowed = StringUtils.isNotEmpty(followed) && followed.indexOf(uid) > -1 ? true : false;
					}
					views.add(view);
				}


			}
		}
		return views;
	}


	@Override
	public List<Follow> getFollowingAll(String uid){
		List<Follow> followList = mongoTemplate.find(Query.query(Criteria.where("userId").is(uid)), Follow.class);
		return followList;
	}

	private void createFamilyUser(long i, StarUser fu) {
		try {
			fu.setUserId(i);
			fu.setCreateTime(new Date());
			starusermapper.insertSelective(fu);
			return;
		} catch (Exception ex) {
			if (ex instanceof DuplicateKeyException
					|| ex instanceof SQLIntegrityConstraintViolationException) {// 数据库有重复记录异常，重新生成添加
				ex.printStackTrace();
				i++;
				createFamilyUser(i, fu);// 递归调用创建
			}
			ex.printStackTrace();
		}
	}

	private JSONObject commonRegister(String phone, String pwd, String nickname, int sex, String picUrl, byte device_type, String device_token, String appKey){
		JSONObject retJson = new JSONObject();
		StarUser fu = new StarUser();
		fu.setUserPhone(phone);
		pwd = (StringUtils.isNullOrEmpty(pwd)|| pwd.length() == 4) ? StringHelper.randomString(6) : pwd;
		fu.setUserPwd(StringHelper.md5ApacheCommonsCodec(pwd));
		fu.setNickname(nickname == null ? phone.replace(phone.substring(3,7), "****")+"_"+ StringHelper.randomString(3) : nickname);
		fu.setSex((byte) sex);
		fu.setPicUrl(StringUtils.isNullOrEmpty(picUrl) ? headUrl : picUrl);// 客户端直接传过来
		//如果是第一次登录，给用户增加3个月VIP会员， zilong 2015.12.18
		fu.setVipValidity(StringHelper.plusMonth(months));
		this.createFamilyUser(0l, fu);
		//============================================================================
//        this.createFamilyUser(0l, fu);// 数据库必须要初始一条数据

		// 再去查询一次
//        StarUser dbFu = starUserMapper.selectByPrimaryKey(fu.getId());
//        dbFu.setLastLoginTime(new Date());
//        starUserMapper.updateByPrimaryKey(dbFu);
//========================================================================================================================
		//startuser的userid 在数据库保存时,有额外操作,需要再查询一次
		StarUser dbFu = starusermapper.selectByPhone(phone);
		dbFu.setLastLoginTime(new Date());
		starusermapper.updateByPrimaryKey(dbFu);
//        fu.setLastLoginTime(new Date());


		// add to cache`
		String newToken = StringHelper.generateUUID();// 生成用户token
		redisService.setUserToken(fu.getUserId(), newToken, appKey);

		JSONObject innerJson = new JSONObject();
		innerJson.put("user_id", fu.getUserId());
		innerJson.put("token", newToken);
		retJson.put("user_meta", innerJson);
		retJson.put("nickname", fu.getNickname());//告知临时昵称
		// 添加用户信息到缓存
		List<StarInfo> list=starUserMapper.findBrandLogoAndMonthAndFollowing(fu.getUserId());
		int mark = 0;
		if(list != null && !list.isEmpty()){
			mark = 1;
		}
		StarBaseVO starBaseVO = new StarBaseVO(fu.getUserId() + "", fu.getNickname(), fu.getPicUrl(), mark);
		redisService.setUserInfo(starBaseVO);
		//更新用户磁盘token信息
		if (!StringUtils.isNullOrEmpty(device_token)  && device_type!=-1 )
			settingDevice(fu.getUserId(),device_type,device_token);//更新用户登录的设备信息
		return retJson;
	}


	//用户设备信息
	private void settingDevice(long userId,byte device_type,String device_token) throws ErrorMsgException
	{
		byte deviceType=0;
		if (device_type==1 || device_type==2)
			deviceType=device_type;

		Date time=new Date();

		StarUserDevice device = starUserDeviceMapper.selectByUserId(userId);
		if (device == null) {
			StarUserDevice model=new StarUserDevice();
			model.setCreateTime(time);
			model.setUpdateTime(time);
			model.setDeviceToken(device_token);
			model.setDeviceType(deviceType);
			model.setUserId(userId);
			starUserDeviceMapper.insertSelective(model);
		}
		else{
			StarUserDevice model=new StarUserDevice();
			model.setUpdateTime(time);
			model.setDeviceToken(device_token);
			model.setDeviceType(deviceType);
			model.setDeviceId(device.getDeviceId());
			starUserDeviceMapper.updateByPrimaryKeySelective(model);
		}
	}


	//用户设备信息
	private StarUserDevice settingDevicenew(long userId,byte device_type,String device_token) throws Exception {
		byte deviceType=0;
		if (device_type==1 || device_type==2)
			deviceType=device_type;

		Date time=new Date();

//		StarUserDevice device = starUserDeviceMapper.selectByUserId(userId);
		StarUserDevice device = starUserDeviceMapper.selectByDeviceToken(device_token, userId);
		if (device == null) {
			StarUserDevice model=new StarUserDevice();
			model.setCreateTime(time);
			model.setUpdateTime(time);
			model.setDeviceToken(device_token);
			model.setDeviceType(deviceType);
			model.setUserId(userId);
//			model.setjPushImPwd(StringHelper.genOrderNO());
			starUserDeviceMapper.insertSelective(model);
			return model;
		}
		return device;
	}
	@Override
	@Transactional
	public JSONObject thirdLogin(ThirdPartyLoginVo loginVo,int type) throws ErrorMsgException
	{

//		Map<String, String> paramMap = new HashMap<>();
//		paramMap.put("third_party_type", type + "");
//		paramMap.put("third_party_id", id);
//		paramMap.put("third_party_nickname", nickname);
//		paramMap.put("third_party_avatar", avatar);
//		if (device_token!=null && device_type!=-1 ) {
//			paramMap.put("device_type", device_type + "");
//			paramMap.put("device_token", device_token);
//		}


//		loginVo.getApp_key(), loginVo.getSign(), loginVo.getTime(), type, loginVo.getId()
//				, loginVo.getNickname(), loginVo.getAvatar(), loginVo.getDevice_type(), loginVo.getDevice_token()
		publicVerify.newCheckSign(loginVo, loginVo.getTime());
//		publicVerify.checkSignValid(appKey, sign, time, paramMap);

		//******************* device_token验证 zilong 2016.1.19****************************
		if (loginVo.getDevice_token()!=null && loginVo.getDevice_token().length()>80 )
			StringHelper.throwMsgException(ActionResult.SYS_9003.getValue());
		//***************** **************************************************************

		return commonThirdRegister(type, loginVo.getId(), loginVo.getNickname(), loginVo.getAvatar(), loginVo.getDevice_type(), loginVo.getDevice_token(), loginVo.getApp_key());
	}

	@Override
	@Transactional
	public void bindRegistrationId(Long userId, String rId ,Byte rType){

		StarUserDevice device = starUserDeviceMapper.selectByDeviceToken(rId, userId);
		if (device == null) {
			StarUserDevice model=new StarUserDevice();
			Date time = new Date();
			model.setCreateTime(time);
			model.setUpdateTime(time);
			model.setDeviceToken(rId);
			model.setDeviceType(rType);
			model.setUserId(userId);
			starUserDeviceMapper.insertSelective(model);
		}
	}


	@Override
	@Transactional
	public JSONObject accountLogin(AccountLoginVo loginVo) throws ErrorMsgException
	{

		JSONObject retJson = new JSONObject();
		try {
//			Map<String, String> paramMap = new HashMap<>();
//			paramMap.put("user_phone", phone);
//			paramMap.put("user_pwd", pwd);
//			if (device_token!=null && device_type!=-1 ) {
//				paramMap.put("device_type", device_type + "");
//				paramMap.put("device_token", device_token);
//			}

			Long time = loginVo.getTime();
			String phone = loginVo.getPhone();
			String pwd = loginVo.getPwd();
			byte deviceType =	loginVo.getDevice_type();
			String deviceToken = loginVo.getDevice_token();

			publicVerify.newCheckSign(loginVo,time);
//			publicVerify.checkSignValid(appKey, sign, time, paramMap);
			//********************************************************************************
			boolean loginCaptcha = false;
			if(pwd.length() > 0 && pwd.length() == 4){//相当于验证码登录
				loginCaptcha = true;
				IRedisService iredis = redisService;
				String validateCode = iredis.getUserCode(phone);
				if (validateCode == null || !pwd.equals(validateCode)) {// 缓存里面没有，直接让用户重新获取验证码
					StringHelper.throwMsgException(ActionResult.LOGIN_1014.getValue());
				}
			}


			StarUser starUser = starUserMapper.selectByPhone(phone);
			if (starUser == null && !loginCaptcha) {// 用户不存在,号码没有注册
				StringHelper.throwMsgException(ActionResult.LOGIN_1019.getValue());
			}else if(starUser == null && loginCaptcha){//用户不存在，验证码登录，自动注册
				return commonRegister(phone, pwd, null, 2, null, (byte)-1, null, loginVo.getApp_key());
			}else if(!loginCaptcha){
				if (!StringHelper.md5ApacheCommonsCodec(pwd).equals(starUser.getUserPwd())) {
					StringHelper.throwMsgException(ActionResult.LOGIN_1015.getValue());
				}
			}else{
			}

			//device_token验证****************************************************************
			if (deviceToken!=null && deviceToken.length()>80 )
				StringHelper.throwMsgException(ActionResult.SYS_9003.getValue());
			//********************************************************************************

			starUser.setLastLoginTime(new Date());
			starUserMapper.updateByPrimaryKeySelective(starUser);// 更新用户最后登录时间

			String newToken = StringHelper.generateUUID();// 生成用户token

			//********************************************************************************

			if (!StringUtils.isNullOrEmpty(deviceToken) && deviceType!=-1 ) {
				StarUserDevice starUserDevice = settingDevicenew(starUser.getUserId(), deviceType, deviceToken);//更新用户登录的设备信息
				retJson.put("jpwd", starUserDevice.getDeviceToken());
			}
			//********************************************************************************

			JSONObject innerJson = new JSONObject();
			innerJson.put("user_id", starUser.getUserId());
			innerJson.put("token", newToken);
			retJson.put("user_meta", innerJson);

			// 添加到缓存
			Map<String, String> map = redisService.getUserInfo(starUser.getUserId());
			if (map == null || map.isEmpty()) {
				// 添加到缓存
				List<StarInfo> list=starUserMapper.findBrandLogoAndMonthAndFollowing(starUser.getUserId());
				int mark = 0;
				if(list != null && !list.isEmpty()){
					mark = 1;
				}
				StarBaseVO starBaseVO = new StarBaseVO(starUser.getUserId() + "", starUser.getNickname(), starUser.getPicUrl(), mark);
				redisService.setUserInfo(starBaseVO);
			}
			// add to cache
			redisService.setUserToken(starUser.getUserId(), newToken, loginVo.getApp_key());

			ShopShop shopShop = shopShopMapper.getUserShop(starUser.getUserId() + "");
			if(null != shopShop){
				retJson.put("sid", shopShop.getId());
			}
			retJson.put("status","ok");
		} catch (ErrorMsgException e) {
			logger.error("login error:" + e);
			retJson.put("status","no");
			retJson.put("message",e.getMessage());
		}catch (Exception e) {
			logger.error("login error:" + e);
			retJson.put("status","no");
			retJson.put("message","服务器异常");
		}

		return retJson;
	}


	@Override
	public JSONObject commonThirdRegister(Integer type, String id, String nickname, String avatar, byte device_type, String device_token, String appKey){
		JSONObject retJson = new JSONObject();
		try {
			System.out.println("type:"+type+"--id:"+id);
			StarUser fuFamilyUser = starUserMapper.selectByThirdType(type, id);
			String new_name= StringUtils.isNullOrEmpty(nickname)? RandomStringUtils.randomAlphanumeric(4).toLowerCase():nickname;
			Long loginId = 0L;
			if (fuFamilyUser == null) {// 用户不存在,创建用户
				StarUser fu = new StarUser();
				fu.setThirdPartyType(type);
				fu.setThirdPartyId(id);
				fu.setNickname(new_name);
				fu.setSex((byte) UserTypeEnum.UNKNOWN.getValue());
				//如果是第一次登录，给用户增加3个月VIP会员， zilong 2015.12.18
				fu.setVipValidity(StringHelper.plusMonth(months));
				//初始化一个密码
				String pwd = StringHelper.randomString(6);
				fu.setUserPwd(StringHelper.md5ApacheCommonsCodec(pwd));
				// 上传第三方头像到oss
				if (avatar != null) {
					//fu.setPicUrl(this.postImgToOSS(avatar, avatarDir));
					fu.setPicUrl(avatar);//直接保存第三方头像
				}
				fu.setLastLoginTime(new Date());
				this.createFamilyUser(0l, fu);// 数据库必须要初始一条数据
				// 再去查询一次
				StarUser dbFu = starUserMapper.selectByThirdType(fu.getThirdPartyType(), fu.getThirdPartyId());
				// add to cache，url需要来自后台存到oss里面的头像
				if (dbFu != null) {
					List<StarInfo> list=starUserMapper.findBrandLogoAndMonthAndFollowing(dbFu.getUserId());
					int mark = 0;
					if(list != null && !list.isEmpty()){
						mark = 1;
					}
					StarBaseVO starBaseVO = new StarBaseVO(dbFu.getUserId() + "", dbFu.getNickname(), dbFu.getPicUrl(), mark);
					redisService.setUserInfo(starBaseVO);
				}
				fuFamilyUser = new StarUser();// 创建对象，防止fuFamilyUser为null
				fuFamilyUser.setUserId(dbFu.getUserId());
				loginId = dbFu.getUserId();
			} else {
				//zilong 2015.11.18
				//当用户没有在手机上编辑过资料，每次登录保持更新策略
				StarUser fui = new StarUser();
				if ( fuFamilyUser.getSex()==(byte)UserTypeEnum.UNKNOWN.getValue()){
					fui.setNickname(new_name);
					fui.setPicUrl(avatar);
					// 更新缓存
					List<StarInfo> list=starUserMapper.findBrandLogoAndMonthAndFollowing(fuFamilyUser.getUserId());
					int mark = 0;
					if(list != null && !list.isEmpty()){
						mark = 1;
					}
					StarBaseVO starBaseVO = new StarBaseVO(fuFamilyUser.getUserId() + "", fuFamilyUser.getNickname(), fuFamilyUser.getPicUrl(), mark);
					redisService.setUserInfo(starBaseVO);
				}
				fui.setUserId(fuFamilyUser.getUserId());
				fui.setLastLoginTime(new Date());
				starUserMapper.updateByUserIdSelective(fui);
//				loginId = fuFamilyUser.getBindto() != null ? fuFamilyUser.getBindto() : fuFamilyUser.getUserId();
				loginId = fuFamilyUser.getUserId();
			}
			// 更新用户新的token
			String newToken = StringHelper.generateUUID();// 生成用户token

			//***************** 如果是新版登录 2016.01.19 zilong ******************************
			if (!StringUtils.isNullOrEmpty(device_token)&& device_type!=-1 )
				settingDevicenew(fuFamilyUser.getUserId(),device_type,device_token);//更新用户登录的设备信息
			//********************************************************************************

			redisService.setUserToken(loginId, newToken, appKey );// add to cache
			ShopShop shopShop = shopShopMapper.getUserShop(fuFamilyUser.getUserId() + "");
			if(null != shopShop){
				retJson.put("sid", shopShop.getId());
			}
			JSONObject innerJson = new JSONObject();
			innerJson.put("user_id", loginId);
			innerJson.put("token", newToken);
			retJson.put("user_meta", innerJson);
			retJson.put("status","ok");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("login error:" + e);
			StringHelper.throwAllException(e);
			retJson.put("status", "no");
		}
		return retJson;
	}

	@Value("${ping.api.key}")
	public String apiKey;

	@Value("${app.id}")
	public  String appId;

	@Override
	public Charge charge4Shop(ChargeVo chargeVo, StarUser starUser, String paymentAmount) throws Exception {
		Pingpp.apiKey = apiKey;
		Map<String, Object> chargeMap = new HashMap<String, Object>();
		BigDecimal amount = new BigDecimal(paymentAmount.toString()).setScale(2,BigDecimal.ROUND_HALF_UP);
		BigDecimal oneHundred = new BigDecimal(100).setScale(2,BigDecimal.ROUND_HALF_UP);
		amount = amount.multiply(oneHundred).setScale(2,BigDecimal.ROUND_HALF_UP);
		chargeMap.put("amount",amount.toString());// 金额需要乘以100
		chargeMap.put("currency", "cny");
//        chargeMap.put("subject", "shop");
		chargeMap.put("subject", chargeVo.getSubject());
		chargeMap.put("body", chargeVo.getSubject());
//        chargeMap.put("body", "shop");
		chargeMap.put("order_no", chargeVo.getOrderNo());
		chargeMap.put("channel", chargeVo.getChannel());
		chargeMap.put("client_ip", org.springframework.util.StringUtils.hasText(chargeVo.getClientIp()) ? chargeVo.getClientIp() : "127.0.0.1");
		Map<String, String> optional = new HashMap<String, String>();
		if(PayChannelEnum.ALIPAY_WAP.getName().equals(chargeVo.getChannel().trim())) {
//            optional.put("success_url","www.baidu.com");
			optional.put("success_url",chargeVo.getSc_url());
//            optional.put("cancel_url", "www.baidu.com");
			optional.put("cancel_url", chargeVo.getCa_url());
		}else if(PayChannelEnum.WX_PUB.getName().equals(chargeVo.getChannel().trim())){
			//设置微信不能信用卡支付
			optional.put("limit_pay", "no_credit");
			optional.put("open_id", redisService.getOpenId(starUser.getUserId()));
		}
		chargeMap.put("extra", optional);
		chargeMap.put("description","shop");
		Map<String, String> app = new HashMap<String, String>();
		app.put("id", appId);
		chargeMap.put("app", app);
		System.out.println("charge4shopmap:" + chargeMap);
		Charge charge = Charge.create(chargeMap);

		return charge;
	}
	
	@Override
	public Charge charge4Crowdfunding(ChargeVo chargeVo, StarUser starUser, String paymentAmount) throws Exception {
		Pingpp.apiKey = apiKey;
		Map<String, Object> chargeMap = new HashMap<String, Object>();
		BigDecimal amount = new BigDecimal(paymentAmount.toString()).setScale(2,BigDecimal.ROUND_HALF_UP);
		BigDecimal oneHundred = new BigDecimal(100).setScale(2,BigDecimal.ROUND_HALF_UP);
		amount = amount.multiply(oneHundred).setScale(2,BigDecimal.ROUND_HALF_UP);
		chargeMap.put("amount",amount.toString());// 金额需要乘以100
		chargeMap.put("currency", "cny");
//        chargeMap.put("subject", "shop");
		chargeMap.put("subject", chargeVo.getSubject());
		chargeMap.put("body", chargeVo.getSubject());
//        chargeMap.put("body", "shop");
		chargeMap.put("order_no", chargeVo.getOrderNo());
		chargeMap.put("channel", chargeVo.getChannel());
		chargeMap.put("client_ip", org.springframework.util.StringUtils.hasText(chargeVo.getClientIp()) ? chargeVo.getClientIp() : "127.0.0.1");
		Map<String, String> optional = new HashMap<String, String>();
		if(PayChannelEnum.ALIPAY_WAP.getName().equals(chargeVo.getChannel().trim())) {
//            optional.put("success_url","www.baidu.com");
			optional.put("success_url",chargeVo.getSc_url());
//            optional.put("cancel_url", "www.baidu.com");
			optional.put("cancel_url", chargeVo.getCa_url());
		}else if(PayChannelEnum.WX_PUB.getName().equals(chargeVo.getChannel().trim())){
			//设置微信不能信用卡支付
			optional.put("limit_pay", "no_credit");
			optional.put("open_id", redisService.getOpenId(starUser.getUserId()));
		}
		chargeMap.put("extra", optional);
		chargeMap.put("description","crowdfunding");
		Map<String, String> app = new HashMap<String, String>();
		app.put("id", appId);
		chargeMap.put("app", app);
		chargeMap.put("metadata", chargeVo.getMetadata());
		System.out.println("charge4shopmap:" + chargeMap);
		Charge charge = Charge.create(chargeMap);

		return charge;
	}
	
	//返回品牌logo和月份和关注数
	@Override
	public List<StarInfo> findBrandLogoAndMonthAndFollowing(Long starId) {
		List<StarInfo> list=starUserMapper.findBrandLogoAndMonthAndFollowing(starId);
		return list;
	}

}
