package com.yixinintl.exception;

public class NoticeException extends HeySoundException implements INotice {

	//@Override
	public NoticeException(String notice) {
		super(notice);
	}
}
