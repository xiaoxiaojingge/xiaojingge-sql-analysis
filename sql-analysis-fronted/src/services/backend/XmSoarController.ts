// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

export async function checkHealthUsingGet(
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/xm/soar/health', {
    method: 'GET',
    ...(options || {}),
  });
}
