import type { UserRole } from '@/types/mobile'

export function getRoleHomePage(role: UserRole) {
  return role === 'parent' ? '/pages/parent/home/index' : '/pages/student/home/index'
}

export function getRoleMessagePage(role: UserRole) {
  return role === 'parent' ? '/pages/parent/messages/index' : '/pages/student/messages/index'
}

export function goToRoleHome(role: UserRole) {
  uni.reLaunch({
    url: getRoleHomePage(role)
  })
}

export function goToRoleMessages(role: UserRole) {
  uni.reLaunch({
    url: getRoleMessagePage(role)
  })
}

export function goToProfile() {
  uni.reLaunch({
    url: '/pages/profile/index'
  })
}

export function goToStudentWrongBook() {
  uni.navigateTo({
    url: '/pages/student/wrongbook/home'
  })
}

export function goToMessageSettings() {
  uni.navigateTo({
    url: '/pages/settings/messages/index'
  })
}

export function goToAccountSecurity() {
  uni.navigateTo({
    url: '/pages/settings/security/index'
  })
}
