package com.itjing.sql.domain.jd.vo;

import lombok.*;

/**
 * 数据库元信息
 *
 * @author lijing
 * @date 2025-04-16
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class DatabaseMetaVO {

	/**
	 * 数据库产品名称（如MySQL）
	 */
	private String databaseProductName;

	/**
	 * 数据库版本（如8.0.32）
	 */
	private String databaseVersion;

	/**
	 * JDBC驱动名称（如MySQL Connector/J）
	 */
	private String driverName;

	/**
	 * JDBC驱动版本（如8.0.32）
	 */
	private String driverVersion;

	/**
	 * 数据库用户名（如root）
	 */
	private String userName;

	/**
	 * 数据库连接URL（如jdbc:mysql://localhost:3306/test）
	 */
	private String connectionUrl;

	/**
	 * 事务支持情况
	 */
	private boolean transactionSupported;

}