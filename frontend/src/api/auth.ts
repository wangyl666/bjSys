import request, { type Result } from '@/utils/request'
import type { LoginDTO, RegisterDTO } from '@/types'

export interface LoginResult {
  token: string
}

export function login(data: LoginDTO): Promise<Result<LoginResult>> {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register(data: RegisterDTO): Promise<Result<{ userId: number }>> {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}
