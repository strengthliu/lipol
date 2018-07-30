package com.yixinintl.domain;

import java.util.Date;

public class StarUserDevice {
    private Long deviceId;

    private Byte deviceType;

    private String deviceToken;

    private Long userId;

    private Date updateTime;

    private Date createTime;

    private String jpushImPwd;

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Byte getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Byte deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken == null ? null : deviceToken.trim();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getJpushImPwd() {
        return jpushImPwd;
    }

    public void setJpushImPwd(String jpushImPwd) {
        this.jpushImPwd = jpushImPwd == null ? null : jpushImPwd.trim();
    }
}