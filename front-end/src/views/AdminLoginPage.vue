<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Monitor, User } from '@element-plus/icons-vue'
import { loginByPassword } from '@/api/auth-service'
import { useAdminPortalStore } from '@/stores/adminPortal'
import { getDefaultRouteForRole } from '@/utils/auth-session'

const router = useRouter()
const store = useAdminPortalStore()

const loading = ref(false)
const rememberMe = ref(true)

const loginForm = reactive({
  account: '',
  password: ''
})

async function submitLogin() {
  if (!loginForm.account.trim() || !loginForm.password.trim()) {
    ElMessage.warning('请输入管理员账号和密码')
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
    ElMessage.success(`欢迎回来，${session.name}`)
    await router.push(getDefaultRouteForRole(session.role))
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '管理员登录失败，请稍后再试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="admin-auth">
    <div class="admin-auth__panel surface-card">
      <div class="admin-auth__brand">
        <span>Admin Console</span>
        <el-icon><Monitor /></el-icon>
      </div>

      <div class="admin-auth__header">
        <h1>管理员登录</h1>
        <p>进入学校组织、账号和关系配置工作台。</p>
      </div>

      <div class="admin-auth__form">
        <el-input
          v-model="loginForm.account"
          size="large"
          clearable
          placeholder="请输入管理员账号"
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
          placeholder="请输入登录密码"
          @keyup.enter="submitLogin"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="admin-auth__meta">
        <el-checkbox v-model="rememberMe">记住登录状态</el-checkbox>
        <router-link to="/login">返回统一登录</router-link>
      </div>

      <el-button class="admin-auth__submit" type="primary" size="large" :loading="loading" @click="submitLogin">
        登录
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
