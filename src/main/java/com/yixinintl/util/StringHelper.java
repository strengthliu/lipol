package com.yixinintl.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

	public static String generateUUID() {
		String uuIdStr = UUID.randomUUID().toString().replaceAll("-", "");
		return uuIdStr;
	}

	/**
	 * 手机号验证
	 * 
	 * @param str
	 * @return 验证通过返回true
	 */
	public static boolean isMobile(String str) {
		Pattern p = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$");
		Matcher m = p.matcher(str);
		return m.matches();

	}

	// 生成订单号
	public static String genOrderNO() {
		Date date = new Date();
		SimpleDateFormat dt1 = new SimpleDateFormat("yyyyMMddHHmmss");
		return dt1.format(date) + randomString(6);
	}
	// 添加过期时间
	public static Date addDateMonths(Date date, int months) {
		if (date == null || date.compareTo(new Date()) < 0) {// 或者过期时间已经小于当前时间,都设置起始时间为当前时间
			date = new Date();
		}
		return DateUtils.addMonths(date, months);
	}

	/**
	 * 密码验证 //验证密码 6---20位
	 * 
	 * @param str
	 * @return 验证通过返回true
	 */
	public static boolean isPwd(String str) {
		Pattern p = null;
		Matcher m = null;
		boolean b = false;
		p = Pattern.compile("^[a-zA-Z0-9]{6,20}$");
		m = p.matcher(str);
		b = m.matches();
		return b;
	}

	// string to md5
	public static String md5ApacheCommonsCodec(String content) {
		return content == null ? null : DigestUtils.md5Hex(content
				+ ConstantType.salt);
	}
	
	  /**
	    * @param plainText  明文
	    * @return 32位密文
	    */
	   public static String md5_32(String plainText) {
	       String re_md5 = new String();
	       try {
	           MessageDigest md = MessageDigest.getInstance("MD5");
	           md.update(plainText.getBytes());
	           byte b[] = md.digest();

	           int i;

	           StringBuffer buf = new StringBuffer("");
	           for (int offset = 0; offset < b.length; offset++) {
	               i = b[offset];
	               if (i < 0)
	                   i += 256;
	               if (i < 16)
	                   buf.append("0");
	               buf.append(Integer.toHexString(i));
	           }

	           re_md5 = buf.toString();

	       } catch (NoSuchAlgorithmException e) {
	           e.printStackTrace();
	       }
	       return re_md5;
	   }


	// return "" string
	public static String getStringIfNull(String content) {
		return StringUtils.isEmpty(content) ? "" : content;
	}

	public static void main(String[] args) {
		String str = "";// "13257175341";
		System.out.println(md5ApacheCommonsCodec(str).length());
	}

	public static String[] parseArray(String strs, String s) {
		if (strs == null || strs.length() == 0) {
			return null;
		}
		String[] ret = strs.split(s);
		return ret;
	}

	// 判断密码是否正确
