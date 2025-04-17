package com.itjing.sql.exception;

/**
 * 自定义异常类
 */
public class BusinessException extends RuntimeException {

	/**
	 * 错误码
	 */
	private final int code;

	public BusinessException(int code, String message) {
		super(message);
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}
