<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearAuthSession, getAuthSession, roleMeta } from '@/utils/auth-session-clean'

const route = useRoute()
const router = useRouter()

const TEXT = {
  studentTitle: '\u5b66\u751f\u5165\u53e3\u5df2\u5c31\u7eea',
  parentTitle: '\u5bb6\u957f\u5165\u53e3\u5df2\u5c31\u7eea',
  studentDescription: '\u5f53\u524d\u8d26\u53f7\u5df2\u5b8c\u6210\u8eab\u4efd\u6821\u9a8c\u3002\u5b66\u751f\u6838\u5fc3\u529f\u80fd\u5efa\u8bae\u901a\u8fc7\u79fb\u52a8\u7aef\u6216\u5c0f\u7a0b\u5e8f\u7ee7\u7eed\u4f7f\u7528\u3002',
  parentDescription: '\u5f53\u524d\u8d26\u53f7\u5df2\u5b8c\u6210\u8eab\u4efd\u6821\u9a8c\u3002\u5bb6\u957f\u6838\u5fc3\u529f\u80fd\u5efa\u8bae\u901a\u8fc7\u79fb\u52a8\u7aef\u6216\u5c0f\u7a0b\u5e8f\u7ee7\u7eed\u4f7f\u7528\u3002',
  switchAccount: '\u5207\u6362\u8d26\u53f7',
  logout: '\u9000\u51fa\u767b\u5f55'
}

const session = computed(() => getAuthSession())
const role = computed(() => {
  const expectedRole = typeof route.meta.role === 'string' ? route.meta.role : session.value?.role
  return expectedRole === 'student' || expectedRole === 'parent' ? expectedRole : session.value?.role
})

const currentMeta = computed(() => {
  if (role.value === 'student' || role.value === 'parent') {
    return roleMeta[role.value]
  }

  return roleMeta.student
})

const title = computed(() => (role.value === 'parent' ? TEXT.parentTitle : TEXT.studentTitle))
const description = computed(() =>
  role.value === 'parent' ? TEXT.parentDescription : TEXT.studentDescription
)

function backToLogin() {
  clearAuthSession()
  void router.push('/login')
}
</script>

<template>
  <div class="role-guide">
    <section class="role-guide__panel surface-card">
      <span class="role-guide__eyebrow">{{ currentMeta.entryLabel }}</span>
      <h1>{{ title }}</h1>
      <p>{{ description }}</p>

      <div class="role-guide__summary">
        <strong>{{ currentMeta.label }}</strong>
        <span>{{ currentMeta.summary }}</span>
      </div>

      <div class="role-guide__actions">
        <el-button type="primary" @click="router.push('/login')">{{ TEXT.switchAccount }}</el-button>
        <el-button @click="backToLogin">{{ TEXT.logout }}</el-button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.role-guide {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 1.5rem;
}

.role-guide__panel {
  width: min(720px, 100%);
  padding: 2rem;
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.82);
}

.role-guide__eyebrow {
  display: inline-flex;
  align-items: center;
  padding: 0.45rem 0.9rem;
  border-radius: 999px;
  background: #e7f1ff;
  color: #25528d;
  font-size: 0.88rem;
  font-weight: 700;
}

.role-guide__panel h1 {
  margin: 1rem 0 0;
  font-size: 2.1rem;
  color: #17324d;
}

.role-guide__panel p {
  margin: 0.8rem 0 0;
  color: #5f7387;
  font-size: 1rem;
}

.role-guide__summary {
  margin-top: 1.4rem;
  padding: 1rem 1.2rem;
  border-radius: 22px;
  background: rgba(240, 247, 255, 0.9);
  border: 1px solid rgba(192, 220, 255, 0.7);
}

.role-guide__summary strong,
.role-guide__summary span {
  display: block;
}

.role-guide__summary span {
  margin-top: 0.35rem;
  color: #5d738a;
}

.role-guide__actions {
  display: flex;
  gap: 0.9rem;
  margin-top: 1.5rem;
}

@media (max-width: 640px) {
  .role-guide__panel {
    padding: 1.35rem;
  }

  .role-guide__panel h1 {
    font-size: 1.7rem;
  }

  .role-guide__actions {
    flex-direction: column;
  }
}
</style>
