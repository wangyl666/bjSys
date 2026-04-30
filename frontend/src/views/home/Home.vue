<template>
  <div class="home-container">
    <div class="page-header">
      <h2 class="page-title">首页</h2>
      <span class="welcome-text">欢迎回来，{{ userInfo?.nickname || userInfo?.username }}！</span>
    </div>
    
    <div class="home-content">
      <el-card class="stats-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>数据统计</span>
          </div>
        </template>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-icon note-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ userStats.noteCount || 0 }}</div>
                <div class="stat-label">笔记数量</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-icon error-icon">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ userStats.errorQuestionCount || 0 }}</div>
                <div class="stat-label">错题数量</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-icon category-icon">
                <el-icon><Folder /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ userStats.categoryCount || 0 }}</div>
                <div class="stat-label">分类数量</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-icon tag-icon">
                <el-icon><PriceTag /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ userStats.tagCount || 0 }}</div>
                <div class="stat-label">标签数量</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="quick-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>快捷操作</span>
              </div>
            </template>
            <div class="quick-actions">
              <el-button type="primary" size="large" @click="handleCreateNote">
                <el-icon><Plus /></el-icon>
                新建笔记
              </el-button>
              <el-button size="large" @click="handleViewNotes">
                <el-icon><Document /></el-icon>
                查看笔记
              </el-button>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="info-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>账号信息</span>
              </div>
            </template>
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">用户名</span>
                <span class="info-value">{{ userInfo?.username }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">昵称</span>
                <span class="info-value">{{ userInfo?.nickname || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">注册时间</span>
                <span class="info-value">{{ formatTime(userInfo?.createdAt) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getUserStats } from '@/api/user'
import { Document, Warning, Folder, PriceTag, Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import type { UserStatsVO } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

const userStats = ref<UserStatsVO>({
  noteCount: 0,
  errorQuestionCount: 0,
  categoryCount: 0,
  tagCount: 0
})

const formatTime = (time: string | undefined) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const fetchUserStats = async () => {
  try {
    const res = await getUserStats()
    userStats.value = res.data
  } catch (error) {
    console.error('获取用户统计数据失败:', error)
  }
}

const handleCreateNote = () => {
  router.push('/notes/create')
}

const handleViewNotes = () => {
  router.push('/notes')
}

onMounted(() => {
  fetchUserStats()
})
</script>

<style scoped>
.home-container {
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

.page-title {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.welcome-text {
  font-size: 14px;
  color: #909399;
}

.home-content {
  flex: 1;
  overflow: auto;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.stats-card {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.note-icon {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.error-icon {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.category-icon {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.tag-icon {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 2px;
}

.quick-card, .info-card {
  margin-bottom: 20px;
}

.quick-actions {
  display: flex;
  gap: 15px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 14px;
  color: #909399;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
</style>
