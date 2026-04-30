<template>
  <div class="knowledge-container">
    <div class="page-header">
      <h2 class="page-title">知识库</h2>
    </div>
    
    <div class="knowledge-content">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="category-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>分类文件夹</span>
                <el-button type="primary" text @click="handleCreateCategory">
                  <el-icon><Plus /></el-icon>
                  新建分类
                </el-button>
              </div>
            </template>
            <div class="category-list">
              <div
                class="category-item"
                :class="{ active: selectedCategory === null }"
                @click="handleSelectCategory(null)"
              >
                <el-icon class="category-icon"><FolderOpened /></el-icon>
                <span class="category-name">全部笔记</span>
                <span class="note-count">{{ allNoteCount }}</span>
              </div>
              <div
                v-for="category in categories"
                :key="category.id"
                class="category-item"
                :class="{ active: selectedCategory === category.id }"
                @click="handleSelectCategory(category.id)"
              >
                <el-icon class="category-icon"><Folder /></el-icon>
                <span class="category-name">{{ category.name }}</span>
                <span class="note-count">{{ getNoteCountByCategory(category.id) }}</span>
                <el-dropdown trigger="click" placement="bottom" @command="handleCategoryCommand">
                  <span class="category-actions" @click.stop>
                    <el-icon><MoreFilled /></el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="{ action: 'rename', id: category.id, name: category.name }">
                        <el-icon><Edit /></el-icon>
                        <span>重命名</span>
                      </el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'delete', id: category.id, name: category.name }" divided>
                        <el-icon><Delete /></el-icon>
                        <span>删除分类</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="18">
          <el-card class="note-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>{{ currentCategoryName }}</span>
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
            </template>
            
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
                <el-table-column label="操作" width="180" fixed="right">
                  <template #default="scope">
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
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <el-dialog
      v-model="categoryDialogVisible"
      :title="isEditCategory ? '重命名分类' : '新建分类'"
      width="400px"
    >
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCategory" :loading="savingCategory">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNoteList, deleteNote, createNote } from '@/api/note'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/category'
import { 
  Plus, Edit, View, Delete, Search, 
  Folder, FolderOpened, MoreFilled 
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import type { NoteVO, CategoryVO, PageParams, NoteDTO } from '@/types'

const router = useRouter()

const loading = ref(false)
const noteList = ref<NoteVO[]>([])
const categories = ref<CategoryVO[]>([])
const total = ref(0)
const keyword = ref('')
const selectedCategory = ref<number | null>(null)
const allNoteCount = ref(0)

const pageParams = reactive<PageParams>({
  page: 1,
  size: 10
})

const categoryDialogVisible = ref(false)
const isEditCategory = ref(false)
const editingCategoryId = ref<number | null>(null)
const savingCategory = ref(false)
const categoryForm = reactive({
  name: ''
})

const currentCategoryName = computed(() => {
  if (selectedCategory.value === null) {
    return '全部笔记'
  }
  const category = categories.value.find(c => c.id === selectedCategory.value)
  return category?.name || '笔记列表'
})

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const getNoteCountByCategory = (categoryId: number) => {
  return noteList.value.filter(n => n.categoryId === categoryId).length
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
    if (selectedCategory.value === null) {
      allNoteCount.value = res.data.total
    }
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

const handleSelectCategory = (categoryId: number | null) => {
  selectedCategory.value = categoryId
  pageParams.page = 1
  fetchNoteList()
}

const handleCreateCategory = () => {
  isEditCategory.value = false
  editingCategoryId.value = null
  categoryForm.name = ''
  categoryDialogVisible.value = true
}

const handleCategoryCommand = (command: { action: string; id: number; name: string }) => {
  if (command.action === 'rename') {
    isEditCategory.value = true
    editingCategoryId.value = command.id
    categoryForm.name = command.name
    categoryDialogVisible.value = true
  } else if (command.action === 'delete') {
    ElMessageBox.confirm(`确定要删除分类"${command.name}"吗？分类下的笔记不会被删除。`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await deleteCategory(command.id)
        ElMessage.success('删除成功')
        if (selectedCategory.value === command.id) {
          selectedCategory.value = null
        }
        fetchCategories()
        fetchNoteList()
      } catch (error) {
        console.error('删除分类失败:', error)
        ElMessage.error('删除失败')
      }
    }).catch(() => {})
  }
}

const handleSaveCategory = async () => {
  if (!categoryForm.name.trim()) {
    ElMessage.warning('分类名称不能为空')
    return
  }

  savingCategory.value = true
  try {
    if (isEditCategory.value && editingCategoryId.value) {
      await updateCategory(editingCategoryId.value, categoryForm.name)
    } else {
      await createCategory(categoryForm.name)
    }
    ElMessage.success(isEditCategory.value ? '修改成功' : '创建成功')
    categoryDialogVisible.value = false
    fetchCategories()
  } catch (error) {
    console.error('保存分类失败:', error)
    ElMessage.error('保存失败')
  } finally {
    savingCategory.value = false
  }
}

const handleCreateNote = () => {
  if (selectedCategory.value !== null) {
    localStorage.setItem('selectedCategoryId', selectedCategory.value.toString())
  } else {
    localStorage.removeItem('selectedCategoryId')
  }
  router.push('/notes/create')
}

const handleEditNote = (note: NoteVO) => {
  router.push(`/notes/${note.id}/edit`)
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
.knowledge-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  flex-shrink: 0;
}

.page-title {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.knowledge-content {
  flex: 1;
  overflow: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.header-right {
  display: flex;
  gap: 15px;
  align-items: center;
}

.category-card, .note-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.category-card >>> .el-card__body {
  flex: 1;
  overflow: auto;
  padding: 10px;
}

.note-card >>> .el-card__body {
  flex: 1;
  overflow: auto;
  padding: 0;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.category-item:hover {
  background-color: #f5f7fa;
}

.category-item.active {
  background-color: #ecf5ff;
  color: #409eff;
}

.category-icon {
  font-size: 18px;
  margin-right: 8px;
}

.category-name {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.note-count {
  font-size: 12px;
  color: #909399;
  margin-right: 8px;
}

.category-actions {
  padding: 4px;
  cursor: pointer;
  border-radius: 4px;
}

.category-actions:hover {
  background-color: #e4e7ed;
}

.note-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
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
