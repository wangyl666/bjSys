import request, { type Result } from '@/utils/request'
import type { DraftDTO, Draft } from '@/types'

export function saveDraft(data: DraftDTO): Promise<Result<void>> {
  return request({
    url: '/drafts',
    method: 'post',
    data
  })
}

export function getDraft(noteId?: number): Promise<Result<Draft | null>> {
  return request({
    url: '/drafts',
    method: 'get',
    params: { noteId }
  })
}

export function deleteDraft(noteId?: number): Promise<Result<void>> {
  return request({
    url: '/drafts',
    method: 'delete',
    params: { noteId }
  })
}
