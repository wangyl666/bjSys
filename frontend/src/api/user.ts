import request, { type Result } from '@/utils/request'
import type { UserVO } from '@/types'

export function getUserInfo(): Promise<Result<UserVO>> {
  return request({
    url: '/users/current',
    method: 'get'
  })
}
