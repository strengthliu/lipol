package com.yixinintl.exception;

public class ErrorMsgException extends RuntimeException{
	private static final long serialVersionUID = -5381602482557022284L;

	public ErrorMsgException(String msg) {
		super(msg);
	}
	
}