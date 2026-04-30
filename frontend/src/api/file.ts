import request, { type Result } from '@/utils/request'

export interface UploadResult {
  url: string
  name: string
}

export function uploadImage(file: File): Promise<Result<UploadResult>> {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
