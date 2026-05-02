export interface UserVO {
  id: number
  username: string
  nickname: string
  avatar: string | null
  role: string
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
  noteCount: number
}

export interface TagVO {
  id: number
  name: string
  color: string
}

export interface GraphNodeVO {
  id: number
  name: string
  color: string
  noteCount: number
  category: number
}

export interface GraphEdgeVO {
  source: number
  target: number
  value: number
}

export interface TagGraphVO {
  nodes: GraphNodeVO[]
  edges: GraphEdgeVO[]
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
  approvalStatus: string
  viewCount: number
  tags: TagVO[]
  createdAt: string
  updatedAt: string
}

export interface NoteApprovalVO {
  id: number
  noteId: number
  noteTitle: string
  userId: number
  username: string
  approvalStatus: string
  adminId: number | null
  adminName: string | null
  adminRemark: string | null
  submittedAt: string
  approvedAt: string | null
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
  tagIds: number[] | null
  savedAt: string
}

export interface PageParams {
  page: number
  size: number
  categoryId?: number
  keyword?: string
  tagId?: number
}

export interface CommunityNoteVO {
  id: number
  userId: number
  username: string
  userAvatar: string | null
  categoryId: number | null
  categoryName: string | null
  title: string
  content: string
  summary: string | null
  viewCount: number
  favoriteCount: number
  commentCount: number
  isFavorited: boolean
  tags: TagVO[]
  createdAt: string
  updatedAt: string
}

export interface NoteCommentVO {
  id: number
  noteId: number
  userId: number
  username: string
  userAvatar: string | null
  parentId: number | null
  replyToUserId: number | null
  replyToUsername: string | null
  content: string
  children?: NoteCommentVO[]
  createdAt: string
  updatedAt: string
}

export interface CreateCommentDTO {
  noteId: number
  parentId?: number
  replyToUserId?: number
  content: string
}

export interface ChatMessageVO {
  id: number
  userId: number
  content: string
  messageType: 'TEXT' | 'SYSTEM'
  createdAt: string
  user?: UserVO
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
