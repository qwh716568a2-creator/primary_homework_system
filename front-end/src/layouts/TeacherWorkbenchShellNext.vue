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

const router = useRouter()
const route = useRoute()
const store = useTeacherPortalStore()
const mobileNavOpen = ref(false)

const menuItems = [
  {
    label: '工作台',
    caption: '今日节奏与核心入口',
    to: '/dashboard',
    icon: HomeFilled
  },
  {
    label: '作业列表',
    caption: '查看、推进、筛选',
    to: '/assignments',
    icon: Document
  },
  {
    label: '发布作业',
    caption: '统一下发到班级',
    to: '/assignments/new',
    icon: EditPen
  },
  {
    label: '批改中心',
    caption: '集中处理提交与订正',
    to: '/grading-center',
    icon: Promotion
  },
  {
    label: '消息中心',
    caption: '通知触达与记录',
    to: '/messages',
    icon: Bell
  },
  {
    label: '班级管理',
    caption: '任教关系与资源',
    to: '/class-management',
    icon: Management
  },
  {
    label: '统计分析',
    caption: '执行表现与复盘',
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
  <div class="teacher-shell-next">
    <aside class="teacher-shell-next__sidebar" :class="{ 'is-open': mobileNavOpen }">
      <div class="teacher-shell-next__brand">
        <img src="/system-logo.png" alt="小课后系统" class="teacher-shell-next__brand-logo" />
        <div>
          <strong>小课后 Teacher Suite</strong>
          <p>{{ store.teacher?.school || '校内统一认证' }}</p>
        </div>
      </div>

      <nav class="teacher-shell-next__nav">
        <router-link
          v-for="item in menuItems"
          :key="item.to"
          :to="item.to"
          class="teacher-shell-next__nav-item"
          active-class="is-active"
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

    <div class="teacher-shell-next__main">
      <header class="teacher-shell-next__topbar">
        <div class="teacher-shell-next__topbar-left">
          <el-button class="teacher-shell-next__menu" circle @click="mobileNavOpen = !mobileNavOpen">
            <el-icon><Menu /></el-icon>
          </el-button>
          <div>
            <p>小学课后作业系统 · 教师端</p>
            <h1>{{ currentTitle }}</h1>
          </div>
        </div>

        <div class="teacher-shell-next__topbar-right">
          <el-button round type="primary" @click="router.push('/assignments/new')">发布作业</el-button>

          <div class="teacher-shell-next__identity">
            <span class="teacher-shell-next__avatar">{{ store.teacher?.avatar || '师' }}</span>
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

      <main class="teacher-shell-next__content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.teacher-shell-next {
  min-height: 100vh;
  display: flex;
  gap: 18px;
  padding: 18px;
  background:
    radial-gradient(circle at top left, rgba(107, 138, 255, 0.16), transparent 24%),
    radial-gradient(circle at top right, rgba(32, 201, 151, 0.08), transparent 18%),
    linear-gradient(180deg, #f4f7fb 0%, #eef3f8 100%);
}

.teacher-shell-next__sidebar {
  width: 238px;
  flex-shrink: 0;
  position: sticky;
  top: 18px;
  height: calc(100vh - 36px);
  padding: 18px 16px;
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(21, 31, 49, 0.98) 0%, rgba(34, 46, 70, 0.97) 100%);
  box-shadow: 0 24px 60px rgba(17, 24, 39, 0.2);
  color: #fff;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.teacher-shell-next__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 4px 12px;
}

.teacher-shell-next__brand-logo {
  width: 44px;
  height: 44px;
  object-fit: cover;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.9);
  padding: 3px;
}

.teacher-shell-next__brand strong {
  display: block;
  font-size: 15px;
  line-height: 1.2;
}

.teacher-shell-next__brand p {
  margin: 4px 0 0;
  font-size: 12px;
  color: rgba(236, 242, 255, 0.72);
}

.teacher-shell-next__nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.teacher-shell-next__nav-item {
  display: grid;
  grid-template-columns: 18px 1fr;
  gap: 12px;
  align-items: start;
  padding: 13px 14px;
  border-radius: 18px;
  color: rgba(239, 244, 255, 0.84);
  transition: transform 0.2s ease, background 0.2s ease, box-shadow 0.2s ease;
}

.teacher-shell-next__nav-item strong,
.teacher-shell-next__nav-item small {
  display: block;
}

.teacher-shell-next__nav-item strong {
  font-size: 15px;
}

.teacher-shell-next__nav-item small {
  margin-top: 3px;
  font-size: 12px;
  line-height: 1.45;
  color: rgba(239, 244, 255, 0.6);
}

.teacher-shell-next__nav-item:hover,
.teacher-shell-next__nav-item.is-active {
  background: linear-gradient(135deg, #5b63ff 0%, #6b8cff 100%);
  color: #fff;
  box-shadow: 0 16px 30px rgba(91, 99, 255, 0.3);
}

.teacher-shell-next__nav-item:hover small,
.teacher-shell-next__nav-item.is-active small {
  color: rgba(255, 255, 255, 0.82);
}

.teacher-shell-next__main {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.teacher-shell-next__topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 18px 28px;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(215, 226, 242, 0.8);
  box-shadow: 0 12px 32px rgba(24, 39, 75, 0.08);
  backdrop-filter: blur(18px);
  margin-bottom: 20px;
}

.teacher-shell-next__topbar-left,
.teacher-shell-next__topbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.teacher-shell-next__topbar-left p {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #7d8ea7;
}

.teacher-shell-next__topbar-left h1 {
  margin: 6px 0 0;
  font-size: 28px;
  line-height: 1;
  color: #13253d;
}

.teacher-shell-next__identity {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 18px;
  background: linear-gradient(180deg, #f5fbff 0%, #eef5fb 100%);
  border: 1px solid #dce9f7;
}

.teacher-shell-next__avatar {
  width: 40px;
  height: 40px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: #15385d;
  background: linear-gradient(135deg, #d7f8ff 0%, #8bc8ff 100%);
}

.teacher-shell-next__identity strong,
.teacher-shell-next__identity small {
  display: block;
}

.teacher-shell-next__identity strong {
  font-size: 15px;
  color: #13253d;
}

.teacher-shell-next__identity small {
  margin-top: 3px;
  font-size: 12px;
  color: #7b8ca2;
}

.teacher-shell-next__menu {
  display: none;
}

.teacher-shell-next__content {
  min-width: 0;
  flex: 1;
}

@media (max-width: 1180px) {
  .teacher-shell-next {
    padding: 14px;
  }

  .teacher-shell-next__sidebar {
    position: fixed;
    left: 14px;
    top: 14px;
    transform: translateX(-120%);
    transition: transform 0.25s ease;
    z-index: 20;
  }

  .teacher-shell-next__sidebar.is-open {
    transform: translateX(0);
  }

  .teacher-shell-next__menu {
    display: inline-flex;
  }
}

@media (max-width: 780px) {
  .teacher-shell-next__topbar {
    padding: 16px 18px;
    flex-direction: column;
    align-items: stretch;
  }

  .teacher-shell-next__topbar-right {
    justify-content: space-between;
    flex-wrap: wrap;
  }
}
</style>
