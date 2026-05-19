<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, Lock, School, User } from '@element-plus/icons-vue'
import { loginByPassword, registerAccount } from '@/api/auth-service-clean'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { getDefaultRouteForRole } from '@/utils/auth-session-clean'

type LoginRole = 'teacher' | 'student' | 'parent'
type SelfRegisterRole = 'student' | 'parent'

const router = useRouter()
const store = useTeacherPortalStore()

const TEXT = {
  eyebrow: '\u5c0f\u5b66\u8bfe\u540e\u4f5c\u4e1a\u7cfb\u7edf',
  heroTitle: '\u7edf\u4e00\u8d26\u53f7\u767b\u5f55',
  heroSummary: '\u6559\u5e08\u3001\u5b66\u751f\u548c\u5bb6\u957f\u4f7f\u7528\u540c\u4e00\u5165\u53e3\u767b\u5f55\uff0c\u7cfb\u7edf\u4f1a\u81ea\u52a8\u8fdb\u5165\u5bf9\u5e94\u9996\u9875\u3002',
  selectRole: '\u8bf7\u9009\u62e9\u8eab\u4efd',
  schoolIdPlaceholder: '\u8bf7\u8f93\u5165\u5b66\u6821 ID',
  studentAccountPlaceholder: '\u8bf7\u8f93\u5165\u5b66\u751f\u5b66\u53f7',
  parentAccountPlaceholder: '\u8bf7\u8f93\u5165\u5bb6\u957f\u624b\u673a\u53f7',
  teacherAccountPlaceholder: '\u8bf7\u8f93\u5165\u6559\u5e08\u624b\u673a\u53f7',
  passwordPlaceholder: '\u8bf7\u8f93\u5165\u767b\u5f55\u5bc6\u7801',
  remember: '\u8bb0\u4f4f\u767b\u5f55\u72b6\u6001',
  forgotPassword: '\u5fd8\u8bb0\u5bc6\u7801\uff1f',
  forgotPasswordMessage: '\u8bf7\u8054\u7cfb\u5b66\u6821\u7ba1\u7406\u5458\u6216\u5e73\u53f0\u8fd0\u8425\u4eba\u5458\u91cd\u7f6e\u5bc6\u7801\u3002',
  login: '\u767b\u5f55',
  loginAgreement: '\u767b\u5f55\u5373\u8868\u793a\u4f60\u540c\u610f\u5e73\u53f0\u8d26\u53f7\u5b89\u5168\u89c4\u8303\u4e0e\u6570\u636e\u4f7f\u7528\u8bf4\u660e\u3002',
  registerName: '\u59d3\u540d',
  registerSchool: '\u5b66\u6821\u540d\u79f0',
  registerStudentPlaceholder: '\u8bf7\u8f93\u5165\u5b66\u53f7',
  registerParentPlaceholder: '\u8bf7\u8f93\u5165\u624b\u673a\u53f7',
  registerStudentHint: '\u5b66\u751f\u6ce8\u518c\u9700\u586b\u5199\u5b66\u53f7\uff0c\u5e76\u4e0e\u5b66\u6821\u6863\u6848\u4e00\u81f4\u3002',
  registerParentHint: '\u5bb6\u957f\u6ce8\u518c\u9700\u586b\u5199\u624b\u673a\u53f7\uff0c\u5e76\u4e0e\u5b66\u6821\u6863\u6848\u4e00\u81f4\u3002',
  setPassword: '\u8bbe\u7f6e\u5bc6\u7801',
  confirmPassword: '\u786e\u8ba4\u5bc6\u7801',
  register: '\u6ce8\u518c',
  registerAgreement: '\u6559\u5e08\u548c\u7ba1\u7406\u5458\u8d26\u53f7\u7531\u7ba1\u7406\u7aef\u521b\u5efa\uff0c\u5b66\u751f\u4e0e\u5bb6\u957f\u53ef\u5728\u6b64\u5b8c\u6210\u6ce8\u518c\u3002',
  noAccount: '\u6ca1\u6709\u8d26\u53f7\uff1f',
  goRegister: '\u53bb\u6ce8\u518c',
  hasAccount: '\u5df2\u6709\u8d26\u53f7\uff1f',
  backLogin: '\u8fd4\u56de\u767b\u5f55',
  roleTeacher: '\u6559\u5e08',
  roleStudent: '\u5b66\u751f',
  roleParent: '\u5bb6\u957f',
  loginEmpty: '\u8bf7\u8f93\u5165\u8d26\u53f7\u548c\u5bc6\u7801\u540e\u518d\u767b\u5f55\u3002',
  loginNeedSchool: '\u5b66\u751f\u767b\u5f55\u9700\u8981\u586b\u5199\u5b66\u6821 ID\u3002',
  loginFallbackError: '\u767b\u5f55\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u3002',
  loginSuccessPrefix: '\u6b22\u8fce\u56de\u6765\uff0c',
  registerIncomplete: '\u8bf7\u5b8c\u6574\u586b\u5199\u6ce8\u518c\u4fe1\u606f\u3002',
  registerPasswordMismatch: '\u4e24\u6b21\u8f93\u5165\u7684\u5bc6\u7801\u4e0d\u4e00\u81f4\uff0c\u8bf7\u91cd\u65b0\u786e\u8ba4\u3002',
  registerSuccess: '\u6ce8\u518c\u6210\u529f\uff0c\u8bf7\u4f7f\u7528\u521a\u521a\u521b\u5efa\u7684\u8d26\u53f7\u767b\u5f55\u3002',
  registerFallbackError: '\u6ce8\u518c\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u3002'
}

