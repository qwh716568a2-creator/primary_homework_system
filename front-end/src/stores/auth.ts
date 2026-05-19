import { defineStore } from 'pinia'
import { loginApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    // 初始化时从本地存储读取
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  
  actions: {
    async login(username?: string, password?: string) {
      try {
        const res = await loginApi(username, password)
        
        if (res.code === 200) {
          // 1. 保存到 Store 内存中
          this.token = res.data.token
          this.userInfo = res.data.userInfo
          
          // 2. 持久化到 localStorage，防止刷新丢失
          localStorage.setItem('token', this.token)
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        }
        return res
      } catch (error) {
        throw error
      }
    },
    
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})