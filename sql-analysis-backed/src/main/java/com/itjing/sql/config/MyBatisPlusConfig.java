package com.itjing.sql.config;

import com.itjing.sql.util.ConfigUtils;
import com.jd.sql.analysis.core.SqlAnalysisAspect;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * MyBatisPlus配置
 *
 * @author lijing
 * @date 2025-04-16
 */
@Configuration
@MapperScan("com.itjing.sql.mapper")
public class MyBatisPlusConfig {

	/**
	 * SQL分析方面
	 * @return {@link SqlAnalysisAspect}
	 */
	@Bean
	public SqlAnalysisAspect sqlAnalysisAspect() {

		// 加载配置文件，此处加载的是名为"sql.slow"的配置
		Properties properties = ConfigUtils.loadConfig("sql.slow");

		// 创建SQL分析切面的实例
		SqlAnalysisAspect sqlAnalysisAspect = new SqlAnalysisAspect();

		// 将加载的配置属性设置到SQL分析切面实例中
		sqlAnalysisAspect.setProperties(properties);

		// 返回配置完毕的SQL分析切面实例
		return sqlAnalysisAspect;
	}

}
