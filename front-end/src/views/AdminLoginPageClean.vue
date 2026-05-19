<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Monitor, User } from '@element-plus/icons-vue'
import { loginByPassword } from '@/api/auth-service-clean'
import { useAdminPortalStore } from '@/stores/adminPortal'
import { getDefaultRouteForRole } from '@/utils/auth-session-clean'

const router = useRouter()
const store = useAdminPortalStore()

const loading = ref(false)
const rememberMe = ref(true)

const TEXT = {
  title: '\u7ba1\u7406\u5458\u767b\u5f55',
  subtitle: '\u8fdb\u5165\u5b66\u6821\u7ec4\u7ec7\u3001\u8d26\u53f7\u548c\u5173\u7cfb\u914d\u7f6e\u5de5\u4f5c\u53f0\u3002',
  accountPlaceholder: '\u8bf7\u8f93\u5165\u7ba1\u7406\u5458\u8d26\u53f7',
  passwordPlaceholder: '\u8bf7\u8f93\u5165\u767b\u5f55\u5bc6\u7801',
  remember: '\u8bb0\u4f4f\u767b\u5f55\u72b6\u6001',
  back: '\u8fd4\u56de\u7edf\u4e00\u767b\u5f55',
  login: '\u767b\u5f55',
  emptyWarning: '\u8bf7\u8f93\u5165\u7ba1\u7406\u5458\u8d26\u53f7\u548c\u5bc6\u7801\u3002',
  successPrefix: '\u6b22\u8fce\u56de\u6765\uff0c',
  errorFallback: '\u7ba1\u7406\u5458\u767b\u5f55\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5\u3002'
}

const loginForm = reactive({
  account: '',
  password: ''
})

async function submitLogin() {
  if (!loginForm.account.trim() || !loginForm.password.trim()) {
    ElMessage.warning(TEXT.emptyWarning)
    return
  }

  loading.value = true

  try {
    const session = await loginByPassword({
      account: loginForm.account,
      password: loginForm.password,
      loginType: 'admin',
      schoolId: null
    })

    store.setAuthenticatedUser(session, { remember: rememberMe.value })
    ElMessage.success(`${TEXT.successPrefix}${session.name}`)
    await router.push(getDefaultRouteForRole(session.role))
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : TEXT.errorFallback)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="admin-auth">
    <div class="admin-auth__panel surface-card">
      <div class="admin-auth__brand">
        <img class="admin-auth__brand-logo" src="/system-logo.png" alt="小学课后作业系统 Logo" />
        <span>Admin Console</span>
        <el-icon><Monitor /></el-icon>
      </div>

      <div class="admin-auth__header">
        <h1>{{ TEXT.title }}</h1>
        <p>{{ TEXT.subtitle }}</p>
      </div>

      <div class="admin-auth__form">
        <el-input
          v-model="loginForm.account"
          size="large"
          clearable
          :placeholder="TEXT.accountPlaceholder"
          @keyup.enter="submitLogin"
        >
          <template #prefix>
            <el-icon><User /></el-icon>
          </template>
        </el-input>

        <el-input
          v-model="loginForm.password"
          size="large"
          type="password"
          show-password
          :placeholder="TEXT.passwordPlaceholder"
          @keyup.enter="submitLogin"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="admin-auth__meta">
        <el-checkbox v-model="rememberMe">{{ TEXT.remember }}</el-checkbox>
        <router-link to="/login">{{ TEXT.back }}</router-link>
      </div>

      <el-button class="admin-auth__submit" type="primary" size="large" :loading="loading" @click="submitLogin">
        {{ TEXT.login }}
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.admin-auth {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 1.5rem;
  background:
    radial-gradient(circle at top, rgba(38, 99, 235, 0.18), transparent 35%),
    linear-gradient(135deg, #081225, #102342 42%, #0d1a30);
}

.admin-auth__panel {
  width: min(460px, 100%);
  padding: 2rem;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.88);
}

.admin-auth__brand {
  display: inline-flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.55rem 0.9rem;
  border-radius: 999px;
  color: #1d4ed8;
  font-size: 0.84rem;
  font-weight: 700;
  background: rgba(219, 234, 254, 0.88);
}

.admin-auth__brand-logo {
  width: 34px;
  height: 34px;
  object-fit: contain;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 10px 18px rgba(37, 99, 235, 0.16);
}

.admin-auth__header {
  margin: 1.4rem 0 1.5rem;
}

.admin-auth__header h1 {
  margin: 0;
  color: #0f2744;
  font-size: 2rem;
}

.admin-auth__header p {
  margin: 0.55rem 0 0;
  color: #698095;
}

.admin-auth__form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.admin-auth__meta {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
  margin: 1rem 0 1.4rem;
  color: #6b8093;
  font-size: 0.92rem;
}

.admin-auth__meta a {
  color: #2563eb;
  font-weight: 600;
}

.admin-auth__submit {
  width: 100%;
  height: 3.25rem;
  border-radius: 16px;
  font-weight: 700;
}

@media (max-width: 640px) {
  .admin-auth__panel {
    padding: 1.3rem;
  }

  .admin-auth__meta {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
