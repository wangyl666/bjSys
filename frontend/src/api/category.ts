import request, { type Result } from '@/utils/request'
import type { CategoryVO } from '@/types'

export function getCategories(): Promise<Result<CategoryVO[]>> {
  return request({
    url: '/categories',
    method: 'get'
  })
}

export function createCategory(name: string): Promise<Result<CategoryVO>> {
  return request({
    url: '/categories',
    method: 'post',
    params: { name }
  })
}

export function updateCategory(id: number, name: string): Promise<Result<CategoryVO>> {
  return request({
    url: `/categories/${id}`,
    method: 'put',
    params: { name }
  })
}

export function deleteCategory(id: number): Promise<Result<void>> {
  return request({
    url: `/categories/${id}`,
    method: 'delete'
  })
}
