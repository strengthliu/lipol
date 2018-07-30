package com.heysound.service.impl;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.device.OnlineStatus;
import cn.jpush.api.device.TagAliasResult;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceType;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;

import com.alibaba.fastjson.JSONObject;
import com.heysound.domain.*;
import com.heysound.exception.HeySoundException;
import com.heysound.mapper.StarUserDeviceMapper;
import com.heysound.model.JPushBean;
import com.heysound.model.PlatformType;
import com.heysound.model.PushMessage;
import com.heysound.service.IJPushService;
import com.heysound.util.JPush.BuildAlert;
import com.heysound.util.JPush.enums.BusType;

import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by zhaiwei on 2016/11/10.
 */
@Service
public class JPushServiceImpl implements IJPushService {

    protected static final Logger LOG = LoggerFactory.getLogger(JPushServiceImpl.class);

    @Autowired
    private JPushClientFactory jPushClientFactory;
    
    @Autowired
    private StarUserDeviceMapper starUserDeviceMapper;

    @Value("${jpush.apns.production}")
    private boolean apnsProduction;
    /**
     * 设置jpush的tag和别名
     * @param registrationId jpush在客户端初始化后生成的设备唯一id
     * @param alias 别名标注是哪个用户
     * @param tagsToAdd 增加tag标签的set
     * @param tagsToRemove 删除tag标签的set
     * @throws Exception
     */
    public void setDeviceTagAlias(String registrationId, String alias, Set<String> tagsToAdd, Set<String> tagsToRemove) throws Exception {

        try {
            JPushClient jPushClient = jPushClientFactory.getJPushClient();

            jPushClient.updateDeviceTagAlias(registrationId,alias,tagsToAdd,tagsToRemove);
        } catch (APIConnectionException e) {
            printRegisteredErrorLog(e);
            throw new Exception(e);
        } catch (APIRequestException e) {
            printRegisteredErrorLog(e);
            throw new Exception(e);
        }

    }


    /**
     * 查询tag和别名
     * @param registrationId jpush在客户端初始化后生成的设备唯一id
     * @throws HeySoundException
     */
    public  void testGetDeviceTagAlias(String registrationId) throws HeySoundException {
        try {
            TagAliasResult result = jPushClientFactory.getJPushClient().getDeviceTagAlias(registrationId);

            LOG.info(result.alias);
            LOG.info(result.tags.toString());

        } catch (APIConnectionException e) {
            printRegisteredErrorLog(e);
            throw new HeySoundException(e);
        } catch (APIRequestException e) {
            printRegisteredErrorLog(e);
            throw new HeySoundException(e);
        }
    }

    /**
     * 查询设备在线状态
     * @param registrationId jpush在客户端初始化后生成的设备唯一id
     * @throws HeySoundException
     */
    public  void getetUserOnlineStatus(String registrationId) throws HeySoundException {
        try {
            Map<String, OnlineStatus> result =  jPushClientFactory.getJPushClient().getUserOnlineStatus(registrationId);

            LOG.info(result.get(registrationId).toString());
//            LOG.info(result.get(REGISTRATION_ID2).toString());
        } catch (APIConnectionException e) {
            printRegisteredErrorLog(e);
            throw new HeySoundException(e);
        } catch (APIRequestException e) {
            printRegisteredErrorLog(e);
            throw new HeySoundException(e);

        }
    }

    /**
     * 单独发送Andorra和ios的通知
     * @param jPushBean
     * @throws HeySoundException
     */
    @Override
    public void sendPushNotificationAndroid_ios(JPushBean jPushBean) throws HeySoundException {
        sendPushAndroid_ios(jPushBean,null);
    }

    /**
     *  单独发送Android和ios的透传消息
     * @param pushMessage
     * @throws HeySoundException
     */
    @Override
    public void sendPushMessageAndroid_ios(PushMessage pushMessage) throws HeySoundException {
        sendPushAndroid_ios(null,pushMessage);
    }

    /**
     * 发送Android和ios平台的通知和消息
     * @param jPushBean
     * @param pushMessage
     * @throws HeySoundException
     */
    public void sendPushAndroid_ios(JPushBean jPushBean,PushMessage pushMessage) throws HeySoundException {
        // HttpProxy proxy = new HttpProxy("localhost", 3128);
        // Can use this https proxy: https://github.com/Exa-Networks/exaproxy

        JPushClient jpushClient = jPushClientFactory.getJPushClient();

        // For push, all you need do is to build PushPayload object.
        PushPayload payload = buildPushObject_android_and_ios(jPushBean,pushMessage);
        System.out.println("jpushpayload:" + JSONObject.toJSONString(payload.toJSON().toString()));
        try {
            PushResult result = jpushClient.sendPush(payload);
            LOG.info("Got result - " + result);
            System.out.println("jushMsgOK - " + result);
        } catch (APIConnectionException e) {
//            printErrorLog(e);
            LOG.error("Connection error. Should retry later. ", e);
            throw new HeySoundException(e);

        } catch (APIRequestException e) {
            LOG.error("Error response from JPush server. Should review and fix it. ", e);
            LOG.info("HTTP Status: " + e.getStatus());
            LOG.info("Error Code: " + e.getErrorCode());
            LOG.info("Error Message: " + e.getErrorMessage());
            LOG.info("Msg ID: " + e.getMsgId());
//            printErrorLog(e);
            throw new HeySoundException(e);
        }
    }



