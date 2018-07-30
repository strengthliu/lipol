package com.yixinintl.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yixinintl.domain.*;
import com.yixinintl.exception.HeySoundException;
import com.yixinintl.exception.RealNameException;
import com.yixinintl.mapper.*;
import com.yixinintl.model.RealNameVO;
import com.yixinintl.service.*;
import com.yixinintl.serviceImpl.JPushServiceImpl;
import com.yixinintl.util.Constants;
import com.yixinintl.util.PublicVerify;
import com.yixinintl.util.RedisUtils;
import com.yixinintl.util.StringHelper;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Null;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/user")
public class UserController {


	@Autowired
	private IUserService userService;

	@Autowired
	private IImageService iimageService;
	@Autowired
	private ISmsService ismsService;
	@Autowired
	private JPushServiceImpl jPushService;

	@Autowired
	private IRedisService redisService;
	@Autowired
	private RedisUtils redisUtils;
	@Autowired
	private PublicVerify publicVerify;
	
//	@Autowired
//	private TUserGrantMapper userGrantMapper;

	@Autowired
	private StarUserMapper starUserMapper;
	
	@Autowired
	private StarUserDeviceMapper starUserDeviceMapper;

//	@Autowired
//	private StarUserExtMapper starUserExtMapper;

	@Autowired
	private StarRealnameMapper starRealnameMapper;

	// @Autowired
	// private StarPayLogMapper starPayLogMapper;

	// @Autowired
	// private StarOrderInfoMapper starOrderInfoMapper;

	private static Logger logger = Logger.getLogger(UserController.class.getName());

	@Value("${oss.video.image}")
	private String imageDir;// 封面oss存储路径

	@Value("${realnameurl}")
	private String realnameurl;

