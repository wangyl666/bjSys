import request, { type Result, type PageResult } from '@/utils/request'
import type { NoteApprovalVO } from '@/types'

export function submitApproval(noteId: number): Promise<Result<NoteApprovalVO>> {
  return request({
    url: '/approvals/submit',
    method: 'post',
    data: { noteId }
  })
}

export function getMyApprovals(page: number, size: number): Promise<Result<PageResult<NoteApprovalVO>>> {
  return request({
    url: '/approvals/my',
    method: 'get',
    params: { page, size }
  })
}

export function getApprovalDetail(id: number): Promise<Result<NoteApprovalVO>> {
  return request({
    url: `/approvals/${id}`,
    method: 'get'
  })
}

export function getPendingApprovals(page: number, size: number): Promise<Result<PageResult<NoteApprovalVO>>> {
  return request({
    url: '/approvals/pending',
    method: 'get',
    params: { page, size }
  })
}

export function processApproval(approvalId: number, approved: boolean, remark?: string): Promise<Result<NoteApprovalVO>> {
  return request({
    url: '/approvals/process',
    method: 'post',
    data: { approvalId, approved, remark }
  })
}

export function viewApproval(approvalId: number): Promise<Result<NoteApprovalVO>> {
  return request({
    url: `/approvals/view/${approvalId}`,
    method: 'post'
  })
}
