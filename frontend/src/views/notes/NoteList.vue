<template>
  <div class="note-list-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">我的笔记</h2>
        <el-select
          v-model="selectedCategory"
          placeholder="选择分类"
          clearable
          @change="handleCategoryChange"
          class="category-select"
        >
          <el-option label="全部笔记" :value="null" />
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
      </div>
      <div class="header-right">
        <el-input
          v-model="keyword"
          placeholder="搜索笔记..."
          prefix-icon="Search"
          clearable
          style="width: 250px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="handleCreateNote">
          <el-icon><Plus /></el-icon>
          新建笔记
        </el-button>
      </div>
    </div>
    
    <div class="note-content">
      <el-table
        v-loading="loading"
        :data="noteList"
        style="width: 100%"
        :empty-text="keyword ? '未找到相关笔记' : '暂无笔记，点击右上角新建笔记'"
      >
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="scope">
            <router-link :to="`/notes/${scope.row.id}`" class="note-title-link">
              {{ scope.row.title }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.categoryName" size="small" type="primary">
              {{ scope.row.categoryName }}
            </el-tag>
            <span v-else class="text-muted">未分类</span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80">
          <template #default="scope">
            {{ scope.row.viewCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button type="primary" text @click="handleCopyNote(scope.row)">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
            <el-button type="primary" text @click="handleEditNote(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="primary" text @click="handleViewNote(scope.row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button type="danger" text @click="handleDeleteNote(scope.row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pageParams.page"
          v-model:page-size="pageParams.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchNoteList"
          @current-change="fetchNoteList"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNoteList, deleteNote, copyNote } from '@/api/note'
import { getCategories } from '@/api/category'
import { Plus, Edit, View, Delete, Search, CopyDocument } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import type { NoteVO, CategoryVO, PageParams } from '@/types'

const router = useRouter()

const loading = ref(false)
const noteList = ref<NoteVO[]>([])
const categories = ref<CategoryVO[]>([])
const total = ref(0)
const keyword = ref('')
const selectedCategory = ref<number | null>(null)

const pageParams = reactive<PageParams>({
  page: 1,
  size: 10
})

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const fetchNoteList = async () => {
  loading.value = true
  try {
    const params = {
      ...pageParams,
      categoryId: selectedCategory.value,
      keyword: keyword.value
    }
    const res = await getNoteList(params)
    noteList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取笔记列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

const handleCreateNote = () => {
  router.push('/notes/create')
}

const handleEditNote = (note: NoteVO) => {
  router.push(`/notes/${note.id}/edit`)
}

const handleCopyNote = async (note: NoteVO) => {
  try {
    const res = await copyNote(note.id)
    ElMessage.success('复制成功')
    router.push(`/notes/${res.data.id}/edit`)
  } catch (error) {
    console.error('复制笔记失败:', error)
  }
}

const handleViewNote = (note: NoteVO) => {
  router.push(`/notes/${note.id}`)
}

const handleDeleteNote = async (note: NoteVO) => {
  await ElMessageBox.confirm(`确定要删除笔记"${note.title}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  
  try {
    await deleteNote(note.id)
    ElMessage.success('删除成功')
    fetchNoteList()
  } catch (error) {
    console.error('删除笔记失败:', error)
  }
}

const handleCategoryChange = () => {
  pageParams.page = 1
  fetchNoteList()
}

const handleSearch = () => {
  pageParams.page = 1
  fetchNoteList()
}

onMounted(() => {
  fetchNoteList()
  fetchCategories()
})
</script>

<style scoped>
.note-list-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.category-select {
  width: 150px;
}

.header-right {
  display: flex;
  gap: 15px;
  align-items: center;
}

.note-content {
  flex: 1;
  overflow: auto;
}

.note-title-link {
  color: #409eff;
  text-decoration: none;
}

.note-title-link:hover {
  text-decoration: underline;
}

.text-muted {
  color: #909399;
  font-size: 14px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
