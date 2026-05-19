<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Bell, House, Menu, SwitchButton, User } from '@element-plus/icons-vue'
import { useParentPortalStore } from '@/stores/parentPortal'

const router = useRouter()
const route = useRoute()
const store = useParentPortalStore()
const mobileNavOpen = ref(false)

const menuItems = [
  {
    label: '首页',
    caption: '作业与提醒',
    to: '/parent/home',
    icon: House
  },
  {
    label: '消息',
    caption: '反馈与通知',
    to: '/parent/messages',
    icon: Bell
  },
  {
    label: '我的',
    caption: '账号与孩子',
    to: '/parent/profile',
    icon: User
  }
]

const currentTitle = computed(() => (typeof route.meta.title === 'string' ? route.meta.title : '家长端'))

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
  <div class="parent-shell">
    <aside class="parent-sidebar" :class="{ 'parent-sidebar-open': mobileNavOpen }">
      <div class="parent-brand">
        <img class="parent-brand__logo" src="/system-logo.png" alt="小课后" />
        <div>
          <strong>Parent Space</strong>
          <p>{{ store.profile.school }}</p>
        </div>
      </div>

      <div class="parent-identity">
        <span class="parent-identity__eyebrow">今日跟进</span>
        <strong>{{ store.profile.name }}</strong>
        <p>{{ store.pendingCount }} 项待完成，{{ store.unreadMessageCount }} 条未读消息。</p>
      </div>

      <nav class="parent-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.label"
          :to="item.to"
          class="parent-nav__item"
          active-class="parent-nav__item-active"
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

    <div class="parent-main">
      <header class="parent-topbar surface-card">
        <div class="parent-topbar__left">
          <el-button class="parent-topbar__toggle" circle @click="mobileNavOpen = !mobileNavOpen">
            <el-icon><Menu /></el-icon>
          </el-button>
          <div>
            <p class="parent-topbar__kicker">小学课后作业系统 · 家长端</p>
            <h1>{{ currentTitle }}</h1>
          </div>
        </div>

        <div class="parent-topbar__right">
          <div class="parent-chip">
            <span class="parent-chip__badge">{{ store.profile.name.slice(0, 1) }}</span>
            <div>
              <strong>{{ store.profile.name }}</strong>
              <small>{{ store.profile.account }}</small>
            </div>
          </div>
          <el-button circle title="退出登录" @click="logout">
            <el-icon><SwitchButton /></el-icon>
          </el-button>
        </div>
      </header>

      <main class="parent-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.parent-shell {
  display: flex;
  min-height: 100vh;
  gap: 18px;
  padding: 18px;
  background:
    radial-gradient(circle at top left, rgba(96, 165, 250, 0.18), transparent 32%),
    radial-gradient(circle at bottom right, rgba(32, 191, 166, 0.14), transparent 34%),
    #f3f7fb;
}

.parent-sidebar {
  position: sticky;
  top: 18px;
  z-index: 12;
  display: flex;
  flex-direction: column;
  gap: 18px;
  width: 238px;
  height: calc(100vh - 36px);
  flex-shrink: 0;
  padding: 18px 16px;
  border-radius: 28px;
  color: white;
  background:
    linear-gradient(180deg, rgba(16, 40, 70, 0.98), rgba(50, 96, 142, 0.94)),
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.14), transparent 36%);
  box-shadow: 0 28px 56px -20px rgba(18, 39, 72, 0.35);
}

.parent-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 4px 12px;
}

.parent-brand__logo {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  object-fit: cover;
  background: rgba(255, 255, 255, 0.96);
}

.parent-brand strong {
  display: block;
  font-size: 15px;
  line-height: 1.2;
}

.parent-brand p {
  margin: 4px 0 0;
  color: rgba(255, 255, 255, 0.74);
  font-size: 12px;
}

.parent-identity {
  padding: 1rem 1.05rem;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.1);
}

.parent-identity__eyebrow {
  display: inline-flex;
  padding: 0.3rem 0.72rem;
  border-radius: 999px;
  background: rgba(190, 234, 255, 0.18);
  color: #dff5ff;
  font-size: 0.78rem;
  font-weight: 700;
}

.parent-identity strong {
  display: block;
  margin-top: 0.8rem;
  font-size: 1.12rem;
}

.parent-identity p {
  margin: 0.4rem 0 0;
  color: rgba(255, 255, 255, 0.76);
  line-height: 1.7;
  font-size: 0.9rem;
}

.parent-nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.parent-nav__item {
  display: grid;
  grid-template-columns: 18px 1fr;
  gap: 12px;
  padding: 13px 14px;
  border-radius: 18px;
  color: rgba(255, 255, 255, 0.84);
  transition: all 0.2s ease;
}

.parent-nav__item strong,
.parent-nav__item small {
  display: block;
}

.parent-nav__item strong {
  font-size: 15px;
}

.parent-nav__item small {
  margin-top: 3px;
  color: rgba(255, 255, 255, 0.68);
  font-size: 12px;
  line-height: 1.45;
}

.parent-nav__item:hover,
.parent-nav__item-active {
  color: white;
  background: linear-gradient(90deg, rgba(47, 124, 255, 0.95), rgba(32, 191, 166, 0.78));
  box-shadow: 0 12px 24px -10px rgba(56, 189, 248, 0.38);
  transform: translateX(4px);
}

.parent-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.parent-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.4rem;
  border-radius: 28px;
}

.parent-topbar__left,
.parent-topbar__right {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.parent-topbar__toggle {
  display: none;
}

.parent-topbar__kicker {
  margin: 0;
  font-size: 0.85rem;
  color: #6d7f92;
}

.parent-topbar h1 {
  margin: 0.2rem 0 0;
  font-size: 2.1rem;
  line-height: 1.05;
  color: #13253d;
}

.parent-chip {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.48rem 0.85rem;
  border-radius: 18px;
  background: rgba(239, 248, 255, 0.96);
}

.parent-chip__badge {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 14px;
  font-weight: 800;
  color: #075985;
  background: linear-gradient(135deg, #bae6fd, #99f6e4);
}

.parent-chip strong,
.parent-chip small {
  display: block;
}

.parent-chip small {
  color: #6d7f92;
}

.parent-content {
  flex: 1;
  min-width: 0;
  padding: 1.35rem 0 0;
}

@media (max-width: 1120px) {
  .parent-shell {
    padding: 14px;
  }

  .parent-sidebar {
    position: fixed;
    top: 14px;
    left: 14px;
    bottom: 14px;
    height: auto;
    transform: translateX(calc(-100% - 14px));
    transition: transform 0.2s ease;
  }

  .parent-sidebar-open {
    transform: translateX(0);
  }

  .parent-topbar__toggle {
    display: inline-flex;
  }
}

@media (max-width: 760px) {
  .parent-topbar {
    flex-direction: column;
    align-items: stretch;
  }

  .parent-topbar__right {
    justify-content: space-between;
  }

  .parent-topbar h1 {
    font-size: 1.7rem;
  }
}
</style>
