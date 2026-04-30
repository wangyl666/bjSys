export interface UserVO {
  id: number
  username: string
  nickname: string
  avatar: string | null
  createdAt: string
}

export interface UserStatsVO {
  noteCount: number
  errorQuestionCount: number
  categoryCount: number
  tagCount: number
}

export interface UpdateUserInfoDTO {
  nickname?: string
  avatar?: string
}

export interface UpdatePasswordDTO {
  oldPassword: string
  newPassword: string
}

export interface LoginDTO {
  username: string
  password: string
}

export interface RegisterDTO {
  username: string
  password: string
  nickname?: string
}

export interface CategoryVO {
  id: number
  name: string
  sort: number
  createdAt: string
  updatedAt: string
}

export interface TagVO {
  id: number
  name: string
  color: string
}

export interface NoteVO {
  id: number
  userId: number
  categoryId: number | null
  categoryName: string | null
  title: string
  content: string
  summary: string | null
  isPublic: number
  viewCount: number
  tags: TagVO[]
  createdAt: string
  updatedAt: string
}

export interface NoteDTO {
  id?: number
  categoryId?: number
  title: string
  content?: string
  summary?: string
  isPublic?: number
  tagIds?: number[]
}

export interface DraftDTO {
  noteId?: number
  categoryId?: number
  title?: string
  content?: string
  tagIds?: number[]
}

export interface Draft {
  userId: number
  noteId: number | null
  title: string | null
  content: string | null
  categoryId: number | null
  savedAt: string
}

export interface PageParams {
  page: number
  size: number
  categoryId?: number
  keyword?: string
}
