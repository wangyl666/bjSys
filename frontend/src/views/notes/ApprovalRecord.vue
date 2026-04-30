<template>
  <div class="approval-record-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">发布记录</h2>
      </div>
      <div class="header-right">
        <el-tag v-if="pendingCount > 0" type="warning" effect="dark">
          {{ pendingCount }} 条待审批
        </el-tag>
      </div>
    </div>
    
    <div class="approval-content">
      <el-table
        v-loading="loading"
        :data="approvalList"
        style="width: 100%"
        :empty-text="'暂无发布记录'"
      >
        <el-table-column prop="noteTitle" label="笔记标题" min-width="200">
          <template #default="scope">
            <router-link 
              v-if="scope.row.noteId" 
              :to="`/notes/${scope.row.noteId}/edit`" 
              class="note-title-link"
            >
              {{ scope.row.noteTitle }}
            </router-link>
            <span v-else>{{ scope.row.noteTitle }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="approvalStatus" label="审批状态" width="120">
          <template #default="scope">
            <el-tag :type="getApprovalTagType(scope.row.approvalStatus)" effect="dark">
              {{ getApprovalStatusText(scope.row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="提交时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.submittedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="approvedAt" label="审批时间" width="180">
          <template #default="scope">
            {{ scope.row.approvedAt ? formatTime(scope.row.approvedAt) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="adminName" label="审批人" width="120">
          <template #default="scope">
            {{ scope.row.adminName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="adminRemark" label="审批意见" min-width="150">
          <template #default="scope">
            <span v-if="scope.row.adminRemark">{{ scope.row.adminRemark }}</span>
            <span v-else class="text-muted">无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.approvalStatus === 'REJECTED' && scope.row.noteId" 
              type="primary" 
              text 
              @click="handleReSubmit(scope.row)"
            >
              重新提交
            </el-button>
            <el-button 
              v-else-if="scope.row.noteId" 
              type="primary" 
              text 
              @click="handleViewNote(scope.row)"
            >
              查看笔记
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
          @size-change="fetchApprovalList"
          @current-change="fetchApprovalList"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyApprovals, submitApproval } from '@/api/approval'
import dayjs from 'dayjs'
import type { NoteApprovalVO } from '@/types'

const router = useRouter()

const loading = ref(false)
const approvalList = ref<NoteApprovalVO[]>([])
const total = ref(0)

const pageParams = reactive({
  page: 1,
  size: 10
})

const pendingCount = computed(() => {
  return approvalList.value.filter(a => a.approvalStatus === 'PENDING').length
})

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const getApprovalStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    DRAFT: '草稿',
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return statusMap[status] || status
}

const getApprovalTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    DRAFT: 'info',
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return typeMap[status] || 'info'
}

const fetchApprovalList = async () => {
  loading.value = true
  try {
    const res = await getMyApprovals(pageParams.page, pageParams.size)
    approvalList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取审批记录失败:', error)
  } finally {
    loading.value = false
  }
}

const handleViewNote = (approval: NoteApprovalVO) => {
  if (approval.noteId) {
    router.push(`/notes/${approval.noteId}/edit`)
  }
}

const handleReSubmit = async (approval: NoteApprovalVO) => {
  if (!approval.noteId) return
  
  await ElMessageBox.confirm(
    '修改后重新提交审批？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  
  router.push(`/notes/${approval.noteId}/edit`)
}

onMounted(() => {
  fetchApprovalList()
})
</script>

<style scoped>
.approval-record-container {
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

.header-right {
  display: flex;
  gap: 15px;
  align-items: center;
}

.approval-content {
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
