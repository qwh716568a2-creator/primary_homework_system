<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Connection,
  DataAnalysis,
  Menu,
  OfficeBuilding,
  SwitchButton,
  UserFilled
} from '@element-plus/icons-vue'
import { useAdminPortalStore } from '@/stores/adminPortal'

const router = useRouter()
const route = useRoute()
const store = useAdminPortalStore()
const mobileNavOpen = ref(false)

const menuItems = [
  {
    label: '数据看板',
    caption: '查看平台整体运行情况',
    to: '/admin/dashboard',
    icon: DataAnalysis
  },
  {
    label: '组织管理',
    caption: '学校与班级组织信息',
    to: '/admin/organization',
    icon: OfficeBuilding
  },
  {
    label: '账号管理',
    caption: '教师、学生、家长与管理员',
    to: '/admin/accounts',
    icon: UserFilled
  },
  {
    label: '关系配置',
    caption: '教师授课与家长绑定配置',
    to: '/admin/relations',
    icon: Connection
  }
]

const currentTitle = computed(() =>
  typeof route.meta.title === 'string' ? route.meta.title : '管理员工作台'
)

function closeMobileNav() {
  mobileNavOpen.value = false
}

function logout() {
  store.setAuthenticatedUser(null)
  void router.push('/admin/login')
}
</script>

<template>
  <div class="admin-shell">
    <aside class="admin-sidebar" :class="{ 'admin-sidebar-open': mobileNavOpen }">
      <div class="admin-brand">
        <div class="admin-brand__badge">A</div>
        <div>
          <strong>Admin Console</strong>
          <p>{{ store.authUser?.school || '平台管理中心' }}</p>
        </div>
      </div>

      <nav class="admin-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.to"
          :to="item.to"
          class="admin-nav__item"
          active-class="admin-nav__item-active"
          @click="closeMobileNav"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>
            <strong>{{ item.label }}</strong>
            <small>{{ item.caption }}</small>
          </span>
        </router-link>
      </nav>

      <div class="admin-sidebar__footer">
        <p>当前账号</p>
        <strong>{{ store.authUser?.name || '管理员' }}</strong>
        <span>{{ store.authUser?.account || '未登录' }}</span>
      </div>
    </aside>

    <div class="admin-main">
      <header class="admin-topbar surface-card">
        <div class="admin-topbar__left">
          <el-button class="admin-topbar__toggle" circle @click="mobileNavOpen = !mobileNavOpen">
            <el-icon><Menu /></el-icon>
          </el-button>
          <div>
            <p>管理员端</p>
            <h1>{{ currentTitle }}</h1>
          </div>
        </div>

        <div class="admin-topbar__right">
          <div class="admin-user-chip">
            <span>{{ store.authUser?.name?.slice(0, 1) || 'A' }}</span>
            <div>
              <strong>{{ store.authUser?.name || '管理员' }}</strong>
              <small>{{ store.authUser?.account || '系统账号' }}</small>
            </div>
          </div>
          <el-button circle @click="logout">
            <el-icon><SwitchButton /></el-icon>
          </el-button>
        </div>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-shell {
  display: flex;
  min-height: 100vh;
  gap: 1.5rem;
  padding: 1.5rem;
  align-items: flex-start;
}

.admin-sidebar {
  position: sticky;
  top: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  flex-shrink: 0;
  width: 290px;
  height: calc(100vh - 3rem);
  padding: 1.5rem;
  border-radius: 32px;
  color: #f8fbff;
  background:
    linear-gradient(180deg, rgba(7, 19, 41, 0.96), rgba(10, 25, 54, 0.92)),
    radial-gradient(circle at top, rgba(30, 144, 255, 0.18), transparent 45%);
  box-shadow: 0 26px 50px -14px rgba(15, 23, 42, 0.35);
}

.admin-brand {
  display: flex;
  align-items: center;
  gap: 0.9rem;
}

.admin-brand__badge {
  width: 48px;
  height: 48px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  font-size: 1.3rem;
  font-weight: 800;
  color: #062648;
  background: linear-gradient(135deg, #d7f4ff, #75cfff);
}

.admin-brand strong {
  display: block;
  font-size: 1.12rem;
}

.admin-brand p {
  margin: 0.2rem 0 0;
  color: rgba(230, 238, 250, 0.72);
  font-size: 0.86rem;
}

.admin-nav {
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
}

.admin-nav__item {
  display: grid;
  grid-template-columns: 20px 1fr;
  gap: 0.95rem;
  padding: 0.95rem 1rem;
  border-radius: 18px;
  color: rgba(236, 244, 255, 0.82);
  transition: all 0.2s ease;
}

.admin-nav__item strong,
.admin-nav__item small {
  display: block;
}

.admin-nav__item strong {
  font-size: 0.96rem;
}

.admin-nav__item small {
  margin-top: 0.22rem;
  color: rgba(214, 226, 245, 0.62);
  font-size: 0.8rem;
}

.admin-nav__item:hover,
.admin-nav__item-active {
  color: white;
  background: linear-gradient(90deg, rgba(40, 115, 255, 0.92), rgba(21, 150, 202, 0.92));
  box-shadow: 0 12px 24px -8px rgba(29, 78, 216, 0.4);
  transform: translateX(4px);
}

.admin-sidebar__footer {
  margin-top: auto;
  padding: 1rem 1.05rem;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-sidebar__footer p,
.admin-sidebar__footer span {
  margin: 0;
  color: rgba(226, 234, 247, 0.72);
  font-size: 0.84rem;
}

.admin-sidebar__footer strong {
  display: block;
  margin: 0.4rem 0 0.15rem;
  font-size: 1.05rem;
}

.admin-main {
  min-width: 0;
  flex: 1;
  min-height: calc(100vh - 3rem);
}

.admin-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.35rem;
  border-radius: 28px;
  margin-bottom: 1.5rem;
}

.admin-topbar__left {
  display: flex;
  align-items: center;
  gap: 0.9rem;
}

.admin-topbar__left p {
  margin: 0;
  color: #6d7f95;
  font-size: 0.82rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.admin-topbar__left h1 {
  margin: 0.18rem 0 0;
  color: #12314f;
  font-size: 1.58rem;
}

.admin-topbar__toggle {
  display: none;
}

.admin-topbar__right {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.admin-user-chip {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.5rem 0.8rem;
  border-radius: 18px;
  background: rgba(241, 248, 255, 0.85);
}

.admin-user-chip span {
  width: 38px;
  height: 38px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: white;
  background: linear-gradient(135deg, #2563eb, #0f8ec5);
}

.admin-user-chip strong,
.admin-user-chip small {
  display: block;
}

.admin-user-chip small {
  color: #698095;
}

.admin-content {
  min-width: 0;
}

@media (max-width: 1180px) {
  .admin-shell {
    padding: 1rem;
  }

  .admin-sidebar {
    position: fixed;
    left: 1rem;
    top: 1rem;
    bottom: 1rem;
    height: auto;
    width: min(300px, 88vw);
    transform: translateX(-120%);
    transition: transform 0.28s ease;
    z-index: 30;
  }

  .admin-sidebar-open {
    transform: translateX(0);
  }

  .admin-topbar__toggle {
    display: inline-flex;
  }
}

@media (max-width: 760px) {
  .admin-topbar {
    flex-direction: column;
    align-items: stretch;
  }

  .admin-topbar__right {
    justify-content: space-between;
  }
}
</style>
