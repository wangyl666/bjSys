import request, { type Result } from '@/utils/request'

export function toggleFavorite(noteId: number): Promise<Result<boolean>> {
  return request({
    url: '/favorites/toggle',
    method: 'post',
    params: { noteId }
  })
}

export function checkFavorite(noteId: number): Promise<Result<boolean>> {
  return request({
    url: '/favorites/check',
    method: 'get',
    params: { noteId }
  })
}

export function getFavoriteCount(noteId: number): Promise<Result<number>> {
  return request({
    url: '/favorites/count',
    method: 'get',
    params: { noteId }
  })
}