const loading = ref(false)
const activePanel = ref<'login' | 'register'>('login')
const loginError = ref('')
const rememberMe = ref(true)

const loginForm = reactive({
  loginType: 'teacher' as LoginRole,
  schoolId: '',
  account: '',
  password: ''
})

const registerForm = reactive({
  name: '',
  school: '',
  role: 'student' as SelfRegisterRole,
  account: '',
  password: '',
  confirmPassword: ''
})

const registerRoleOptions = computed(() =>
  ([
    { role: 'student', label: TEXT.roleStudent },
    { role: 'parent', label: TEXT.roleParent }
  ] as const)
)

const requiresSchoolId = computed(() => loginForm.loginType === 'student')

const loginAccountPlaceholder = computed(() => {
  switch (loginForm.loginType) {
    case 'student':
      return TEXT.studentAccountPlaceholder
    case 'parent':
      return TEXT.parentAccountPlaceholder
    default:
      return TEXT.teacherAccountPlaceholder
  }
})

const registerAccountPlaceholder = computed(() =>
  registerForm.role === 'student' ? TEXT.registerStudentPlaceholder : TEXT.registerParentPlaceholder
)

const registerAccountHint = computed(() =>
  registerForm.role === 'student' ? TEXT.registerStudentHint : TEXT.registerParentHint
)

function switchPanel(panel: 'login' | 'register') {
  activePanel.value = panel
  loginError.value = ''
}

function resetRegisterForm() {
  registerForm.name = ''
  registerForm.school = ''
  registerForm.role = 'student'
  registerForm.account = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
}

function forgotPassword() {
  ElMessage.info(TEXT.forgotPasswordMessage)
}

