import request from '@/utils/request'
import type { ChatMessageVO, PageResult } from '@/types'

export function getChatMessages(page: number = 1, size: number = 50) {
  return request<PageResult<ChatMessageVO>>({
    url: '/chat/messages',
    method: 'get',
    params: { page, size }
  })
}
