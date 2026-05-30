<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Bell, CollectionTag, House, Menu, Reading, SwitchButton, User } from '@element-plus/icons-vue'
import { useStudentPortalStore } from '@/stores/studentPortal'

const router = useRouter()
const route = useRoute()
const store = useStudentPortalStore()
const mobileNavOpen = ref(false)

const menuItems = [
  {
    label: '学习台',
    caption: '查看学习概览与提醒',
    to: '/student/home',
    icon: House
  },
  {
    label: '我的作业',
    caption: '查看作业清单与状态',
    to: '/student/homeworks',
    icon: Reading
  },
  {
    label: '错题本',
    caption: '智能练习与订正记录',
    to: '/student/wrong-book',
    icon: CollectionTag
  },
  {
    label: '消息',
    caption: '老师反馈和提醒通知',
    to: '/student/messages',
    icon: Bell
  },
  {
    label: '我的',
    caption: '账号与学习概览',
    to: '/student/profile',
    icon: User
  }
]

const currentTitle = computed(() =>
  typeof route.meta.title === 'string' ? route.meta.title : '学生学习台'
)

function closeMobileNav() {
  mobileNavOpen.value = false
}

function logout() {
  store.setAuthenticatedUser(null)
  store.resetState()
  void router.push('/login')
}

onMounted(() => {
  if (store.isAuthenticated) {
    void store.initializeWorkspace().catch(() => undefined)
  }
})
</script>

<template>
  <div class="student-shell">
    <aside class="student-sidebar" :class="{ 'student-sidebar-open': mobileNavOpen }">
      <div class="student-brand">
        <img class="student-brand__logo" src="/system-logo.png" alt="小学课后作业系统 Logo" />
        <div>
          <strong>Student Space</strong>
          <p>{{ store.profile.school }}</p>
        </div>
      </div>

      <div class="student-identity surface-card">
        <span class="student-identity__eyebrow">今日学习</span>
        <strong>{{ store.profile.name }}</strong>
        <p>{{ store.profile.headline }}</p>
      </div>

      <nav class="student-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.to"
          :to="item.to"
          class="student-nav__item"
          active-class="student-nav__item-active"
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

    <div class="student-main">
      <header class="student-topbar surface-card">
        <div class="student-topbar__left">
          <el-button class="student-topbar__toggle" circle @click="mobileNavOpen = !mobileNavOpen">
            <el-icon><Menu /></el-icon>
          </el-button>
          <div>
            <p class="student-topbar__kicker">小学课后作业系统 · 学生端</p>
            <h1>{{ currentTitle }}</h1>
          </div>
        </div>

        <div class="student-topbar__right">
          <div class="student-chip">
            <span class="student-chip__badge">{{ store.profile.name.slice(0, 1) }}</span>
            <div>
              <strong>{{ store.profile.name }}</strong>
              <small>{{ store.profile.account }}</small>
            </div>
          </div>
          <el-button circle @click="logout" title="退出登录">
            <el-icon><SwitchButton /></el-icon>
          </el-button>
        </div>
      </header>

      <main class="student-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.student-shell {
  display: flex;
  min-height: 100vh;
  align-items: flex-start;
  gap: 18px;
  padding: 18px;
}

.student-sidebar {
  position: sticky;
  top: 18px;
  z-index: 12;
  display: flex;
  flex-direction: column;
  gap: 18px;
  flex-shrink: 0;
  width: 238px;
  height: calc(100vh - 36px);
  padding: 18px 16px;
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(12, 34, 62, 0.94), rgba(24, 67, 116, 0.88)),
    radial-gradient(circle at top right, rgba(120, 227, 255, 0.16), transparent 42%);
  color: white;
  box-shadow: 0 28px 56px -20px rgba(15, 23, 42, 0.34);
}

.student-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 4px 12px;
}

.student-brand__logo {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  object-fit: cover;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14px 28px rgba(83, 180, 255, 0.18);
}

.student-brand strong {
  display: block;
  font-size: 15px;
  line-height: 1.2;
}

.student-brand p {
  margin: 4px 0 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}

.student-identity {
  padding: 1rem 1.05rem;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.12);
  color: white;
}

.student-identity__eyebrow {
  display: inline-flex;
  padding: 0.28rem 0.7rem;
  border-radius: 999px;
  background: rgba(143, 220, 255, 0.12);
  color: #b9ebff;
  font-size: 0.78rem;
  font-weight: 700;
}

.student-identity strong {
  display: block;
  margin-top: 0.8rem;
  font-size: 1.12rem;
}

.student-identity p {
  margin: 0.4rem 0 0;
  color: rgba(255, 255, 255, 0.72);
  line-height: 1.7;
  font-size: 0.9rem;
}

.student-nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.student-nav__item {
  display: grid;
  grid-template-columns: 18px 1fr;
  gap: 12px;
  align-items: start;
  padding: 13px 14px;
  border-radius: 18px;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.2s ease;
}

.student-nav__item strong,
.student-nav__item small {
  display: block;
}

.student-nav__item strong {
  font-size: 15px;
}

.student-nav__item small {
  margin-top: 3px;
  color: rgba(255, 255, 255, 0.63);
  font-size: 12px;
  line-height: 1.45;
}

.student-nav__item:hover,
.student-nav__item-active {
  color: white;
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.92), rgba(45, 212, 191, 0.78));
  box-shadow: 0 12px 24px -10px rgba(45, 212, 191, 0.36);
  transform: translateX(4px);
}

.student-main {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
  min-height: calc(100vh - 36px);
}

.student-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.4rem;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.82);
}

.student-topbar__left,
.student-topbar__right {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.student-topbar__toggle {
  display: none;
}

.student-topbar__kicker {
  margin: 0;
  font-size: 0.85rem;
  color: #6d7f92;
}

.student-topbar h1 {
  margin: 0.2rem 0 0;
  font-size: 2.1rem;
  line-height: 1.05;
  color: #13253d;
}

.student-chip {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.48rem 0.85rem;
  border-radius: 18px;
  background: rgba(240, 248, 255, 0.9);
}

.student-chip__badge {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 14px;
  font-weight: 800;
  color: #174272;
  background: linear-gradient(135deg, #bfe4ff, #7dd3fc);
}

.student-chip strong,
.student-chip small {
  display: block;
}

.student-chip small {
  color: #6d7f92;
}

.student-content {
  flex: 1;
  min-width: 0;
  padding: 1.35rem 0 0;
}

@media (max-width: 1120px) {
  .student-shell {
    padding: 14px;
  }

  .student-sidebar {
    position: fixed;
    top: 14px;
    left: 14px;
    bottom: 14px;
    height: auto;
    transform: translateX(calc(-100% - 14px));
    transition: transform 0.2s ease;
  }

  .student-sidebar-open {
    transform: translateX(0);
  }

  .student-topbar__toggle {
    display: inline-flex;
  }
}

@media (max-width: 760px) {
  .student-topbar {
    flex-direction: column;
    align-items: stretch;
  }

  .student-topbar__right {
    justify-content: space-between;
  }

  .student-topbar h1 {
    font-size: 1.7rem;
  }
}
</style>
