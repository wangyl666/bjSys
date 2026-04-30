<template>
  <div class="note-detail-container">
    <div class="detail-header">
      <div class="header-left">
        <el-button @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>
      <div class="header-right">
        <el-button @click="handleCopy">
          <el-icon><CopyDocument /></el-icon>
          复制
        </el-button>
        <el-button @click="handleEdit">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button type="danger" @click="handleDelete">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </div>
    </div>
    
    <div class="detail-content" v-loading="loading">
      <div class="note-header" v-if="note">
        <h1 class="note-title">{{ note.title }}</h1>
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
              <el-icon><Clock /></el-icon>
              创建于 {{ formatTime(note.createdAt) }}
            </span>
            <span class="meta-item" v-if="note.updatedAt !== note.createdAt">
              <el-icon><EditPen /></el-icon>
              更新于 {{ formatTime(note.updatedAt) }}
            </span>
          </div>
        </div>
      </div>
      
      <div class="note-body">
        <div id="vditor-preview" class="vditor-preview-container"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Vditor from 'vditor'
import { getNoteById, deleteNote, copyNote } from '@/api/note'
import { ArrowLeft, Edit, Delete, Folder, PriceTag, View, Clock, EditPen, CopyDocument } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import type { NoteVO } from '@/types'

const route = useRoute()
const router = useRouter()

const noteId = ref(Number(route.params.id))
const note = ref<NoteVO | null>(null)
const loading = ref(false)
let previewElement: any = null

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const fetchNoteDetail = async () => {
  loading.value = true
  try {
    const res = await getNoteById(noteId.value)
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

const handleEdit = () => {
  router.push(`/notes/${noteId.value}/edit`)
}

const handleCopy = async () => {
  try {
    const res = await copyNote(noteId.value)
    ElMessage.success('复制成功')
    router.push(`/notes/${res.data.id}/edit`)
  } catch (error) {
    console.error('复制笔记失败:', error)
  }
}

const handleDelete = async () => {
  await ElMessageBox.confirm(`确定要删除笔记"${note.value?.title}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  
  try {
    await deleteNote(noteId.value)
    ElMessage.success('删除成功')
    router.push('/notes')
  } catch (error) {
    console.error('删除笔记失败:', error)
  }
}

onMounted(() => {
  fetchNoteDetail()
})
</script>

<style scoped>
.note-detail-container {
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
</style>
