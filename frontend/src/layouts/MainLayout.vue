<template>
  <el-container class="main-container">
    <el-aside width="240px" class="sidebar">
      <div class="logo">
        <el-icon size="24"><Edit /></el-icon>
        <span class="logo-text">笔记管理系统</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        router
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/knowledge">
          <el-icon><FolderOpened /></el-icon>
          <span>知识库</span>
        </el-menu-item>
      </el-menu>
      
      <div class="sidebar-bottom">
        <el-dropdown trigger="click" placement="top-start">
          <div class="user-info">
            <el-avatar :size="36" v-if="userInfo?.avatar" :src="userInfo.avatar" />
            <el-avatar :size="36" v-else-if="userInfo?.nickname">
              {{ userInfo.nickname.charAt(0) }}
            </el-avatar>
            <el-avatar :size="36" v-else>
              <el-icon><User /></el-icon>
            </el-avatar>
            <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
            <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleOpenInfoDialog">
                <el-icon><Edit /></el-icon>
                <span>修改信息</span>
              </el-dropdown-item>
              <el-dropdown-item @click="handleOpenPasswordDialog">
                <el-icon><Key /></el-icon>
                <span>修改密码</span>
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-aside>
    
    <el-main class="main-content">
      <router-view />
    </el-main>
    
    <el-dialog
      v-model="infoDialogVisible"
      title="修改个人信息"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="infoForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="infoForm.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="infoForm.nickname" placeholder="请输入昵称" />
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
              <img v-if="infoForm.avatar" :src="infoForm.avatar" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">点击上传头像，支持 JPG、PNG、GIF 格式，大小不超过 10MB</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="infoDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateInfo" :loading="updatingInfo">
          保存修改
        </el-button>
      </template>
    </el-dialog>
    
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="500px"
      :close-on-click-modal="false"
    >
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
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdatePassword" :loading="updatingPassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { updateUserInfo, updatePassword, uploadAvatar } from '@/api/user'
import { Document, Edit, User, SwitchButton, HomeFilled, ArrowDown, Key, Plus, FolderOpened } from '@element-plus/icons-vue'
import type { UpdateUserInfoDTO, UpdatePasswordDTO } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

const activeMenu = computed(() => {
  if (route.path === '/') {
    return '/'
  }
  if (route.path.startsWith('/notes')) {
    return '/knowledge'
  }
  if (route.path.startsWith('/knowledge')) {
    return '/knowledge'
  }
  return route.path
})

const infoDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const updatingInfo = ref(false)
const updatingPassword = ref(false)
const uploadingAvatar = ref(false)

const infoForm = reactive<{
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

const passwordFormRef = ref<FormInstance>()

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

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}

const handleOpenInfoDialog = () => {
  if (userInfo.value) {
    infoForm.username = userInfo.value.username
    infoForm.nickname = userInfo.value.nickname || ''
    infoForm.avatar = userInfo.value.avatar
  }
  infoDialogVisible.value = true
}

const handleOpenPasswordDialog = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

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
    infoForm.avatar = res.data.avatar
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
  if (!infoForm.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }

  updatingInfo.value = true
  try {
    const updateData: UpdateUserInfoDTO = {
      nickname: infoForm.nickname
    }
    const res = await updateUserInfo(updateData)
    userStore.userInfo = res.data
    ElMessage.success('修改成功')
    infoDialogVisible.value = false
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
        passwordDialogVisible.value = false
        userStore.logout()
        router.push('/login')
      } catch (error: any) {
        console.error('修改密码失败:', error)
        ElMessage.error(error?.message || '修改密码失败')
      } finally {
        updatingPassword.value = false
      }
    }
  })
}
</script>

<style scoped>
.main-container {
  height: 100vh;
}

.sidebar {
  background-color: #f5f7fa;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 10px;
}

.logo-text {
  margin-left: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.sidebar-menu {
  border-right: none;
  flex: 1;
}

.sidebar-bottom {
  padding: 15px 20px;
  border-top: 1px solid #e4e7ed;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #e4e7ed;
}

.username {
  margin-left: 10px;
  font-size: 14px;
  color: #303133;
  flex: 1;
}

.dropdown-arrow {
  font-size: 12px;
  color: #909399;
}

.main-content {
  background-color: #ffffff;
  padding: 0;
  overflow: auto;
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
</style>
