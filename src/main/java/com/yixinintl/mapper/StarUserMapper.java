package com.yixinintl.mapper;


import org.apache.ibatis.annotations.*;

import com.yixinintl.domain.StarUser;

import java.util.Date;
import java.util.List;

public interface StarUserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(StarUser record);

    int insertSelective(StarUser record);

    StarUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StarUser record);

    int updateByPrimaryKey(StarUser record);

    int updateByUserIdSelective(StarUser record);

    @Select("select * from star_user where user_phone=#{phone}")
    @ResultMap("BaseResultMap")
    StarUser selectByPhone(@Param("phone") String phone);

    @Update("update star_user set nickname = #{record.nickname},sex = #{record.sex},"
            + "age = #{record.age},horoscope = #{record.horoscope},"
            + "sign = #{record.sign} "
            + "where user_id=#{record.userId}")
    void updateByUserid(@Param("record")StarUser record);

    @Select("select * from star_user order by create_time desc limit #{offset},#{pagenum}")
    @ResultMap("BaseResultMap")
    List<StarUser> getUserInfoByPage(@Param("offset")int offset,@Param("pagenum")int pagenum);

    @Select("select b.* from t_videoInfo a left join star_user b on a.`ower_user_id` = b.user_id where a.status!=10 group by a.`ower_user_id` limit #{offset},#{pagenum}")
    @ResultMap("BaseResultMap")
    List<StarUser> findUserInfoByPage(@Param("offset")int offset,@Param("pagenum")int pagenum);

    @Select("select count(1) from star_user")
    int calTotalUserCount();

    List<StarUser> getUserInfoByCondition(@Param("offset")int offset,@Param("pagenum")int pagenum,
                                          @Param("condition")String condition,@Param("timestart")String timestart,@Param("timeend")String timeend);

    int calUserCountByCondition(@Param("condition")String condition,@Param("timestart")String timestart,@Param("timeend")String timeend);

    @Select("select * from star_user where user_id = #{uid}")
    @ResultMap("BaseResultMap")
    StarUser getStarUserByUid(long uid);

    @Select("select * from star_user where bindTo = #{bind} order by last_login_time desc")
    @ResultMap("BaseResultMap")
    List<StarUser> getStarUserByBindTo(@Param("bind")long bind);

    @Select("select * from star_user where user_phone = #{phone}")
    @ResultMap("BaseResultMap")
    StarUser getStarUserByPhone(String phone);

    //同步到redis语句  zilong 2015.12.11
    @Select("select * from star_user")
    @ResultMap("BaseResultMap")
    List<StarUser> getStarUserAll();

    @Update("update star_user set ticket_count=ticket_count+#{ticketCount} where user_id=#{userId}")
    void updateUserTicketNumByUserId(@Param("ticketCount")Integer ticketCount,@Param("userId")Long userId);

    @Update("update star_user set vip_validity=#{date} where user_id=#{userId}")
    void updateUserVipDateByUserId(@Param("date")Date date,@Param("userId")Long userId);


    @Update("update star_user set gift_count=gift_count+#{giftCount} where user_id=#{userId}")
    void updateUserGiftNumByUserId(@Param("giftCount")Integer giftCount,@Param("userId")Long userId);

    @Update("update star_user set ticket_count=ticket_count+#{propnum}")
    void updateAllUserTicket(int propnum);

    @Update("update star_user set gift_count=gift_count+#{propnum}")
    void updateAllUserGift(int propnum);

    void updateAllUserVip(int propnum);

    @Select("Select * From star_user Where user_id=#{user_id} and nickname = #{nickname}")
    @ResultMap("BaseResultMap")
    StarUser getUserByNickName(@Param("nickname")String nickname,@Param("user_id")Long user_id);

    @Select("Select * From star_user Where nickname = #{nickname}")
    @ResultMap("BaseResultMap")
    StarUser getUserByName(@Param("nickname")String nickname);


    @Select("Select * From star_user Where nickname like '%${nickname}%' limit #{pageIndex}, #{pageSize}")
    @ResultMap("BaseResultMap")
    List<StarUser> getUserByNickNamePattern(@Param("nickname")String nickname,@Param("pageIndex")int pageIndex, @Param("pageSize") int pageSize);

    @Select("select * from star_user where third_party_type=#{thirdPartyType} and third_party_id=#{thirdPartyId}")
    @ResultMap("BaseResultMap")
    StarUser selectByThirdType(@Param("thirdPartyType")Integer thirdPartyType,@Param("thirdPartyId")String thirdPartyId);

    @Select("select * from star_user where third_party_type=#{thirdPartyType} and user_id=#{userId}")
    @ResultMap("BaseResultMap")
    StarUser selectByThirdTypeOnly(@Param("thirdPartyType")Integer thirdPartyType, @Param("userId")Long  userId);
    
    @Select("select * from star_user where user_id=#{userId}")
    @ResultMap("BaseResultMap")
    StarUser selectByUserId(Long  userId);

    @Select("select pic_url from star_user where user_id = #{user_id}")
    @ResultType(String.class)
    String getUserPicUrl(@Param("user_id") Long uid);
}