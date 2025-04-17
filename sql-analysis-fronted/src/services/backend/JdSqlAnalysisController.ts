// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** 测试数据库连接 POST /api/sql/test-connection */
export async function testConnectionUsingPost(
  body: API.SqlConnectionRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/jd/sql-analysis/testConnection', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 分析SQL语句 POST /api/sql/analyze */
export async function analyzeSqlUsingPost(
  body: API.SqlAnalyzeRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseSqlAnalysisResult_>('/jd/sql-analysis/analyze', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
