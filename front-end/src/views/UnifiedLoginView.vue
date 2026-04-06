<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, Lock, School, User } from '@element-plus/icons-vue'
import { inferLoginClientType, loginByPassword, registerAccount } from '@/api/auth'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { getDefaultRouteForRole, roleMeta } from '@/utils/auth-session'

type SelfRegisterRole = 'student' | 'parent'

const router = useRouter()
const store = useTeacherPortalStore()

const loading = ref(false)
const activePanel = ref<'login' | 'register'>('login')
const loginError = ref('')
const rememberMe = ref(true)

const loginForm = reactive({
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
  (['student', 'parent'] as SelfRegisterRole[]).map((role) => ({
    role,
    label: roleMeta[role].label
  }))
)

const loginAccountPlaceholder = computed(() => {
  const inferredType = inferLoginClientType(loginForm.account)

  if (inferredType === 'teacher-web') {
    return '请输入手机号'
  }

  if (inferredType === 'admin-web') {
    return '请输入管理员账号'
  }

  return '学生请输入学号，教师/家长请输入手机号'
})

const registerAccountPlaceholder = computed(() =>
  registerForm.role === 'student' ? '请输入学号' : '请输入手机号'
)

const registerAccountHint = computed(() =>
  registerForm.role === 'student' ? '学生注册需填写学号并与学校档案一致。' : '家长注册需填写手机号并与学校档案一致。'
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
  ElMessage.info('请联系学校管理员或平台运营人员重置密码。')
}

async function submitLogin() {
  if (!loginForm.account.trim() || !loginForm.password.trim()) {
    loginError.value = '请输入账号和密码后再登录。'
    ElMessage.warning(loginError.value)
    return
  }

  loading.value = true
  loginError.value = ''

  try {
    const session = await loginByPassword({
      account: loginForm.account,
      password: loginForm.password
    })

    store.setAuthenticatedUser(session, { remember: rememberMe.value })
    ElMessage.success(`欢迎回来，${session.name}`)
    await router.push(getDefaultRouteForRole(session.role))
  } catch (error) {
    const message = error instanceof Error ? error.message : '登录失败，请稍后重试。'
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
    ElMessage.warning('请完整填写注册信息。')
    return
  }

  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致，请重新确认。')
    return
  }

  loading.value = true
  loginError.value = ''

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
    resetRegisterForm()
    switchPanel('login')
    ElMessage.success('注册成功，请直接使用账号密码登录。')
  } catch (error) {
    const message = error instanceof Error ? error.message : '注册失败，请稍后重试。'
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
          <span class="immersive-auth-line"></span>
          <p class="immersive-auth-eyebrow">小学课后作业系统</p>
          <h1>让作业发布、批改与家校协同回到同一条线上</h1>
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
              placeholder="请输入登录密码"
              @keyup.enter="submitLogin"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="immersive-auth-meta">
            <el-checkbox v-model="rememberMe">记住登录状态</el-checkbox>
            <button type="button" class="immersive-auth-text-button" @click="forgotPassword">忘记密码？</button>
          </div>

          <el-button
            class="immersive-auth-submit"
            type="primary"
            size="large"
            :loading="loading"
            @click="submitLogin"
          >
            <span>登录</span>
            <el-icon><ArrowRight /></el-icon>
          </el-button>

          <p class="immersive-auth-agreement">登录即表示你同意平台账号安全策略与数据使用规范。</p>
        </div>

        <div v-else class="immersive-auth-form">
          <div class="immersive-auth-inputs">
            <div class="immersive-auth-register-grid">
              <el-input v-model="registerForm.name" size="large" placeholder="姓名">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>

              <el-input v-model="registerForm.school" size="large" placeholder="学校名称">
                <template #prefix>
                  <el-icon><School /></el-icon>
                </template>
              </el-input>
            </div>

            <el-select v-model="registerForm.role" size="large" placeholder="请选择身份">
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
                placeholder="设置密码"
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
                placeholder="确认密码"
                @keyup.enter="submitRegister"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </div>
          </div>

          <el-button class="immersive-auth-submit" type="primary" size="large" :loading="loading" @click="submitRegister">
            注册
          </el-button>

          <p class="immersive-auth-agreement">教师和管理员账号由管理端创建，学生与家长可在此完成实名注册。</p>
        </div>

        <div class="immersive-auth-footer">
          <template v-if="activePanel === 'login'">
            <span>没有账号？</span>
            <button type="button" class="immersive-auth-text-button" @click="switchPanel('register')">
              去注册
            </button>
          </template>
          <template v-else>
            <span>已有账号？</span>
            <button type="button" class="immersive-auth-text-button" @click="switchPanel('login')">
              返回登录
            </button>
          </template>
        </div>
      </section>
    </div>
  </div>
</template>