    private PushPayload buildPushObject_android_and_ios(JPushBean jPushBean,PushMessage pushMessage) {
             PushPayload.Builder builder = PushPayload.newBuilder();

             if(null != jPushBean.getPlatform()){
                 //设置平台
                 builder.setPlatform(jPushBean.getPlatform());
             }

             //设置推送目标
             builder = setAudience(jPushBean,builder);

             //设置通知
             if(null != jPushBean){
                 builder = setNotification(jPushBean,builder);
             }


             //设置消息
             if(null != pushMessage){
                 builder = setPushPayloadMessage(pushMessage,builder);
             }


             //设置过期时间
             builder.setOptions(Options.newBuilder().setTimeToLive(86400*7).setApnsProduction(apnsProduction).build());
             System.out.println("pushpayloadbuilder:" + JSONObject.toJSONString(builder));
             return builder.build();

    }


    private PushPayload.Builder setPushPayloadMessage(PushMessage pushMessage, PushPayload.Builder builder){

        Message.Builder mbuilder = Message.newBuilder();

        if(StringUtils.hasText(pushMessage.getMsgTitle())){
            mbuilder.setTitle(pushMessage.getMsgTitle());
        }
        if(StringUtils.hasText(pushMessage.getMsgContent())){
            mbuilder.setMsgContent(pushMessage.getMsgContent());
        }
        if(StringUtils.hasText(pushMessage.getMsgContentType())){
            mbuilder.setContentType(pushMessage.getMsgContentType());
        }
        if(!CollectionUtils.isEmpty(pushMessage.getMsgExtras())){
            mbuilder.addExtras(pushMessage.getMsgExtras());
        }
        builder.setMessage(mbuilder.build());

        return builder;
    }

    private PushPayload.Builder setAudience(JPushBean jPushBean, PushPayload.Builder builder){

        if(null == jPushBean.getAudienceType()){
            builder.setAudience(Audience.all());
        }else{
            if(AudienceType.ALIAS.equals(jPushBean.getAudienceType())){
                //推送目标
                builder.setAudience(Audience.alias(jPushBean.getAlias()));
            }
            if(AudienceType.TAG.equals(jPushBean.getAudienceType())){
                //推送目标
                builder.setAudience(Audience.tag(jPushBean.getTag()));
            }
            if(AudienceType.TAG_AND.equals(jPushBean.getAudienceType())){
                //推送目标
                builder.setAudience(Audience.tag_and(jPushBean.getTag_and()));
            }
            if(AudienceType.REGISTRATION_ID.equals(jPushBean.getAudienceType())){
                //推送目标
                builder.setAudience(Audience.registrationId(jPushBean.getRegistrationId()));
            }
        }
        return builder;

    }

    private PushPayload.Builder setNotification(JPushBean jPushBean, PushPayload.Builder builder){

        List<NotificationInfo> notifications = jPushBean.getNotifications();

        Notification.Builder nuilder = Notification.newBuilder();
        //双平台都发送通知消息,Android平台和ios平台内容是一样的 所以只要取集合第一个元素的alert属性即可
        nuilder.setAlert(notifications.get(0).getAlert());

        if(jPushBean.getPlatformType().equals(PlatformType.Android_ios)){

                for(NotificationInfo notificationInfo : notifications){

                    if(notificationInfo instanceof NotificationAndoridInfo){

                        nuilder = generateAndroidBotificationBuilder(nuilder,notificationInfo);
                        builder.setNotification(nuilder.build());
                    }else if(notificationInfo instanceof NotificationIosInfo) {

                        nuilder = generateIosBotificationBuilder(nuilder,notificationInfo);
                        builder.setNotification(nuilder.build());
                    }
                }

        }

            

        return builder;
    }

    private Notification.Builder generateAndroidBotificationBuilder( Notification.Builder nuilder , NotificationInfo notificationInfo){

        NotificationAndoridInfo nai = (NotificationAndoridInfo) notificationInfo;

        AndroidNotification.Builder ani = AndroidNotification.newBuilder();

        ani.setAlert(nai.getAlert());
        if(StringUtils.hasText(nai.getTitle())){
            ani.setTitle(nai.getTitle());
        }

        if(!CollectionUtils.isEmpty(nai.getExtras())){
            ani.addExtras(nai.getExtras());
        }
        if(nai.getBuilder_id() != -1){
            ani.addExtras(nai.getExtras());
        }


        nuilder.addPlatformNotification(ani.build());

        return nuilder;
    }

