import request, { type Result } from '@/utils/request'
import type { NoteCommentVO, CreateCommentDTO } from '@/types'

export function createComment(data: CreateCommentDTO): Promise<Result<NoteCommentVO>> {
  return request({
    url: '/comments',
    method: 'post',
    data
  })
}

export function deleteComment(id: number): Promise<Result<void>> {
  return request({
    url: `/comments/${id}`,
    method: 'delete'
  })
}

export function getCommentsByNoteId(noteId: number): Promise<Result<NoteCommentVO[]>> {
  return request({
    url: `/comments/note/${noteId}`,
    method: 'get'
  })
}

export function getCommentCount(noteId: number): Promise<Result<number>> {
  return request({
    url: '/comments/count',
    method: 'get',
    params: { noteId }
  })
}