async function submitLogin() {
  if (!loginForm.account.trim() || !loginForm.password.trim()) {
    loginError.value = TEXT.loginEmpty
    ElMessage.warning(loginError.value)
    return
  }

  if (requiresSchoolId.value && !`${loginForm.schoolId}`.trim()) {
    loginError.value = TEXT.loginNeedSchool
    ElMessage.warning(loginError.value)
    return
  }

  loading.value = true
  loginError.value = ''

  try {
    const session = await loginByPassword({
      account: loginForm.account,
      password: loginForm.password,
      loginType: loginForm.loginType,
      schoolId: requiresSchoolId.value ? loginForm.schoolId : null
    })

    store.setAuthenticatedUser(session, { remember: rememberMe.value })
    ElMessage.success(`${TEXT.loginSuccessPrefix}${session.name}`)
    await router.push(getDefaultRouteForRole(session.role))
  } catch (error) {
    const message = error instanceof Error ? error.message : TEXT.loginFallbackError
    loginError.value = message
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

async function submitRegister() {
  if (
    !registerForm.name.trim() ||
    !registerForm.school.trim() ||
    !registerForm.account.trim() ||
    !registerForm.password.trim() ||
    !registerForm.confirmPassword.trim()
  ) {
    ElMessage.warning(TEXT.registerIncomplete)
    return
  }

  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.error(TEXT.registerPasswordMismatch)
    return
  }

  loading.value = true

  try {
    const result = await registerAccount({
      name: registerForm.name,
      school: registerForm.school,
      role: registerForm.role,
      account: registerForm.account,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword
    })

    loginForm.account = result.account
    loginForm.password = registerForm.password
    loginForm.loginType = registerForm.role
    resetRegisterForm()
    switchPanel('login')
    ElMessage.success(TEXT.registerSuccess)
  } catch (error) {
    const message = error instanceof Error ? error.message : TEXT.registerFallbackError
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="immersive-auth-page">
    <div class="immersive-auth-layout">
      <section class="immersive-auth-hero">
        <div class="immersive-auth-copy">
          <div
            style="display:inline-flex;align-items:center;justify-content:center;width:144px;height:144px;margin-bottom:1.25rem;padding:.8rem;border-radius:32px;background:rgba(255,255,255,.62);backdrop-filter:blur(18px);box-shadow:0 22px 48px rgba(37,99,235,.16);"
          >
            <img
              src="/system-logo.png"
              alt="小学课后作业系统 Logo"
              style="width:100%;height:100%;object-fit:contain;filter:drop-shadow(0 14px 30px rgba(37,99,235,.14));"
            />
          </div>
          <span class="immersive-auth-line"></span>
          <p class="immersive-auth-eyebrow">{{ TEXT.eyebrow }}</p>
          <h1>{{ TEXT.heroTitle }}</h1>
          <p class="immersive-auth-summary">{{ TEXT.heroSummary }}</p>
        </div>

        <div class="immersive-auth-ornaments" aria-hidden="true">
          <span class="immersive-auth-ornament immersive-auth-ornament-a"></span>
          <span class="immersive-auth-ornament immersive-auth-ornament-b"></span>
          <span class="immersive-auth-ornament immersive-auth-ornament-c"></span>
        </div>
      </section>

      <section class="immersive-auth-card">
        <div class="immersive-auth-card-top" aria-hidden="true">
          <span></span>
          <span></span>
          <span></span>
        </div>

        <div v-if="activePanel === 'login'" class="immersive-auth-form">
          <el-alert v-if="loginError" :closable="false" type="error" :title="loginError" show-icon />

          <div class="immersive-auth-inputs">
            <el-select v-model="loginForm.loginType" size="large" :placeholder="TEXT.selectRole" class="mb-2">
              <el-option :label="TEXT.roleTeacher" value="teacher" />
              <el-option :label="TEXT.roleStudent" value="student" />
              <el-option :label="TEXT.roleParent" value="parent" />
            </el-select>

            <el-input
              v-if="requiresSchoolId"
              v-model="loginForm.schoolId"
              size="large"
              clearable
              :placeholder="TEXT.schoolIdPlaceholder"
            >
              <template #prefix>
                <el-icon><School /></el-icon>
              </template>
            </el-input>

            <el-input
              v-model="loginForm.account"
              size="large"
              clearable
              :placeholder="loginAccountPlaceholder"
              @keyup.enter="submitLogin"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>

            <el-input
              v-model="loginForm.password"
              size="large"
              show-password
              type="password"
              :placeholder="TEXT.passwordPlaceholder"
              @keyup.enter="submitLogin"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="immersive-auth-meta">
            <el-checkbox v-model="rememberMe">{{ TEXT.remember }}</el-checkbox>
            <button type="button" class="immersive-auth-text-button" @click="forgotPassword">{{ TEXT.forgotPassword }}</button>
          </div>

          <el-button
            class="immersive-auth-submit"
            type="primary"
            size="large"
            :loading="loading"
            @click="submitLogin"
          >
            <span>{{ TEXT.login }}</span>
            <el-icon><ArrowRight /></el-icon>
          </el-button>

          <p class="immersive-auth-agreement">{{ TEXT.loginAgreement }}</p>
        </div>

        <div v-else class="immersive-auth-form">
          <div class="immersive-auth-inputs">
            <div class="immersive-auth-register-grid">
              <el-input v-model="registerForm.name" size="large" :placeholder="TEXT.registerName">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>

              <el-input v-model="registerForm.school" size="large" :placeholder="TEXT.registerSchool">
                <template #prefix>
                  <el-icon><School /></el-icon>
                </template>
              </el-input>
            </div>

            <el-select v-model="registerForm.role" size="large" :placeholder="TEXT.selectRole">
              <el-option
                v-for="item in registerRoleOptions"
                :key="item.role"
                :label="item.label"
                :value="item.role"
              />
            </el-select>

            <el-input
              v-model="registerForm.account"
              size="large"
              clearable
              :placeholder="registerAccountPlaceholder"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>

            <p class="immersive-auth-field-hint">{{ registerAccountHint }}</p>

            <div class="immersive-auth-register-grid">
              <el-input
                v-model="registerForm.password"
                size="large"
                show-password
                type="password"
                :placeholder="TEXT.setPassword"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>

              <el-input
                v-model="registerForm.confirmPassword"
                size="large"
                show-password
                type="password"
                :placeholder="TEXT.confirmPassword"
                @keyup.enter="submitRegister"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </div>
          </div>

          <el-button class="immersive-auth-submit" type="primary" size="large" :loading="loading" @click="submitRegister">
            {{ TEXT.register }}
          </el-button>

          <p class="immersive-auth-agreement">{{ TEXT.registerAgreement }}</p>
        </div>

        <div class="immersive-auth-footer">
          <template v-if="activePanel === 'login'">
            <span>{{ TEXT.noAccount }}</span>
            <button type="button" class="immersive-auth-text-button" @click="switchPanel('register')">
              {{ TEXT.goRegister }}
            </button>
          </template>
          <template v-else>
            <span>{{ TEXT.hasAccount }}</span>
            <button type="button" class="immersive-auth-text-button" @click="switchPanel('login')">
              {{ TEXT.backLogin }}
            </button>
          </template>
        </div>
      </section>
    </div>
  </div>
</template>
