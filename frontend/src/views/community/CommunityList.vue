<template>
  <div class="community-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">笔记社区</h2>
        <el-select
          v-model="selectedCategory"
          placeholder="选择分类"
          clearable
          @change="handleCategoryChange"
          class="category-select"
        >
          <el-option label="全部分类" :value="null" />
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
      </div>
    </div>
    
    <div class="note-content" v-loading="loading">
      <el-empty v-if="noteList.length === 0 && !loading" :description="keyword ? '未找到相关笔记' : '暂无公开笔记'" />
      
      <div class="note-cards" v-else>
        <div
          v-for="note in noteList"
          :key="note.id"
          class="note-card"
          @click="handleViewNote(note)"
        >
          <div class="card-header">
            <div class="user-info">
              <el-avatar :size="36" v-if="note.userAvatar" :src="note.userAvatar" />
              <el-avatar :size="36" v-else>
                {{ note.username?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ note.username }}</span>
            </div>
            <div class="note-time">{{ formatTime(note.createdAt) }}</div>
          </div>
          
          <div class="card-title">{{ note.title }}</div>
          
          <div class="card-summary" v-if="note.summary">
            {{ note.summary }}
          </div>
          
          <div class="card-tags" v-if="note.tags && note.tags.length > 0">
            <el-tag
              v-for="tag in note.tags"
              :key="tag.id"
              size="small"
              effect="plain"
              :style="{ borderColor: tag.color, color: tag.color }"
            >
              {{ tag.name }}
            </el-tag>
          </div>
          
          <div class="card-footer">
            <span class="footer-item">
              <el-icon><View /></el-icon>
              {{ note.viewCount || 0 }}
            </span>
            <span class="footer-item">
              <el-icon><Star /></el-icon>
              {{ note.favoriteCount || 0 }}
            </span>
            <span class="footer-item">
              <el-icon><ChatDotRound /></el-icon>
              {{ note.commentCount || 0 }}
            </span>
            <span class="footer-item favorite" :class="{ active: note.isFavorited }">
              <el-icon><StarFilled /></el-icon>
              {{ note.isFavorited ? '已收藏' : '收藏' }}
            </span>
          </div>
        </div>
      </div>
      
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCommunityNotes } from '@/api/community'
import { getCategories } from '@/api/category'
import { Search, View, Star, StarFilled, ChatDotRound } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import type { CommunityNoteVO, CategoryVO, PageParams } from '@/types'

const router = useRouter()

const loading = ref(false)
const noteList = ref<CommunityNoteVO[]>([])
const categories = ref<CategoryVO[]>([])
const total = ref(0)
const keyword = ref('')
const selectedCategory = ref<number | null>(null)

const pageParams = reactive<PageParams>({
  page: 1,
  size: 10
})

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const fetchNoteList = async () => {
  loading.value = true
  try {
    const params = {
      ...pageParams,
      categoryId: selectedCategory.value,
      keyword: keyword.value
    }
    const res = await getCommunityNotes(params)
    noteList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取社区笔记列表失败:', error)
    ElMessage.error('获取笔记列表失败')
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

const handleViewNote = (note: CommunityNoteVO) => {
  router.push(`/community/${note.id}`)
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
.community-container {
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

.note-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.note-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.note-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.note-time {
  font-size: 12px;
  color: #909399;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-summary {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.footer-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #909399;
}

.footer-item.favorite {
  cursor: pointer;
  transition: color 0.2s;
}

.footer-item.favorite:hover,
.footer-item.favorite.active {
  color: #f59e0b;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
