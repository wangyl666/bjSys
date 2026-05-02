import request, { type Result } from '@/utils/request'
import type { UserVO, UserStatsVO, UpdateUserInfoDTO, UpdatePasswordDTO } from '@/types'

export function getUserInfo(): Promise<Result<UserVO>> {
  return request({
    url: '/users/current',
    method: 'get'
  })
}

export function getUserStats(): Promise<Result<UserStatsVO>> {
  return request({
    url: '/users/stats',
    method: 'get'
  })
}

export function updateUserInfo(data: UpdateUserInfoDTO): Promise<Result<UserVO>> {
  return request({
    url: '/users/info',
    method: 'put',
    data
  })
}

export function updatePassword(data: UpdatePasswordDTO): Promise<Result<void>> {
  return request({
    url: '/users/password',
    method: 'put',
    data
  })
}

export function uploadAvatar(file: File): Promise<Result<UserVO>> {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/users/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
