<template>
  <div class="tiptap-editor-container">
    <div class="editor-toolbar" v-if="showToolbar">
      <div class="toolbar-group">
        <button 
          class="toolbar-btn" 
          :class="{ active: editor?.isActive('bold') }"
          @click="editor?.chain().focus().toggleBold().run()"
          :title="'粗体 (Ctrl+B)'"
        >
          <strong>B</strong>
        </button>
        <button 
          class="toolbar-btn" 
          :class="{ active: editor?.isActive('italic') }"
          @click="editor?.chain().focus().toggleItalic().run()"
          :title="'斜体 (Ctrl+I)'"
        >
          <em>I</em>
        </button>
        <button 
          class="toolbar-btn" 
          :class="{ active: editor?.isActive('strike') }"
          @click="editor?.chain().focus().toggleStrike().run()"
          :title="'删除线'"
        >
          <s>S</s>
        </button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button 
          class="toolbar-btn" 
          :class="{ active: editor?.isActive('heading', { level: 1 }) }"
          @click="editor?.chain().focus().toggleHeading({ level: 1 }).run()"
          :title="'标题 1'"
        >
          H1
        </button>
        <button 
          class="toolbar-btn" 
          :class="{ active: editor?.isActive('heading', { level: 2 }) }"
          @click="editor?.chain().focus().toggleHeading({ level: 2 }).run()"
          :title="'标题 2'"
        >
          H2
        </button>
        <button 
          class="toolbar-btn" 
          :class="{ active: editor?.isActive('heading', { level: 3 }) }"
          @click="editor?.chain().focus().toggleHeading({ level: 3 }).run()"
          :title="'标题 3'"
        >
          H3
        </button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button 
          class="toolbar-btn" 
          :class="{ active: editor?.isActive('bulletList') }"
          @click="editor?.chain().focus().toggleBulletList().run()"
          :title="'无序列表'"
        >
          <svg viewBox="0 0 24 24" width="16" height="16">
            <path fill="currentColor" d="M4 10.5c-.83 0-1.5.67-1.5 1.5s.67 1.5 1.5 1.5 1.5-.67 1.5-1.5-.67-1.5-1.5-1.5m0-6c-.83 0-1.5.67-1.5 1.5S3.17 7.5 4 7.5 5.5 6.83 5.5 6 4.83 4.5 4 4.5m0 12c-.83 0-1.5.67-1.5 1.5s.67 1.5 1.5 1.5 1.5-.67 1.5-1.5-.67-1.5-1.5-1.5M7 19h14v-2H7v2m0-6h14v-2H7v2m0-8v2h14V5H7z"/>
          </svg>
        </button>
        <button 
          class="toolbar-btn" 
          :class="{ active: editor?.isActive('orderedList') }"
          @click="editor?.chain().focus().toggleOrderedList().run()"
          :title="'有序列表'"
        >
          <svg viewBox="0 0 24 24" width="16" height="16">
            <path fill="currentColor" d="M7 13h14v-2H7v2m-2 8h2v-1H6v1m0-3h1.8l2-2.5H6.5v-.5H9V15H6v3h-1v-3H3.5v.5h1.3l-2 2.5H3V20h3v1H2v1h5v-5M7 5v2h14V5H7m0 6h14V9H7v2z"/>
          </svg>
        </button>
        <button 
          class="toolbar-btn" 
          :class="{ active: editor?.isActive('blockquote') }"
          @click="editor?.chain().focus().toggleBlockquote().run()"
          :title="'引用'"
        >
          <svg viewBox="0 0 24 24" width="16" height="16">
            <path fill="currentColor" d="M6 17h3l2-4V7H5v6h3zm8 0h3l2-4V7h-6v6h3z"/>
          </svg>
        </button>
        <button 
          class="toolbar-btn" 
          :class="{ active: editor?.isActive('codeBlock') }"
          @click="editor?.chain().focus().toggleCodeBlock().run()"
          :title="'代码块'"
        >
          <svg viewBox="0 0 24 24" width="16" height="16">
            <path fill="currentColor" d="M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z"/>
          </svg>
        </button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button 
          class="toolbar-btn" 
          @click="insertTable"
          :title="'插入表格'"
        >
          <svg viewBox="0 0 24 24" width="16" height="16">
            <path fill="currentColor" d="M20 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h15c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 2v4H5V5h15zM5 11h4v4H5v-4zm6 0h4v4h-4v-4zm6 0h4v4h-4v-4zM5 17h4v2H5v-2zm6 2v-2h4v2h-4zm6 0v-2h4v2h-4z"/>
          </svg>
        </button>
        <template v-if="editor?.isActive('table')">
          <button 
            class="toolbar-btn" 
            @click="editor?.chain().focus().addColumnBefore().run()"
            :title="'在左侧插入列'"
          >
            <svg viewBox="0 0 24 24" width="16" height="16">
              <path fill="currentColor" d="M15.41 16.59L10.83 12l4.58-4.59L14 6l-6 6 6 6 1.41-1.41z"/>
            </svg>
          </button>
          <button 
            class="toolbar-btn" 
            @click="editor?.chain().focus().addColumnAfter().run()"
            :title="'在右侧插入列'"
          >
            <svg viewBox="0 0 24 24" width="16" height="16">
              <path fill="currentColor" d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"/>
            </svg>
          </button>
          <button 
            class="toolbar-btn" 
            @click="editor?.chain().focus().deleteColumn().run()"
            :title="'删除当前列'"
          >
            <svg viewBox="0 0 24 24" width="16" height="16">
              <path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
          <button 
            class="toolbar-btn" 
            @click="editor?.chain().focus().addRowBefore().run()"
            :title="'在上方插入行'"
          >
            <svg viewBox="0 0 24 24" width="16" height="16">
              <path fill="currentColor" d="M7.41 15.41L12 10.83l4.59 4.58L18 14l-6-6-6 6 1.41 1.41z"/>
            </svg>
          </button>
          <button 
            class="toolbar-btn" 
            @click="editor?.chain().focus().addRowAfter().run()"
            :title="'在下方插入行'"
          >
            <svg viewBox="0 0 24 24" width="16" height="16">
              <path fill="currentColor" d="M16.59 8.59L12 13.17 7.41 8.59 6 10l6 6 6-6z"/>
            </svg>
          </button>
          <button 
            class="toolbar-btn" 
            @click="editor?.chain().focus().deleteRow().run()"
            :title="'删除当前行'"
          >
            <svg viewBox="0 0 24 24" width="16" height="16">
              <path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
          <button 
            class="toolbar-btn" 
            @click="editor?.chain().focus().mergeCells().run()"
            :title="'合并单元格'"
          >
            <svg viewBox="0 0 24 24" width="16" height="16">
              <path fill="currentColor" d="M17 20.41L18.41 19 15 15.59 13.59 17 17 20.41zM3.91 9.16l4.07-4.08L9.5 6.6l-4.08 4.07-1.51-1.51M20.5 3.5l-4.08 4.07 1.51 1.51 4.07-4.08-1.5-1.5M12 14c-2.21 0-4-1.79-4-4s1.79-4 4-4 4 1.79 4 4-1.79 4-4 4z"/>
            </svg>
          </button>
          <button 
            class="toolbar-btn" 
            @click="editor?.chain().focus().splitCell().run()"
            :title="'拆分单元格'"
          >
            <svg viewBox="0 0 24 24" width="16" height="16">
              <path fill="currentColor" d="M13.41 12l4.3-4.29-1.42-1.42L12 9.17l-4.29-4.3-1.42 1.42L10.59 12l-4.3 4.29 1.42 1.42L12 14.83l4.29 4.3 1.42-1.42-4.3-4.29z"/>
            </svg>
          </button>
          <button 
            class="toolbar-btn toolbar-btn-danger"
            @click="editor?.chain().focus().deleteTable().run()"
            :title="'删除表格'"
          >
            <svg viewBox="0 0 24 24" width="16" height="16">
              <path fill="currentColor" d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
            </svg>
          </button>
        </template>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button 
          class="toolbar-btn" 
          @click="insertLink"
          :title="'插入链接'"
        >
          <svg viewBox="0 0 24 24" width="16" height="16">
            <path fill="currentColor" d="M3.9 12c0-1.71 1.39-3.1 3.1-3.1h4V7H7c-2.76 0-5 2.24-5 5s2.24 5 5 5h4v-1.9H7c-1.71 0-3.1-1.39-3.1-3.1zM8 13h8v-2H8v2zm9-6h-4v1.9h4c1.71 0 3.1 1.39 3.1 3.1s-1.39 3.1-3.1 3.1h-4V17h4c2.76 0 5-2.24 5-5s-2.24-5-5-5z"/>
          </svg>
        </button>
        <button 
          class="toolbar-btn" 
          @click="handleImageUpload"
          :title="'插入图片'"
        >
          <svg viewBox="0 0 24 24" width="16" height="16">
            <path fill="currentColor" d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
          </svg>
        </button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button 
          class="toolbar-btn" 
          @click="editor?.chain().focus().undo().run()"
          :disabled="!editor?.can().undo()"
          :title="'撤销 (Ctrl+Z)'"
        >
          <svg viewBox="0 0 24 24" width="16" height="16">
            <path fill="currentColor" d="M12.5 8c-2.65 0-5.05.99-6.9 2.6L2 7v9h9l-3.62-3.62c1.39-1.16 3.16-1.88 5.12-1.88 3.54 0 6.55 2.31 7.6 5.5l2.37-.78C21.08 11.03 17.15 8 12.5 8z"/>
          </svg>
        </button>
        <button 
          class="toolbar-btn" 
          @click="editor?.chain().focus().redo().run()"
          :disabled="!editor?.can().redo()"
          :title="'重做 (Ctrl+Y)'"
        >
          <svg viewBox="0 0 24 24" width="16" height="16">
            <path fill="currentColor" d="M18.4 10.6C16.55 8.99 14.15 8 11.5 8c-4.65 0-8.58 3.03-9.96 7.22L3.9 16c1.05-3.19 4.05-5.5 7.6-5.5 1.95 0 3.73.72 5.12 1.88L13 16h9V7l-3.6 3.6z"/>
          </svg>
        </button>
      </div>
    </div>
    
    <editor-content :editor="editor" class="editor-content" />
    
    <input 
      ref="fileInputRef" 
      type="file" 
      accept="image/*" 
      style="display: none" 
      @change="onFileChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Table from '@tiptap/extension-table'
