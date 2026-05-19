<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, Lock, Setting, User } from '@element-plus/icons-vue'
import { loginByPassword } from '@/api/auth-service'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { getDefaultRouteForRole } from '@/utils/auth-session'

const router = useRouter()
const store = useTeacherPortalStore()

const loading = ref(false)
const loginError = ref('')
const rememberMe = ref(true)

const loginForm = reactive({
  account: '',
  password: ''
})

async function submitLogin() {
  if (!loginForm.account.trim() || !loginForm.password.trim()) {
    loginError.value = '请输入系统管理员账号和密码后再登录。'
    ElMessage.warning(loginError.value)
    return
  }

  loading.value = true
  loginError.value = ''

  try {
    const session = await loginByPassword({
      account: loginForm.account,
      password: loginForm.password,
      loginType: 'admin',
      schoolId: null
    })

    store.setAuthenticatedUser(session, { remember: rememberMe.value })
    ElMessage.success(`欢迎回来，${session.name}`)
    await router.push(getDefaultRouteForRole(session.role))
  } catch (error) {
    const message = error instanceof Error ? error.message : '管理员登录失败，请核对权限或密码。'
    loginError.value = message
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="super-auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <el-icon class="auth-icon"><Setting /></el-icon>
        <h2>系统控制台登录</h2>
        <p>System Administration Login</p>
      </div>

      <el-alert v-if="loginError" :closable="false" type="error" :title="loginError" show-icon />

      <div class="auth-inputs">
        <el-input
          v-model="loginForm.account"
          size="large"
          clearable
          placeholder="请输入系统管理员账号"
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
          placeholder="请输入控制台密码"
          @keyup.enter="submitLogin"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="auth-meta">
        <el-checkbox v-model="rememberMe">保持管理员会话</el-checkbox>
      </div>

      <el-button
        class="auth-submit"
        type="primary"
        color="#0f172a"
        size="large"
        :loading="loading"
        @click="submitLogin"
      >
        <span>进入控制台</span>
        <el-icon><ArrowRight /></el-icon>
      </el-button>

      <p class="auth-agreement">此通道仅供平台内部运维或授权管理员使用。</p>
    </div>
  </div>
</template>

<style scoped>
.super-auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: #0f172a;
  background-image: 
    radial-gradient(circle at 50% 0%, rgba(56, 189, 248, 0.15), transparent 40%),
    radial-gradient(circle at 100% 100%, rgba(99, 102, 241, 0.15), transparent 40%);
  padding: 1.5rem;
}

.auth-card {
  width: 100%;
  max-width: 440px;
  padding: 2.5rem;
  border-radius: 28px;
  background: rgba(30, 41, 59, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.auth-header {
  text-align: center;
  color: white;
}

.auth-icon {
  font-size: 2.5rem;
  color: #38bdf8;
  margin-bottom: 0.5rem;
}

.auth-header h2 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 700;
}

.auth-header p {
  margin: 0.3rem 0 1rem;
  color: #94a3b8;
  font-size: 0.9rem;
  letter-spacing: 0.05em;
}

.auth-inputs {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

:deep(.el-input__wrapper) {
  background: rgba(15, 23, 42, 0.6) !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
  box-shadow: none !important;
}

:deep(.el-input__inner) {
  color: #f8fafc !important;
}

.auth-meta {
  display: flex;
  justify-content: flex-start;
}

:deep(.el-checkbox__label) {
  color: #94a3b8 !important;
}

.auth-submit {
  width: 100%;
  font-weight: 700;
  border-radius: 12px;
  display: flex;
  justify-content: space-between;
  padding: 0 1.5rem;
  height: 3rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.auth-agreement {
  text-align: center;
  font-size: 0.8rem;
  color: #64748b;
  margin: 0;
}
</style>
