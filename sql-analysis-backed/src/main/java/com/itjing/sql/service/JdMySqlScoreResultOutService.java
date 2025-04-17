package com.itjing.sql.service;

import cn.hutool.core.util.StrUtil;
import com.jd.sql.analysis.score.SqlScoreResult;
import com.jd.sql.analysis.score.SqlScoreResultDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * SQL 分数结果输出服务默认服务
 *
 * @author lijing
 * @date 2025-04-16
 */
@Slf4j
@Service
public class JdMySqlScoreResultOutService {

	public String outResult(SqlScoreResult sqlScoreResult) {
		StringBuilder result = new StringBuilder();
		result.append("======================================自定义分析结果===================================\n\n");
		AtomicInteger atomicInteger = new AtomicInteger(1);
		if (Objects.isNull(sqlScoreResult)) {
			throw new RuntimeException("分析失败，sqlScoreResult is null");
		}
		Consumer<SqlScoreResultDetail> printConsumer = item -> {
			result.append(
					StrUtil.format("\n=============({})命中规则===================\n", atomicInteger.getAndIncrement()));
			result.append(StrUtil.format("规则命中原因：{}\n", item.getReason()));
			result.append(StrUtil.format("规则命中，修改建议：{}\n", item.getSuggestion()));
			result.append(StrUtil.format("规则命中，{}{} 分\n", item.getScoreDeduction() < 0 ? "加上分数：+" : "减去分数 ",
					-item.getScoreDeduction()));
		};
		if (Objects.nonNull(sqlScoreResult.getNeedWarn())) {
			result.append(StrUtil.format("分析中的 SQL 语句 ID：{}\n", sqlScoreResult.getSqlId()));
			if (Boolean.TRUE.equals(sqlScoreResult.getNeedWarn())) {
				result.append(StrUtil.format("SQL分析结果的分数为:{}，低于预期值请判断是否修改\n", sqlScoreResult.getScore()));
				if (sqlScoreResult.getAnalysisResults() != null) {
					sqlScoreResult.getAnalysisResults().forEach(printConsumer);
				}
			}
			else {
				result.append(StrUtil.format("SQL分析结果的分数为:{}，分析正常\n", sqlScoreResult.getScore()));
				result.append(StrUtil.format("=====给出的修改建议如下=====\n"));
				sqlScoreResult.getAnalysisResults().forEach(printConsumer);
				result.append("=========================\n");
			}
		}
		result.append("\n========================================结束=====================================\n");
		return result.toString();
	}

}