    private Notification.Builder generateIosBotificationBuilder( Notification.Builder nuilder , NotificationInfo notificationInfo){
        NotificationIosInfo iai = (NotificationIosInfo) notificationInfo;

        IosNotification.Builder ini = IosNotification.newBuilder();
        ini.setAlert(iai.getAlert());
        if(!CollectionUtils.isEmpty(iai.getExtras())){
            ini.addExtras(iai.getExtras());
        }
        if(iai.getBadge() != -1){
            ini.setBadge(iai.getBadge());
        }
        if(StringUtils.hasText(iai.getSound())){
            ini.setSound(iai.getSound());
        }
        if(iai.isContentAvailable()){
            ini.setContentAvailable(iai.isContentAvailable());
        }
        if(iai.isMutableContent()){
            ini.setContentAvailable(iai.isMutableContent());
        }
        if(StringUtils.hasText(iai.getCategory())){
            ini.setCategory(iai.getCategory());
        }

        nuilder.addPlatformNotification(ini.build());

        return nuilder;
    }

    private void printRegisteredErrorLog(Exception e){
        if(e instanceof APIConnectionException){
            LOG.error("Connection error. Should retry later. ", e);
        }else if (e instanceof APIRequestException){
            APIRequestException e1 = (APIRequestException) e;
            LOG.error("Error response from JPush server. Should review and fix it. ", e1);
            LOG.info("HTTP Status: " + e1.getStatus());
            LOG.info("Error Code: " + e1.getErrorCode());
            LOG.info("Error Message: " + e1.getErrorMessage());
        }
    }


    @Override
    public Map<String,Object> sendMsg(int busType, OrderVO order, GoodsVO goods, String rId) throws HeySoundException {
        //订单收到支付成功推送通知

        JPushBean jPushBean = new JPushBean();
        //这里传过来的是用户id，要根据用户id查询用户的极光token，在device表里
        List<Long> toUserIds = new ArrayList<>();
        String[] idStrings = rId.split(",");
        for(int i = 0 ;i < idStrings.length; i++){
        	toUserIds.add(Long.parseLong(idStrings[i]));
        }
        //根据用户id查询用户极光id
        jPushBean.setRegistrationId(starUserDeviceMapper.selectTokenListByUid(toUserIds));
        jPushBean.setAudienceType(AudienceType.REGISTRATION_ID);
        jPushBean.setPlatform(Platform.all());
        jPushBean.setPlatformType(PlatformType.Android_ios);
        List<NotificationInfo> notifications = new ArrayList<NotificationInfo>();
//        NotificationInfo notificationInfo = new NotificationInfo();
        NotificationAndoridInfo notificationInfoAndroid = new NotificationAndoridInfo();
        NotificationIosInfo notificationIosInfo = new NotificationIosInfo();
        String sendMsg = BuildAlert.createAlert(busType,order.getOrderNos(),"");
        notificationInfoAndroid.setAlert(sendMsg);
        notificationIosInfo.setAlert(sendMsg);
//        notificationInfo.setAlert(sendMsg);
        Map<String,String> extras = new HashMap<>();
        extras.put("orderNo",order.getOrderNos());
        extras.put("msgType","3");
        extras.put("orderStatus",order.getStatus());
        Date date = new Date();
        String dateTime = DateUtils.formatDate(date,"yyyy-MM-dd HH:mm:ss");
//                    String dateTime = DateUtil.formatAlternativeIso8601Date(new Date());
        extras.put("sendTime",dateTime);
        notificationInfoAndroid.setExtras(extras);
        notificationIosInfo.setExtras(extras);
//        notificationInfo.setExtras(extras);
        notifications.add(notificationInfoAndroid);
        notifications.add(notificationIosInfo);
//        notifications.add(notificationInfo);
        jPushBean.setNotifications(notifications);

        PushMessage pushMessage = new PushMessage();
        pushMessage.setMsgContent(sendMsg);
        pushMessage.setMsgTitle(sendMsg);
        extras = new HashMap<>();
        extras.put("goodName",goods.getName());
        extras.put("msgTitle",sendMsg);
        extras.put("msgType","3");
        extras.put("orderNo",order.getId().toString());
        extras.put("orderStatus",order.getStatus());
        extras.put("picUrl",goods.getDetailPicUrl());
        extras.put("sendTime",dateTime);
        pushMessage.setMsgExtras(extras);

        Map<String,Object> obs = new HashMap<>();
        obs.put("jPushBean",jPushBean);
        obs.put("pushMessage",pushMessage);
//        return obs;
        sendPushAndroid_ios(jPushBean,pushMessage);
        return obs;
    }
}
