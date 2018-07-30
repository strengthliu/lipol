package com.yixinintl.exception;

public class HeySoundException extends Exception {
	public HeySoundException(String notice) {
		super(notice);
	}
	public HeySoundException(Exception exception) {
		super(exception);
	}

}
