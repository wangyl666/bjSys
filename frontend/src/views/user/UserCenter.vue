<template>
  <div class="user-center-container">
    <div class="page-header">
      <h2 class="page-title">用户中心</h2>
    </div>
    
    <div class="user-content">
      <el-row :gutter="20">
        <el-col :span="16">
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
          
          <el-card class="info-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>基本信息</span>
              </div>
            </template>
            <el-form :model="userForm" label-width="100px" style="max-width: 500px">
              <el-form-item label="用户名">
                <el-input v-model="userForm.username" disabled />
              </el-form-item>
              <el-form-item label="昵称">
                <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
              </el-form-item>
              <el-form-item label="头像">
                <div class="avatar-uploader">
                  <el-upload
                    class="avatar-upload"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :http-request="handleAvatarUpload"
                    action="#"
                  >
                    <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                  <div class="upload-tip">点击上传头像，支持 JPG、PNG、GIF 格式，大小不超过 10MB</div>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUpdateInfo" :loading="updatingInfo">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="password-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>修改密码</span>
              </div>
            </template>
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
              <el-form-item label="旧密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入旧密码"
                  show-password
                />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                  show-password
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUpdatePassword" :loading="updatingPassword">
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
          
          <el-card class="user-info-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>账号信息</span>
              </div>
            </template>
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">用户ID</span>
                <span class="info-value">{{ userInfo?.id }}</span>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getUserStats, updateUserInfo, updatePassword, uploadAvatar } from '@/api/user'
import { Document, Warning, Folder, PriceTag, Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import type { UserStatsVO, UserVO, UpdateUserInfoDTO, UpdatePasswordDTO } from '@/types'

const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

const userStats = ref<UserStatsVO>({
  noteCount: 0,
  errorQuestionCount: 0,
  categoryCount: 0,
  tagCount: 0
})

const userForm = reactive<{
  username: string
  nickname: string
  avatar: string | null
}>({
  username: '',
  nickname: '',
  avatar: null
})

const passwordForm = reactive<{
  oldPassword: string
  newPassword: string
  confirmPassword: string
}>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const passwordFormRef = ref<FormInstance>()
const updatingInfo = ref(false)
const updatingPassword = ref(false)

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

const initUserForm = () => {
  if (userInfo.value) {
    userForm.username = userInfo.value.username
    userForm.nickname = userInfo.value.nickname || ''
    userForm.avatar = userInfo.value.avatar
  }
}

const uploadingAvatar = ref(false)

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

const handleAvatarUpload = async (options: any) => {
  const { file } = options
  uploadingAvatar.value = true
  try {
    const res = await uploadAvatar(file)
    userForm.avatar = res.data.avatar
    userStore.userInfo = res.data
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  } finally {
    uploadingAvatar.value = false
  }
}

const handleUpdateInfo = async () => {
  if (!userForm.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }

  updatingInfo.value = true
  try {
    const updateData: UpdateUserInfoDTO = {
      nickname: userForm.nickname
    }
    const res = await updateUserInfo(updateData)
    userStore.userInfo = res.data
    ElMessage.success('修改成功')
  } catch (error) {
    console.error('修改用户信息失败:', error)
    ElMessage.error('修改失败')
  } finally {
    updatingInfo.value = false
  }
}

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      updatingPassword.value = true
      try {
        const updateData: UpdatePasswordDTO = {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        }
        await updatePassword(updateData)
        ElMessage.success('密码修改成功，请重新登录')
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } catch (error: any) {
        console.error('修改密码失败:', error)
        ElMessage.error(error?.message || '修改密码失败')
      } finally {
        updatingPassword.value = false
      }
    }
  })
}

onMounted(() => {
  initUserForm()
  fetchUserStats()
})
</script>

<style scoped>
.user-center-container {
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

.user-content {
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

.info-card, .password-card, .user-info-card {
  margin-bottom: 20px;
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.avatar-upload {
  width: 100px;
  height: 100px;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
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
