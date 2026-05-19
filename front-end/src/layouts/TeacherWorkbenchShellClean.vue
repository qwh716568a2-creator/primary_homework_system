<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Bell,
  DataAnalysis,
  Document,
  EditPen,
  HomeFilled,
  Management,
  Menu,
  Promotion,
  SwitchButton
} from '@element-plus/icons-vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { roleMeta } from '@/utils/auth-session-clean'
import '@/styles/teacher-workspace.css'

const router = useRouter()
const route = useRoute()
const store = useTeacherPortalStore()
const mobileNavOpen = ref(false)

const menuItems = [
  { label: '总览', helper: '今天任务与节奏', to: '/dashboard', icon: HomeFilled },
  { label: '作业台账', helper: '查看、推进、筛选', to: '/assignments', icon: Document },
  { label: '新建作业', helper: '统一下发到班级', to: '/assignments/new', icon: EditPen },
  { label: '批改中心', helper: '沉浸式处理提交', to: '/grading-center', icon: Promotion },
  { label: '消息中心', helper: '触达与记录', to: '/messages', icon: Bell },
  { label: '班级管理', helper: '任教关系与资源', to: '/class-management', icon: Management },
  { label: '统计分析', helper: '执行表现与复盘', to: '/statistics', icon: DataAnalysis }
]

const currentItem = computed(() =>
  menuItems.find((item) => route.path === item.to || route.path.startsWith(`${item.to}/`))
)

const currentTitle = computed(
  () => (typeof route.meta.title === 'string' ? route.meta.title : currentItem.value?.label || '教师工作台')
)

const currentSubtitle = computed(
  () => currentItem.value?.helper || '统一查看作业、批改、消息与班级事务'
)

const teacherName = computed(() => store.authUser?.name || store.teacher?.name || '教师')

const teacherMeta = computed(() => {
  if (!store.authUser) {
    return '教师账号'
  }

  return `${roleMeta[store.authUser.role]?.label || '教师'} · ${store.authUser.school}`
})

function closeMobileNav() {
  mobileNavOpen.value = false
}

function logout() {
  store.setAuthenticatedUser(null)
  void router.push('/login')
}
</script>

<template>
  <div class="teacher-shell-next">
    <aside class="teacher-shell-next__sidebar" :class="{ 'teacher-shell-next__sidebar--open': mobileNavOpen }">
      <div class="teacher-shell-next__brand">
        <img class="teacher-shell-next__logo" src="/system-logo.png" alt="小课后 Logo" />
        <div>
          <strong>小课后</strong>
          <span>Teacher Suite</span>
        </div>
      </div>

      <nav class="teacher-shell-next__nav">
        <router-link
          v-for="item in menuItems"
          :key="item.to"
          :to="item.to"
          class="teacher-shell-next__nav-item"
          active-class="teacher-shell-next__nav-item--active"
          @click="closeMobileNav"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <div>
            <strong>{{ item.label }}</strong>
            <span>{{ item.helper }}</span>
          </div>
        </router-link>
      </nav>

      <div class="teacher-shell-next__sidebar-foot">
        <div class="teacher-shell-next__teacher">
          <span>{{ teacherName.slice(0, 1) }}</span>
          <div>
            <strong>{{ teacherName }}</strong>
            <small>{{ teacherMeta }}</small>
          </div>
        </div>
      </div>
    </aside>

    <section class="teacher-shell-next__stage">
      <header class="teacher-shell-next__topbar">
        <button
          type="button"
          class="teacher-shell-next__icon-btn teacher-shell-next__icon-btn--mobile"
          @click="mobileNavOpen = !mobileNavOpen"
        >
          <el-icon><Menu /></el-icon>
        </button>

        <div class="teacher-shell-next__context">
          <span>{{ currentSubtitle }}</span>
          <h1>{{ currentTitle }}</h1>
        </div>

        <div class="teacher-shell-next__topbar-actions">
          <button type="button" class="teacher-button" @click="router.push('/assignments/new')">新建作业</button>
          <button type="button" class="teacher-shell-next__icon-btn" @click="logout">
            <el-icon><SwitchButton /></el-icon>
          </button>
        </div>
      </header>

      <main class="teacher-shell-next__content">
        <router-view />
      </main>
    </section>
  </div>
</template>
