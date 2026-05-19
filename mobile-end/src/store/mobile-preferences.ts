import { defineStore } from 'pinia'
import type {
  MobileNotificationSettings,
  MobileSecuritySettings,
  PasswordCheckPayload,
  UserRole
} from '@/types/mobile'
import {
  checkPassword,
  fetchNotificationSettings,
  fetchSecuritySettings,
  markAllMessagesRead,
  resetNotificationSettings as resetNotificationSettingsRequest,
  resetSecuritySettings as resetSecuritySettingsRequest,
  saveNotificationSettings as saveNotificationSettingsRequest,
  saveSecuritySettings as saveSecuritySettingsRequest
} from '@/services/preferences'

const NOTIFICATION_SETTINGS_KEY = 'primary-homework-mobile-notification-settings'
const SECURITY_SETTINGS_KEY = 'primary-homework-mobile-security-settings'
const SESSION_KEY = 'primary-homework-mobile-session'

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

function getCurrentRole(): UserRole | null {
  const session = uni.getStorageSync(SESSION_KEY) as { role?: UserRole } | undefined
  return session?.role ?? null
}

function mergeNotificationSettings(
  source?: Partial<MobileNotificationSettings> | null
): MobileNotificationSettings {
  return {
    ...createDefaultNotificationSettings(),
    ...(source ?? {})
  }
}

function mergeSecuritySettings(source?: Partial<MobileSecuritySettings> | null): MobileSecuritySettings {
  return {
    ...createDefaultSecuritySettings(),
    ...(source ?? {})
  }
}

export const useMobilePreferencesStore = defineStore('mobile-preferences', {
  state: () => ({
    notificationSettings: createDefaultNotificationSettings(),
    securitySettings: createDefaultSecuritySettings()
  }),
  actions: {
    async bootstrap() {
      this.notificationSettings = readStorage(
        NOTIFICATION_SETTINGS_KEY,
        createDefaultNotificationSettings()
      )
      this.securitySettings = readStorage(
        SECURITY_SETTINGS_KEY,
        createDefaultSecuritySettings()
      )

      const role = getCurrentRole()
      if (!role) {
        return
      }

      try {
        const [notificationSettings, securitySettings] = await Promise.all([
          fetchNotificationSettings(role),
          fetchSecuritySettings(role)
        ])
        this.notificationSettings = mergeNotificationSettings(notificationSettings)
        this.securitySettings = mergeSecuritySettings(securitySettings)
        uni.setStorageSync(NOTIFICATION_SETTINGS_KEY, this.notificationSettings)
        uni.setStorageSync(SECURITY_SETTINGS_KEY, this.securitySettings)
      } catch (error) {
        console.warn('sync mobile preferences failed, fallback to local cache', error)
      }
    },
    async saveNotificationSettings(settings: MobileNotificationSettings) {
      const role = getCurrentRole()
      const nextSettings = role
        ? await saveNotificationSettingsRequest(role, settings)
        : settings
      this.notificationSettings = mergeNotificationSettings(nextSettings)
      uni.setStorageSync(NOTIFICATION_SETTINGS_KEY, this.notificationSettings)
      return this.notificationSettings
    },
    async resetNotificationSettings() {
      const role = getCurrentRole()
      const nextSettings = role
        ? await resetNotificationSettingsRequest(role)
        : createDefaultNotificationSettings()
      this.notificationSettings = mergeNotificationSettings(nextSettings)
      uni.setStorageSync(NOTIFICATION_SETTINGS_KEY, this.notificationSettings)
      return this.notificationSettings
    },
    async markAllMessagesRead() {
      const role = getCurrentRole()
      if (!role) {
        return
      }
      await markAllMessagesRead(role)
    },
    async saveSecuritySettings(settings: MobileSecuritySettings) {
      const role = getCurrentRole()
      const nextSettings = role
        ? await saveSecuritySettingsRequest(role, settings)
        : settings
      this.securitySettings = mergeSecuritySettings(nextSettings)
      uni.setStorageSync(SECURITY_SETTINGS_KEY, this.securitySettings)
      return this.securitySettings
    },
    async resetSecuritySettings() {
      const role = getCurrentRole()
      const nextSettings = role
        ? await resetSecuritySettingsRequest(role)
        : createDefaultSecuritySettings()
      this.securitySettings = mergeSecuritySettings(nextSettings)
      uni.setStorageSync(SECURITY_SETTINGS_KEY, this.securitySettings)
      return this.securitySettings
    },
    async markPasswordChecked(payload?: PasswordCheckPayload) {
      const role = getCurrentRole()
      if (role && payload) {
        this.securitySettings = mergeSecuritySettings(await checkPassword(role, payload))
      } else {
        this.securitySettings = {
          ...this.securitySettings,
          passwordCheckedAt: formatNow()
        }
      }
      uni.setStorageSync(SECURITY_SETTINGS_KEY, this.securitySettings)
      return this.securitySettings
    }
  }
})