import TableRow from '@tiptap/extension-table-row'
import TableCell from '@tiptap/extension-table-cell'
import TableHeader from '@tiptap/extension-table-header'
import Image from '@tiptap/extension-image'
import { Markdown } from 'tiptap-markdown'
import { ElMessage, ElMessageBox } from 'element-plus'
import { uploadImage } from '@/api/file'

interface Props {
  modelValue?: string
  placeholder?: string
  showToolbar?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '开始编写您的笔记...',
  showToolbar: true
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'change', value: string): void
}>()

const fileInputRef = ref<HTMLInputElement>()

let isUpdating = false

const editor = useEditor({
  content: props.modelValue || '',
  extensions: [
    StarterKit.configure({
      heading: {
        levels: [1, 2, 3]
      }
    }),
    Table.configure({
      resizable: true,
      HTMLAttributes: {
        class: 'editor-table'
      }
    }),
    TableRow,
    TableHeader,
    TableCell.configure({
      HTMLAttributes: {
        class: 'editor-table-cell'
      }
    }),
    Image.configure({
      inline: true,
      allowBase64: true,
      HTMLAttributes: {
        class: 'editor-image'
      }
    }),
    Markdown.configure({
      html: true,
      tightLists: true,
      tightListClass: 'tight-list',
      bulletListMarker: '-',
      linkify: true,
      breaks: true
    })
  ],
  onUpdate: ({ editor }) => {
    if (isUpdating) return
    const markdown = editor.storage.markdown.getMarkdown()
    emit('update:modelValue', markdown)
    emit('change', markdown)
  },
  editorProps: {
    attributes: {
      class: 'prose prose-sm sm:prose lg:prose-lg xl:prose-2xl m-5 focus:outline-none'
    }
  }
})

