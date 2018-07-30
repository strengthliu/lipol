package com.heysound.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.jpush.api.common.TimeUnit;
import com.heysound.mapper.StarAuthenticationMapper;
import com.heysound.util.ConstantType;
import com.heysound.vo.StarRankingInfoDetail;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.heysound.domain.TChargingMode;
import com.heysound.service.IRedisService;
import com.heysound.util.RedisUtils;
import com.heysound.util.StringHelper;
import com.heysound.util.type.EAppKey;
import com.heysound.util.type.EGrantType;
import com.heysound.vo.GuardSortView;
import com.heysound.vo.StarBaseVO;
import com.heysound.vo.VideoMsgVO;
import com.superstar.hsy.Constants;

@Service
//@Scope("prototype")
public class RedisServiceImpl implements IRedisService {

	private static Logger logger = Logger.getLogger(RedisServiceImpl.class.getName());

    @Autowired
	private JedisPool jedisPool;
	
	
	@Value("${msg.expire.time}")
	private int msg_expire_time;	
	@Value("${msg.top.len}")
	private int msg_top_len;
	@Value("${msg.total.len}")
	private int msg_total_len;
	@Value("${gift.list.len}")
	private int gift_list_len;

	//2017年09月01日 by gzy
	private String default_profit = "DEFAULT_PORFIT";

	private static String star_string_error = "star_string_error"; //错误信息
	private static String star_hash_video_info_ = "star_hash_video_info_"; //直播点播流信息	
	private static String star_hash_error = "star_hash_error"; //错误代码
	private static String star_string_user_token_="star_string_user_token_"; //用户登录token 
	private static String user_token_="newtoken_"; //新用户登录token 2017-07-23 by 小芊 
	private static String star_string_user_code_="star_string_user_code_"; 	 //验证码
	private static String star_string_auth_code_="star_string_auth_code_"; //授权代码
	private static String star_hash_user_ = "star_hash_user_"; //用户信息
	private static String star_zset_msg_list_ = "star_zset_msg_list_"; //留言列表
	private static String star_hash_msg_ = "star_hash_msg_"; //留言内容			
	private static String star_zset_gitf_ = "star_zset_gitf_"; //送礼排行榜	
	private static String star_string_gitf_vote_count = "star_string_gitf_vote_count"; //送礼得票数	
	private static String star_hash_name_ = "star_hash_name_"; //明星信息
	private static String star_hash_user_video_gift_ = "star_hash_user_video_gift_"; //用户送礼设置
	private static String star_zset_name_vote_ = "star_zset_name_vote_"; //得票排行榜
	private static String star_zset_seat_ = "star_zset_seat_"; //贵宾席列表
	private static String star_hash_seat_ = "star_hash_seat_"; //贵宾信息
	private static String star_hash_seat_out_ = "star_hash_seat_out_"; //被踢人信息
	private static String star_hash_pk_ = "star_hash_pk_"; //PK信息	
	private static String star_hash_pk_front = "star_hash_pk_front"; //上一轮PK信息
	private static String star_string_forbid_ = "star_string_forbid_";  //禁言用户
	private static String star_string_live_dilian_ = "star_string_live_dilian_";  //帝联直播地址
	
	private static String star_list_video_msg = "star_list_video_msg"; // 视频消息弹幕列表
	private static String live_guest = "star_map_live_guest_"; // 视频消息弹幕列表


	private static int VOTECOUNT=10;
	private static int MSGLEN=100;


	@Autowired
	private StarAuthenticationMapper authenticationMapper;

	public Jedis openRedis() {
		Jedis jedis = null;
		jedis = jedisPool.getResource();		
		return jedis;
	}
	
	public void closeRedis(Jedis jedis) {
		if (jedis != null) {
			jedis.close();
		}
	}
	
	// 管理员发送留言
	// content：留言内容
	// return 0：参数错误； -1：系统错误；1：成功
	public int sendMsg(long video_id,int msg_type,String content) 
	{
		Jedis jedis = openRedis();
		try {						
			Long rn=Long.parseLong(System.currentTimeMillis()+StringHelper.getRandonNumLen(3)+"");
			
			
	
				Map<String, String> map = new HashMap<String, String>();
				map.put("user_id",  "0");
				map.put("message_id", rn + "");
				map.put("content", content+ "");
				map.put("type", msg_type+ "");
				map.put("pic_url", "");
				map.put("nickname", "管理员");
				map.put("create_time", System.currentTimeMillis() + "");
				
				
				Transaction tx = jedis.multi();
				tx.zadd(star_zset_msg_list_+video_id , rn , rn+"");	

				tx.hmset(star_hash_msg_ + rn, map);		            	
				//设置过期时间
				tx.expire(star_zset_msg_list_+video_id, msg_expire_time);
				tx.expire(star_hash_msg_+rn, msg_expire_time);					
				tx.exec();	
				
				//只保留最新的top条
//					long count = jedis.zcard(star_zset_msg_list_+video_id);			
//					long dNum = count-msg_total_len;			
//					if ( dNum > 0 ){					
//						jedis.zremrangeByRank(star_zset_msg_list_+video_id, 0,dNum-1);					
//					}
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website sendMsg error:"+e.getMessage());				
		}
		finally{
			closeRedis(jedis);
		}
		return -1;
	}
	
	//返回最新留言
	public JSONArray getMsgList(long video_id,long max_message_id)
	{	
		Jedis jedis = openRedis();
		try {	
			
			
			JSONArray jsonarray= new JSONArray();
			
			String msg_id=System.currentTimeMillis()-(msg_expire_time*1000)+"000";
		    if(max_message_id > 0)
		    	msg_id=max_message_id+1+"";
		    
		    Set<Tuple> set=jedis.zrangeByScoreWithScores(star_zset_msg_list_+video_id, msg_id+"","+inf", 0, msg_top_len);
		    
		    for(Tuple t : set ){		    	
		    	String uuid=t.getElement();		
		    	Map<String, String> map= new HashMap<String,String>();	
		    	map = jedis.hgetAll(star_hash_msg_+uuid);
		    	if ( map!=null && map.get("user_id")!=null){
		    		JSONObject json=new JSONObject();
		    		json.put("user_id", Long.parseLong(map.get("user_id")));
		    		json.put("message_id", map.get("message_id")==null?-1: Long.parseLong(map.get("message_id")));
		    		json.put("content",map.get("content"));
		    		json.put("type", map.get("type")==null?-1: Integer.parseInt(map.get("type")));
		    		json.put("pic_url", map.get("pic_url"));
		    		json.put("nickname",map.get("nickname")); 	
		    		json.put("create_time", StringHelper.dateToString(new Date(System.currentTimeMillis()),""));
		    		jsonarray.add(json);
		    	}
		    }		    
		    return jsonarray;		    
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website getMsgList error:"+e.getMessage());				
		}
		finally{
			closeRedis(jedis);
		}		
		return null;
	}
	
	// 配置得票数
	public int setGitfVoteCount(int vote_count) {
		Jedis jedis = openRedis();
		try {
			jedis.set(star_string_gitf_vote_count, vote_count + "");
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website setGitfVoteCount error:"+e.getMessage());	
		}
		finally{
			closeRedis(jedis);
		}
		return -1;
		
	}
	
	// 返回得票配置数
	public int getGitfVoteCount() {
		Jedis jedis = openRedis();
		try {
			return Integer.parseInt(jedis.get(star_string_gitf_vote_count));			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website getGitfVoteCount error:"+e.getMessage());	
		}
		finally{
			closeRedis(jedis);
		}
		return VOTECOUNT;		
	}
	
