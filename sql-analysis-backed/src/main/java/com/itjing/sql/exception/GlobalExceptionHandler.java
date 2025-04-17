package com.itjing.sql.exception;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObjectIter;
import com.alibaba.fastjson2.JSONObject;
import com.itjing.sql.response.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public AjaxResult businessExceptionHandler(BusinessException e) {
		log.error("BusinessException: {}", e.getMessage());
		return AjaxResult.error(e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public AjaxResult methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
		log.error("MethodArgumentNotValidException: {}", e.getMessage());
		Map<String, String> errors = new HashMap<>();
		e.getBindingResult().getFieldErrors().forEach(error -> {
			errors.put(error.getField(), error.getDefaultMessage());
		});
		return AjaxResult.error(JSONObject.toJSONString(errors));
	}

	@ExceptionHandler(Exception.class)
	public AjaxResult exceptionHandler(Exception e) {
		log.error("Exception: {}", e.getMessage());
		return AjaxResult.error(e.getMessage());
	}

}
