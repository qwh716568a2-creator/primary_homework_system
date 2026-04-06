<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  DataAnalysis,
  Document,
  EditPen,
  HomeFilled,
  Menu,
  Promotion,
  SwitchButton
} from '@element-plus/icons-vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { roleMeta } from '@/utils/auth-session'

const router = useRouter()
const route = useRoute()
const store = useTeacherPortalStore()
const mobileNavOpen = ref(false)

const menuItems = [
  {
    label: '工作台',
    caption: '今日待办与快捷入口',
    to: '/dashboard',
    icon: HomeFilled
  },
  {
    label: '作业列表',
    caption: '筛选、查看与催交',
    to: '/assignments',
    icon: Document
  },
  {
    label: '发布作业',
    caption: '创建标准化任务',
    to: '/assignments/new',
    icon: EditPen
  },
  {
    label: '批改中心',
    caption: '进入最近批改任务',
    to: '/assignments',
    icon: Promotion
  },
  {
    label: '统计分析',
    caption: '班级执行效果',
    to: '/statistics',
    icon: DataAnalysis
  }
]

const currentTitle = computed(() =>
  typeof route.meta.title === 'string' ? route.meta.title : '教师作业工作台'
)

function closeMobileNav() {
  mobileNavOpen.value = false
}

function logout() {
  store.setAuthenticatedUser(null)
  router.push('/login')
}
</script>

<template>
  <div class="teacher-shell">
    <aside class="shell-sidebar" :class="{ 'shell-sidebar-open': mobileNavOpen }">
      <div class="brand-panel">
        <div class="brand-badge">T</div>
        <div>
          <strong>Teacher Portal</strong>
          <p>{{ store.teacher.school }}</p>
        </div>
      </div>

      <nav class="shell-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.label"
          :to="item.to"
          class="nav-item"
          active-class="nav-item-active"
          @click="closeMobileNav"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>
            <strong>{{ item.label }}</strong>
            <small>{{ item.caption }}</small>
          </span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <p class="sidebar-eyebrow">当前提醒</p>
        <strong>{{ store.reviewQueue.length }} 条待处理批改</strong>
        <p>建议先处理近 24 小时内的提交，避免学生等待反馈过久。</p>
      </div>
    </aside>

    <div class="shell-main">
      <header class="shell-topbar surface-card">
        <div class="topbar-left">
          <el-button class="mobile-toggle" circle @click="mobileNavOpen = !mobileNavOpen">
            <el-icon><Menu /></el-icon>
          </el-button>
          <div>
            <p class="page-kicker">小学课后作业系统 · 教师端</p>
            <h1>{{ currentTitle }}</h1>
          </div>
        </div>

        <div class="topbar-right">
          <el-button round type="primary" @click="router.push('/assignments/new')">发布作业</el-button>
          <div class="teacher-chip">
            <span class="teacher-avatar">{{ store.teacher.avatar }}</span>
            <div>
              <strong>{{ store.authUser?.name || store.teacher.name }}</strong>
              <small>
                {{ store.authUser ? `${roleMeta[store.authUser.role].label} · ${store.authUser.school}` : store.teacher.role }}
              </small>
            </div>
          </div>
          <el-button circle @click="logout">
            <el-icon><SwitchButton /></el-icon>
          </el-button>
        </div>
      </header>

      <main class="shell-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.teacher-shell {
  display: grid;
  grid-template-columns: 292px minmax(0, 1fr);
  min-height: 100vh;
  align-items: start;
}

.shell-sidebar {
  position: sticky;
  top: 0;
  z-index: 12;
  display: flex;
  flex-direction: column;
  gap: 1.4rem;
  padding: 1.5rem;
  height: 100vh;
  overflow-y: auto;
  background:
    linear-gradient(180deg, rgba(16, 47, 73, 0.98), rgba(17, 63, 95, 0.98)),
    radial-gradient(circle at top, rgba(100, 215, 226, 0.3), transparent 45%);
  color: white;
}

.brand-panel {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  padding: 0.4rem 0.2rem 1rem;
}

.brand-badge {
  width: 48px;
  height: 48px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  font-size: 1.3rem;
  font-weight: 800;
  color: #123553;
  background: linear-gradient(135deg, #b7f8ff, #58d0ff);
}

.brand-panel strong {
  display: block;
  font-size: 1.15rem;
}

.brand-panel p {
  margin: 0.18rem 0 0;
  color: rgba(255, 255, 255, 0.72);
  font-size: 0.88rem;
}

.shell-nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.nav-item {
  display: grid;
  grid-template-columns: 22px 1fr;
  gap: 0.9rem;
  align-items: start;
  padding: 0.95rem 1rem;
  border-radius: 18px;
  color: rgba(255, 255, 255, 0.82);
  transition:
    background 0.2s ease,
    transform 0.2s ease,
    color 0.2s ease;
}

.nav-item strong,
.nav-item small {
  display: block;
}

.nav-item strong {
  font-size: 0.98rem;
}

.nav-item small {
  margin-top: 0.22rem;
  color: rgba(255, 255, 255, 0.65);
  font-size: 0.8rem;
}

.nav-item:hover,
.nav-item-active {
  color: white;
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(2px);
}

.sidebar-footer {
  margin-top: auto;
  padding: 1.25rem;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.09);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.sidebar-eyebrow {
  margin: 0 0 0.45rem;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.8rem;
}

.sidebar-footer strong {
  font-size: 1.25rem;
}

.sidebar-footer p:last-child {
  margin: 0.5rem 0 0;
  color: rgba(255, 255, 255, 0.75);
  line-height: 1.6;
}

.shell-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 100vh;
  padding: 1.4rem;
}

.shell-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.2rem;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 0.9rem;
}

.mobile-toggle {
  display: none;
}

.page-kicker {
  margin: 0;
  font-size: 0.8rem;
  color: #6e8093;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.topbar-left h1 {
  margin: 0.2rem 0 0;
  font-size: 1.6rem;
  color: #17324d;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.teacher-chip {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.5rem 0.8rem;
  border-radius: 16px;
  background: #f2f8fc;
}

.teacher-avatar {
  width: 38px;
  height: 38px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: #133d63;
  background: linear-gradient(135deg, #d5f9ff, #89d1ff);
}

.teacher-chip strong,
.teacher-chip small {
  display: block;
}

.teacher-chip small {
  margin-top: 0.1rem;
  color: #6b7f94;
}

.shell-content {
  min-width: 0;
  padding-top: 1.25rem;
}

@media (max-width: 1100px) {
  .teacher-shell {
    grid-template-columns: 1fr;
  }

  .shell-sidebar {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    width: min(320px, 88vw);
    transform: translateX(-105%);
    transition: transform 0.24s ease;
  }

  .shell-sidebar-open {
    transform: translateX(0);
  }

  .mobile-toggle {
    display: inline-flex;
  }
}

@media (max-width: 760px) {
  .shell-main {
    padding: 1rem;
  }

  .shell-topbar {
    flex-direction: column;
    align-items: stretch;
  }

  .topbar-right {
    justify-content: space-between;
    flex-wrap: wrap;
  }
}
</style>