watch(
  () => props.modelValue,
  (newVal) => {
    if (isUpdating) return
    const currentMarkdown = editor.value?.storage.markdown.getMarkdown()
    if (newVal !== currentMarkdown && editor.value) {
      isUpdating = true
      editor.value.commands.setContent(newVal || '', { emitUpdate: false })
      setTimeout(() => {
        isUpdating = false
      }, 0)
    }
  }
)

const insertTable = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入表格行数和列数（格式：行数,列数）', '插入表格', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^\d+,\d+$/,
      inputErrorMessage: '格式不正确，请输入类似 "3,3" 的格式',
      inputValue: '3,3'
    })
    
    const [rows, cols] = (value || '3,3').split(',').map(Number)
    
    editor.value?.chain().focus().insertTable({ rows, cols, withHeaderRow: true }).run()
    ElMessage.success('表格已插入')
  } catch {
    // 用户取消
  }
}

const insertLink = async () => {
  try {
    const { value: url } = await ElMessageBox.prompt('请输入链接地址', '插入链接', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/,
      inputErrorMessage: '请输入有效的URL地址'
    })
    
    if (url) {
      const linkUrl = url.startsWith('http') ? url : `https://${url}`
      editor.value?.chain().focus().extendMarkRange('link').setLink({ href: linkUrl }).run()
      ElMessage.success('链接已插入')
    }
  } catch {
    // 用户取消
  }
}

