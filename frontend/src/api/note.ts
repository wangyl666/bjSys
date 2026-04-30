import request, { type Result, type PageResult } from '@/utils/request'
import type { NoteVO, NoteDTO, PageParams } from '@/types'

export function createNote(data: NoteDTO): Promise<Result<NoteVO>> {
  return request({
    url: '/notes',
    method: 'post',
    data
  })
}

export function updateNote(data: NoteDTO): Promise<Result<NoteVO>> {
  return request({
    url: '/notes',
    method: 'put',
    data
  })
}

export function deleteNote(id: number): Promise<Result<void>> {
  return request({
    url: `/notes/${id}`,
    method: 'delete'
  })
}

export function copyNote(id: number): Promise<Result<NoteVO>> {
  return request({
    url: `/notes/${id}/copy`,
    method: 'post'
  })
}

export function getNoteById(id: number): Promise<Result<NoteVO>> {
  return request({
    url: `/notes/${id}`,
    method: 'get'
  })
}

export function getNoteList(params: PageParams): Promise<Result<PageResult<NoteVO>>> {
  return request({
    url: '/notes',
    method: 'get',
    params
  })
}