	// 送礼
	// send_type：0 普通送礼， 1 PK送礼
	// return 0：参数错误； -1：系统错误；1：成功
	public int sendGitf(long video_id,long user_id,int name_id,int send_type) {
		Jedis jedis = openRedis();
		try {	
			int s_count=VOTECOUNT;
			try	{
				s_count = Integer.parseInt(jedis.get(star_string_gitf_vote_count));
			}
			catch(Exception ex){				
			}
			//PK送礼
			String vote_field="vote_count1";	
			if ( send_type==1){
				Map<String, String> map= new HashMap<String,String>();	
		    	map = jedis.hgetAll(star_hash_pk_+video_id);
		    	if ( map!=null && Integer.parseInt(map.get("name_id2"))==name_id){		
		    		vote_field="vote_count2";		    		
		    	}
			}	
			Transaction tx = jedis.multi();
			//设置用户名星票数					
			tx.hincrBy(star_hash_name_+name_id, "total_vote", s_count);	
			tx.zincrby(star_zset_name_vote_+video_id, s_count, name_id+"");
			//设置用户送礼数	
			tx.hincrBy(star_hash_user_video_gift_+user_id, "total_gift", 1);
			tx.zincrby(star_zset_gitf_+video_id, 1, user_id+"");			
			//PK送礼
			if ( send_type==1){
				tx.hincrBy(star_hash_pk_+video_id, vote_field, s_count);	
			}			
			
			tx.exec();
			
			//只保留最新的position_list_len条
			long count = jedis.zcard(star_zset_gitf_+video_id);			
			long dNum = count-gift_list_len;			
			if ( dNum > 0 ){					
				jedis.zremrangeByRank(star_zset_gitf_+video_id, 0,dNum-1);					
			}	
			
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website sendMsg error:" + e.getMessage());
		}
		finally{
			closeRedis(jedis);
		}
		return -1;
	}
	
	
	// 设置名星信息
	public int setStarInfo(Map<String, String> name_map) {
		Jedis jedis = openRedis();
		try {
			if (name_map.get("name_id")!=null){				
				Map<String, String> map = new HashMap<String, String>();
				
				long total_vote=name_map.get("total_vote")==null?0L:Long.parseLong(name_map.get("total_vote"));
				
				if (name_map.get("name_id")!=null)
					map.put("name_id", name_map.get("name_id"));
				if (name_map.get("video_id")!=null)
					map.put("video_id", name_map.get("video_id"));
				if (name_map.get("star_name")!=null)
					map.put("star_name", name_map.get("star_name")+"");
				if (name_map.get("pic_url")!=null)
					map.put("pic_url", name_map.get("pic_url")+"");
				if (name_map.get("total_vote")!=null)
					map.put("total_vote",total_vote+"");
				
				Transaction tx=jedis.multi();	
				tx.hmset(star_hash_name_ + name_map.get("name_id"), map);
				tx.zadd(star_zset_name_vote_+name_map.get("video_id"), total_vote, name_map.get("name_id")+"");
				tx.exec();
				
				return 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website setNameInfo error:" + e.getMessage());
		}
		finally{
			closeRedis(jedis);
		}
		return -1;
	}
	
	// 返回明星信息
	public Map<String, String> getStarInfo(int name_id) {
		Jedis jedis = openRedis();
		try {
			return jedis.hgetAll(star_hash_name_ + name_id);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis getNameInfo error:" + e.getMessage());
		}
		finally	{
			closeRedis(jedis);
		}
		return null;
	}
	
	//删除明星
	public int delStarKey(int name_id) {
		Jedis jedis = openRedis();
		try{		
			jedis.del(star_hash_name_ + name_id);
			return 1;
		}	
		catch(Exception e){
			e.printStackTrace();		
		}	
		finally	{
			closeRedis(jedis);
		}
		return 0;
	}
	

	//返回明星得票排行榜
	public JSONArray getStarList(long video_id)
	{	
		Jedis jedis = openRedis();
		try {	
			JSONArray jsonarray= new JSONArray();
		    Set<Tuple> set=jedis.zrevrangeByScoreWithScores(star_zset_name_vote_+video_id, "+inf","-inf");		
		    for(Tuple t : set ){		    	
		    	String name_id=t.getElement();		
		    	Map<String, String> map= new HashMap<String,String>();	
		    	map = jedis.hgetAll(star_hash_name_+name_id);
		    	if ( map!=null && map.get("name_id")!=null){		                
		    		JSONObject json=new JSONObject();
		    		json.put("name_id", Long.parseLong(map.get("name_id")));
		    		json.put("star_name", map.get("star_name")); 
		    		json.put("pic_url", map.get("pic_url"));
		    		json.put("video_id", map.get("video_id")==null?0:Long.parseLong(map.get("video_id")));	
		    		String total_vote=jedis.hget(star_hash_name_+name_id, "total_vote");
		    		int vc=total_vote==null?0:Integer.parseInt(total_vote);				    		
		    		json.put("vote_count", vc);		    				    		
		    		jsonarray.add(json);		    	
		    	}
		    }		    
		    return jsonarray;		    
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website getNameList error:" + e.getMessage());
		}
		finally{
			closeRedis(jedis);
		}		
		return null;
	}	
	
	
	// 设置用户送礼统计信息
	public int setVideoUserGift(Map<String, String> gift_map) {
		Jedis jedis = openRedis();
		try {
			if (gift_map.get("user_id")!=null){			
				String video_id=gift_map.get("video_id")==null?"0":gift_map.get("video_id");
				String total_gift=gift_map.get("total_gift")==null?"0":gift_map.get("total_gift");
				String user_id=gift_map.get("user_id");
				
				Map<String, String> map = new HashMap<String, String>();	
				map.put("video_id", video_id);				
				map.put("total_gift", total_gift);			
				jedis.hmset(star_hash_user_video_gift_ +user_id, map);
				return 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website setVideoUserGift error:"+e.getMessage());
		}
		finally{
			closeRedis(jedis);
		}
		return -1;
	}
		
	// 返回用户送礼统计信息
	public Map<String, String> getVideoUserGift(long user_Id) {
		Jedis jedis = openRedis();
		try {
			return jedis.hgetAll(star_hash_user_video_gift_ + user_Id);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website getVideoUserGift error:"+e.getMessage());
		}
		finally	{
			closeRedis(jedis);
		}
		return null;
	}

	//返回送礼排行榜
	public JSONArray getGiftList(long video_id)
	{	
		Jedis jedis = openRedis();
		try {	
			JSONArray jsonarray= new JSONArray();
		    Set<Tuple> set=jedis.zrevrangeByScoreWithScores(star_zset_gitf_ + video_id, "+inf", "-inf");
		    for(Tuple t : set ){		    	
		    	String user_id=t.getElement();		    
		    	Map<String, String> map= new HashMap<String,String>();	
		    	map = jedis.hgetAll(star_hash_user_+user_id);
		    	if ( map!=null && map.get("user_id")!=null){		                
		    		JSONObject json=new JSONObject();
		    		json.put("user_id",Long.parseLong(map.get("user_id")));
		    		json.put("nickname", map.get("nickname"));
		    		json.put("pic_url",  map.get("pic_url"));
		    		json.put("video_id", map.get("video_id")==null?0:Long.parseLong(map.get("video_id")));		    		
		    		String total_gift=jedis.hget(star_hash_user_video_gift_+user_id, "total_gift");
		    		int tg=total_gift==null?0:Integer.parseInt(total_gift);		    		
		    		json.put("gift_count", tg);
		    		jsonarray.add(json);
		    	}
		    }		    
		    return jsonarray;		    
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website getGiftList error:"+e.getMessage());				
		}
		finally{
			closeRedis(jedis);
		}		
		return null;
	}	
	
	//初始化贵宾席
	public int initSeat(long video_id,int seat_id,int next_gift_count)
	{	
		Jedis jedis = openRedis();
		try {	
			Transaction tx=jedis.multi();		
			tx.zadd(star_zset_seat_ + video_id, seat_id, seat_id + "");
			   
			Map<String, String> map = new HashMap<String, String>();
			map.put("seat_id",seat_id+"");
			map.put("video_id", video_id + "");
			map.put("user_id", "0");
			map.put("gift_count", "0");
			map.put("next_gift_count", next_gift_count + "");

			tx.hmset(star_hash_seat_ + seat_id, map);	
			
			tx.exec();
			
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website getGiftList error:"+e.getMessage());				
		}
		finally{
			closeRedis(jedis);
		}		
		return -1;
	}	
	
	//返回贵宾席列表
	public JSONArray getSeatList(long video_id)
	{	
		Jedis jedis = openRedis();
		try {	
			JSONArray jsonarray= new JSONArray();
		    Set<Tuple> set=jedis.zrangeByScoreWithScores(star_zset_seat_+video_id,"-inf", "+inf");				    
		    for(Tuple t : set ){		    	
		    	String seat_id=t.getElement();		
		    	Map<String, String> map= new HashMap<String,String>();	
		    	map = jedis.hgetAll(star_hash_seat_+seat_id);
		    	if ( map!=null && map.get("video_id")!=null){	
		    		
		    		long user_id=map.get("user_id")==null?0:Long.parseLong(map.get("user_id"));
		    	
		    		JSONObject json=new JSONObject();
		    		json.put("seat_id",map.get("seat_id")==null?0:Integer.parseInt(map.get("seat_id")));
		    		json.put("video_id",Long.parseLong(map.get("video_id")));
		    		json.put("user_id", user_id);
		    		json.put("gift_count", map.get("gift_count")==null?0:Long.parseLong(map.get("gift_count")));
		    		json.put("next_gift_count", map.get("next_gift_count")==null?0:Long.parseLong(map.get("next_gift_count")));	
		    		
		    		
		    		Map<String, String> map_user= new HashMap<String,String>();	
		    		map_user = jedis.hgetAll(star_hash_user_+user_id);
			    	if ( map_user!=null && map_user.get("user_id")!=null){ 			    		
			    		json.put("nickname", map_user.get("nickname"));
			    		json.put("pic_url",  map_user.get("pic_url"));
			    	}
			    	else{	
			    		json.put("nickname", "");
			    		json.put("pic_url",  "");
			    	}		    	
		    		
		    		jsonarray.add(json);
		    	}
		    }		    
		    return jsonarray;		    
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website getSeatList error:"+e.getMessage());				
		}
		finally{
			closeRedis(jedis);
		}		
		return null;
	}	

	//抢位
	//out_user_id:被抢人的用户ID
	//user_id:踢馆者ID
	public int takeSeat(long video_id,int seat_id,long user_id,int gift_count, 
			int next_gift_count,long out_user_id)
	{	
		Jedis jedis = openRedis();
		try {	
			Map<String, String> map = new HashMap<String, String>();
			map.put("video_id", video_id + "");
			map.put("user_id", user_id+"");
			map.put("gift_count", gift_count+"");
			map.put("next_gift_count", next_gift_count + "");
			
			Transaction tx=jedis.multi();	
			tx.hmset(star_hash_seat_ + seat_id, map);
			tx.zadd(star_zset_seat_+video_id, seat_id, seat_id+"");
			
			//被抢人需要放入缓存
			if (out_user_id>0){				
				Map<String, String> map_out = new HashMap<String, String>();				
				map_out.put("user_id", user_id+""); //踢馆者ID
				map.put("out_time", System.currentTimeMillis()+"");
				map.put("next_gift_count", next_gift_count+"");
				map.put("seat_id", seat_id+"");				
				tx.hmset(star_hash_seat_out_ + out_user_id, map);
			}
			
			tx.exec();			
		    return 1;		    
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website getSeatList error:"+e.getMessage());				
		}
		finally{
			closeRedis(jedis);
		}		
		return -1;
	}
	
	//获取被踢人信息
	public JSONObject getKickInfo(long user_id)
	{	
		Jedis jedis = openRedis();
		try {	
			JSONObject json=new JSONObject();
			Map<String, String> map= new HashMap<String,String>();	
		    map = jedis.hgetAll(star_hash_seat_out_+user_id);
		    if ( map!=null && map.get("user_id")!=null){
		    	json.put("user_id",Long.parseLong(map.get("user_id")));	
		    	json.put("out_time", Long.parseLong(map.get("out_time")));
		    	json.put("next_gift_count", Integer.parseInt(map.get("next_gift_count")));		    	
		    	json.put("seat_id", Integer.parseInt(map.get("seat_id")));
		    	
			    //获取后需马上删除
			    jedis.del(star_hash_seat_out_ + user_id);
		    }
		    
		    return json;	    		    
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website getTakeInfo error:"+e.getMessage());				
		}
		finally{
			closeRedis(jedis);
		}		
		return null;
	}
	
	//获取PK信息
	public JSONObject getPKInfo(long video_id)
	{	
		JSONObject json=new JSONObject();
		json.put("front_pk_info", getPK(video_id,0));
		json.put("current_pk_info", getPK(video_id,1));
		
		JSONObject jsonMain=new JSONObject();		
		jsonMain.put("pk_info",json);
		return json;
	}
	
	private JSONObject getPK(long video_id,int pk_type)
	{
		JSONObject json=new JSONObject();
		Jedis jedis = openRedis();	
		try
		{
			Map<String, String> map= new HashMap<String,String>();	
			if (pk_type==0)
				map = jedis.hgetAll(star_hash_pk_front+video_id);		
			else
				map = jedis.hgetAll(star_hash_pk_+video_id);
			
		    if ( map!=null && map.get("pk_id")!=null){		    	
		    	int name_id1=map.get("name_id1")==null?0:Integer.parseInt(map.get("name_id1"));
		    	int name_id2=map.get("name_id2")==null?0:Integer.parseInt(map.get("name_id2"));
		    	int vote_count1=map.get("vote_count1")==null?0:Integer.parseInt(map.get("vote_count1"));
		    	int vote_count2=map.get("vote_count2")==null?0:Integer.parseInt(map.get("vote_count2"));
		    	
		    	json.put("pk_id",Long.parseLong(map.get("pk_id")));	
		    	json.put("status",Integer.parseInt(map.get("status")));	
		    	json.put("pk_time",Long.parseLong(map.get("pk_time")));		    	
		    	json.put("title", map.get("title"));
		    	json.put("content", map.get("content"));
		    	
		    	JSONObject json_pk1=new JSONObject();
		    	if ( name_id1!=0 ){
			    	Map<String, String> map_pk1 = jedis.hgetAll(star_hash_name_+ name_id1);
				    if ( map_pk1!=null ){				    	
				    	json_pk1.put("name_id", name_id1);
				    	json_pk1.put("star_name", map_pk1.get("star_name"));
				    	json_pk1.put("pic_url", map_pk1.get("pic_url"));
				    	json_pk1.put("video_id", video_id);
				    	json_pk1.put("vote_count", vote_count1);
				    }
		    	}		    	
		    	json.put("pk1", json_pk1);
		    	
		    	JSONObject json_pk2=new JSONObject();
		    	if ( name_id2!=0 ){
			    	Map<String, String> map_pk2 = jedis.hgetAll(star_hash_name_+ name_id2);
				    if ( map_pk2!=null ){				    	
				    	json_pk2.put("name_id", name_id2);
				    	json_pk2.put("star_name", map_pk2.get("star_name"));
				    	json_pk2.put("pic_url", map_pk2.get("pic_url"));
				    	json_pk2.put("video_id", video_id);
				    	json_pk2.put("vote_count", vote_count2);
				    }
		    	}	
		    	json.put("pk2", json_pk2);
		    }
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website etPKInfo error:" + e.getMessage());
		}
		finally{
			closeRedis(jedis);
		}	
		return json;
	}
	
	//设置PK信息
	//pk_type:0上一轮，1当前
	public int setPkInfo(Map<String, String> map,int pk_type)
	{	
		Jedis jedis = openRedis();	
		try {	
			if ( map!=null && map.get("pk_id")!=null){	
				Map<String, String> map_pk = new HashMap<String, String>();	
				if (map.get("pk_id")!=null)
					map_pk.put("pk_id", map.get("pk_id") + "");
				if (map.get("status")!=null)
					map_pk.put("status", map.get("status") + "");
				if (map.get("pk_time")!=null)
						map_pk.put("pk_time", map.get("pk_time") + "");
				if (map.get("title")!=null)
					map_pk.put("title", map.get("title") + "");
				if (map.get("content")!=null)
					map_pk.put("content", map.get("content") + "");
				if (map.get("name_id1")!=null)
					map_pk.put("name_id1", map.get("name_id1") + "");
				if (map.get("name_id2")!=null)
					map_pk.put("name_id2", map.get("name_id2") + "");
				if (map.get("vote_count1")!=null)
					map_pk.put("vote_count1", map.get("vote_count1") + "");
				if (map.get("vote_count2")!=null)
					map_pk.put("vote_count2", map.get("vote_count2") + "");
					
				if (pk_type==0)
					jedis.hmset(star_hash_pk_front+map.get("video_id"), map_pk);
				else
					jedis.hmset(star_hash_pk_+map.get("video_id"), map_pk);
			}
		    return 1;		    
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website setPkInfo error:" + e.getMessage());
		}
		finally{
			closeRedis(jedis);
		}		
		return -1;
	}	
		
	
	//删除PK
	public int delPKKey(long pk_id) {
		Jedis jedis = openRedis();
		try{		
			jedis.del(star_hash_pk_ + pk_id);
			return 1;
		}	
		catch(Exception e){
			e.printStackTrace();		
		}	
		finally	{
			closeRedis(jedis);
		}
		return 0;
	}			
	
		
	
	// 禁言
	//minutes:禁言时间，-1为永久禁言
	public int setforbid(long user_Id,int minutes) {
		Jedis jedis = openRedis();
		try {
			Transaction tx=jedis.multi();	
			tx.set(star_string_forbid_ + user_Id, minutes * 60 + "");
			if (minutes>0)
				tx.expire(star_string_forbid_+user_Id, minutes*60);
			
			tx.exec();	
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website getUserInfo error:" + e.getMessage());
		}
		finally	{
			closeRedis(jedis);
		}
		return -1;
	}		
	
	/*************************************** 一期 ***************************************/		
	// 返回直播点播流信息
	public Map<String, String> getLiveInfo(String video_id) {
		Jedis jedis = openRedis();
		try {
			return jedis.hgetAll(star_hash_video_info_ + video_id);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website getLiveInfo error:" + e.getMessage());
		}
		finally{
			closeRedis(jedis);
		}
		return null;
	}
	
	// 返回直播点播流信息状态
	//0转码, 1 直播, 2 点播
	public String getLiveFlag(String video_id) {
		Jedis jedis = openRedis();
		try {
			return jedis.hget(star_hash_video_info_ + video_id, "flag");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website getLiveStatus error:" + e.getMessage());
		}
		finally{
			closeRedis(jedis);
		}
		return null;
	}	
	
	/*
	 * 设置直播到hash zilong 2015.10.30 json return 1
	 */
	public int setHashLive(Map<String, String> hm) {
		Jedis jedis = openRedis();
		try {
			String video_id = hm.get("video_id") == null ? "" : hm.get("video_id");
			if (!video_id.equals("")) {
				String video_type = hm.get("video_type") == null ? "" : hm.get("video_type");
				String flag = hm.get("flag") == null ? "" : hm.get("flag");
				String live_id = hm.get("live_id") == null ? "" : hm.get("live_id");

				Map<String, String> hashMap = new LinkedHashMap<String, String>();
				if (!video_type.equals(""))
					hashMap.put("video_type", hm.get("video_type") + ""); // 视频分类 （1视频，2回放，3预告）
				if (!flag.equals(""))
					hashMap.put("flag", hm.get("flag") + ""); 	// 直播视频状态（0 直播中,1 转码中,2  回放）
				if (!live_id.equals(""))
					hashMap.put("live_id", hm.get("live_id") + ""); // 对应的直播ID
								
				hashMap.put("start_time", hm.get("start_time") + ""); //视频对外开放时间
				jedis.hmset(star_hash_video_info_ + video_id, hashMap);

				return 1;
			
			}
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website addHashLive error:" + e.getMessage());
		} finally {
			closeRedis(jedis);
		}
		return 0;
	}
	
	//删除直播key
	public int delLiveKey(String video_id) {
		Jedis jedis = openRedis();
		try{		
			jedis.del(star_hash_video_info_ + video_id);
			return 1;
		}	
		catch(Exception e){
			e.printStackTrace();		
		}	
		finally	{
			closeRedis(jedis);
		}
		return 0;
	}	
	
	// 设置错误信息(2015.11.04新）
	public int setErrorMsgNew(String msg) {
		Jedis jedis = openRedis();
		try {
			jedis.set(star_string_error, msg);			
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website setErrorMsgNew error:" + e.getMessage());
		}
		finally{
			closeRedis(jedis);
		}
		return -1;
	}
	
	// 一次性返回错误信息(2015.11.04新）
	public String getErrorMsgNew() {	
		Jedis jedis = openRedis();
		try {
			return jedis.get(star_string_error);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website getErrorMsgNew error:"+e.getMessage());	
		}
		finally{
			closeRedis(jedis);
		}
		return null;
	}
	
	
	// 返回错误信息
	public String getErrorMsg(int errorId) {
		Jedis jedis = openRedis();
		try {
			return jedis.hget(star_hash_error, errorId + "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website getErrorMsg error:"+e.getMessage());	
		}
		finally{
			closeRedis(jedis);
		}
		return null;

	}

	// 设置错误信息
	public long setErrorMsg(int errorId, String fieldValue) {
		Jedis jedis = openRedis();
		try {
			return jedis.hset(star_hash_error, errorId + "", fieldValue);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("redis website setErrorMsg error:"+e.getMessage());	
		}
		finally{
			closeRedis(jedis);
		}
		return -1;
		
	}
	
	//设置用户验证码
	public String setUserCode(String phone,String code) {
		Jedis jedis = openRedis();
		try{		
			String keyString=star_string_user_code_+phone;
			String statusCode=jedis.set(keyString,code);
			//设置过期时间
			jedis.expire(keyString, 86400);
			return 	statusCode;		
		}	
		catch(Exception e){
			e.printStackTrace();	
			logger.error("redis website setUserCode error:"+e.getMessage());	
		}	
		finally{
			closeRedis(jedis);
		}
		return null;
	}

	//删除用户验证码
	public void delUserCode(String phone) {
		Jedis jedis = openRedis();
		try{
			String keyString=star_string_user_code_+phone;
			jedis.del(keyString);
		}
		catch(Exception e){
			e.printStackTrace();
			logger.error("redis website setUserCode error:"+e.getMessage());
		}
		finally{
			closeRedis(jedis);
		}
	}
	
	//获取用户验证码
	public String getUserCode(String phone) {
		Jedis jedis = openRedis();
		try{		
			return jedis.get(star_string_user_code_ + phone);
		}	
		catch(Exception e){
			e.printStackTrace();
			logger.error("redis website getUserCode error:"+e.getMessage());
		}	
		finally{
			closeRedis(jedis);
		}
		return null;
	}
	
	
	//设置用户token
	public String setUserToken(long user_id,String token) {
		Jedis jedis = openRedis();
		try{	
			return jedis.set(star_string_user_token_ + user_id, token);
		}	
		catch(Exception e){
			e.printStackTrace();	
			logger.error("redis website setUserToken error:"+e.getMessage());
		}	
		finally{
			closeRedis(jedis);
		}
		return null;
	}
	
	//获取用户token
	public String getUserToken(long user_id) {
		Jedis jedis = openRedis();
		try{		
			return jedis.get(star_string_user_token_ + user_id);
		}	
		catch(Exception e){
			e.printStackTrace();	
			logger.error("redis website getUserToken error:"+e.getMessage());
		}	
		finally{
			closeRedis(jedis);
		}
		return null;
	}

	@Override
	public String getAuthString(String appKey) {
		if(StringUtils.isBlank(appKey))
			return null;

		Jedis jedis = openRedis();
		try{
			String authString = jedis.get(star_string_auth_code_ + appKey);
			/**
			 * 2017年08月01日 by GZY
			 * 从redis缓存中提取app_secret。如果为空则从数据库中提取，如果依旧为空返回null。
			 * 如果数据控中含有，则将其注入到redis缓存中。
			 */
			if(StringUtils.isBlank(authString)){
				authString = authenticationMapper.getAppSecret(appKey);
				if(StringUtils.isBlank(authString))
					jedis.set(star_string_auth_code_ + appKey,authString);
				else
					return null;
			}
			return authString;
		}	
		catch(Exception e){
			e.printStackTrace();	
			logger.error("redis website getAuthString error:"+e.getMessage());
		}	
		finally{
			closeRedis(jedis);
		}
		return null;
	}

	@Override
	public String setAuthString(String appKey, String fieldValue) {
		Jedis jedis = openRedis();
		try{		
			return jedis.set(star_string_auth_code_ + appKey, fieldValue);
		}	
		catch(Exception e){
			e.printStackTrace();	
			logger.error("redis website setAuthString error:"+e.getMessage());
		}	
		finally{
			closeRedis(jedis);
		}
		return null;
	}
	
	// 返回用户某个字段
		public String getUserField(long user_id, String fieldName) {
			Jedis jedis = openRedis();
			try {
				return jedis.hget(star_hash_user_ + user_id, fieldName);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("redis website getUserField error:"+e.getMessage());
			}
			finally{
				closeRedis(jedis);
			}
			return null;

		}

		// 设置用户某个字段
		public long setUserField(long user_id, String fieldName, String fieldValue) {
			Jedis jedis = openRedis();
			try {
				return jedis.hset(star_hash_user_ + user_id, fieldName, fieldValue);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("redis website setUserField error:"+e.getMessage());
			}
			finally{
				closeRedis(jedis);
			}
			return -1;
		}

		// 设置用户信息
		public int setUserInfo(long user_Id, String nickname, String picURL) {
			Jedis jedis = openRedis();
			try {
				Map<String, String> map = new HashMap<String, String>();
				map.put("user_id", user_Id + "");
				map.put("nickname", nickname + "");
				map.put("pic_url", picURL + "");
				jedis.hmset(star_hash_user_ + user_Id, map);
				return 1;
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("redis website setUserInfo error:"+e.getMessage());
			}
			finally{
				closeRedis(jedis);
			}
			return -1;
		}
		
		// 新设置用户信息 by 小芊 2017-04-28 新增代言人属性
		public int setUserInfo(StarBaseVO starBaseVO) {
			Jedis jedis = openRedis();
			try {
				Map<String, String> map = new HashMap<String, String>();
				map.put("user_id", starBaseVO.getId());
				map.put("nickname", starBaseVO.getName());
				map.put("pic_url", starBaseVO.getPicUrl());
				map.put("mark", starBaseVO.getMark() + "");
				jedis.hmset(star_hash_user_ + starBaseVO.getId(), map);
				return 1;
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("redis website setUserInfo error:"+e.getMessage());
			}
			finally{
				closeRedis(jedis);
			}
			return -1;
		}

		// 返回用户所有字段
		public Map<String, String> getUserInfo(long user_Id) {
			Jedis jedis = openRedis();
			try {
				return jedis.hgetAll(star_hash_user_ + user_Id);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("redis website getUserInfo error:"+e.getMessage());
			}
			finally	{
				closeRedis(jedis);
			}
			return null;
		}
	
		public String setDLurl(String video_id,String urlJson) {
			Jedis jedis = openRedis();
			try{		
				if (urlJson!=null && !urlJson.equals("")){
					return jedis.set(star_string_live_dilian_+video_id,urlJson);			
				}					
			}	
			catch(Exception e){
				e.printStackTrace();	
				logger.error("redis website setAuthString error:"+e.getMessage());
			}	
			finally{
				closeRedis(jedis);
			}
			return null;
		}

	@Autowired
	private RedisUtils redisUtils;

	@Override
	public void setLoginToken(String token, String userinfo) {
		if(StringUtils.isNotEmpty(token) && StringUtils.isNotEmpty(userinfo)){
			redisUtils.set(Constants.login_prefix_+userinfo, token);
		}
	}

	@Override
	public String getLoginToken(String userinfo) {
		if(StringUtils.isNotEmpty(userinfo)){
			return redisUtils.get(Constants.login_prefix_+userinfo);
		}
		return null;
	}

	@Override
	public String getResourceMapTable(String resourceType) {
		return redisUtils.get(Constants.resource_prefix_ + resourceType);
	}

	/**
	 *授权操作，可查询，可删除
	 * @param fromUserId 授权用户id
	 * @param toUserId  接受用户id
	 * @param type  权限类型
	 * @param operate  接口操作：0 查询 1 删除
	 * @param direction 操作方向：0 from方向 1 to方向
	 * @param tmp 是否操作临时键，
	 * @return success , error , userID, NULL, 1
	 * 删除部分有很多无用逻辑，因为数据存储结构中间有过变更。暂且不管
	 */
	@Override
	public String operateGrant(String fromUserId, String toUserId, int type, int operate, int direction, boolean tmp) {
		String result = "success";
		switch (operate){
			case 0://查询
				if(direction == 0){//查询授权用户的临时记录
					return tmp ? redisUtils.get(Constants.grant_prefix_from_tmp + fromUserId + "_" + toUserId + "_" + type) : redisUtils.hget(Constants.grant_prefix_from + fromUserId, type + "");
				}else{
					return tmp ? redisUtils.get(Constants.grant_prefix_to_tmp + toUserId + "_" + fromUserId + "_" + type) : redisUtils.hget(Constants.grant_prefix_to + toUserId, fromUserId);
				}
			case 1://删除
				if(direction == 0){//删除from
					if(EGrantType.getEGrant(type) != null && type != EGrantType.GUEST.ordinal()){//只允许一次授权
						if(tmp) {
							redisUtils.del(Constants.grant_prefix_from_tmp + fromUserId + "_" + toUserId + "_" + type);
						}else{
							redisUtils.hdel(Constants.grant_prefix_from + fromUserId, type + "");
						}
					}else{//允许多次授权
						if(StringUtils.isEmpty(toUserId)) {//删除单个用户授权
							String grantUsers = null;
							if(tmp) {
								grantUsers = redisUtils.get(Constants.grant_prefix_from_tmp + fromUserId + "_" + toUserId + "_" + type);
								if(StringUtils.isEmpty(grantUsers)){
									return "error";
								}
								redisUtils.del(Constants.grant_prefix_from_tmp + fromUserId + "_" + toUserId + "_" + type);
							}else{
								grantUsers = redisUtils.hget(Constants.grant_prefix_from + fromUserId, type + "");
								if (StringUtils.isEmpty(grantUsers) || grantUsers.indexOf(toUserId) < 0) {
									//删除失败！
									return "error";
								} else {
									int pos = 0;
									if ((pos = grantUsers.indexOf(toUserId)) > 0) {
										grantUsers = grantUsers.substring(0, pos - 1) + grantUsers.substring(pos + toUserId.length());
									} else if (pos == 0 && grantUsers.indexOf(",") > -1) {
										grantUsers = grantUsers.substring(pos + 1 + toUserId.length());
									}else{
										grantUsers = "";
									}
									if(StringUtils.isNotEmpty(grantUsers)) {
											redisUtils.hset(Constants.grant_prefix_from + fromUserId, type + "", grantUsers);
									}else{
											redisUtils.hdel(Constants.grant_prefix_from + fromUserId, type + "");
									}
								}
							}
						} else {//删除多个用户授权
							if(tmp) {
								redisUtils.del(Constants.grant_prefix_from_tmp + fromUserId + "_" + toUserId + "_" + type);
							}else{
								redisUtils.hdel(Constants.grant_prefix_from + fromUserId, type + "");
							}
						}
					}
				}else{//为1，删除to方向
					if(StringUtils.isNotEmpty(fromUserId) && StringUtils.isNotEmpty(toUserId)) {//
						if(EGrantType.getEGrant(type) != null) {//删除单个权限
							String grantTypes = null;
							if(tmp) {
								grantTypes = redisUtils.get(Constants.grant_prefix_to_tmp + toUserId + "_" + fromUserId + "_" + type);
								if(StringUtils.isEmpty(grantTypes)){
									return "error";
								}
								redisUtils.del(Constants.grant_prefix_to_tmp + toUserId + "_" + fromUserId + "_" + type);
							}else{
								grantTypes = redisUtils.hget(Constants.grant_prefix_to + toUserId, fromUserId);
								int pos = 0;
								if(StringUtils.isNotEmpty(grantTypes) && (pos = grantTypes.indexOf(type + "")) > -1){
									grantTypes = (pos == 0 && grantTypes.indexOf(",") > -1) ? grantTypes.substring((type + "").length()+1) :
											(pos == 0 && grantTypes.indexOf(",") < 0) ? "" : grantTypes.substring(0, pos-1) + grantTypes.substring(pos + 1 + (type + "").length());
									if(StringUtils.isNotEmpty(grantTypes)) {
										redisUtils.hset(Constants.grant_prefix_to + toUserId, fromUserId, grantTypes);
									}else{
										redisUtils.hdel(Constants.grant_prefix_to + toUserId, fromUserId);
									}
								}else{
									return "error";
								}
							}
						}else{//如果type不匹配任何类型，直接删除所有权限
							if(tmp) {
								redisUtils.del(Constants.grant_prefix_to_tmp + toUserId + "_" + fromUserId + "_" + type);
							}else{
								redisUtils.hdel(Constants.grant_prefix_to + toUserId, fromUserId);
							}
						}
					}else{//参数错误
						return "error";
					}
				}
				break;
			default:
				break;
		}
		return result;
	}

	/**
	 *授权操作，可设置
	 * @param fromUserId 授权用户id
	 * @param toUserId 接受用户id
	 * @param type 授权类型
	 * @param direction 授权方向: 0 from , 1 to
	 * @param tmp 是否临时键: true 临时,false 正式记录,为false时，也需要同步db
	 * @return -2 ，-1， 0 ， 1
	 */
	@Override
	public long setGrant(String fromUserId, String toUserId, int type, int direction, boolean tmp, int seconds) {
		long result = 0;
		if(StringUtils.isNotEmpty(fromUserId) && StringUtils.isNotEmpty(toUserId)) {
			if (EGrantType.getEGrant(type) != null && type == EGrantType.GUEST.ordinal()) {//多次授权
				if (tmp) {
					if(direction == 0) {//from方向
						redisUtils.setex(Constants.grant_prefix_from_tmp + fromUserId + "_" + toUserId + "_" + type, Constants.EXISTS_MARK, seconds);
					}else{//to 方向
						redisUtils.setex(Constants.grant_prefix_to_tmp + toUserId + "_" + fromUserId + "_" + type, Constants.EXISTS_MARK, seconds);
					}
				} else {
					if(direction ==0) {
						String grantUsers = redisUtils.hget(Constants.grant_prefix_from + fromUserId, type + "");
						grantUsers = grantUsers == null ? "" : grantUsers;
						grantUsers += (StringUtils.isNotEmpty(grantUsers) && grantUsers.indexOf(toUserId) < 0) ? "," + toUserId : (StringUtils.isNotEmpty(grantUsers) && grantUsers.indexOf(toUserId) > -1) ? "" : StringUtils.isEmpty(grantUsers) ? toUserId : "," + toUserId;
						result += redisUtils.hset(Constants.grant_prefix_from + fromUserId, type + "", grantUsers);
					}else{
						String grantTypes = redisUtils.hget(Constants.grant_prefix_to + toUserId, fromUserId);

						if(StringUtils.isNotEmpty(grantTypes) && grantTypes.indexOf(type + "") < 0){
							grantTypes += "," + type;
						}else if(StringUtils.isNotEmpty(grantTypes) && grantTypes.indexOf(type + "") > -1){
							result = -1;
							return result;
						}else if(StringUtils.isEmpty(grantTypes)){
							grantTypes = type + "";
						}
						result += redisUtils.hset(Constants.grant_prefix_to + toUserId, fromUserId, grantTypes);
					}
				}
			} else if (EGrantType.getEGrant(type) != null && type != EGrantType.GUEST.ordinal()) {
				if (tmp) {
					if(direction == 0) {
						redisUtils.setex(Constants.grant_prefix_from_tmp + fromUserId + "_" + toUserId + "_" + type, Constants.EXISTS_MARK, seconds);
					}else{
						redisUtils.setex(Constants.grant_prefix_to_tmp + toUserId + "_" + fromUserId + "_" + type, Constants.EXISTS_MARK, seconds);
					}
				} else {
					if(direction == 0) {
						result += redisUtils.hset(Constants.grant_prefix_from + fromUserId, type + "", toUserId);
					}else{
						result += redisUtils.hset(Constants.grant_prefix_to + toUserId, fromUserId, type + "");
					}
				}
			}
			return result;
		}
		return -2;
	}

	@Override
	public String setPK(String pkId, String group, Integer count) {
		redisUtils.incrBy(Constants.pk_group + pkId + group, count);
		return "success";
	}

	@Override
	public String getPK(String pkId, String group) {
		return redisUtils.get(Constants.pk_group + pkId + group);
	}

	@Override
	public String setUserVote(String userId, String pkId, String group, Integer count) {
		String vote = redisUtils.hget(Constants.pk_user + userId, pkId + group);
		redisUtils.hset(Constants.pk_user + userId, pkId + group, (Integer.parseInt(vote) + count) + "");
		return "success";
	}

	@Override
	public Double setGuardGift(String videoItemId, String starId, String userId, Integer count) {
		//记录用户个人记录
		Double s = redisUtils.zincrby(Constants.guard + videoItemId + "_" + starId, userId, count);
		//记录业务守护席位
		redisUtils.zincrby(Constants.video_guard + videoItemId, starId, count);

		return s;

	}

	public String getVideoPlayTimes(String videoItemID) {
		return redisUtils.get(Constants.video_play_times + videoItemID);
	}

	@Override
	public String setVideoPlayTimes(String videoItemID,Integer count) {
		//先取，再加，最后设值。
		String c = getVideoPlayTimes(videoItemID);
		c = c == null ? "0" : c;
		Integer ci = (Integer.parseInt(c) + count);
		redisUtils.set(Constants.video_play_times + videoItemID ,ci.toString() );
		return ci.toString();
	}

	/**
	 * 递减排序，当前业务守护明星的粉丝排行
	 * @param videoItemId
	 * @param start
	 * @param stop
	 * @return
	 */
	@Override
	public Set<String> getGuardGiftSort(String videoItemId, String starId, Long start, Long stop) {
		return redisUtils.zrevrange(Constants.guard + videoItemId + "_" + starId, start, stop);
	}


	/**
	 * 获取当前业务的守护前五排行
	 * @param videoItemId
	 * @return
	 */
	@Override
	public List<GuardSortView> getGuardSortFive(String videoItemId) {
		List<GuardSortView> views = new ArrayList<>();
		Set<String> starIds = redisUtils.zrevrange(Constants.video_guard + videoItemId, 0L, 5L);
		for(String id : starIds){
			GuardSortView view = new GuardSortView();
			StarBaseVO starBase = new StarBaseVO(id, redisUtils.hget(star_hash_user_ + id, "nickname"), redisUtils.hget(star_hash_user_ + id, "pic_url"));
			view.star = starBase;
			Set<String> user = getGuardGiftSort(videoItemId, id, 0L, 1L);
			for(String uid : user){
				//
				StarBaseVO userBase = new StarBaseVO(uid, redisUtils.hget(star_hash_user_ + uid, "nickname"), redisUtils.hget(star_hash_user_ + uid, "pic_url"));
				view.user = userBase;
			}
			view.videoItemId = videoItemId;
			view.count = redisUtils.zscore(Constants.video_guard + videoItemId, id) + "";
			views.add(view);
		}
		return views;
	}

	@Override
	public void setSeatGift(String videoItemId, Integer seatIndex, String starId, String userId, String count) {
		if(userId == null){//初始化守护席位明星
			redisUtils.hset(Constants.video_guard_seat + videoItemId, seatIndex + "", userId + "_" + starId + "_0");
		}else {
			Double s = redisUtils.zscore(Constants.guard + videoItemId + "_" + starId, userId);
			s = s == null ? 0 : s;
			redisUtils.zadd(Constants.guard + videoItemId + "_" + starId, userId, new BigDecimal(count).add(new BigDecimal(s + "")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			//记录业务守护席位
			Double score = redisUtils.zscore(Constants.video_guard + videoItemId, starId);
			score = score == null ? 0 : score;
			redisUtils.zadd(Constants.video_guard + videoItemId, starId, new BigDecimal(count).add(new BigDecimal(score + "")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			redisUtils.hset(Constants.video_guard_seat + videoItemId, seatIndex + "", userId + "_" + starId + "_" + count);
		}
	}

	@Override
	public Map<String, String> getSeatGiftSort(String videoItemId) {
		return redisUtils.hgetAll(Constants.video_guard_seat + videoItemId);
	}

	@Override
	public List<GuardSortView> getSeatGiftSortInView(String videoItemId) {
		Map<String, String> map = getSeatGiftSort(videoItemId);
		List<GuardSortView> views = new ArrayList<>();
		for(int i=0;i<Constants.dic_guard_price.length;i++){
			GuardSortView view = new GuardSortView();
			view.count = Constants.dic_guard_price[i] + "";
			view.newCount = view.count;
			views.add(view);
		}
		for(Map.Entry<String, String> entry : map.entrySet()){
			int index = Integer.parseInt(entry.getKey());
			GuardSortView view = views.get(index);
			String uid = entry.getValue().split("_")[0];
			String sid = entry.getValue().split("_")[1];
			String count = entry.getValue().split("_")[2];
			StarBaseVO starBase = new StarBaseVO(sid, redisUtils.hget(star_hash_user_ + sid, "nickname"), redisUtils.hget(star_hash_user_ + sid, "pic_url"));
				view.star = starBase;
				StarBaseVO userBase = new StarBaseVO(uid, redisUtils.hget(star_hash_user_ + uid, "nickname"), redisUtils.hget(star_hash_user_ + uid, "pic_url"));
				view.user = userBase;
				view.videoItemId = videoItemId;
				view.count = new BigDecimal(count).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
				view.newCount = new BigDecimal(view.count).add(new BigDecimal(Constants.dic_guard_price[index])).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
		}
		return views;
	}


	@Override
	public TChargingMode getChargingMode(String videoItemId) {
		String charg = redisUtils.get(Constants.charging + videoItemId);
		return StringUtils.isNotEmpty(charg) ? (TChargingMode)JSONObject.parseObject(charg, TChargingMode.class) : null;
	}

	/**
	 * 只存json字符串，不分开存储s
	 * @param mode
	 */
	@Override
	public void setChargingMode(TChargingMode mode) {
		redisUtils.set(Constants.charging + mode.getVideoitemid(), JSONObject.toJSONString(mode));
	}

	@Override
	public int videoWord(String videoItemId) {
		try {
			String wordCount = redisUtils.hget(Constants.video_statistic + videoItemId, "word");
			if (StringUtils.isEmpty(wordCount)) {
				redisUtils.hset(Constants.video_statistic + videoItemId, "word", "1");
			} else {
				redisUtils.hset(Constants.video_statistic + videoItemId, "word", (Long.parseLong(wordCount) + 1) + "");
			}
		}catch (Exception e){
			return -1;
		}
		return 1;
	}

	@Override
	public long getVideoWord(String videoItemId) {
		String wordCount = redisUtils.hget(Constants.video_statistic + videoItemId, "word");
		return StringUtils.isEmpty(wordCount) ? -1 : Long.parseLong(wordCount);
	}

	@Override
	public int videoShare(String videoItemId) {
		try {
			String wordCount = redisUtils.hget(Constants.video_statistic + videoItemId, "share");
			if (StringUtils.isEmpty(wordCount)) {
				redisUtils.hset(Constants.video_statistic + videoItemId, "share", "1");
			} else {
				redisUtils.hset(Constants.video_statistic + videoItemId, "share", (Long.parseLong(wordCount) + 1) + "");
			}
		}catch (Exception e){
			return -1;
		}
		return 1;
	}

	@Override
	public long getVideoShare(String videoItemId) {
		String shareCount = redisUtils.hget(Constants.video_statistic + videoItemId, "share");
		return StringUtils.isEmpty(shareCount) ? -1 : Long.parseLong(shareCount);
	}

	@Override
	public int videoFavor(String videoItemId, String uid) {
		try {
			String wordCount = redisUtils.hget(Constants.video_statistic + videoItemId, "favor");
			if (StringUtils.isEmpty(wordCount)) {
				redisUtils.hset(Constants.video_statistic + videoItemId, "favor", "1");
			} else {
				redisUtils.hset(Constants.video_statistic + videoItemId, "favor", (Long.parseLong(wordCount) + 1) + "");
			}
			String append = videoItemId;
			if(StringUtils.isNotEmpty(redisUtils.get(Constants.user_favor + uid))){
				append = "," + videoItemId;
			}
			redisUtils.append(Constants.user_favor + uid, append);
		}catch (Exception e){
			return -1;
		}
		return 1;
	}

	@Override
	public JSONObject getVideoFavor(String videoItemId, String uid) {
		JSONObject jsonObject = new JSONObject();
		try {
			if (StringUtils.isNotEmpty(videoItemId)) {//取视频被喜欢列表
				jsonObject.put("ulist", redisUtils.get(Constants.video_favor + videoItemId));
				jsonObject.put("ucount", redisUtils.hget(Constants.video_statistic + videoItemId, "favor"));
			}
			if (StringUtils.isNotEmpty(uid)) {//取用户喜欢视频列表
				jsonObject.put("vlist", redisUtils.get(Constants.user_favor + uid));
			}
		}catch (Exception e){
			jsonObject.put("status", "no");
		}
		return jsonObject;
	}

	/**
	 * 方法查询列表缓存处理，默认缓存1天，后面需要手动更新缓存
	 * @param key
	 * @param value
	 */
	@Override
	public void setMethodCache(String key, String value) {
		redisUtils.setex(key, value, Constants.default_expire);
	}

	@Override
	public String getMethodCache(String key) {
		return redisUtils.get(key);
	}

	@Override
	public void addVideoMsg(VideoMsgVO vo) {
		redisUtils.lpush(star_list_video_msg, JSONObject.toJSONString(vo));
	}
	
	@Override
	public VideoMsgVO popVideoMsg() {
		String msg = redisUtils.rpop(star_list_video_msg);
		if (StringUtils.isEmpty(msg)) return null;
		return JSONObject.parseObject(msg, VideoMsgVO.class);
	}

	@Override
	public String setLiveGuestCount(Boolean add, String vid) {
		String key = live_guest + vid;
		int value = redisUtils.hget(key, "cur") == null ? 0 : ( add ? Integer.parseInt(redisUtils.hget(key, "cur")) + 1 : Integer.parseInt(redisUtils.hget(key, "cur")) - 1);
		redisUtils.hset(key, "cur", value + "");
		if(add){
			int tmp = 0 ;
			redisUtils.hset(key, "max", redisUtils.hget(key, "max") == null ? value + "" : ((tmp = Integer.parseInt(redisUtils.hget(key, "max"))) < value ? value + "" : tmp + ""));
		}
		return value + "";
	}

	//TODO should finish create by xiaoqian 2017-06-28
	@Override
	public List<StarRankingInfoDetail> getRepresentRankingList(Long actRepresentId, Integer pageNum) {
		
		return null;
	}

	@Override
	public int setRepresentRankingList(Long actRepresentId) {
		return 0;
	}

	@Override
	public int videoPraise(String videoItemId) {
		int count = 0 ;
		try {
			String videoPraise = redisUtils.hget(Constants.video_statistic + videoItemId, "praise");
			count = StringUtils.isEmpty(videoPraise) ? 1 : Integer.parseInt(videoPraise) + 1;
			redisUtils.hset(Constants.video_statistic + videoItemId, "praise", count + "" );
		}catch (Exception e){
			return -1;
		}
		return count;
	}

	@Override
	public Long setUserToken(long user_id, String token, String end) {
		Jedis jedis = openRedis();
		try{
			if(EAppKey.ANDROID.getValue().equals(end) || EAppKey.IOS.getValue().equals(end)){
				return jedis.hset(user_token_ + user_id,  EAppKey.ANDROID.name() + "_or_" + EAppKey.IOS.name(), token);
			}else if(EAppKey.MOBILE.getValue().equals(end) || 
					EAppKey.MOBILE_ANDROID.getValue().equals(end) || 
					EAppKey.MOBILE_IOS.getValue().equals(end)){
				return jedis.hset(user_token_ + user_id, EAppKey.MOBILE.name(), token);
			}else{
				return 0L;
			}
		}
		catch(Exception e){
			e.printStackTrace();	
			logger.error("redis website setUserToken error:"+e.getMessage());
		}	
		finally{
			closeRedis(jedis);
		}
		return null;
	}

	@Override
	public String getUserToken(long user_id, String end) {
		Jedis jedis = openRedis();
		try{
			if(EAppKey.ANDROID.getValue().equals(end) || EAppKey.IOS.getValue().equals(end)){
				return jedis.hget(user_token_ + user_id, EAppKey.ANDROID.name() + "_or_" + EAppKey.IOS.name());
			}else if(EAppKey.MOBILE.getValue().equals(end) || 
					EAppKey.MOBILE_ANDROID.getValue().equals(end) || 
					EAppKey.MOBILE_IOS.getValue().equals(end)){
				return jedis.hget(user_token_ + user_id, EAppKey.MOBILE.name());
			}else{
				return null;
			}
		}
		catch(Exception e){
			e.printStackTrace();	
			logger.error("redis website getUserToken error:"+e.getMessage());
		}	
		finally{
			closeRedis(jedis);
		}
		return null;
	}

	@Override
	public String getOpenId(long user_id) {
		if(user_id <= 0)
			return null;
		Jedis jedis = openRedis();
		try {
			return jedis.get(ConstantType.WEIXIN_OPENID + "_" + user_id);

		}catch (Exception e){
			e.printStackTrace();
			logger.error("redis website getOpenId error:"+e.getMessage());
		}finally {
			closeRedis(jedis);
		}
		return null;
	}

	@Override
	public void setOpenId(String open_id,long user_id) {
		if(StringUtils.isBlank(open_id) || user_id <= 0)
			return;
		Jedis jedis = openRedis();
		try {
			jedis.set(ConstantType.WEIXIN_OPENID+"_"+user_id,open_id);

		}catch (Exception e){
			e.printStackTrace();
			logger.error("redis website getOpenId error:"+e.getMessage());
		}finally {
			closeRedis(jedis);
		}
	}

	@Override
	public String getDefaultProfit() {
		String defaultProfit = "";
		Jedis jedis = openRedis();
		try {
//			jedis.set(default_profit,"5");
			defaultProfit = jedis.get(default_profit);

		}catch (Exception e){
			e.printStackTrace();
			logger.error("redis website getOpenId error:"+e.getMessage());
		}finally {
			closeRedis(jedis);
		}
		return defaultProfit;
	}

}
