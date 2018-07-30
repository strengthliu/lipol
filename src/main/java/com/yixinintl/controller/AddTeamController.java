package com.yixinintl.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.yixinintl.utils.IDHelper;

@RestController
// @RequestMapping("/v1")
public class AddTeamController {
	
//	@Autowired
//	AddTeamMapper addTeam;
//
//	@Autowired
//	ContactMapper contact;

	@RequestMapping(value = "addTeamApply", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody JSONObject addTeamApply(@RequestBody JSONObject j, HttpServletRequest request)
			throws Exception {
		JSONObject ret = new JSONObject();
		System.out.println(j.toJSONString());
		try {
//			t1 t = new t1();
//			t.setA("aa");
//			t.setB("bb");
//			t1m.insert(t);
//			AddTeam at = new AddTeam();
//			at.setId(IDHelper.getUniqueID());
//			at.set姓名(j.getString("name"));
//			at.set性别代码(j.getString("sex"));
//			at.set性别(j.getString("sexName"));
//			at.set手机号码(j.getString("phone"));
//			at.set最佳联系时段(j.getString("contactAvailableDayName"));
//			at.set最佳联系时段代码(j.getString("contactAvailableDay"));
//			at.set最佳联系时间(j.getString("contactAvailableTimeName"));
//			at.set最佳联系时间代码(j.getString("contactAvailableTime"));
//			at.set申请人ip地址(this.getRemoteHost(request));
//			at.set申请城市(j.getString("cityName"));
//			at.set申请城市代码(j.getString("city"));
//			at.set申请城市省份(j.getString("provinceName"));
//			at.set申请城市省份代码(j.getString("province"));
//			at.set申请时间(this.getCurrentTime());
//			at.set邮箱(j.getString("email"));
			ret.put("status", "ok");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	@RequestMapping(value = "contact", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody JSONObject contact(@RequestBody JSONObject j, HttpServletRequest request)
			throws Exception {
		JSONObject ret = new JSONObject();
		
		System.out.println(j.toJSONString());
		try {
			ret.put("status", "ok");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	// public static void main(String []argc) {
	// AddTeamController ac = new AddTeamController();
	// ac.getCurrentTime();
	// }
	public String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd HH点mm分ss秒");
		String d = format.format(System.currentTimeMillis());
		System.out.println(d);
		return d;
	}

	public String getRemoteHost(javax.servlet.http.HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
	}
}
