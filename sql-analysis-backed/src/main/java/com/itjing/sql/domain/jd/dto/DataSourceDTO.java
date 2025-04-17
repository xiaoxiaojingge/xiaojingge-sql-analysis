package com.itjing.sql.domain.jd.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serial;
import java.io.Serializable;

/**
 * 数据源
 *
 * @author lijing
 * @date 2025-04-16
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class DataSourceDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 数据库连接
	 */
	@NotBlank(message = "URL不能为空")
	@Pattern(regexp = "^jdbc:mysql://([\\w.-]+)(:\\d+)?/[\\w-]+(\\?.*)?$",
			message = "URL格式应为jdbc:mysql://host:port/dbname")
	private String url;

	/**
	 * 用户名
	 */
	@NotBlank(message = "用户名不能为空")
	private String username;

	/**
	 * 密码
	 */
	@NotBlank(message = "密码不能为空")
	private String password;

	/**
	 * 需要分析的sql
	 */
	private String sql;

}
