import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!user.value)

  // 动作
  const setToken = (newToken) => {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  const setUser = (userData) => {
    user.value = userData
  }

  const login = async (loginData) => {
    try {
      const response = await authApi.login(loginData)
      if (response.code === 200) {
        setToken(response.data.token)
        setUser(response.data.user)
        return { success: true }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      return { success: false, message: error.message || '登录失败' }
    }
  }

  const register = async (registerData) => {
    try {
      const response = await authApi.register(registerData)
      if (response.code === 200) {
        return { success: true, message: '注册成功' }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      return { success: false, message: error.message || '注册失败' }
    }
  }

  const logout = () => {
    setToken('')
    setUser(null)
  }

  const fetchUserProfile = async () => {
    try {
      const response = await authApi.getUserProfile()
      if (response.code === 200) {
        setUser(response.data)
        return { success: true }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      return { success: false, message: error.message || '获取用户信息失败' }
    }
  }

  return {
    user,
    token,
    isLoggedIn,
    setToken,
    setUser,
    login,
    register,
    logout,
    fetchUserProfile
  }
})