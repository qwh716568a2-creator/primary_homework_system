import type {
  MobileNotificationSettings,
  MobileSecuritySettings,
  PasswordCheckPayload,
  UserRole
} from '@/types/mobile'
import { request } from '@/services/request'

function buildBaseUrl(role: UserRole) {
  return `/api/${role}/settings`
}

export function fetchNotificationSettings(role: UserRole) {
  return request<MobileNotificationSettings>({
    url: `${buildBaseUrl(role)}/messages`,
    method: 'GET'
  })
}

export function saveNotificationSettings(role: UserRole, payload: MobileNotificationSettings) {
  return request<MobileNotificationSettings, MobileNotificationSettings>({
    url: `${buildBaseUrl(role)}/messages`,
    method: 'POST',
    data: payload
  })
}

export function resetNotificationSettings(role: UserRole) {
  return request<MobileNotificationSettings>({
    url: `${buildBaseUrl(role)}/messages/reset`,
    method: 'POST'
  })
}

export function markAllMessagesRead(role: UserRole) {
  return request<null, Record<string, never>>({
    url: `${buildBaseUrl(role)}/messages/read-all`,
    method: 'POST',
    data: {}
  })
}

export function fetchSecuritySettings(role: UserRole) {
  return request<MobileSecuritySettings>({
    url: `${buildBaseUrl(role)}/security`,
    method: 'GET'
  })
}

export function saveSecuritySettings(role: UserRole, payload: MobileSecuritySettings) {
  return request<MobileSecuritySettings, MobileSecuritySettings>({
    url: `${buildBaseUrl(role)}/security`,
    method: 'POST',
    data: payload
  })
}

export function resetSecuritySettings(role: UserRole) {
  return request<MobileSecuritySettings>({
    url: `${buildBaseUrl(role)}/security/reset`,
    method: 'POST'
  })
}

export function checkPassword(role: UserRole, payload: PasswordCheckPayload) {
  return request<MobileSecuritySettings, PasswordCheckPayload>({
    url: `${buildBaseUrl(role)}/security/password-check`,
    method: 'POST',
    data: payload
  })
}
