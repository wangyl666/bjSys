<template>
  <div class="approval-manage-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">审批管理</h2>
        <el-radio-group v-model="statusFilter" @change="handleStatusChange">
          <el-radio-button label="PENDING">待审批</el-radio-button>
          <el-radio-button label="APPROVED">已通过</el-radio-button>
          <el-radio-button label="REJECTED">已拒绝</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    
    <div class="approval-content">
      <el-table
        v-loading="loading"
        :data="approvalList"
        style="width: 100%"
        :empty-text="'暂无审批记录'"
      >
        <el-table-column prop="noteTitle" label="笔记标题" min-width="200">
          <template #default="scope">
            <el-button 
              type="primary" 
              text 
              @click="handleViewNote(scope.row)"
            >
              {{ scope.row.noteTitle }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="提交人" width="120">
          <template #default="scope">
            {{ scope.row.username }}
          </template>
        </el-table-column>
        <el-table-column prop="approvalStatus" label="审批状态" width="120">
          <template #default="scope">
            <el-tag :type="getApprovalTagType(scope.row.approvalStatus)" effect="dark">
              {{ getApprovalStatusText(scope.row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewedByAdmin" label="查看状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.viewedByAdmin === 1" type="success" effect="dark" size="small">
              已查看
            </el-tag>
            <el-tag v-else type="info" effect="dark" size="small">
              未查看
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
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              text 
              @click="handleViewNote(scope.row)"
            >
              查看
            </el-button>
            <template v-if="scope.row.approvalStatus === 'PENDING'">
              <el-button 
                :type="scope.row.viewedByAdmin === 1 ? 'success' : 'info'"
                :disabled="scope.row.viewedByAdmin !== 1"
                text 
                @click="handleApprove(scope.row)"
              >
                通过
              </el-button>
              <el-button 
                :type="scope.row.viewedByAdmin === 1 ? 'danger' : 'info'"
                :disabled="scope.row.viewedByAdmin !== 1"
                text 
                @click="handleReject(scope.row)"
              >
                拒绝
              </el-button>
            </template>
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
    
    <el-dialog v-model="approvalDialogVisible" title="审批处理" width="500px">
      <el-form :model="approvalForm" label-width="80px">
        <el-form-item label="笔记标题">
          <el-input :value="currentApproval?.noteTitle" disabled />
        </el-form-item>
        <el-form-item label="提交人">
          <el-input :value="currentApproval?.username" disabled />
        </el-form-item>
        <el-form-item label="提交时间">
          <el-input :value="currentApproval?.submittedAt ? formatTime(currentApproval.submittedAt) : ''" disabled />
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input
            v-model="approvalForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button 
          :type="approvalForm.approved ? 'success' : 'danger'" 
          @click="handleConfirmApproval"
          :loading="processing"
        >
          确认{{ approvalForm.approved ? '通过' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="noteDetailVisible" title="笔记详情" width="80%" top="5vh">
      <div class="note-detail-content" v-if="selectedNote">
        <h2 class="note-title">{{ selectedNote.title }}</h2>
        <div class="note-meta">
          <el-tag v-if="selectedNote.categoryName" type="primary" size="small">
            {{ selectedNote.categoryName }}
          </el-tag>
          <el-tag
            v-for="tag in selectedNote.tags"
            :key="tag.id"
            size="small"
            effect="plain"
            :style="{ borderColor: tag.color, color: tag.color }"
          >
            {{ tag.name }}
          </el-tag>
        </div>
        <div id="note-preview" class="note-preview"></div>
      </div>
      <template #footer>
        <el-button @click="noteDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Vditor from 'vditor'
import { getPendingApprovals, processApproval, getMyApprovals, viewApprovalNote } from '@/api/approval'
import dayjs from 'dayjs'
import type { NoteApprovalVO, NoteVO } from '@/types'

const router = useRouter()

const loading = ref(false)
const processing = ref(false)
const approvalList = ref<NoteApprovalVO[]>([])
const total = ref(0)
const statusFilter = ref('PENDING')

const pageParams = reactive({
  page: 1,
  size: 10
})

const approvalDialogVisible = ref(false)
const currentApproval = ref<NoteApprovalVO | null>(null)
const approvalForm = reactive({
  approved: true,
  remark: ''
})

const noteDetailVisible = ref(false)
const selectedNote = ref<NoteVO | null>(null)
const currentViewingApprovalId = ref<number | null>(null)

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
    let res
    if (statusFilter.value === 'PENDING') {
      res = await getPendingApprovals(pageParams.page, pageParams.size)
    } else {
      res = await getMyApprovals(pageParams.page, pageParams.size)
      res.data.records = res.data.records.filter(
        (r: NoteApprovalVO) => r.approvalStatus === statusFilter.value
      )
      res.data.total = res.data.records.length
    }
    approvalList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取审批列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleStatusChange = () => {
  pageParams.page = 1
  fetchApprovalList()
}

const handleViewNote = async (approval: NoteApprovalVO) => {
  if (!approval.id) return
  
  currentViewingApprovalId.value = approval.id
  
  try {
    const res = await viewApprovalNote(approval.id)
    selectedNote.value = res.data
    noteDetailVisible.value = true
    
    if (approval.approvalStatus === 'PENDING' && approval.viewedByAdmin !== 1) {
      approval.viewedByAdmin = 1
      ElMessage.success('已标记为已查看')
    }
    
    nextTick(() => {
      renderNotePreview()
    })
  } catch (error) {
    console.error('获取笔记详情失败:', error)
    ElMessage.error('获取笔记详情失败')
  }
}

const renderNotePreview = () => {
  if (!selectedNote.value) return
  
  const previewElement = document.getElementById('note-preview')
  if (!previewElement) return
  
  Vditor.preview(previewElement, selectedNote.value.content || '', {
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

const handleApprove = (approval: NoteApprovalVO) => {
  currentApproval.value = approval
  approvalForm.approved = true
  approvalForm.remark = ''
  approvalDialogVisible.value = true
}

const handleReject = (approval: NoteApprovalVO) => {
  currentApproval.value = approval
  approvalForm.approved = false
  approvalForm.remark = ''
  approvalDialogVisible.value = true
}

const handleConfirmApproval = async () => {
  if (!currentApproval.value) return
  
  processing.value = true
  try {
    await processApproval(
      currentApproval.value.id,
      approvalForm.approved,
      approvalForm.remark
    )
    ElMessage.success(approvalForm.approved ? '审批通过' : '已拒绝')
    approvalDialogVisible.value = false
    fetchApprovalList()
  } catch (error: any) {
    console.error('处理审批失败:', error)
    ElMessage.error(error.message || '处理审批失败')
  } finally {
    processing.value = false
  }
}

onMounted(() => {
  fetchApprovalList()
})
</script>

<style scoped>
.approval-manage-container {
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

.approval-content {
  flex: 1;
  overflow: auto;
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

.note-detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.note-title {
  margin: 0 0 20px 0;
  font-size: 24px;
  color: #303133;
}

.note-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.note-preview {
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