	@RequestMapping("getPhone")
	public @ResponseBody JSONObject getPhoneByUid(HttpServletRequest request) {
		publicVerify.checkSignValid(request.getParameter(Constants.APPKEY), request.getParameter(Constants.SIGN),
				Long.parseLong(request.getParameter("time")), "");
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "ok");
		long uid = 0l;
		if (StringUtils.isNotEmpty(request.getHeader(Constants.UID))
				&& (uid = Long.parseLong(request.getHeader(Constants.UID))) > 0) {
			try {
				StarUser user = starUserMapper.getStarUserByUid(uid);
				if (user != null) {
					jsonObject.put("data", user.getUserPhone());
				} else {
					jsonObject.put("status", "no");
					jsonObject.put("msg", "请先登录，亲！");
				}
			} catch (Exception e) {
				logger.error("服务器错误！");
				jsonObject.put("status", "no");
				jsonObject.put("msg", "服务器错误！[uid=" + uid + "]");
				return jsonObject;
			}
		}
		return jsonObject;
	}

	@RequestMapping("bindWeixin")
	public @ResponseBody JSONObject isBindToWeixin(HttpServletRequest request) {
		publicVerify.checkSignValid(request.getParameter(Constants.APPKEY), request.getParameter(Constants.SIGN),
				Long.parseLong(request.getParameter("time")), "");
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "ok");
		long uid = 0l;
		if (StringUtils.isNotEmpty(request.getHeader(Constants.UID))
				&& (uid = Long.parseLong(request.getHeader(Constants.UID))) > 0) {
			try {
				StarUser user = starUserMapper.getStarUserByUid(uid);
				// 判断主账号
				if (user.getUserPhone() != null) {// 当前账号是主账户
					boolean binded = false;
					String thirdPartyId = "";
					List<StarUser> users = starUserMapper.getStarUserByBindTo(uid);
					for (StarUser u : users) {
						if (u.getThirdPartyType() == 2) {
							binded = true;
							thirdPartyId = u.getThirdPartyId();
						}
					}
					if (binded) {
						jsonObject.put("data", thirdPartyId);
					} else {
						jsonObject.put("status", "no");
						jsonObject.put("msg", "当前用户未绑定微信账号！");
					}
				} else {
					if (user.getThirdPartyType() == 2) {
						jsonObject.put("data", user.getThirdPartyId());
					} else {
						jsonObject.put("status", "no");
						jsonObject.put("msg", "当前用户未绑定微信账号！");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				jsonObject.put("status", "no");
				jsonObject.put("msg", "服务器错误！");
			}
		}
		return jsonObject;
	}

	@RequestMapping(value = "userPhoto", method = RequestMethod.POST)
	public @ResponseBody JSONObject userImg(@RequestParam("imgurl") MultipartFile fileToUpload,
			HttpServletRequest request) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "ok");
		long uid = 0l;
		if (StringUtils.isNotEmpty(request.getHeader(Constants.UID))
				&& (uid = Long.parseLong(request.getHeader(Constants.UID))) > 0) {
			CommonsMultipartFile file = (CommonsMultipartFile) fileToUpload;
			String fileName = "";
			if (file.getSize() != 0) {
				String obj = imageDir;
				try {
					fileName = iimageService.postImgToOSS(file.getInputStream(), obj);
					StarUser user = starUserMapper.getStarUserByUid(uid);
					user.setPicUrl(fileName);
					starUserMapper.updateByPrimaryKey(user);
					// 2015.12.07 zilong
					Map<String, String> map_star = new HashMap<String, String>();
					map_star.put("pic_url", fileName);
					jsonObject.put("img", fileName);
				} catch (IOException e) {
					jsonObject.put("status", "no");
					jsonObject.put("msg", "服务器错误！");
					e.printStackTrace();
				}
			}
		} else {
			jsonObject.put("status", "no");
			jsonObject.put("msg", "请先登录！");
		}

		return jsonObject;
	}

	/**
	 * 绑定手机号改为不更新token，因为新token机制已经保证了账户安全性 by 小芊 2017年7月25日下午3:06:59
	 * 
	 * @param request
	 * @param phone
	 * @param captcha
	 * @param appKey
	 * @return
	 */
	@RequestMapping("bindPhone")
	public @ResponseBody JSONObject bindPhone(HttpServletRequest request, String phone, String captcha, String appKey) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "ok");
		long uid = 0L;
		if (StringUtils.isNotEmpty(request.getHeader(Constants.UID))
				&& (uid = Long.parseLong(request.getHeader(Constants.UID))) > 0 && StringUtils.isNotEmpty(phone)) {

			// 0.验证手机号码格式
			Pattern p = Pattern.compile("^((1[3,5,7,8]))\\d{9}$");
			Matcher m = p.matcher(phone);
			if (!m.matches()) {
				jsonObject.put("status", "no");
				jsonObject.put("msg", "请输入正确的手机号码！");
				return jsonObject;
			}
			String code = redisService.getUserCode(phone);
			if (code == null || !code.equals(captcha)) {
				jsonObject.put("status", "no");
				jsonObject.put("msg", "验证码错误！");
				return jsonObject;
			}
			// 1.验证是否是主账户
			StarUser mainAccount = starUserMapper.getStarUserByPhone(phone);
			StarUser user = starUserMapper.getStarUserByUid(uid);
			if (mainAccount != null && mainAccount.getUserId() == uid) {
				// 覆盖当前手机号码,暂时不开通
				jsonObject.put("status", "no");
				jsonObject.put("msg", "暂时不支持更换手机号码，如有需要请联系客服！");
				return jsonObject;
			} else if (mainAccount != null && mainAccount.getUserId() != uid) {// 存在此手机号码的主账户
				user.setBindto(mainAccount.getUserId());
				starUserMapper.updateByPrimaryKey(user);
				return jsonObject;
			} else {
			}
			// 2.验证当前账号是否绑定过
			StarUser bindToUser = null;
			Long bindTo = 0l;
			if (StringUtils.isNotEmpty(user.getThirdPartyId()) && (bindTo = user.getBindto()) != null
					&& (bindToUser = starUserMapper.getStarUserByUid(bindTo)) != null) {
				if (StringUtils.isNotEmpty(bindToUser.getUserPhone()) && bindToUser.getUserPhone().equals(phone)) {// 此是第三方登录账号，已经绑定过
					jsonObject.put("status", "no");
					jsonObject.put("msg", "此用户已经绑定过此手机号码！");
					return jsonObject;
				} else {// 未绑定过此手机号码
					bindToUser.setUserPhone(phone);
					starUserMapper.updateByPrimaryKey(bindToUser);
					return jsonObject;
				}
			} else if (StringUtils.isNotEmpty(user.getThirdPartyId())) {// 生成主账户，用手机号码登录
				try {
					StarUser starUser = new StarUser();
					starUser.setUserId(0l);
					starUser.setUserPhone(phone);
					starUser.setNickname(phone.replace(phone.substring(3, 7), "****"));
					starUser.setPicUrl(user.getPicUrl());
					starUser.setUserPwd(StringHelper.md5ApacheCommonsCodec(phone));
					starUserMapper.insertSelective(starUser);
					starUser = starUserMapper.selectByPrimaryKey(starUser.getId());
					user.setBindto(starUser.getUserId());
					starUserMapper.updateByPrimaryKey(user);
					return jsonObject;
				} catch (Exception e) {
					jsonObject.put("status", "no");
					jsonObject.put("msg", "服务器错误！");
					return jsonObject;
				}
			}
		} else {
			jsonObject.put("status", "no");
			jsonObject.put("msg", "参数错误！[uid = " + request.getHeader(Constants.UID) + ", phone = " + phone + "]");
		}
		return jsonObject;
	}

	/**
	 * 验证是否主账号
	 * 
	 * @return
	 */
	@RequestMapping("checkAccount")
	public @ResponseBody JSONObject checkIfMain(HttpServletRequest request) {
		publicVerify.checkSignValid(request.getParameter(Constants.APPKEY), request.getParameter(Constants.SIGN),
				Long.parseLong(request.getParameter("time")), "");
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "ok");
		long uid = 0L;
		if (StringUtils.isNotEmpty(request.getHeader(Constants.UID))
				&& (uid = Long.parseLong(request.getHeader(Constants.UID))) > 0) {
			StarUser user = starUserMapper.getStarUserByUid(uid);
			if (StringUtils.isEmpty(user.getUserPhone()) && user.getBindto() == null) {
				jsonObject.put("status", "no");
				jsonObject.put("msg", "用户未绑定手机号码！");
			} else {
				String phone = user.getUserPhone();
				if (StringUtils.isEmpty(phone)) {
					user = starUserMapper.getStarUserByUid(user.getBindto());
					if (user != null && StringUtils.isNotEmpty(user.getUserPhone())) {
						phone = user.getUserPhone();
					} else {
						jsonObject.put("status", "no");
						jsonObject.put("msg", "用户未绑定手机号码！");
					}
				}
				jsonObject.put("data", phone);
			}
		} else {
			jsonObject.put("status", "no");
			jsonObject.put("msg", "参数错误！[uid = " + request.getHeader(Constants.UID) + "]");
		}
		return jsonObject;
	}


	/**
	 * 绑定极光唯一设备号
	 * 
	 * @param bindRegistrationIDVo
	 * @param bindingResult
	 * @return
	 */
	@ApiOperation(value = "绑定极光唯一设备号", notes = "绑定极光唯一设备号", tags = { "binde_registrationId" })
	@RequestMapping(value = "/bind/registrationId", method = RequestMethod.POST)
	public @ResponseBody JSONObject bindeRegistrationId(
			@ApiParam(name = "uid", value = "用户id", required = true) @RequestHeader(value = "uid") String uid,

			@ApiParam(name = "token", value = "登录标示", required = true) @RequestHeader(value = "token") String token,

			@ApiParam(name = "loginVo", value = "第三方登录实体", required = true) @RequestBody(required = true) @Validated BindRegistrationIDVo bindRegistrationIDVo,
			BindingResult bindingResult) {
		JSONObject jsonObject = new JSONObject();

		Map<String, Object> resultMap = BeanCheckUtil.bindingResultCheck("status", "msg", bindingResult);
		if (null != resultMap) {
			jsonObject.putAll(resultMap);
			return jsonObject;
		}

		userService.bindRegistrationId(Long.parseLong(uid),bindRegistrationIDVo.getRegistrationId(),bindRegistrationIDVo.getRegistrationType());

		return jsonObject;
	}

	/**
	 * 推送消息
	 * 
	 * @return
	 */
	@ApiOperation(value = "推送jpush消息", notes = "推送jpush消息", tags = { "send_jpushMsg" })
	@RequestMapping(value = "/send/jushMsg", method = RequestMethod.POST)
	public @ResponseBody JSONObject sendJPushMsg(@RequestBody(required = true) JPushMsgVO jPushMsgVO) {
		JSONObject jsonObject = new JSONObject();

		try {
			System.out.println("jushMsgSend:" + JSONObject.toJSONString(jPushMsgVO));
			jPushService.sendMsg(jPushMsgVO.getBusType(), jPushMsgVO.getOrder(), jPushMsgVO.getGoods(),
					jPushMsgVO.getrId());
			jsonObject.put("status", "ok");
		} catch (HeySoundException e) {
			logger.error(e.getMessage());
			jsonObject.put("status", "no");
			jsonObject.put("msg", e.getMessage());
		}

		return jsonObject;
	}

	/**
	 * 第三方登录
	 * 
	 * @param type
	 * @param loginVo
	 * @param bindingResult
	 * @return
	 */
	@ApiOperation(value = "第三方登录", notes = "第三方登录", tags = { "thirdParty_login" })
	@RequestMapping(value = "/thirdParty/{type}/n/login", method = RequestMethod.POST)
	public @ResponseBody JSONObject thirdPartyogin(
			// @ApiParam(name="loginType", value="登录类型 第三方:thirdParty
			// 账号密码:account", required=true)
			// @PathVariable String loginType,

			@ApiParam(name = "type", value = "第三方登录类型 1-qq 2-weixin 3-weibo", required = true) @PathVariable int type,

			@ApiParam(name = "loginVo", value = "第三方登录实体", required = true) @RequestBody(required = true) @Validated ThirdPartyLoginVo loginVo,
			BindingResult bindingResult) {
		JSONObject jsonObject = new JSONObject();

		Map<String, Object> resultMap = BeanCheckUtil.bindingResultCheck("status", "msg", bindingResult);
		if (null != resultMap) {
			jsonObject.putAll(resultMap);
			return jsonObject;
		}

		System.out.println("/thirdParty/{type}/n/login:" + type + "===" + JSONObject.toJSON(loginVo));
		jsonObject = userService.thirdLogin(loginVo, type);

		return jsonObject;
	}

	/**
	 * 账号密码登录
	 * 
	 * @param loginVo
	 * @param bindingResult
	 * @return
	 */
	@ApiOperation(value = "账号密码登录", notes = "账号密码登录", tags = { "account_login" })
	@RequestMapping(value = "/account/n/login", method = RequestMethod.POST)
	public @ResponseBody JSONObject accountLogin(
			// @ApiParam(name="loginType", value="登录类型 第三方:thirdParty
			// 账号密码:account", required=true)
			// @PathVariable String loginType,

			@ApiParam(name = "loginVo", value = "账号密码登录实体", required = true) @RequestBody(required = true) @Validated AccountLoginVo loginVo,
			BindingResult bindingResult) {
		JSONObject jsonObject = new JSONObject();

		Map<String, Object> resultMap = BeanCheckUtil.bindingResultCheck("status", "msg", bindingResult);
		if (null != resultMap) {
			jsonObject.putAll(resultMap);
			return jsonObject;
		}

		jsonObject = userService.accountLogin(loginVo);

		return jsonObject;
	}

	@ApiOperation(value = "发送验证码", notes = "发送验证码", tags = { "send_captcha" })
	@RequestMapping(value = "sendCaptcha", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject sendCaptcha(
			@ApiParam(name = "sendCaptchaVO", value = "发送验证码消息体", required = true) @RequestBody(required = true) @Validated SendCaptchaVO sendCaptchaVO)
			throws ErrorMsgException {
		String validateCode = null;
		JSONObject retJson = new JSONObject();
		try {
			// 签名
			Map<String, String> paramMap = new HashMap<>();
			paramMap.put("user_phone", sendCaptchaVO.getPhone());
			paramMap.put("usage", sendCaptchaVO.getUsage() + "");
			publicVerify.newCheckSign(sendCaptchaVO, sendCaptchaVO.getTime());
			// publicVerify.checkSignValid(sendCaptchaVO.getApp_key(),
			// sendCaptchaVO.getSign(), sendCaptchaVO.getTime(), paramMap);

			// 用户电话号码
			String eMessage = this.toValidatePhone(sendCaptchaVO.getPhone());
			if (eMessage != null) {
				throw new ErrorMsgException(eMessage);
			}

			if ("zucp".equals(smsType)) {// 漫道
				validateCode = ismsService.sendMsgWithValidateCode(sendCaptchaVO.getPhone());
			} else if ("testin".equals(smsType)) {// testin
				validateCode = ismsService.sendMsgWithValidateCodeByTestIn(sendCaptchaVO.getPhone());
			} else {
				// 默认使用漫道
				validateCode = ismsService.sendMsgWithValidateCode(sendCaptchaVO.getPhone());
			}

			retJson.put("success", true);
			// 添加到缓存,uvc没有来自数据库查询 //添加一天过期,秒 86400
			redisService.setUserCode(sendCaptchaVO.getPhone(), validateCode);
		} catch (Exception e) {
			logger.error("sendCaptcha error:" + e);
			// StringHelper.throwAllException(e);
			retJson.put("success", false);
			retJson.put("msg", e.getMessage());
		}

		return retJson;
	}

	@ApiOperation(value = "验证手机验证码", notes = "验证手机验证码", tags = { "varify_captcha" })
	@RequestMapping(value = "varifyCaptcha", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject varifyCaptcha(
			@ApiParam(name = "sendCaptchaVO", value = "手机验证码消息体", required = true) @RequestBody(required = true) @Validated SendCaptchaVO sendCaptchaVO)
			throws ErrorMsgException {
		JSONObject retJson = new JSONObject();
		retJson.put("success", true);
		try {
			// 签名
			String captcha = sendCaptchaVO.getUseto();
			String phone = sendCaptchaVO.getPhone();
			publicVerify.newCheckSign(sendCaptchaVO, sendCaptchaVO.getTime());

			// 用户电话号码
			String eMessage = this.toValidatePhone(sendCaptchaVO.getPhone());
			if (eMessage != null) {
				throw new ErrorMsgException(eMessage);
			}
			if (captcha == null || !captcha.equals(redisService.getUserCode(phone))) {
				retJson.put("success", false);
				retJson.put("msg", "验证码信息错误！");
				return retJson;
			} else {
				redisService.delUserCode(sendCaptchaVO.getPhone());
			}
		} catch (Exception e) {
			logger.error("varifyCaptcha error:" + e);
			retJson.put("success", false);
			retJson.put("msg", e.getMessage());
		}
		return retJson;
	}


	@RequestMapping("passUser")
	public @ResponseBody JSONObject passUser(String userId, boolean isPass) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "ok");
		if (StringUtils.isNotEmpty(userId) && StringUtils.isNumeric(userId)) {
			StarUser user = starUserMapper.getStarUserByUid(Long.parseLong(userId));
			if (user == null) {
				jsonObject.put("status", "no");
				jsonObject.put("msg", "用户不存在！[userId = " + userId + "]");
			} else {
				user.setRealname(isPass ? "OK" : "NO");
				starUserMapper.updateByPrimaryKey(user);
			}
		} else {
			jsonObject.put("status", "no");
			jsonObject.put("msg", "参数错误！[userId = " + userId + "]");
		}
		return jsonObject;
	}

	@RequestMapping("getUser")
	public @ResponseBody JSONObject getCurUser(HttpServletRequest request) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "ok");
		Map<String, String> userMap = redisService.getUserInfo(Long.parseLong(request.getHeader(Constants.UID)));
		StarUser user = starUserMapper.getStarUserByUid(Long.parseLong(request.getHeader(Constants.UID)));
		if (userMap == null || userMap.isEmpty()) {
			List<StarInfo> list = starUserMapper.findBrandLogoAndMonthAndFollowing(user.getUserId());
			int mark = 0;
			if (list != null && !list.isEmpty()) {
				mark = 1;
			}
			StarBaseVO starBaseVO = new StarBaseVO(user.getUserId() + "", user.getNickname(), user.getPicUrl(), mark);
			redisService.setUserInfo(starBaseVO);
		} else {
			userMap.clear();
		}
		userMap.putAll(JSONObject.toJavaObject((JSON) JSONObject.toJSON(user), Map.class));
		try {
			BeanUtils.copyProperties(userMap, user);
			if (userMap.get("userPwd") != null) {
				userMap.remove("userPwd");
				userMap.put("userPwd", "true");
			} else {
				userMap.put("userPwd", "false");
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			jsonObject.put("status", "no");
			jsonObject.put("msg", "亲，见谅！服务器抽风了！");
			return jsonObject;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			jsonObject.put("status", "no");
			jsonObject.put("msg", "亲，见谅！服务器抽筋了！");
			return jsonObject;
		}
		boolean identificationed = false;
		boolean binded = true;
		if (StringUtils.isNotEmpty(user.getUserPhone())) {// 主账号
			identificationed = StringUtils.isNotEmpty(user.getRealname()) && user.getRealname().equals("YES");
		} else {
			binded = (user.getBindto() != null);
			if (binded) {
				StarUser bindUser = starUserMapper.getStarUserByUid(user.getBindto());
				userMap.put("userPhone", bindUser.getUserPhone());
				identificationed = StringUtils.isNotEmpty(bindUser.getRealname())
						&& bindUser.getRealname().equals("YES");
			}
		}
		StarUserExt ext = starUserExtMapper.selectByUserId(Long.parseLong(request.getHeader(Constants.UID)),
				Constants.star_user_ext_key_withdraw);
		boolean withdraw = ext != null;
		userMap.put("identification", identificationed + "");
		userMap.put("binded", binded + "");
		userMap.put("withdraw", withdraw + "");
		//add by 小芊  2017-11-10 16：05：00
		if(starUserExtMapper.selectByUserId(user.getUserId(), "applyShop") != null){
			userMap.put("applyShop", "1");//已申请
		}else{
			userMap.put("applyShop", "0");//未申请 
		}
			
		List<Follow> follow = mongoTemplate
				.find(Query.query(Criteria.where("userId").is(request.getHeader(Constants.UID))), Follow.class);
		int funs = 0;
		int Ifollow = 0;
		if (follow != null && follow.size() > 0) {
			String followed = follow.get(0).getFollowd();
			String following = follow.get(0).getFollowing();
			if (StringUtils.isNotEmpty(followed)) {
				funs = followed.split(",").length;

			}
			if (StringUtils.isNotEmpty(following)) {
				Ifollow = following.split(",").length;
			}
		}
		// 查一遍是否当过代言人
		List<StarInfo> list = userService.findBrandLogoAndMonthAndFollowing(user.getUserId());
		if (list != null && !list.isEmpty()) {
			userMap.put("mark", "1");
		} else {
			userMap.put("mark", "0");
		}
		userMap.put("funs", funs + "");
		userMap.put("follow", Ifollow + "");
		jsonObject.put("data", userMap);
		return jsonObject;
	}

	/**
	 * 昵称去重校验
	 * 
	 * @return
	 */
	@RequestMapping("checkName")
	public @ResponseBody JSONObject checkNickName(String nickname) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "ok");
		boolean success = true;
		StarUser star = starUserMapper.getUserByName(nickname);
		if (star != null) {
			success = false;
		}
		jsonObject.put("available", success);
		return jsonObject;
	}

	@RequestMapping("searchOne")
	public @ResponseBody JSONObject searchStars(String nickname, int pageNum) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "ok");
		try {
			int pageIndex = ((pageNum - 1) * ConstantType.PAGE_SIZE < 0) ? 0 : (pageNum - 1) * ConstantType.PAGE_SIZE;
			List<StarUser> users = starUserMapper.getUserByNickNamePattern(nickname.trim(), pageIndex,
					ConstantType.PAGE_SIZE);
			List<StarBaseVO> stars = new ArrayList<>();
			for (StarUser user : users) {
				StarBaseVO vo = new StarBaseVO(user.getUserId() + "", user.getNickname(), user.getPicUrl());
				stars.add(vo);
			}
			jsonObject.put("data", stars);
		} catch (Exception e) {
			jsonObject.put("status", "no");
			jsonObject.put("msg", "服务器错误！");
			e.printStackTrace();
		}
		return jsonObject;
	}

	@RequestMapping("getOStar")
	public @ResponseBody JSONObject getOneStar(Long starId) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "ok");
		try {
			if (starId != null && starId > 0) {
				StarUser user = starUserMapper.getStarUserByUid(starId);
				if (user != null) {
					StarBaseVO vo = new StarBaseVO(user.getUserId() + "", user.getNickname(),
							user.getPicUrl() + "@120h_120w_1e_1c_1l");// 增加阿里云图片处理后缀
					user.setUserPhone("");
					user.setUserPwd("");
					jsonObject.put("data", user);
				} else {
					jsonObject.put("status", "no");
					jsonObject.put("msg", "该用户找不到了，亲！");
				}
			}
		} catch (Exception e) {
			jsonObject.put("status", "no");
			jsonObject.put("msg", "服务器错误！");
			e.printStackTrace();
			return jsonObject;
		}
		return jsonObject;
	}

	/**
	 * 分页获取所有明星列表，只返回明星基本信息
	 * 
	 * @param pageNum
	 * @return
	 */
	@RequestMapping("getUsers")
	public @ResponseBody JSONObject getUser(int pageNum) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "ok");
		try {
			int pageIndex = ((pageNum - 1) < 0) ? 0 : (pageNum - 1) * ConstantType.PAGE_SIZE;
			List<StarUser> users = starUserMapper.getUserInfoByPage(pageIndex, ConstantType.PAGE_SIZE);
			List<StarBaseVO> stars = new ArrayList<>();
			for (StarUser user : users) {
				StarBaseVO vo = new StarBaseVO(user.getUserId() + "", user.getNickname(),
						user.getPicUrl() + "@120h_120w_1e_1c_1l");// 增加阿里云图片处理后缀
				Follow follows = mongoTemplate.findOne(Query.query(Criteria.where("userId").is(user.getUserId() + "")),
						Follow.class);
				if (follows != null) {
					String followed = follows.getFollowd();
					if (StringUtils.isNotEmpty(followed)) {
						vo.setFollowed(followed.split(",").length);
					}
				}
				stars.add(vo);
			}
			jsonObject.put("data", stars);
		} catch (Exception e) {
			jsonObject.put("status", "no");
			jsonObject.put("msg", "服务器错误！");
			e.printStackTrace();
		}
		return jsonObject;
	}

	/**
	 * 获取用户列表信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getUserlist")
	public @ResponseBody JSONObject getUserlist(HttpServletRequest request, int pageNum) {
		JSONObject resp = new JSONObject();

		try {
			// 判断参数是否为空
			if (resp.get("error") != null) {
				logger.error(XServer.createReturnData(request, resp));
				return resp;
			}
			int pageIndex = ((pageNum - 1) * ConstantType.PAGE_SIZE < 0) ? 0 : (pageNum - 1) * ConstantType.PAGE_SIZE;
			// 获取用户列表
			resp = userService.getUserList(pageIndex);
			if (resp.get("error") != null) {
				logger.error(XServer.createReturnData(request, resp));
				return resp;
			}
			resp.put("success", true);
		} catch (Exception e) {
			e.printStackTrace();
			resp.put("error", EActionResult.EXCEPTION.getValue());
			resp.put("msg", EActionResult.EXCEPTION.getStr());
			logger.error(XServer.createReturnData(request, resp, e.getMessage()));
		}

		return resp;
	}

	@RequestMapping(value = "/updateUserInfo", method = RequestMethod.POST)
	public @ResponseBody JSONObject updateUserInfo(HttpServletRequest request, @RequestBody UserCenterInfoVO info) {
		// publicVerify.newCheckSign(info, info.time);
		JSONObject resp = new JSONObject();

		try {
			String nickname = info.nickname;
			String id = request.getHeader(Constants.UID);
			String sex = info.sex;
			String age = info.age;
			String sign = info.starSign;
			String phone = info.phone;
			String pwd = info.pwd;
			String captcha = info.captcha;
			String picUrl = info.picUrl;
			String location = info.location;
			if (pwd != null) {
				if (captcha == null || !captcha.equals(redisService.getUserCode(phone))) {
					resp.put("success", false);
					resp.put("msg", "验证码信息错误！");
					return resp;
				}
			}

			// 修改用户信息
			resp = userService.updateUserInfo(nickname, id, sex, age, phone, pwd, sign, picUrl, location);
			if (resp.get("error") != null) {
				logger.error(XServer.createReturnData(request, resp));
				return resp;
			}
			resp.put("success", true);
		} catch (Exception e) {
			e.printStackTrace();
			resp.put("error", EActionResult.EXCEPTION.getValue());
			resp.put("msg", EActionResult.EXCEPTION.getStr());
			logger.error(XServer.createReturnData(request, resp, e.getMessage()));
		}

		return resp;
	}

	// 使用sms厂商
	@Value("${sms.producer}")
	private String smsType;

	@RequestMapping(value = "checkToken", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject checkToken(@RequestBody(required = true) LoginVO loginVO) {

		JSONObject retJson = new JSONObject();
		// token是否失效
		try {
			if (StringUtils.isNotEmpty(loginVO.reuid) && StringUtils.isNotEmpty(loginVO.retoken)) {
				retJson.put("user_id", loginVO.reuid);
				retJson.put("token", loginVO.retoken);
				retJson.put("appKey", loginVO.app_key);
				logger.info("****************************************checktoken loginVO : " + loginVO.toString());
				userService.checkToken(retJson);
				retJson.put("success", "true");
			}

		} catch (Exception e) {
			// logger.error("获取支付凭证异常", e);
			e.printStackTrace();
			retJson.put("success", "false");

			// throw new ErrorMsgException(ActionResult.SYS_9007.getValue());//
			// 抛出异常
		}
		return retJson;
	}


	/**
	 * 获取验证手机
	 *
	 * @param phone
	 * @return
	 */
	private String toValidatePhone(String phone) {
		// 手机验证
		if (StringUtils.isEmpty(phone)) {
			return ActionResult.LOGIN_1001.getValue();
		}
		if (!StringHelper.isMobile(phone)) {
			return ActionResult.LOGIN_1003.getValue();
		}
		return null;
	}
	
	@RequestMapping(value = "realName2", method = RequestMethod.POST)
	public @ResponseBody JSONObject realNameBind(@RequestBody RealNameVO realNameVO) {
		publicVerify.newCheckSign(realNameVO, realNameVO.time);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "ok");
		try {
			StarUser starUser = null;
			String realName = null;
			String idCard = null;
			String idCardpics = null;
			long starId = 0l;
			if (StringUtils.isNotEmpty(realNameVO.starId) && (starId = Long.parseLong(realNameVO.starId)) > 0
					&& (starUser = starUserMapper.getStarUserByUid(starId)) != null) {
				StarRealname nam = starRealnameMapper.selectPassedByUserId(starId);
				if (null == nam) {
					// 判断主账号的实名认证
					StarUser bindUser = null;
					if (starUser.getBindto() != null
							&& (bindUser = starUserMapper.getStarUserByUid(starUser.getBindto())) != null) {
						if ("YES".equals(bindUser.getRealname())) {
							// 主账号如果实名认证过，可以不进行实名认证
							jsonObject.put("status", "no");
							jsonObject.put("msg", "您已通过实名认证，无需重复操作！");
						}
					}
					if (StringUtils.isNotEmpty(realName = realNameVO.realName)
							&& StringUtils.isNotEmpty(idCard = realNameVO.idCard)
							&& StringUtils.isNotEmpty(idCardpics = realNameVO.idCardpics)) {
						StarRealname starRealname = new StarRealname();
						starRealname.setUserId(bindUser == null ? starUser.getUserId() : bindUser.getUserId());
						starRealname.setRealname(realName);
						starRealname.setIdcard(idCard);
						starRealname.setIdcardpics(idCardpics);
						Map<String, Object> params = new HashMap<>();
						params.put("realname", realName);
						params.put("idcard", idCard);
						JSONObject result = userService.checkRealName(realnameurl, params);
						System.out.println("realname result:" + JSONObject.toJSONString(result));
						if (result.getJSONObject("data").getString("code").equals("1000")) {
							starRealname.setStatus((byte) 1);
							starRealnameMapper.insertSelective(starRealname);

							if (bindUser == null) {
								starUser.setRealname("YES");
								starUserMapper.updateByPrimaryKeySelective(starUser);
							} else {
								bindUser.setRealname("YES");
								starUserMapper.updateByPrimaryKeySelective(bindUser);
							}
						} else {
							starRealname.setStatus((byte) 0);
							starRealnameMapper.insertSelective(starRealname);
							if (bindUser == null) {
								starUser.setRealname("NO");
								starUserMapper.updateByPrimaryKeySelective(starUser);
							} else {
								bindUser.setRealname("NO");
								starUserMapper.updateByPrimaryKeySelective(bindUser);
							}
							jsonObject.put("status", "no");
							jsonObject.put("msg", "实名认证未通过，请再次提交！");
						}
					}
				} else {
					jsonObject.put("status", "no");
					jsonObject.put("msg", "您已通过实名认证，无需重复操作！");
				}
			} else {
				jsonObject.put("status", "no");
				jsonObject.put("msg", "参数错误！-- " + JSONObject.toJSONString(realNameVO));
			}
		} catch (RealNameException ex) {
			jsonObject.put("status", "no");
			jsonObject.put("msg", ex.getMessage());
		} catch (Exception e) {
			jsonObject.put("status", "no");
			jsonObject.put("msg", "服务器错误！");
		}
		return jsonObject;
	}

	@RequestMapping(value = "realName", method = RequestMethod.POST)
	public @ResponseBody JSONObject realName(@RequestBody RealNameVO realNameVO) {
		publicVerify.newCheckSign(realNameVO, realNameVO.time);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "ok");
		StarUser starUser = null;
		String realName = null;
		String idCard = null;
		String idCardpics = null;
		if (StringUtils.isNotEmpty(realNameVO.starId) && Long.parseLong(realNameVO.starId) > 0
				&& (starUser = starUserMapper.getStarUserByUid(Long.parseLong(realNameVO.starId))) != null
				&& StringUtils.isNotEmpty(realName = realNameVO.realName)
				&& StringUtils.isNotEmpty(idCard = realNameVO.idCard)
				&& StringUtils.isNotEmpty(idCardpics = realNameVO.idCardpics)) {
			long bindTo = 0l;
			StarUser bindUser = null;
			if (StringUtils.isEmpty(starUser.getUserPhone()) && (bindTo = starUser.getBindto()) > 0
					&& (bindUser = starUserMapper.getStarUserByUid(bindTo)) != null) {
				bindUser.setRealname(realName);
				bindUser.setIdcard(idCard);
				starUserMapper.updateByPrimaryKey(bindUser);
			} else if (StringUtils.isNotEmpty(starUser.getUserPhone()) && StringUtils.isEmpty(starUser.getRealname())) {
				starUser.setRealname(realName);
				starUser.setIdcard(idCard);
				starUserMapper.updateByPrimaryKey(starUser);
			} else if (StringUtils.isNotEmpty(starUser.getUserPhone())
					&& StringUtils.isNotEmpty(starUser.getRealname())) {
				jsonObject.put("status", "no");
				jsonObject.put("msg", "您已进行过实名认证！");
			} else {
				jsonObject.put("status", "no");
				jsonObject.put("msg", "数据出错！");
			}
		} else {
			jsonObject.put("status", "no");
			jsonObject.put("msg", "参数错误！-- " + JSONObject.toJSONString(realNameVO));
		}
		return jsonObject;
	}


}