//	public static JSONObject checkPwd(String pwd) {
//		JSONObject ret = new JSONObject();
//
//		// 密码6-20位
//		if (pwd.length() < 6 || pwd.length() > 20) {
//			ret.put("code", EActionResult.PWD_LENGTH_ERR.getValue());
//			return ret;
//		}
//
//		// 判断密码是否由字母，数字组成
//		if (!StringHelper.isPwd(pwd)) {
//			ret.put("code", EActionResult.SETPWD_ERR.getValue());
//			return ret;
//		}
//
//		//ret.put("code", EActionResult.SUCCESS.getValue());
//		return ret;
//	}

	/**
	 * 获取间隔天数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
//	public static int getDifferenceDays(Date startDate, Date endDate) {
//		int daysdiff = 0;
//		long diff = endDate.getTime() - startDate.getTime();
//		long diffDays = diff / (24 * 60 * 60 * 1000);// + 1
//		daysdiff = (int) diffDays;
//		return daysdiff;
//	}
//
//	public static int getDifferenceDays(long time, Date endDate) {
//		int daysdiff = 0;
//		Date startDate = new Date(time);
//		long diff = endDate.getTime() - startDate.getTime();
//		long diffDays = diff / (24 * 60 * 60 * 1000);// + 1
//		daysdiff = (int) diffDays;
//		return daysdiff;
//	}

	//获取间隔的天数
	public static long getDaysBetween(Date startDate, Date endDate) {
		Calendar fromCalendar = Calendar.getInstance();
		fromCalendar.setTime(startDate);
		fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
		fromCalendar.set(Calendar.MINUTE, 0);
		fromCalendar.set(Calendar.SECOND, 0);
		fromCalendar.set(Calendar.MILLISECOND, 0);

		Calendar toCalendar = Calendar.getInstance();
		toCalendar.setTime(endDate);
		toCalendar.set(Calendar.HOUR_OF_DAY, 0);
		toCalendar.set(Calendar.MINUTE, 0);
		toCalendar.set(Calendar.SECOND, 0);
		toCalendar.set(Calendar.MILLISECOND, 0);

		return  ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
	}
	
	// 获取随机数
	public static int getRandomNum(int num) {
		return (int) ((Math.random() * 9 + 1) * num);// 10000
	}

	/* 
	 * 返回长度为【strLength】的随机数，在前面补0 
	 */  
	public static String getRandonNumLen(int strLength) {  
	      
	    Random rm = new Random();  
	      
	    // 获得随机数  
	    double pross = (1 + rm.nextDouble()) * Math.pow(10, strLength);  
	  
	    // 将获得的获得随机数转化为字符串  
	    String fixLenthString = String.valueOf(pross);  
	  
	    // 返回固定的长度的随机数  
	    return fixLenthString.substring(1, strLength + 1);  
	}  
	
	public static String format(Date d) {
		if (d == null) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(d);
	}

	/**
	 * 字符串转换为日期格式
	 * 
	 * @param property
	 * @param pattern
	 * @return
	 * @throws Exception
	 */
	public static String dateToString(Date property, String pattern) {
		if (!StringUtils.isEmpty(property)) {
			if (!StringUtils.isEmpty(pattern)) {
				return new SimpleDateFormat(pattern).format(property);
			}
		}
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(property);
	}

	/**
	 * 获取一个月最后一天日期
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	public static Date getLastDateOfMonth(int year, int month) {
		Calendar calendar = Calendar.getInstance();
		// passing month-1 because 0-->jan, 1-->feb... 11-->dec
		calendar.set(year, month - 1, 1);
		calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
		Date date = calendar.getTime();
		return date;
	}

	/**
	 * 字符串to Date
	 * 
	 * @param dateStr
	 * @param pattern
	 * @return
	 * @throws ParseException
	 */
	public static Date dateStrToDate(String dateStr, String pattern) {
		if (!StringUtils.isEmpty(dateStr)) {
			if (!StringUtils.isEmpty(pattern)) {
				try {
					return new SimpleDateFormat(pattern).parse(dateStr);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = format.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * 日期添加天数
	 * 
	 * @param dt
	 * @param days
	 * @return
	 */
	public static Date toAddDay(Date dt, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.add(Calendar.DATE, days);
		return new Date(c.getTimeInMillis());
	}
	
	public static Long toAddDayOfTime(Date dt, int days) {
		return toAddDay(dt, days).getTime();
	}

	// "yyyy-MM-dd HH:mm:ss"或 "yyyy-MM-dd"
	public static String format(Long time, String pattern) {
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}

	public static String format2Date(Date d) {
		if (d == null) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(d);
	}

	public static boolean isSameDay(Date startDate, Date endDate) {
		String start = format2Date(startDate);
		String end = format2Date(endDate);

		boolean ret = false;
		if (start.equals("") || end.equals("")) {
			return ret;

		} else if (start.equals(end)) {
			ret = true;
		}

		return ret;
	}

	/**
	 * 获取间隔时间(时分秒)
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public String getHoursAndMinsSec(Date startDate, Date endDate) {
		Interval interval = new Interval(startDate.getTime(), endDate.getTime());
		Period period = interval.toPeriod();
		String retStr = period.getHours() + ":" + period.getMinutes() + ":"
				+ period.getSeconds();
		return retStr;
	}
	
	public static String[] parseString2Arr(String str){
		if(str == null || str.equals("")){
			return null;
			
		}else{
			String[] strs = str.split(",");

			return strs;
		}
	}
	
	public static List<Long> parseString2Longlist(String str){
		if(str == null || str.equals("")){
			return null;
			
		}else{
			String[] strs = str.split(",");
			List<Long> list = new ArrayList<Long>();
			
			for(int i=0;i<strs.length;i++){
				list.add(Long.valueOf(strs[i]));
			}
			
			return list;
		}
	}
	
	public static Boolean checkEmojiString(String... strs) {
		 Pattern emoji = Pattern.compile(
			        "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
			        Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
		 
		 for(int i=0;i<strs.length;i++){
			 if(strs[i] != null){
				 Matcher matcher = emoji.matcher(strs[i]);
				 if(matcher.find()){
					 return true;
				 }
			 }
		 }
		return false;
	}
	
	public static Boolean checkNumber(String ... args){
		 Pattern p = Pattern.compile("^//d*[1-9]//d*$");  
		 
		 for(int i =0;i<args.length;i++){
			 if(args[i] != null){
			     Matcher m = p.matcher(args[i]);  
			     if(!m.matches()){
			    	 return false;
			     } 
			 } 
		 }

	     return true;  
	}
	
	public static Boolean checkFloat(String ... args){
		 Pattern p = Pattern.compile("^((//d+//.//d*[1-9]//d*)|(//d*[1-9]//d*//.//d+)|(//d*[1-9]//d*))$");  
		 
		 for(int i =0;i<args.length;i++){
		     Matcher m = p.matcher(args[i]);  
		     if(!m.matches()){
		    	 return false;
		     }  
		 }

	     return true;
	}
	
	public static String getStringLen(String content,int len) {
		String retStr=content;
		if (retStr!=null && retStr.length()>len)
			retStr=content.substring(0,len);

		return retStr;
	}
	
	/**
	 * 获取页数
	 */
	public static int getPageNum(int listsize){
		int mod = listsize % ConstantType.PAGE_NUM;
		int pagenum = listsize / ConstantType.PAGE_NUM;
		return mod == 0 ? pagenum : pagenum + 1;
	}
	
	
	public static String getAllDeTokens(List<String> deviceTokens){
		 StringBuffer contentBuffer = new StringBuffer();
		    for (String dtoken: deviceTokens) {
		        contentBuffer.append(dtoken);
		        contentBuffer.append("\n"); 
		    }
		    
		 return contentBuffer.toString();

	}

	public static boolean isGreaterThan(long time) {
		return (System.currentTimeMillis()-time)/1000>3000000;//300秒
	}


	public static final String elements = "0123456789abcdefghijklmnopqrstuvwxyz";

	public static String randomString(int len) {
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(elements.charAt(rnd.nextInt(elements.length())));
		return sb.toString();
	}

	//增加日期
	public static Date plusMonth(int months){
		Date newDay=new DateTime().plusMonths(months).toDate();
		return newDay;
	}

	// 抛出异常
	public static void throwMsgException(String errorCode)
			throws ErrorMsgException {
		throw new ErrorMsgException(errorCode);
	}

	// 抛出异常
	public static void throwAllException(Exception e) {
		if (!(e instanceof ErrorMsgException)) {
			throw new ErrorMsgException(ActionResult.SYS_9007.getValue());// 抛出业务异常代码
		}
		throw new ErrorMsgException(e.getMessage());// 抛出系统异常
	}
}
