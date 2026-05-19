import { useAuthStore } from '@/store/auth'
import { useMobilePortalStore } from '@/store/mobile-portal'
import type { UserRole } from '@/types/mobile'

export function ensureMobileRole(role: UserRole) {
  const authStore = useAuthStore()
  const mobilePortalStore = useMobilePortalStore()

  authStore.bootstrap()
  mobilePortalStore.bootstrap()

  if (!authStore.isLoggedIn || authStore.role !== role) {
    uni.reLaunch({ url: '/pages/auth/index' })
    return false
  }

  return true
}

export function ensureMobileLogin() {
  const authStore = useAuthStore()
  const mobilePortalStore = useMobilePortalStore()

  authStore.bootstrap()
  mobilePortalStore.bootstrap()

  if (!authStore.isLoggedIn || !authStore.role) {
    uni.reLaunch({ url: '/pages/auth/index' })
    return false
  }

  return true
}
