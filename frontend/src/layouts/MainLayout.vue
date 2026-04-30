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
        <el-menu-item index="/notes">
          <el-icon><Document /></el-icon>
          <span>笔记列表</span>
        </el-menu-item>
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <span>用户中心</span>
        </el-menu-item>
      </el-menu>
      
      <div class="sidebar-bottom">
        <div class="user-info">
          <el-avatar :size="36" v-if="userInfo?.nickname">
            {{ userInfo.nickname.charAt(0) }}
          </el-avatar>
          <el-avatar :size="36" v-else>
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
        </div>
        <el-button type="text" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </el-aside>
    
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox } from 'element-plus'
import { Document, Edit, User, SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

const activeMenu = computed(() => route.path)

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
  margin-bottom: 10px;
}

.username {
  margin-left: 10px;
  font-size: 14px;
  color: #303133;
}

.main-content {
  background-color: #ffffff;
  padding: 0;
  overflow: auto;
}
</style>
