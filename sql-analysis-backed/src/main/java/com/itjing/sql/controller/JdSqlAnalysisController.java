package com.itjing.sql.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.itjing.sql.domain.jd.dto.DataSourceDTO;
import com.itjing.sql.domain.jd.vo.DatabaseMetaVO;
import com.itjing.sql.service.JdMySqlScoreResultOutService;
import com.itjing.sql.response.AjaxResult;
import com.jd.sql.analysis.analysis.SqlAnalysisResult;
import com.jd.sql.analysis.analysis.SqlAnalysisResultList;
import com.jd.sql.analysis.score.SqlScoreResult;
import com.jd.sql.analysis.score.SqlScoreService;
import com.jd.sql.analysis.score.SqlScoreServiceRulesEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.validation.Valid;
import java.sql.*;
import java.util.*;

/**
 * 京东sql-analysis Sql分析控制器
 *
 * @author lijing
 * @date 2025-04-16
 */
@RestController
@Slf4j
@RequestMapping("/jd/sql-analysis")
public class JdSqlAnalysisController {

	@Resource
	private JdMySqlScoreResultOutService jdMySqlScoreResultOutService;

	/**
	 * 测试连接
	 * @param config 配置
	 * @return {@link AjaxResult }
	 */
	@PostMapping("/testConnection")
	public AjaxResult<?> testConnection(@Valid @RequestBody DataSourceDTO config) {
		log.info("测试连接，数据源信息：{}", config);
		try {
			DataSource dataSource = buildDataSource(config);
			try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
				stmt.execute("SELECT 1");
				DatabaseMetaData metaData = conn.getMetaData();
				DatabaseMetaVO metaInfo = extractMetadata(metaData);
				log.info("测试连接成功，数据库元信息：{}", metaInfo);
				return AjaxResult.success(metaInfo);
			}
		}
		catch (SQLException e) {
			log.error("测试连接失败，{}", e.getMessage());
			// return AjaxResult.error(StrUtil.format("测试连接失败，请检查url、用户名、密码是否正确"));
			return AjaxResult.error(e.getMessage());
		}
	}

	/**
	 * 分析SQL
	 * @param config 配置
	 * @return {@link AjaxResult }
	 */
	// @PostMapping("/analyze")
	// public AjaxResult<?> analyzeSql(@Valid @RequestBody DataSourceDTO config) {
	// try {
	// if (StrUtil.isBlank(config.getSql())) {
	// return AjaxResult.error("sql不能为空");
	// }
	// DataSource dataSource = buildDataSource(config);
	// try (Connection conn = dataSource.getConnection()) {
	// SqlExtractResult sqlExtractResult = new SqlExtractResult();
	// sqlExtractResult.setSourceSql(config.getSql());
	// sqlExtractResult.setSqlId("1");
	// SqlAnalysisResultList resultList = SqlAnalysis.analysis(sqlExtractResult, conn);
	// // 调用规则引擎评分（需初始化规则引擎）
	// SqlScoreService scoreService = new SqlScoreServiceRulesEngine();
	// SqlScoreResult score = scoreService.score(resultList);
	// return AjaxResult.success("分析成功", mySqlScoreResultOutService.outResult(score));
	// }
	// } catch (SQLException e) {
	// log.error("分析sql失败，{}", e.getMessage());
	// return AjaxResult.error(e.getMessage());
	// }
	// }

	/**
	 * 分析SQL
	 * @param config 配置
	 * @return {@link AjaxResult }
	 */
	@PostMapping("/analyze")
	public AjaxResult<?> analyzeSql(@Valid @RequestBody DataSourceDTO config) {
		try {
			if (StrUtil.isBlank(config.getSql())) {
				return AjaxResult.error("sql不能为空");
			}
			Map<String, Object> result = new HashMap<>();
			DataSource dataSource = buildDataSource(config);
			try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
				// 执行 EXPLAIN 获取执行计划
				ResultSet explainResult = stmt.executeQuery("EXPLAIN " + config.getSql());
				// 解析为结构化数据
				SqlAnalysisResultList resultList = parseExplainResult(explainResult);
				log.info(JSONObject.toJSONString(resultList.getResultList()));
				// 调用规则引擎评分（需初始化规则引擎）
				SqlScoreService scoreService = new SqlScoreServiceRulesEngine();
				SqlScoreResult score = scoreService.score(resultList);
				String scoreResult = jdMySqlScoreResultOutService.outResult(score);
				result.put("explainResultList", resultList.getResultList());
				result.put("scoreResult", scoreResult);
				return AjaxResult.success(result);
			}
		}
		catch (SQLException e) {
			log.error("分析sql失败，{}", e.getMessage());
			return AjaxResult.error(e.getMessage());
		}
	}

	/**
	 * 解析 explain 结果
	 * @param rs explain 结果
	 * @return {@link Map }<{@link String }, {@link Object }>
	 */
	public SqlAnalysisResultList parseExplainResult(ResultSet rs) throws SQLException {
		SqlAnalysisResultList resultList = new SqlAnalysisResultList();
		List<SqlAnalysisResult> analysisResults = new ArrayList<>();

		// 遍历 EXPLAIN 结果集的每一行
		while (rs.next()) {
			SqlAnalysisResult result = new SqlAnalysisResult();
			// 映射字段（需适配不同数据库方言）
			result.setId(rs.getLong("id"));
			result.setSelectType(rs.getString("select_type"));
			result.setTable(rs.getString("table"));
			result.setType(rs.getString("type"));
			result.setPossibleKeys(rs.getString("possible_keys"));
			result.setKey(rs.getString("key"));
			result.setRows(rs.getString("rows"));
			result.setFiltered(rs.getDouble("filtered"));
			result.setExtra(rs.getString("Extra"));
			analysisResults.add(result);
		}

		resultList.setResultList(analysisResults);
		return resultList;
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

	/**
	 * 提取元数据
	 * @param metaData 元数据
	 * @return {@link DatabaseMetaVO }
	 * @throws SQLException sql异常
	 */
	private DatabaseMetaVO extractMetadata(DatabaseMetaData metaData) throws SQLException {
		return new DatabaseMetaVO(metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion(),
				metaData.getDriverName(), metaData.getDriverVersion(), metaData.getUserName(), metaData.getURL(),
				metaData.supportsTransactions());
	}

}
