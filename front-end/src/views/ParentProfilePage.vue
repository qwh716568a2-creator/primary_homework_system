<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useParentPortalStore } from '@/stores/parentPortal'

const router = useRouter()
const store = useParentPortalStore()

const familySummary = computed(() =>
  store.children.map((item) => ({
    id: item.id,
    name: item.name,
    meta: `${item.gradeName} · ${item.className}`,
    pending: item.pendingCount,
    revision: item.revisionCount
  }))
)

function logout() {
  store.setAuthenticatedUser(null)
  store.resetState()
  void router.push('/login')
}

onMounted(() => {
  if (!store.children.length) {
    void store.loadChildren().catch(() => undefined)
  }
})
</script>

<template>
  <div class="page-stack parent-profile-page">
    <section class="surface-card parent-profile-hero">
      <img class="parent-profile-hero__logo" src="/system-logo.png" alt="系统 Logo" />
      <div>
        <span>当前账号</span>
        <h2>{{ store.profile.name }}</h2>
        <p>{{ store.profile.school }}</p>
        <small>{{ store.profile.account }}</small>
      </div>
    </section>

    <section class="surface-card section-card">
      <div class="dashboard-section-head">
        <h3>已绑定孩子</h3>
      </div>

      <div v-if="familySummary.length" class="family-grid">
        <article v-for="item in familySummary" :key="item.id" class="family-card">
          <div>
            <strong>{{ item.name }}</strong>
            <span>{{ item.meta }}</span>
          </div>
          <div class="family-card__stats">
            <span>待完成 {{ item.pending }}</span>
            <span>待订正 {{ item.revision }}</span>
          </div>
        </article>
      </div>

      <div v-else class="empty-state">当前账号暂无绑定孩子。</div>
    </section>

    <section class="surface-card section-card">
      <div class="dashboard-section-head">
        <h3>常用操作</h3>
      </div>

      <div class="parent-profile-actions">
        <el-button @click="router.push('/parent/messages')">消息中心</el-button>
        <el-button type="primary" @click="router.push('/parent/home')">返回首页</el-button>
        <el-button @click="logout">退出登录</el-button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.parent-profile-page {
  max-width: 980px;
}

.parent-profile-hero {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 24px 28px;
  border-radius: 28px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(247, 251, 255, 0.96)),
    radial-gradient(circle at 100% 0%, rgba(47, 124, 255, 0.14), transparent 34%);
}

.parent-profile-hero__logo {
  width: 72px;
  height: 72px;
  border-radius: 22px;
  object-fit: cover;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 16px 32px rgba(31, 68, 118, 0.12);
}

.parent-profile-hero span,
.parent-profile-hero p,
.parent-profile-hero small {
  color: #60758a;
}

.parent-profile-hero h2 {
  margin: 4px 0;
  color: #08213f;
  font-size: 30px;
  letter-spacing: -0.04em;
}

.family-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 14px;
}

.family-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px;
  border: 1px solid rgba(190, 205, 224, 0.72);
  border-radius: 20px;
  background: linear-gradient(135deg, #fff, #f7fbff);
}

.family-card strong,
.family-card span {
  display: block;
}

.family-card strong {
  color: #08213f;
  font-size: 18px;
}

.family-card span {
  margin-top: 4px;
  color: #60758a;
}

.family-card__stats {
  text-align: right;
}

.parent-profile-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 1rem;
}

@media (max-width: 760px) {
  .parent-profile-hero,
  .family-card {
    align-items: flex-start;
    flex-direction: column;
  }

  .family-grid {
    grid-template-columns: 1fr;
  }

  .family-card__stats {
    text-align: left;
  }
}
</style>
