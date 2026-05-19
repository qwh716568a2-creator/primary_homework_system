import { defineStore } from 'pinia'
import type { MobileNotificationSettings, MobileSecuritySettings } from '@/types/mobile'

const NOTIFICATION_SETTINGS_KEY = 'primary-homework-mobile-notification-settings'
const SECURITY_SETTINGS_KEY = 'primary-homework-mobile-security-settings'

function createDefaultNotificationSettings(): MobileNotificationSettings {
  return {
    masterEnabled: true,
    assignmentEnabled: true,
    reviewEnabled: true,
    reminderEnabled: true,
    systemEnabled: true,
    soundEnabled: true,
    vibrationEnabled: true,
    quietHoursEnabled: false,
    quietStart: '22:00',
    quietEnd: '07:00'
  }
}

function createDefaultSecuritySettings(): MobileSecuritySettings {
  return {
    hideAccountIdentifier: false,
    rememberAccount: true,
    loginAlertEnabled: true,
    appLockEnabled: false,
    biometricEnabled: false,
    passwordCheckedAt: ''
  }
}

function readStorage<T>(key: string, fallback: T): T {
  const cached = uni.getStorageSync(key)

  if (!cached) {
    return fallback
  }

  return {
    ...fallback,
    ...(cached as Partial<T>)
  }
}

function formatNow() {
  const now = new Date()
  const month = `${now.getMonth() + 1}`.padStart(2, '0')
  const day = `${now.getDate()}`.padStart(2, '0')
  const hour = `${now.getHours()}`.padStart(2, '0')
  const minute = `${now.getMinutes()}`.padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day} ${hour}:${minute}`
}

export const useMobilePreferencesStore = defineStore('mobile-preferences', {
  state: () => ({
    notificationSettings: createDefaultNotificationSettings(),
    securitySettings: createDefaultSecuritySettings()
  }),
  actions: {
    bootstrap() {
      this.notificationSettings = readStorage(
        NOTIFICATION_SETTINGS_KEY,
        createDefaultNotificationSettings()
      )
      this.securitySettings = readStorage(
        SECURITY_SETTINGS_KEY,
        createDefaultSecuritySettings()
      )
    },
    saveNotificationSettings(settings: MobileNotificationSettings) {
      this.notificationSettings = {
        ...settings
      }
      uni.setStorageSync(NOTIFICATION_SETTINGS_KEY, this.notificationSettings)
    },
    resetNotificationSettings() {
      this.notificationSettings = createDefaultNotificationSettings()
      uni.setStorageSync(NOTIFICATION_SETTINGS_KEY, this.notificationSettings)
    },
    saveSecuritySettings(settings: MobileSecuritySettings) {
      this.securitySettings = {
        ...settings
      }
      uni.setStorageSync(SECURITY_SETTINGS_KEY, this.securitySettings)
    },
    resetSecuritySettings() {
      this.securitySettings = createDefaultSecuritySettings()
      uni.setStorageSync(SECURITY_SETTINGS_KEY, this.securitySettings)
    },
    markPasswordChecked() {
      this.securitySettings = {
        ...this.securitySettings,
        passwordCheckedAt: formatNow()
      }
      uni.setStorageSync(SECURITY_SETTINGS_KEY, this.securitySettings)
    }
  }
})
