import request, { type Result, type PageResult } from '@/utils/request'
import type { CommunityNoteVO, PageParams } from '@/types'

export function getCommunityNotes(params: PageParams): Promise<Result<PageResult<CommunityNoteVO>>> {
  return request({
    url: '/community/notes',
    method: 'get',
    params
  })
}

export function getCommunityNoteDetail(id: number): Promise<Result<CommunityNoteVO>> {
  return request({
    url: `/community/notes/${id}`,
    method: 'get'
  })
}
