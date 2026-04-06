export type UserRole = 'teacher' | 'admin' | 'student' | 'parent'

export interface AuthSession {
  authenticated: boolean
  account: string
  name: string
  school: string
  schoolId?: number | string | null
  role: UserRole
  token?: string
  expiresIn?: number
  permissions?: string[]
}

export interface RegistrationDraft {
  name: string
  school: string
  role: UserRole
  account: string
  password: string
}

export interface RegisteredUser extends RegistrationDraft {}
