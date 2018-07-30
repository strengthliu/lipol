package com.yixinintl.util;

/**
 * Created by eilir on 16/7/21.
 */
public enum ThirdTypeEnum {
    QQ(1,"qq"),
    WEIXIN(2,"微信"),
    WEIBO(3,"新浪微博"),
    ;

    private int value;
    private String name;

    ThirdTypeEnum(int value, String name) {
        this.value = value;
        this.name=name;
    }

    public int getValue() {
        return value;
    }
    public String getName() {
        return name;
    }


}
