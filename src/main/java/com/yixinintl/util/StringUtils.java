package com.yixinintl.util;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class StringUtils {

	public static int DEFALUT_SIZE = 16;

	public static String[] newSplit(String source, String separator){
		ArrayList<String> list = new ArrayList<>();
		int lastMatch = 0;
		char[] seq = separator.toCharArray();
		int step = seq.length;
		int pos = 0;
		cond : while(source.length() >= 0){
			if((pos = source.indexOf(separator)) > -1){
				list.add(source.substring(0, pos));
				source = source.substring(pos + step);
			}else{
				list.add(source);
				break;
			}
		}
		String[] result = new String[list.size()];
		return list.toArray(result);
	}

	/**
	 * 以“,”为分隔符的字符串转数组
	 * 
	 * @param param
	 * @return
	 */
	public static String[] splitStringToArray(String property, String separator)
			throws Exception {
		if (separator == null || separator.length() == 0)
			return property.split(",");
		else
			return property.split(separator);
	}

	/**
	 * 字符数组转换为List
	 * 
	 * @param properties
	 * @return
	 */
	public static List<String> stringArrayToList(String properties[]) {
		if (properties.length > 0)
			return Arrays.asList(properties);
		else
			return null;
	}

	/**
	 * 拼SQL like 字符串转换
	 * 
	 * @param name
	 * @param type
	 *            类型：LEFT(左匹配)，RIGHT(右匹配)，FULL(全匹配)
	 * @return
	 */
	public static String like(String property) throws Exception {
		if (isNotEmpty(property)) {
			return String.format("%%%s%%", property);
		}
		return "";
	}

	/**
	 * 判断字符串是否为空
	 * 
	 * @param property
	 * @return
	 */
	public static boolean isNotEmpty(String property) {
		return property != null && property.trim().length() > 0 ? true : false;
	}

	/**
	 * 判断对象是否为空
	 * 
	 * @param property
	 * @return
	 */
	public static boolean isNotEmpty(Object property) {
		return property != null ? true : false;
	}

	// 获取几天前日期
	public static String getDayBefore7Today() throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -6);// 总共7天
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(calendar.getTime());
		return dateStr;
	}

	/**
	 * 获取3天后日期
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getDayAfter3Today() throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, +3);// 总共7天
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(calendar.getTime());
		return dateStr;
	}

	/**
	 * 判断布尔类型是否为空
	 * 
	 * @param property
	 * @return
	 */
	public static boolean isNotEmpty(Boolean property) {
		return property != null && property;
	}

	/**
	 * 判断日期是否为空
	 * 
	 * @param property
	 * @return
	 */
	public static boolean isNotEmpty(Date property) {
		return property != null ? true : false;
	}

	/**
	 * 判断数组是否为空
	 * 
	 * @param property
	 * @return
	 */
	public static boolean isNotEmpty(Object[] properties) {
		return properties != null && properties.length > 0 ? true : false;
	}

	/**
	 * 判断数组是否为空
	 * 
	 * @param property
	 * @return
	 */
	public static boolean isEmpty(Object properties) {
		return properties == null ? true : false;
	}

	/**
	 * 判断数组是否为空
	 * 
	 * @param property
	 * @return
	 */
	public static boolean isEmpty(String properties) {
		return properties == null || properties.length() == 0 ? true : false;
	}

	/**
	 * 判断字符是否包含
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public static boolean isNotContain(String property, String value) {
		return property != null && property.trim().contains(value) ? false
				: true;
	}

	/**
	 * 判断字符是否超过预设的大小
	 * 
	 * @param property
	 * @param size
	 * @return
	 */
	public static boolean isBeyondMaxSize(String property, int size) {
		return property != null && property.trim().length() > size ? true
				: false;
	}

	/**
	 * 判断字符是否超过预设的大小
	 * 
	 * @param property
	 * @param size
	 * @return
	 */
	public static boolean isBeyondMaxSize(String property) {
		return property != null && property.trim().length() > DEFALUT_SIZE ? true
				: false;
	}

	/**
	 * 判断集合是否为空
	 * 
	 * @param property
	 * @return
	 */
	public static <T> boolean isNotEmpty(Collection<T> property) {
		return property != null && property.size() > 0 ? true : false;
	}

	/**
	 * 判断数字是否为空
	 * 
	 * @param property
	 * @return
	 */
	public static boolean isNotEmpty(Integer property) {
		return property != null && property != 0 ? true : false;
	}

	
	
	/**
	 * 字符串转换为日期格式
	 * 
	 * @param property
	 * @param pattern
	 * @return
	 * @throws Exception
	 */
	public static Date string2Date(String property, String pattern)
			throws Exception {
		if (isNotEmpty(property)) {
			if (isNotEmpty(pattern)) {
				return new SimpleDateFormat(pattern).parse(property);
			}
		}
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(property);
	}

	/**
	 * 获得当前时间
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getNow() throws Exception {
		return dateToString(new Date(), "yyyyMMddHHmmss");
	}

	/**
	 * 获得当前时间
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getCurrentDate()  {
		try {
			return dateToString(new Date(), "yyyy-MM-dd");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 字符串转换为日期格式
	 * 
	 * @param property
	 * @param pattern
	 * @return
	 * @throws Exception
	 */
	public static String dateToString(String pattern) throws Exception {
		if (isNotEmpty(pattern)) {
			return new SimpleDateFormat(pattern).format(new Date());
		}
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}

	/**
	 * 字符串转换为日期格式
	 * 
	 * @param property
	 * @param pattern
	 * @return
	 * @throws Exception
	 */
	public static String dateToString(Date property, String pattern)
			throws Exception {
		if (isNotEmpty(property)) {
			if (isNotEmpty(pattern)) {
				return new SimpleDateFormat(pattern).format(property);
			}
		}
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(property);
	}

	/**
	 * 截止日期是否超过当前日期
	 * 
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static boolean compareEndDateIsBeyondCurrenDate(Date date)
			throws Exception {
		if (StringUtils.isNotEmpty(date)) {
			final SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");
			String currentDate = d.format(new Date()).replace("-", "");
			String endDate = d.format(date).replace("-", "");
			return Long.parseLong(currentDate) > Long.parseLong(endDate);
		}
		return false;
	}

	/**
	 * Object对象转Int
	 * 
	 * @param property
	 * @return
	 */
	public static int formateObjectToInt(Object property) {
		if (isNotEmpty(property)) {
			if (isNotEmpty(property.toString())) {
				return Integer.parseInt(property.toString());
			}
		}
		return 0;
	}

	/**
	 * 
	 * @return
	 */
	public static boolean isTrueExpresion(String property) {
		List<String> le = new ArrayList<String>();
		le.add("是");
		le.add("true");
		le.add("t");
		le.add("yes");
		le.add("y");
		if (le.contains(property))
			return true;
		return false;
	}

	/**
	 * 计算日期差值
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Date getNextYearToday() throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(calendar.getTime());
		return sdf.parse(dateStr);
	}

	/**
	 * 字符串转换为日期格式
	 * 
	 * @param property
	 * @param pattern
	 * @return
	 * @throws Exception
	 */
	public static Date string2Date(String property) throws Exception {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(property);
	}
	
	public static Date stringDate(String property) throws Exception {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(property+" 23:59:59");
	}

	/**
	 * 拼SQL like 默认字符串转换
	 * 
	 * @param name
	 * @param type
	 *            类型：FULL(全匹配)
	 * @return
	 */
	public static String formatLikeString(String property) throws Exception {
		if (isNotEmpty(property) && property.indexOf('%') < 0) {
			return String.format("%%%s%%", property);
		}
		return property;
	}

	/**
	 * 
	 * TODO ip转换为数字 规则：a.b.c.d ==> a*256*256*256+b*256*256+c*256+d ===>
	 * 256*(c+256*(b+256*a))+d// C# IP地址转长整数
	 * 
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public static Long ipToNum(String ip) throws Exception {
		Long nip = null;
		if (isNotEmpty(ip)) {
			String[] ips = ip.split("\\.");
			if (ips.length == 4) {
				// 256*(c+256*(b+256*a))+d
				nip = 256
						* (Long.parseLong(ips[2]) + 256 * (Long
								.parseLong(ips[1]) + 256 * Long
								.parseLong(ips[0]))) + Long.parseLong(ips[3]);
			}
		}
		return nip;
	}

	/**
	 * 
	 * TODO 判断手机是否是否正确
	 * 
	 * @param mobiles
	 * @return
	 */
	public static boolean isMobile(String mobile) {
		try {
			if (!isNotEmpty(mobile)) {
				return false;
			}
			Pattern p = Pattern
					.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
			Matcher m = p.matcher(mobile);
			return m.matches();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}
	
	public static boolean isSpecialChar(String str) {
		 if(str == null)
			 return true;
	        String regEx = "[ `~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
	        Pattern p = Pattern.compile(regEx);
	        Matcher m = p.matcher(str);
	        return m.find();
	}
	

}
