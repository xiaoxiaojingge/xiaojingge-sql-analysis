package com.itjing.sql.util;

import com.itjing.sql.domain.jd.dto.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库测试连接
 *
 * @author lijing
 * @date 2025-04-17
 */
@Slf4j
public class DataSourceConnectDemo {

	public void testConnection(DataSourceDTO config) {
		log.info("测试连接，数据源信息：{}", config);
		try {
			DataSource dataSource = buildDataSource(config);
			try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
				stmt.execute("SELECT 1");
				// 数据库元信息 TODO
				DatabaseMetaData metaData = conn.getMetaData();
				log.info("测试连接成功");
			}
		}
		catch (SQLException e) {
			log.error("测试连接失败，{}", e.getMessage());
		}
	}

	/**
	 * 构建数据源
	 * @param config 配置
	 * @return {@link DataSource }
	 */
	private DataSource buildDataSource(DataSourceDTO config) {
		return DataSourceBuilder.create()
			.url(config.getUrl())
			.username(config.getUsername())
			.password(config.getPassword())
			.build();
	}

}
