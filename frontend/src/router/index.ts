import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/',
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/Home.vue'),
        meta: { title: '首页', requiresAuth: true }
      },
      {
        path: 'knowledge',
        name: 'KnowledgeBase',
        component: () => import('@/views/knowledge/KnowledgeBase.vue'),
        meta: { title: '知识库', requiresAuth: true }
      },
      {
        path: 'graph',
        name: 'TagGraph',
        component: () => import('@/views/knowledge/TagGraph.vue'),
        meta: { title: '智慧图谱', requiresAuth: true }
      },
      {
        path: 'notes',
        name: 'NoteList',
        component: () => import('@/views/notes/NoteList.vue'),
        meta: { title: '我的笔记', requiresAuth: true }
      },
      {
        path: 'tags',
        name: 'TagManage',
        component: () => import('@/views/notes/TagManage.vue'),
        meta: { title: '标签管理', requiresAuth: true }
      },
      {
        path: 'notes/create',
        name: 'NoteCreate',
        component: () => import('@/views/notes/NoteEdit.vue'),
        meta: { title: '新建笔记', requiresAuth: true }
      },
      {
        path: 'notes/:id/edit',
        name: 'NoteEdit',
        component: () => import('@/views/notes/NoteEdit.vue'),
        meta: { title: '编辑笔记', requiresAuth: true }
      },
      {
        path: 'notes/:id',
        name: 'NoteDetail',
        component: () => import('@/views/notes/NoteDetail.vue'),
        meta: { title: '笔记详情', requiresAuth: true }
      },
      {
        path: 'approvals',
        name: 'ApprovalRecord',
        component: () => import('@/views/notes/ApprovalRecord.vue'),
        meta: { title: '发布记录', requiresAuth: true }
      },
      {
        path: 'admin/approvals',
        name: 'ApprovalManage',
        component: () => import('@/views/admin/ApprovalManage.vue'),
        meta: { title: '审批管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/community/CommunityList.vue'),
        meta: { title: '笔记社区', requiresAuth: true }
      },
      {
        path: 'community/:id',
        name: 'CommunityNoteDetail',
        component: () => import('@/views/community/CommunityNoteDetail.vue'),
        meta: { title: '笔记详情', requiresAuth: true }
      },
      {
        path: 'chat',
        name: 'ChatRoom',
        component: () => import('@/views/chat/ChatRoom.vue'),
        meta: { title: '公共聊天室', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 笔记管理系统` : '笔记管理系统'
  
  const userStore = useUserStore()
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (token && !userStore.isLoggedIn) {
    try {
      await userStore.fetchCurrentUser()
      
      if (to.meta.requiresAdmin && userStore.userInfo?.role !== 'ADMIN') {
        ElMessage?.warning('无权限访问该页面')
        next('/')
        return
      }
      
      next()
    } catch (error) {
      localStorage.removeItem('token')
      next('/login')
    }
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/')
  } else if (to.meta.requiresAdmin && userStore.isLoggedIn && userStore.userInfo?.role !== 'ADMIN') {
    ElMessage?.warning('无权限访问该页面')
    next('/')
  } else {
    next()
  }
})

export default router
