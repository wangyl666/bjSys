<template>
  <div class="tag-manage-container">
    <div class="page-header">
      <h2 class="page-title">标签管理</h2>
      <el-button type="primary" @click="handleOpenCreateDialog">
        <el-icon><Plus /></el-icon>
        新建标签
      </el-button>
    </div>
    
    <div class="tag-content">
      <el-empty v-if="tags.length === 0 && !loading" description="暂无标签，点击右上角新建标签" />
      
      <div v-else class="tag-grid">
        <div
          v-for="tag in tags"
          :key="tag.id"
          class="tag-card"
        >
          <div class="tag-info">
            <el-tag :color="tag.color" size="large" effect="dark">
              {{ tag.name }}
            </el-tag>
            <span class="tag-count">创建于 {{ formatTime(tag.createdAt) }}</span>
          </div>
          <div class="tag-actions">
            <el-button type="danger" text @click="handleDeleteTag(tag)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <el-dialog v-model="createDialogVisible" title="新建标签" width="400px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="标签名称" required>
          <el-input
            v-model="createForm.name"
            placeholder="请输入标签名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="标签颜色">
          <div class="color-picker">
            <div
              v-for="color in colorOptions"
              :key="color"
              class="color-option"
              :class="{ active: createForm.color === color }"
              :style="{ backgroundColor: color }"
              @click="createForm.color = color"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTag" :loading="creating">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTags, createTag, deleteTag } from '@/api/tag'
import { Plus, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import type { TagVO } from '@/types'

const loading = ref(false)
const tags = ref<TagVO[]>([])
const createDialogVisible = ref(false)
const creating = ref(false)

const colorOptions = [
  '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
  '#00D4FF', '#00B42A', '#FF7D00', '#F53F3F', '#722ED1'
]

const createForm = reactive({
  name: '',
  color: colorOptions[0]
})

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const fetchTags = async () => {
  loading.value = true
  try {
    const res = await getTags()
    tags.value = res.data
  } catch (error) {
    console.error('获取标签列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleOpenCreateDialog = () => {
  createForm.name = ''
  createForm.color = colorOptions[0]
  createDialogVisible.value = true
}

const handleCreateTag = async () => {
  if (!createForm.name.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  
  creating.value = true
  try {
    await createTag(createForm.name.trim(), createForm.color)
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    fetchTags()
  } catch (error) {
    console.error('创建标签失败:', error)
    ElMessage.error('创建失败')
  } finally {
    creating.value = false
  }
}

const handleDeleteTag = async (tag: TagVO) => {
  await ElMessageBox.confirm(`确定要删除标签"${tag.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  
  try {
    await deleteTag(tag.id)
    ElMessage.success('删除成功')
    fetchTags()
  } catch (error) {
    console.error('删除标签失败:', error)
  }
}

onMounted(() => {
  fetchTags()
})
</script>

<style scoped>
.tag-manage-container {
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

.tag-content {
  flex: 1;
  overflow: auto;
}

.tag-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.tag-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fff;
  transition: all 0.2s;
}

.tag-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.tag-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tag-count {
  font-size: 12px;
  color: #909399;
}

.tag-actions {
  display: flex;
  gap: 8px;
}

.color-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.color-option {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.color-option:hover {
  transform: scale(1.1);
}

.color-option.active {
  border-color: #303133;
  transform: scale(1.1);
}
</style>
