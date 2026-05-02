<template>
  <div class="chat-room-container">
    <div class="chat-header">
      <el-icon><ChatDotRound /></el-icon>
      <span class="header-title">公共聊天室</span>
      <span class="connection-status" :class="statusClass">
        {{ connectionStatus }}
      </span>
    </div>
    
    <div class="chat-content" ref="chatContentRef">
      <div v-loading="loading" class="message-list">
        <div 
          v-for="message in messages" 
          :key="message.id || Math.random()" 
          class="message-item"
          :class="{ 
            'system-message': message.messageType === 'SYSTEM',
            'self-message': isCurrentUser(message.userId)
          }"
        >
          <template v-if="message.messageType === 'SYSTEM'">
            <div class="system-content">
              <el-icon><InfoFilled /></el-icon>
              <span>{{ message.content }}</span>
            </div>
          </template>
          
          <template v-else>
            <div class="message-avatar" v-if="!isCurrentUser(message.userId)">
              <el-avatar :size="40" v-if="message.user?.avatar" :src="message.user.avatar" />
              <el-avatar :size="40" v-else-if="message.user?.nickname">
                {{ message.user.nickname.charAt(0) }}
              </el-avatar>
              <el-avatar :size="40" v-else>
                <el-icon><User /></el-icon>
              </el-avatar>
            </div>
            
            <div class="message-wrapper" :class="{ 'align-right': isCurrentUser(message.userId) }">
              <div class="message-info" :class="{ 'text-right': isCurrentUser(message.userId) }">
                <span class="username">
                  {{ message.user?.nickname || message.user?.username || '未知用户' }}
                </span>
                <span class="time">{{ formatTime(message.createdAt) }}</span>
              </div>
              
              <div class="message-bubble" :class="{ 'self-bubble': isCurrentUser(message.userId) }">
                {{ message.content }}
              </div>
            </div>
            
            <div class="message-avatar" v-if="isCurrentUser(message.userId)">
              <el-avatar :size="40" v-if="currentUser?.avatar" :src="currentUser.avatar" />
              <el-avatar :size="40" v-else-if="currentUser?.nickname">
                {{ currentUser.nickname.charAt(0) }}
              </el-avatar>
              <el-avatar :size="40" v-else>
                <el-icon><User /></el-icon>
              </el-avatar>
            </div>
          </template>
        </div>
      </div>
    </div>
    
    <div class="chat-input-area">
      <div class="input-wrapper">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          placeholder="输入消息..."
          resize="none"
          @keyup.enter.exact="handleSendMessage"
          :disabled="!isConnected"
        />
      </div>
      <div class="action-bar">
        <span class="tip" v-if="!isConnected">连接已断开，无法发送消息</span>
        <span class="tip" v-else>按 Enter 发送</span>
        <el-button 
          type="primary" 
          @click="handleSendMessage"
          :loading="sending"
          :disabled="!inputMessage.trim() || !isConnected"
        >
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { ChatDotRound, User, InfoFilled } from '@element-plus/icons-vue'
import { getChatMessages } from '@/api/chat'
import dayjs from 'dayjs'
import type { ChatMessageVO, UserVO } from '@/types'

const userStore = useUserStore()

const chatContentRef = ref<HTMLElement | null>(null)
const messages = ref<ChatMessageVO[]>([])
const inputMessage = ref('')
const loading = ref(false)
const sending = ref(false)
const isConnected = ref(false)
const connectionStatus = ref('连接中...')
const statusClass = ref('connecting')

let ws: WebSocket | null = null

const currentUser = computed<UserVO | null>(() => userStore.userInfo)

const isCurrentUser = (userId: number) => {
  return currentUser.value !== null && userId === currentUser.value.id
}

const formatTime = (time: string) => {
  return dayjs(time).format('HH:mm')
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContentRef.value) {
      chatContentRef.value.scrollTop = chatContentRef.value.scrollHeight
    }
  })
}

