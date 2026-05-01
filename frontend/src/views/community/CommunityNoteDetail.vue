<template>
  <div class="community-detail-container">
    <div class="detail-header">
      <div class="header-left">
        <el-button @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>
      <div class="header-right">
        <el-button :type="note?.isFavorited ? 'warning' : 'default'" @click="handleToggleFavorite">
          <el-icon><StarFilled /></el-icon>
          {{ note?.isFavorited ? '已收藏' : '收藏' }}
        </el-button>
        <el-button type="primary" @click="handleCopyNote">
          <el-icon><CopyDocument /></el-icon>
          复制笔记
        </el-button>
      </div>
    </div>
    
    <div class="detail-content" v-loading="loading">
      <div class="note-header" v-if="note">
        <h1 class="note-title">{{ note.title }}</h1>
        <div class="note-author">
          <el-avatar :size="40" v-if="note.userAvatar" :src="note.userAvatar" />
          <el-avatar :size="40" v-else>
            {{ note.username?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="author-info">
            <span class="author-name">{{ note.username }}</span>
            <span class="publish-time">发布于 {{ formatTime(note.createdAt) }}</span>
          </div>
        </div>
        <div class="note-meta">
          <div class="meta-left">
            <el-tag v-if="note.categoryName" type="primary" size="small">
              <el-icon><Folder /></el-icon>
              {{ note.categoryName }}
            </el-tag>
            <el-tag
              v-for="tag in note.tags"
              :key="tag.id"
              size="small"
              effect="plain"
              :style="{ borderColor: tag.color, color: tag.color }"
            >
              <el-icon><PriceTag /></el-icon>
              {{ tag.name }}
            </el-tag>
          </div>
          <div class="meta-right">
            <span class="meta-item">
              <el-icon><View /></el-icon>
              {{ note.viewCount || 0 }} 浏览
            </span>
            <span class="meta-item">
              <el-icon><Star /></el-icon>
              {{ note.favoriteCount || 0 }} 收藏
            </span>
            <span class="meta-item">
              <el-icon><ChatDotRound /></el-icon>
              {{ note.commentCount || 0 }} 评论
            </span>
          </div>
        </div>
      </div>
      
      <div class="note-body">
        <div id="vditor-preview" class="vditor-preview-container"></div>
      </div>
      
      <div class="comment-section">
        <div class="comment-header">
          <span class="comment-title">评论 ({{ comments.length }})</span>
        </div>
        
        <div class="comment-input-section">
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="3"
            placeholder="发表你的评论..."
            maxlength="500"
            show-word-limit
          />
          <div class="comment-actions">
            <el-button type="primary" :loading="submittingComment" @click="handleSubmitComment">
              发表评论
            </el-button>
          </div>
        </div>
        
        <div class="comment-list">
          <div v-if="comments.length === 0" class="empty-comments">
            暂无评论，快来发表第一条评论吧~
          </div>
          
          <div
            v-for="comment in comments"
            :key="comment.id"
            class="comment-item"
          >
            <div class="comment-avatar">
              <el-avatar :size="36" v-if="comment.userAvatar" :src="comment.userAvatar" />
              <el-avatar :size="36" v-else>
                {{ comment.username?.charAt(0) || 'U' }}
              </el-avatar>
            </div>
            <div class="comment-content">
              <div class="comment-header">
                <span class="comment-author">{{ comment.username }}</span>
                <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                <el-button text type="primary" size="small" @click="handleReply(comment)">
                  回复
                </el-button>
              </div>
              <div class="comment-text">
                <span v-if="comment.replyToUsername" class="reply-to">
                  回复 @{{ comment.replyToUsername }}：
                </span>
                {{ comment.content }}
              </div>
              
              <div v-if="replyTarget && replyTarget.id === comment.id" class="reply-input">
                <el-input
                  v-model="replyContent"
                  type="textarea"
                  :rows="2"
                  :placeholder="`回复 @${replyTarget.username}...`"
                  maxlength="500"
                  show-word-limit
                />
                <div class="reply-actions">
                  <el-button size="small" @click="handleCancelReply">取消</el-button>
                  <el-button type="primary" size="small" :loading="submittingReply" @click="handleSubmitReply">
                    回复
                  </el-button>
                </div>
              </div>
              
              <div v-if="comment.children && comment.children.length > 0" class="replies">
                <div
                  v-for="child in comment.children"
                  :key="child.id"
                  class="reply-item"
                >
                  <div class="comment-avatar">
                    <el-avatar :size="28" v-if="child.userAvatar" :src="child.userAvatar" />
                    <el-avatar :size="28" v-else>
                      {{ child.username?.charAt(0) || 'U' }}
                    </el-avatar>
                  </div>
                  <div class="comment-content">
                    <div class="comment-header">
                      <span class="comment-author">{{ child.username }}</span>
                      <span class="comment-time">{{ formatTime(child.createdAt) }}</span>
                      <el-button text type="primary" size="small" @click="handleReply(child)">
                        回复
                      </el-button>
                    </div>
                    <div class="comment-text">
                      <span v-if="child.replyToUsername" class="reply-to">
                        回复 @{{ child.replyToUsername }}：
                      </span>
                      {{ child.content }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Vditor from 'vditor'
import { getCommunityNoteDetail } from '@/api/community'
import { toggleFavorite } from '@/api/favorite'
import { createComment, getCommentsByNoteId } from '@/api/comment'
import { copyNote } from '@/api/note'
import { ArrowLeft, StarFilled, CopyDocument, Folder, PriceTag, View, Star, ChatDotRound } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import type { CommunityNoteVO, NoteCommentVO, CreateCommentDTO } from '@/types'

const route = useRoute()
const router = useRouter()

const noteId = ref(Number(route.params.id))
const note = ref<CommunityNoteVO | null>(null)
const comments = ref<NoteCommentVO[]>([])
const loading = ref(false)
const submittingComment = ref(false)
const submittingReply = ref(false)
const newComment = ref('')
const replyContent = ref('')
const replyTarget = ref<NoteCommentVO | null>(null)

let previewElement: any = null

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const fetchNoteDetail = async () => {
  loading.value = true
  try {
    const res = await getCommunityNoteDetail(noteId.value)
    note.value = res.data
    
    nextTick(() => {
      renderPreview()
    })
  } catch (error) {
    console.error('获取笔记详情失败:', error)
    ElMessage.error('获取笔记详情失败')
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const res = await getCommentsByNoteId(noteId.value)
    comments.value = res.data
  } catch (error) {
    console.error('获取评论列表失败:', error)
  }
}

const renderPreview = () => {
  if (!note.value) return
  
  previewElement = Vditor.preview(document.getElementById('vditor-preview')!, note.value.content || '', {
    mode: 'light',
    hljs: {
      enable: true,
      style: 'github',
      lineNumber: true
    },
    markdown: {
      toc: true,
      mark: true,
      footnotes: true,
      codeBlockPreview: true
    },
    speech: {
      enable: false
    }
  })
}

const handleBack = () => {
  router.back()
}

const handleToggleFavorite = async () => {
  if (!note.value) return
  
  try {
    const res = await toggleFavorite(noteId.value)
    note.value.isFavorited = res.data
    note.value.favoriteCount = res.data ? (note.value.favoriteCount || 0) + 1 : (note.value.favoriteCount || 0) - 1
    ElMessage.success(res.data ? '收藏成功' : '已取消收藏')
  } catch (error) {
    console.error('收藏失败:', error)
    ElMessage.error('操作失败')
  }
}

const handleCopyNote = async () => {
  try {
    const res = await copyNote(noteId.value)
    ElMessage.success('复制成功')
    router.push(`/notes/${res.data.id}/edit`)
  } catch (error) {
    console.error('复制笔记失败:', error)
    ElMessage.error('复制失败')
  }
}

const handleSubmitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  submittingComment.value = true
  try {
    const dto: CreateCommentDTO = {
      noteId: noteId.value,
      content: newComment.value.trim()
    }
    await createComment(dto)
    ElMessage.success('评论成功')
    newComment.value = ''
    fetchComments()
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error('评论失败')
  } finally {
    submittingComment.value = false
  }
}

const handleReply = (comment: NoteCommentVO) => {
  replyTarget.value = comment
  replyContent.value = ''
}

const handleCancelReply = () => {
  replyTarget.value = null
  replyContent.value = ''
}

const handleSubmitReply = async () => {
  if (!replyContent.value.trim() || !replyTarget.value) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  submittingReply.value = true
  try {
    const dto: CreateCommentDTO = {
      noteId: noteId.value,
      parentId: replyTarget.value.parentId || replyTarget.value.id,
      replyToUserId: replyTarget.value.userId,
      content: replyContent.value.trim()
    }
    await createComment(dto)
    ElMessage.success('回复成功')
    replyTarget.value = null
    replyContent.value = ''
    fetchComments()
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  } finally {
    submittingReply.value = false
  }
}

onMounted(() => {
  fetchNoteDetail()
  fetchComments()
})
</script>

<style scoped>
.community-detail-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fff;
  flex-shrink: 0;
}

.detail-content {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.note-header {
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.note-title {
  margin: 0 0 20px 0;
  font-size: 28px;
  color: #303133;
  line-height: 1.4;
}

.note-author {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.publish-time {
  font-size: 12px;
  color: #909399;
}

.note-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.meta-left {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.meta-right {
  display: flex;
  gap: 20px;
  align-items: center;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #909399;
}

.note-body {
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.vditor-preview-container {
  min-height: 300px;
}

:deep(.vditor-preview) {
  padding: 0;
}

:deep(.vditor-preview__action) {
  display: none;
}

:deep(.vditor-reset) {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
}

:deep(.vditor-reset h1) {
  font-size: 24px;
  border-bottom: 2px solid #e4e7ed;
  padding-bottom: 10px;
  margin-top: 30px;
  margin-bottom: 20px;
}

:deep(.vditor-reset h2) {
  font-size: 20px;
  margin-top: 25px;
  margin-bottom: 15px;
}

:deep(.vditor-reset h3) {
  font-size: 18px;
  margin-top: 20px;
  margin-bottom: 12px;
}

:deep(.vditor-reset p) {
  margin-bottom: 16px;
}

:deep(.vditor-reset blockquote) {
  border-left: 4px solid #409eff;
  padding: 10px 20px;
  margin: 16px 0;
  background-color: #f5f7fa;
  color: #606266;
}

:deep(.vditor-reset pre) {
  background-color: #f6f8fa;
  border-radius: 6px;
  padding: 16px;
  overflow-x: auto;
  margin: 16px 0;
}

:deep(.vditor-reset code) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  background-color: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 14px;
}

:deep(.vditor-reset pre code) {
  background-color: transparent;
  padding: 0;
  color: #333;
}

:deep(.vditor-reset img) {
  max-width: 100%;
  border-radius: 6px;
  margin: 16px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.vditor-reset table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

:deep(.vditor-reset table th),
:deep(.vditor-reset table td) {
  border: 1px solid #e4e7ed;
  padding: 10px 15px;
  text-align: left;
}

:deep(.vditor-reset table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

:deep(.vditor-reset ul),
:deep(.vditor-reset ol) {
  padding-left: 24px;
  margin: 16px 0;
}

:deep(.vditor-reset li) {
  margin-bottom: 8px;
}

:deep(.vditor-reset hr) {
  border: none;
  border-top: 1px solid #e4e7ed;
  margin: 24px 0;
}

:deep(.vditor-reset a) {
  color: #409eff;
  text-decoration: none;
}

:deep(.vditor-reset a:hover) {
  text-decoration: underline;
}

.comment-section {
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.comment-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.comment-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.comment-input-section {
  margin-bottom: 30px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.comment-list {
  margin-top: 20px;
}

.empty-comments {
  text-align: center;
  padding: 40px;
  color: #909399;
  font-size: 14px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  padding-bottom: 0;
  border-bottom: none;
}

.comment-author {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  word-break: break-word;
}

.reply-to {
  color: #409eff;
}

.reply-input {
  margin-top: 12px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

.replies {
  margin-top: 12px;
  padding-left: 48px;
}

.reply-item {
  display: flex;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.reply-item:last-child {
  border-bottom: none;
}
</style>
