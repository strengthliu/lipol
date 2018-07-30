package com.yixinintl.util;

import net.sf.json.util.NewBeanInstanceStrategy;

/**
 * <p>User: Zhang Kaitao
 * <p>Date: 14-2-26
 * <p>Version: 1.0
 */
public class Constants {
//  param names
    public static final String PARAM_DIGEST = "digest";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";

    public static final String TOKEN = "token";
    public static final String UID = "uid";
    public static final String KEY = "appKey";
    public static final String APPKEY = "app_key";
    public static final String SIGN = "sign";
//  redis key names
    public static final String role_prefix_ = "role_";
    public static final String login_prefix_="login_";
    public static final String resource_prefix_="os_";
    public static final String grant_prefix_from="grant_from_";
    public static final String grant_prefix_from_tmp="grant_from_tmp_";
    public static final String grant_prefix_to="grant_to_";
    public static final String grant_prefix_to_tmp="grant_to_tmp_";
    public static final String pk_group="pk_group_";
    public static final String pk_user="pk_user_";
    public static final String charging="charg_";

    public static final String video_play_times = "video_play_times_";
    public static final String guard="guard_";
    public static final String video_guard="video_guard_";
    public static final String video_guard_seat="video_guard_seat_";

    public static final String video_statistic = "video_statistic_";//hash
    public static final String user_favor = "user_favor_";//string
    public static final String video_favor = "video_favor_";//string
//  字典表缓存前缀
    public static final String dic = "dic_";//string
//  守护席位递增价格常量
    public static final Integer[] dic_guard_price = {5,4,3,2,1};

 //  calculate constans
    public static final String salt = "dadewq2ewdwadswdqdwadsadasd";
//method condition
    public static final String EXISTS_MARK = "1";

//    cache default expire
    public static final int default_expire = 86400;

    public static final String star_user_ext_key_withdraw = "withdraw";
    public static final String star_user_ext_key_applyShop = "applyShop";
	


    //需要收费
    public static String IS_CHARGE = "isCharge";

    //免费
    public static String IS_FREE = "isFree";
    
//    代言活动列表，包含全部信息，30分钟一更新，最外层缓存
    public static String ENDORSE_ACTIVITY_LIST = "endorse_activity_";
//     品牌活动列表,不包含品牌活动细节数据，更新频率很低，在运用后台变更参与品牌的时候更新
    public static String ENDORSE_ACT_BRAND_REPRESENT_LIST = "endorse_brandactrep_";
//    品牌当前代言人,每月1号更新数据库的时候更新全部
    public static String ENDORSE_CUR_REPRESENT = "endorse_cur_";
//    当前参与者的基本信息，永久
    public static String ENDORSE_PARTICIPATION = "endorse_participation_";
//    品牌活动参与人数，每30分钟更新
    public static String ENDORSE_PARTICIPATION_NUM = "endorse_participationnum_";
//    品牌活动最高个人收入，每30分钟更新
    public static String ENDORSE_MAX_INCOME = "endorse_maxincome_";
    public static Integer CACHE_PERPETUAL = 9999999;
//    我的活动收入排名，30分钟一更新
    public static String ENDORSE_MY_RANKING = "endorse_myranking_";
//    我的活动收入总额，30分钟一更新
    public static String ENDORSE_MY_INCOME = "endorse_myincome_";
//    粉丝排行列表，30分钟一更新
    public static String ENDORSE_CONTR_FUNS = "endorse_funs_";
//    参与视频排名，30分钟一更新
    public static String ENDORSE_VIDEOS = "endorse_videos_";
//    竞选代言人排名，30分钟一更新
    public static String ENDORSE_STARS = "endorse_stars_";
//    代言活动品牌活动详情，30分钟
    public static String ENDORSE_DETAILS = "endorse_details_";
//    当前参与视频,永久
    public static String ENDORSE_PARTI_VIDEO = "endorse_partivideo_";
    
    public static String[] MONTHS = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

}