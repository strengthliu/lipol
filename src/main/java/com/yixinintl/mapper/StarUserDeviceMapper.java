package com.yixinintl.mapper;


import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import com.yixinintl.domain.StarUserDevice;

public interface StarUserDeviceMapper {
    int deleteByPrimaryKey(Long deviceId);

    int insert(StarUserDevice record);

    int insertSelective(StarUserDevice record);

    StarUserDevice selectByPrimaryKey(Long deviceId);

    int updateByPrimaryKeySelective(StarUserDevice record);

    int updateByPrimaryKey(StarUserDevice record);

    /**
     * 获取最近登录设备
     * @param userId
     * @return
     */
    @Select("select * from star_user_device where user_id = #{userId} order by create_time desc limit 1")
    @ResultMap("BaseResultMap")
    StarUserDevice selectDeviceByUser(Long userId);

    @Select("select * from star_user_device where user_id=#{userId}")
    @ResultMap("BaseResultMap")
    StarUserDevice selectByUserId(@Param("userId") long userId);

    //同一个设备，不同第三方类型，会查出多个结果，所以要加上userId条件，否则报错
    @Select("select * from star_user_device where device_token = #{deviceToken} and user_id = #{userId}")
    @ResultMap("BaseResultMap")
    StarUserDevice selectByDeviceToken(@Param("deviceToken")String deviceToken, @Param("userId")Long userId);
    
    Set<String> selectTokenListByUid(List<Long> uids);
}