const fetchHistoryMessages = async () => {
  loading.value = true
  try {
    const res = await getChatMessages(1, 100)
    const historyMessages = res.data.records.reverse()
    messages.value = [...historyMessages, ...messages.value]
  } catch (error) {
    console.error('获取历史消息失败:', error)
  } finally {
    loading.value = false
  }
}

const connectWebSocket = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    console.error('WebSocket连接失败: 没有找到token')
    ElMessage.error('请先登录')
    return
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws/chat?token=${encodeURIComponent(token)}`
  
  console.log('尝试连接WebSocket:', wsUrl)
  connectionStatus.value = '连接中...'
  statusClass.value = 'connecting'

  try {
    ws = new WebSocket(wsUrl)

    ws.onopen = () => {
      console.log('✅ WebSocket连接成功')
      isConnected.value = true
      connectionStatus.value = '已连接'
      statusClass.value = 'connected'
    }

    ws.onmessage = (event) => {
      console.log('📨 收到WebSocket消息:', event.data)
      try {
        const message: ChatMessageVO = JSON.parse(event.data)
        messages.value.push(message)
        scrollToBottom()
      } catch (error) {
        console.error('❌ 解析消息失败:', error, '原始数据:', event.data)
      }
    }

    ws.onclose = (event) => {
      console.log('🔌 WebSocket连接已关闭', 'code:', event.code, 'reason:', event.reason, 'wasClean:', event.wasClean)
      isConnected.value = false
      connectionStatus.value = '已断开'
      statusClass.value = 'disconnected'
      
      if (event.code !== 1000) {
        console.log('⏰ 3秒后尝试重新连接...')
        setTimeout(() => {
          if (!isConnected.value) {
            connectWebSocket()
          }
        }, 3000)
      }
    }

    ws.onerror = (error) => {
      console.error('❌ WebSocket连接错误:', error)
      isConnected.value = false
      connectionStatus.value = '连接失败'
      statusClass.value = 'disconnected'
    }
  } catch (error) {
    console.error('❌ 创建WebSocket连接失败:', error)
    connectionStatus.value = '连接失败'
    statusClass.value = 'disconnected'
  }
}

const handleSendMessage = () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  if (!isConnected.value || !ws) {
    ElMessage.error('连接已断开，请稍后重试')
    return
  }

  const messageContent = inputMessage.value.trim()
  const message = {
    type: 'TEXT',
    content: messageContent
  }

  sending.value = true
  try {
    ws.send(JSON.stringify(message))
    inputMessage.value = ''
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  } finally {
    sending.value = false
  }
}

onMounted(() => {
  fetchHistoryMessages()
  connectWebSocket()
})

onUnmounted(() => {
  if (ws) {
    ws.close()
    ws = null
  }
})
</script>

<style scoped>
.chat-room-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 20px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-right: 10px;
}

.connection-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.connection-status.connecting {
  color: #e6a23c;
  background-color: #fdf6ec;
}

.connection-status.connected {
  color: #67c23a;
  background-color: #f0f9eb;
}

.connection-status.disconnected {
  color: #f56c6c;
  background-color: #fef0f0;
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.system-message {
  justify-content: center;
}

.system-content {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background-color: #e4e7ed;
  color: #909399;
  border-radius: 16px;
  font-size: 12px;
}

.message-avatar {
  flex-shrink: 0;
}

.message-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 70%;
}

.message-wrapper.align-right {
  align-items: flex-end;
}

.message-info {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.message-info.text-right {
  flex-direction: row-reverse;
}

.message-info .username {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.message-info .time {
  font-size: 11px;
  color: #c0c4cc;
}

.message-bubble {
  padding: 10px 16px;
  background-color: #fff;
  border-radius: 12px;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  word-break: break-word;
  line-height: 1.5;
  color: #303133;
}

.message-bubble.self-bubble {
  background-color: #409eff;
  color: #fff;
  border-top-left-radius: 12px;
  border-top-right-radius: 4px;
}

.chat-input-area {
  background-color: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 15px 20px;
  flex-shrink: 0;
}

.input-wrapper {
  margin-bottom: 10px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tip {
  font-size: 12px;
  color: #909399;
}
</style>
