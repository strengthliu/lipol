package com.yixinintl.model;

/**
 * Created by eilir on 16/8/4.
 */
public class StarBaseVO extends BaseVO{
    public String id;
    public String name;
    public String picUrl;
    public int followed;//粉丝数
    public int mark;//是否成为过代言人
    
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public int getFollowed() {
		return followed;
	}

	public void setFollowed(int followed) {
		this.followed = followed;
	}

	public StarBaseVO(){
        super();
    }

    public int getMark() {
		return mark;
	}

	public void setMark(int mark) {
		this.mark = mark;
	}

	public StarBaseVO(boolean debug){
        this.id = "105402";
        this.name = "185****7627_7ua";
        this.picUrl = "http://hsyshopimage.oss-cn-shanghai.aliyuncs.com/images/touxiang_default.png";
        this.followed = 123;
    }
    public StarBaseVO(String id, String name, String picUrl){
        this.id = id;
        this.name = name;
        this.picUrl = picUrl;
    }
    public StarBaseVO(String id, String name, String picUrl, int mark){
        this.id = id;
        this.name = name;
        this.picUrl = picUrl;
        this.mark = mark;
    }

    
}
