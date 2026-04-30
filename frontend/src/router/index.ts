import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

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
      next()
    } catch (error) {
      localStorage.removeItem('token')
      next('/login')
    }
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/')
  } else {
    next()
  }
})

export default router