const handleImageUpload = () => {
  fileInputRef.value?.click()
}

const onFileChange = async (e: Event) => {
  const target = e.target as HTMLInputElement
  const files = target.files
  if (!files || files.length === 0) return
  
  const file = files[0]
  
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  try {
    const res = await uploadImage(file)
    if (res.data.url) {
      editor.value?.chain().focus().setImage({ src: res.data.url, alt: file.name }).run()
      ElMessage.success('图片上传成功')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  } finally {
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }
}

const getValue = () => {
  return editor.value?.storage.markdown.getMarkdown() || ''
}

const setValue = (value: string) => {
  isUpdating = true
  editor.value?.commands.setContent(value || '', { emitUpdate: false })
  setTimeout(() => {
    isUpdating = false
  }, 0)
}

defineExpose({
  editor,
  getValue,
  setValue
})
</script>

<style scoped>
.tiptap-editor-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #fff;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fafafa;
  flex-wrap: wrap;
  gap: 4px;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 2px;
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background-color: #e4e7ed;
  margin: 0 8px;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 4px;
  background-color: transparent;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.toolbar-btn:hover {
  background-color: #e4e7ed;
  color: #409eff;
}

.toolbar-btn.active {
  background-color: #409eff;
  color: #fff;
}

.toolbar-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.toolbar-btn-danger:hover {
  background-color: #fef0f0;
  color: #f56c6c;
}

.editor-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

:deep(.ProseMirror) {
  outline: none;
  min-height: 200px;
}

:deep(.ProseMirror p.is-editor-empty:first-child::before) {
  color: #adb5bd;
  content: attr(data-placeholder);
  float: left;
  height: 0;
  pointer-events: none;
}

:deep(.ProseMirror table) {
  border-collapse: collapse;
  width: 100%;
  margin: 16px 0;
  overflow-x: auto;
}

:deep(.ProseMirror table td),
:deep(.ProseMirror table th) {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
  min-width: 100px;
  position: relative;
  vertical-align: top;
  text-align: left;
}

:deep(.ProseMirror table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

:deep(.ProseMirror table .selectedCell) {
  background-color: #ecf5ff;
}

:deep(.ProseMirror img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 16px 0;
}

:deep(.ProseMirror blockquote) {
  border-left: 3px solid #409eff;
  padding-left: 16px;
  margin: 16px 0;
  color: #606266;
  background-color: #f5f7fa;
  padding: 12px 16px;
  border-radius: 4px;
}

:deep(.ProseMirror pre) {
  background-color: #f6f8fa;
  border-radius: 6px;
  padding: 16px;
  overflow-x: auto;
  margin: 16px 0;
}

:deep(.ProseMirror code) {
  background-color: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 14px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

:deep(.ProseMirror pre code) {
  background-color: transparent;
  padding: 0;
  color: #333;
}

:deep(.ProseMirror h1) {
  font-size: 28px;
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 2px solid #e4e7ed;
  padding-bottom: 8px;
}

:deep(.ProseMirror h2) {
  font-size: 24px;
  margin-top: 20px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #303133;
}

:deep(.ProseMirror h3) {
  font-size: 20px;
  margin-top: 16px;
  margin-bottom: 8px;
  font-weight: 600;
  color: #303133;
}

:deep(.ProseMirror p) {
  margin-bottom: 12px;
  line-height: 1.8;
  color: #303133;
}

:deep(.ProseMirror ul),
:deep(.ProseMirror ol) {
  padding-left: 24px;
  margin: 12px 0;
}

:deep(.ProseMirror li) {
  margin-bottom: 4px;
  line-height: 1.8;
}

:deep(.ProseMirror a) {
  color: #409eff;
  text-decoration: none;
}

:deep(.ProseMirror a:hover) {
  text-decoration: underline;
}

:deep(.ProseMirror hr) {
  border: none;
  border-top: 1px solid #e4e7ed;
  margin: 24px 0;
}
</style>
