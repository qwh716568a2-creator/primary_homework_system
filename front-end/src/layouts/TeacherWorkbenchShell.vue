<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
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
    caption: '创建并下发到多个班级',
    to: '/assignments/new',
    icon: EditPen
  },
  {
    label: '批改中心',
    caption: '集中处理学生提交与订正',
    to: '/grading-center',
    icon: Promotion
  },
  {
    label: '班级管理',
    caption: '绑定新的任教班级与学科',
    to: '/class-management',
    icon: Management
  },
  {
    label: '统计分析',
    caption: '查看班级执行情况',
    to: '/statistics',
    icon: DataAnalysis
  }
]

const currentTitle = computed(() =>
  typeof route.meta.title === 'string' ? route.meta.title : '教师工作台'
)

function closeMobileNav() {
  mobileNavOpen.value = false
}

function logout() {
  store.setAuthenticatedUser(null)
  void router.push('/login')
}
</script>

<template>
  <div class="teacher-shell">
    <aside class="shell-sidebar" :class="{ 'shell-sidebar-open': mobileNavOpen }">
      <div class="brand-panel">
        <div class="brand-badge">T</div>
        <div>
          <strong>Teacher Portal</strong>
          <p>{{ store.teacher?.school || '校内统一认证' }}</p>
        </div>
      </div>

      <nav class="shell-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.to"
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
            <span class="teacher-avatar">{{ store.teacher?.avatar || '师' }}</span>
            <div>
              <strong>{{ store.authUser?.name || store.teacher?.name || '教师' }}</strong>
              <small>
                {{
                  store.authUser
                    ? `${roleMeta[store.authUser.role]?.label || '教师'} · ${store.authUser.school}`
                    : store.teacher?.role
                }}
              </small>
            </div>
          </div>
          <el-button circle @click="logout" title="退出登录">
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
  display: flex;
  min-height: 100vh;
  align-items: flex-start;
  gap: 1.5rem;
  padding: 1.5rem;
}

.shell-sidebar {
  position: sticky;
  flex-shrink: 0;
  top: 1.5rem;
  z-index: 12;
  display: flex;
  flex-direction: column;
  gap: 1.4rem;
  padding: 1.5rem;
  height: calc(100vh - 3rem);
  width: 290px;
  border-radius: 32px;
  overflow: hidden;
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 24px 48px -12px rgba(15, 23, 42, 0.3);
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
  transition: all 0.2s ease;
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
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.85), rgba(79, 70, 229, 0.95));
  box-shadow: 0 10px 20px -8px rgba(99, 102, 241, 0.5);
  transform: translateX(4px);
}

.shell-main {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
  min-height: calc(100vh - 3rem);
}

.shell-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.4rem;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 10px 30px -10px rgba(15, 23, 42, 0.05);
  margin-bottom: 1.5rem;
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
  flex: 1;
}

@media (max-width: 1100px) {
  .teacher-shell {
    padding: 1rem;
  }

  .shell-sidebar {
    position: fixed;
    top: 1rem;
    left: 1rem;
    bottom: 1rem;
    height: calc(100vh - 2rem);
    width: min(300px, 88vw);
    transform: translateX(-120%);
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .shell-sidebar-open {
    transform: translateX(0);
  }

  .mobile-toggle {
    display: inline-flex;
  }
}

@media (max-width: 760px) {
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
