package com.itjing.sql.response;

import com.itjing.sql.constant.HttpStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

/**
 * 操作消息提醒
 *
 * @author xiaojingge@date 2024-12-24@date 2024-12-24
 */
public class AjaxResult<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 状态码
	 */
	public int code;

	/**
	 * 返回内容
	 */
	public String msg;

	/**
	 * 数据对象
	 */
	public T data;

	/**
	 * 初始化一个新创建的 AjaxResult 对象，使其表示一个空消息。
	 */
	public AjaxResult() {
	}

	/**
	 * 初始化一个新创建的 AjaxResult 对象
	 * @param code 状态码
	 * @param msg 返回内容
	 */
	public AjaxResult(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	/**
	 * 初始化一个新创建的 AjaxResult 对象
	 * @param code 状态码
	 * @param msg 返回内容
	 * @param data 数据对象
	 */
	public AjaxResult(int code, String msg, T data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
	}

	/**
	 * 返回成功消息
	 * @return 成功消息
	 */
	public static AjaxResult success() {
		return AjaxResult.success("操作成功");
	}

	/**
	 * 返回成功数据
	 * @return 成功消息
	 */
	public static AjaxResult success(Object data) {
		return AjaxResult.success("操作成功", data);
	}

	/**
	 * 返回成功消息
	 * @param msg 返回内容
	 * @return 成功消息
	 */
	public static AjaxResult success(String msg) {
		return AjaxResult.success(msg, null);
	}

	/**
	 * 返回成功消息
	 * @param msg 返回内容
	 * @param data 数据对象
	 * @return 成功消息
	 */
	public static AjaxResult success(String msg, Object data) {
		return new AjaxResult(HttpStatus.SUCCESS, msg, data);
	}

	/**
	 * 返回警告消息
	 * @param msg 返回内容
	 * @return 警告消息
	 */
	public static AjaxResult warn(String msg) {
		return AjaxResult.warn(msg, null);
	}

	/**
	 * 返回警告消息
	 * @param msg 返回内容
	 * @param data 数据对象
	 * @return 警告消息
	 */
	public static AjaxResult warn(String msg, Object data) {
		return new AjaxResult(HttpStatus.WARN, msg, data);
	}

	/**
	 * 返回错误消息
	 * @return 错误消息
	 */
	public static AjaxResult error() {
		return AjaxResult.error("操作失败");
	}

	/**
	 * 返回错误消息
	 * @param msg 返回内容
	 * @return 错误消息
	 */
	public static AjaxResult error(String msg) {
		return AjaxResult.error(msg, null);
	}

	/**
	 * 返回错误消息
	 * @param msg 返回内容
	 * @param data 数据对象
	 * @return 错误消息
	 */
	public static AjaxResult error(String msg, Object data) {
		return new AjaxResult(HttpStatus.ERROR, msg, data);
	}

	/**
	 * 返回错误消息
	 * @param code 状态码
	 * @param msg 返回内容
	 * @return 错误消息
	 */
	public static AjaxResult error(int code, String msg) {
		return new AjaxResult(code, msg, null);
	}

}
