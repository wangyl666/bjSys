<template>
  <div class="note-edit-container">
    <div class="edit-header">
      <div class="header-left">
        <el-button @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <span class="page-title">{{ isEdit ? '编辑笔记' : '新建笔记' }}</span>
      </div>
      <div class="header-right">
        <el-tag v-if="hasDraft" type="warning" effect="dark">
          <el-icon><Clock /></el-icon>
          有未保存的草稿
        </el-tag>
        <el-button type="primary" :loading="saving" @click="handleSave">
          <el-icon><Check /></el-icon>
          保存
        </el-button>
      </div>
    </div>
    
    <div class="edit-content">
      <div class="note-meta">
        <el-form :model="noteForm" label-width="80px">
          <el-form-item label="标题" required>
            <el-input
              v-model="noteForm.title"
              placeholder="请输入笔记标题"
              size="large"
              class="title-input"
            />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="分类">
                <el-select
                  v-model="noteForm.categoryId"
                  placeholder="选择分类"
                  clearable
                  style="width: 100%"
                >
                  <el-option
                    v-for="category in categories"
                    :key="category.id"
                    :label="category.name"
                    :value="category.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="标签">
                <el-select
                  v-model="selectedTags"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  placeholder="选择或创建标签"
                  style="width: 100%"
                  @change="handleTagChange"
                  @create="handleCreateTag"
                >
                  <el-option
                    v-for="tag in tags"
                    :key="tag.id"
                    :label="tag.name"
                    :value="tag.id"
                  >
                    <span>
                      <el-tag :color="tag.color" size="small">{{ tag.name }}</el-tag>
                    </span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="公开">
                <el-switch v-model="isPublic" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      
      <div class="editor-wrapper" ref="editorRef">
        <div id="vditor" class="vditor-container"></div>
      </div>
    </div>
    
    <el-dialog v-model="draftDialogVisible" title="检测到草稿" width="400px">
      <div class="draft-dialog-content">
        <p>检测到您有未保存的草稿，是否恢复？</p>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="标题">
            {{ draftData?.title || '无标题' }}
          </el-descriptions-item>
          <el-descriptions-item label="保存时间">
            {{ draftData?.savedAt ? formatTime(draftData.savedAt) : '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="handleSkipDraft">跳过</el-button>
        <el-button type="primary" @click="handleRestoreDraft">恢复草稿</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Vditor from 'vditor'
import debounce from 'lodash-es/debounce'
import { createNote, updateNote, getNoteById } from '@/api/note'
import { getCategories, createCategory } from '@/api/category'
import { getTags, createTag } from '@/api/tag'
import { getDraft, saveDraft, deleteDraft } from '@/api/draft'
import { uploadImage } from '@/api/file'
import { ArrowLeft, Check, Clock } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import type { NoteDTO, NoteVO, CategoryVO, TagVO, Draft, DraftDTO } from '@/types'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const noteId = computed(() => route.params.id ? Number(route.params.id) : undefined)

const editorRef = ref<HTMLElement>()
let vditor: Vditor | null = null

const loading = ref(false)
const saving = ref(false)
const categories = ref<CategoryVO[]>([])
const tags = ref<TagVO[]>([])
const selectedTags = ref<number[]>([])
const hasDraft = ref(false)
const draftData = ref<Draft | null>(null)
const draftDialogVisible = ref(false)
const isPublic = ref(false)

const noteForm = reactive<NoteDTO>({
  title: '',
  content: '',
  categoryId: undefined,
  tagIds: [],
  isPublic: 0
})

const isEditorReady = ref(false)
const pendingContent = ref('')

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const handlePasteImage = async (e: ClipboardEvent) => {
  const items = e.clipboardData?.items
  if (!items) return

  for (const item of items) {
    if (item.type.indexOf('image') !== -1) {
      e.preventDefault()
      
      const file = item.getAsFile()
      if (!file) continue
      
      try {
        const res = await uploadImage(file)
        if (vditor && res.data.url) {
          const markdown = `![${file.name}](${res.data.url})`
          vditor.insertValue(markdown)
          ElMessage.success('图片粘贴成功')
        }
      } catch (error) {
        console.error('粘贴图片上传失败:', error)
        ElMessage.error('图片粘贴失败，请重试')
      }
      return
    }
  }
}

const initEditor = () => {
  if (!editorRef.value) return
  
  vditor = new Vditor('vditor', {
    height: '100%',
    placeholder: '开始编写您的笔记...',
    theme: 'light',
    preview: {
      mode: 'both',
      hljs: {
        enable: true,
        style: 'github',
        lineNumber: true
      },
      markdown: {
        toc: true,
        mark: true,
        footnotes: true
      }
    },
    toolbar: [
      'headings', 'bold', 'italic', 'strike', '|',
      'list', 'ordered-list', 'check', 'outdent', 'indent', '|',
      'quote', 'line', 'code', 'inline-code', '|',
      'link', 'image', 'table', '|',
      'undo', 'redo', '|',
      'fullscreen', 'preview', 'info'
    ],
    cache: {
      enable: false
    },
    upload: {
      accept: 'image/*',
      multiple: false,
      handler: async (files: File[]) => {
        if (!files || files.length === 0) return
        const file = files[0]
        try {
          const res = await uploadImage(file)
          return {
            errFiles: [],
            succMap: {
              [file.name]: res.data.url
            }
          }
        } catch (error) {
          ElMessage.error('图片上传失败')
          return {
            errFiles: [file.name],
            succMap: {}
          }
        }
      }
    },
    paste: {
      enable: true,
      style: true,
      handler: async (text: string, html?: string) => {
        return text
      }
    },
    tab: '\t',
    input: (val: string) => {
      noteForm.content = val
      debouncedSaveDraft()
    },
    after: () => {
      isEditorReady.value = true
      if (noteForm.content) {
        vditor?.setValue(noteForm.content)
      } else if (pendingContent.value) {
        vditor?.setValue(pendingContent.value)
        pendingContent.value = ''
      }
    }
  })

  document.addEventListener('paste', handlePasteImage, true)
}

const debouncedSaveDraft = debounce(async () => {
  if (!vditor) return
  
  const content = vditor.getValue()
  const draftDTO: DraftDTO = {
    noteId: noteId.value,
    title: noteForm.title,
    content: content,
    categoryId: noteForm.categoryId,
    tagIds: selectedTags.value
  }
  
  try {
    await saveDraft(draftDTO)
    hasDraft.value = true
    ElMessage.success('自动保存成功')
  } catch (error) {
    console.error('自动保存草稿失败:', error)
    ElMessage.error('自动保存失败')
  }
}, 3000)

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const fetchTags = async () => {
  try {
    const res = await getTags()
    tags.value = res.data
  } catch (error) {
    console.error('获取标签失败:', error)
  }
}

const fetchNoteDetail = async () => {
  if (!noteId.value) return
  loading.value = true
  try {
    const res = await getNoteById(noteId.value)
    const note = res.data
    noteForm.title = note.title
    noteForm.content = note.content || ''
    noteForm.categoryId = note.categoryId || undefined
    noteForm.isPublic = note.isPublic
    isPublic.value = note.isPublic === 1
    
    if (note.tags && note.tags.length > 0) {
      selectedTags.value = note.tags.map(t => t.id)
      noteForm.tagIds = note.tags.map(t => t.id)
    }
    
    if (isEditorReady.value && vditor && note.content) {
      vditor.setValue(note.content)
    } else if (note.content) {
      pendingContent.value = note.content
    }
  } catch (error) {
    console.error('获取笔记详情失败:', error)
    ElMessage.error('获取笔记详情失败')
  } finally {
    loading.value = false
  }
}

const checkDraft = async () => {
  try {
    const res = await getDraft(noteId.value)
    if (res.data) {
      draftData.value = res.data
      hasDraft.value = true
      draftDialogVisible.value = true
    }
  } catch (error) {
    console.error('检查草稿失败:', error)
  }
}

const handleSkipDraft = () => {
  draftDialogVisible.value = false
}

const handleRestoreDraft = () => {
  if (!draftData.value) return
  
  noteForm.title = draftData.value.title || ''
  noteForm.content = draftData.value.content || ''
  noteForm.categoryId = draftData.value.categoryId || undefined
  
  if (draftData.value.tagIds && draftData.value.tagIds.length > 0) {
    selectedTags.value = draftData.value.tagIds
    noteForm.tagIds = draftData.value.tagIds
  }
  
  if (vditor && draftData.value.content) {
    vditor.setValue(draftData.value.content)
  }
  
  draftDialogVisible.value = false
  ElMessage.success('草稿已恢复')
}

const handleTagChange = (val: number[]) => {
  noteForm.tagIds = val
}

const handleCreateTag = async (tagName: string) => {
  try {
    const res = await createTag(tagName)
    tags.value.push(res.data)
    selectedTags.value.push(res.data.id)
    noteForm.tagIds = [...selectedTags.value]
  } catch (error) {
    ElMessage.error('创建标签失败')
  }
}

const handleSave = async () => {
  if (!noteForm.title.trim()) {
    ElMessage.warning('请输入笔记标题')
    return
  }
  
  noteForm.tagIds = selectedTags.value
  noteForm.isPublic = isPublic.value ? 1 : 0
  
  if (vditor) {
    noteForm.content = vditor.getValue()
  }
  
  saving.value = true
  try {
    let res
    if (isEdit.value && noteId.value) {
      noteForm.id = noteId.value
      res = await updateNote(noteForm)
    } else {
      res = await createNote(noteForm)
    }
    
    ElMessage.success('保存成功')
    
    if (noteId.value) {
      await deleteDraft(noteId.value)
    }
    hasDraft.value = false
    
    router.push(`/notes/${res.data.id}`)
  } catch (error) {
    console.error('保存笔记失败:', error)
  } finally {
    saving.value = false
  }
}

const handleBack = () => {
  if (hasDraft.value) {
    ElMessageBox.confirm('您有未保存的草稿，确定要离开吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.back()
    }).catch(() => {})
  } else {
    router.back()
  }
}

watch(isPublic, (val) => {
  noteForm.isPublic = val ? 1 : 0
})

onMounted(async () => {
  await Promise.all([
    fetchCategories(),
    fetchTags()
  ])
  
  if (!isEdit.value) {
    const selectedCategoryId = localStorage.getItem('selectedCategoryId')
    if (selectedCategoryId) {
      const categoryId = Number(selectedCategoryId)
      if (categories.value.find(c => c.id === categoryId)) {
        noteForm.categoryId = categoryId
      }
      localStorage.removeItem('selectedCategoryId')
    }
  }
  
  nextTick(() => {
    initEditor()
  })
  
  if (isEdit.value) {
    await fetchNoteDetail()
  }
  
  await checkDraft()
})

onUnmounted(() => {
  document.removeEventListener('paste', handlePasteImage, true)
  if (vditor) {
    vditor.destroy()
    vditor = null
  }
  debouncedSaveDraft.cancel()
})
</script>

<style scoped>
.note-edit-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fff;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.edit-content {
  flex: 1;
  overflow: auto;
  display: flex;
  flex-direction: column;
}

.note-meta {
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fafafa;
  flex-shrink: 0;
}

.title-input {
  font-size: 16px;
}

.editor-wrapper {
  flex: 1;
  min-height: 0;
  position: relative;
}

.vditor-container {
  height: 100% !important;
}

:deep(.vditor-toolbar) {
  border-top: none;
}

:deep(.vditor-content) {
  height: calc(100% - 52px) !important;
}

.draft-dialog-content {
  padding: 10px 0;
}

.draft-dialog-content p {
  margin: 0 0 15px 0;
  color: #606266;
}
</style>
