import request, { type Result } from '@/utils/request'
import type { TagVO } from '@/types'

export function getTags(): Promise<Result<TagVO[]>> {
  return request({
    url: '/tags',
    method: 'get'
  })
}

export function createTag(name: string, color?: string): Promise<Result<TagVO>> {
  return request({
    url: '/tags',
    method: 'post',
    params: { name, color }
  })
}

export function deleteTag(id: number): Promise<Result<void>> {
  return request({
    url: `/tags/${id}`,
    method: 'delete'
  })
}
