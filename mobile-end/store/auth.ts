import { defineStore } from 'pinia'
import type { LoginPayload, MobileSession, UserRole } from '@/types/mobile'
import { fetchCurrentUser, loginByPassword } from '@/services/auth'

const SESSION_KEY = 'primary-homework-mobile-session'
const USE_LOCAL_MOCK = import.meta.env.VITE_USE_MOBILE_MOCK === 'true'

function readSession() {
  const cached = uni.getStorageSync(SESSION_KEY)
  const session = cached ? (cached as MobileSession) : null

  if (!USE_LOCAL_MOCK && session?.token?.startsWith('mobile-local-')) {
    uni.removeStorageSync(SESSION_KEY)
    return null
  }

  return session
}

function mergeSessionProfile(session: MobileSession, currentUser?: {
  userId?: string | number
  userName?: string
  roleType?: UserRole
  schoolId?: string | number
  schoolName?: string
  permissions?: string[]
}) {
  if (!currentUser) {
    return session
  }

  return {
    ...session,
    userId: String(currentUser.userId ?? session.userId ?? ''),
    userName: currentUser.userName ?? session.userName,
    role: (currentUser.roleType ?? session.role) as UserRole,
    schoolId: String(currentUser.schoolId ?? session.schoolId ?? ''),
    schoolName: currentUser.schoolName ?? session.schoolName,
    permissions: currentUser.permissions ?? session.permissions ?? []
  }
}

export const useAuthStore = defineStore('mobile-auth', {
  state: () => ({
    session: null as MobileSession | null
  }),
  getters: {
    isLoggedIn(state) {
      return Boolean(state.session?.token)
    },
    role(state): UserRole | null {
      return state.session?.role ?? null
    }
  },
  actions: {
    bootstrap() {
      this.session = readSession()
    },
    async login(payload: LoginPayload) {
      const loginSession = await loginByPassword(payload)
      let finalSession = loginSession

      if (!loginSession.token.startsWith('mobile-local-')) {
        try {
          const currentUser = await fetchCurrentUser()
          finalSession = mergeSessionProfile(loginSession, currentUser)
        } catch (error) {
          console.warn('fetchCurrentUser failed, fallback to login result', error)
        }
      }

      this.session = finalSession
      uni.setStorageSync(SESSION_KEY, finalSession)
      return finalSession
    },
    async refreshSession() {
      this.bootstrap()

      if (!this.session?.token || this.session.token.startsWith('mobile-local-')) {
        return this.session
      }

      const currentUser = await fetchCurrentUser()
      this.session = mergeSessionProfile(this.session, currentUser)
      uni.setStorageSync(SESSION_KEY, this.session)
      return this.session
    },
    logout() {
      this.session = null
      uni.removeStorageSync(SESSION_KEY)
    }
  }
})
