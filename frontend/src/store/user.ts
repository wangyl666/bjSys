import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo } from '@/api/user'
import type { UserVO } from '@/types'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserVO | null>(null)
  const isLoggedIn = ref(false)

  const setToken = (token: string) => {
    localStorage.setItem('token', token)
  }

  const getToken = () => {
    return localStorage.getItem('token')
  }

  const clearToken = () => {
    localStorage.removeItem('token')
    userInfo.value = null
    isLoggedIn.value = false
  }

  const fetchCurrentUser = async () => {
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      isLoggedIn.value = true
      return res
    } catch (error) {
      clearToken()
      throw error
    }
  }

  const logout = () => {
    clearToken()
  }

  return {
    userInfo,
    isLoggedIn,
    setToken,
    getToken,
    clearToken,
    fetchCurrentUser,
    logout
  }
})
