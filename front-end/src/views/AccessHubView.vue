<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { roleMeta } from '@/utils/auth-session'

const router = useRouter()
const store = useTeacherPortalStore()

const currentRole = computed(() => store.authUser?.role ?? 'teacher')
const currentMeta = computed(() => roleMeta[currentRole.value])
const allRoles = computed(() =>
  (Object.keys(roleMeta) as Array<keyof typeof roleMeta>).map((role) => ({
    role,
    ...roleMeta[role],
    active: currentRole.value === role
  }))
)

function goTeacherWorkspace() {
  if (currentRole.value !== 'teacher') {
    ElMessage.info('当前账号已识别为其他角色，教师端工作台默认只对教师角色开放。')
    return
  }

  router.push('/dashboard')
}

function logout() {
  store.setAuthenticatedUser(null)
  router.push('/login')
}
</script>

<template>
  <section class="page-stack">
    <article class="section-card surface-card role-hub-hero">
      <div>
        <span class="soft-chip">统一角色入口</span>
        <h2 style="margin-top: 0.85rem;">已识别当前账号为“{{ currentMeta.label }}”</h2>
        <p class="section-subtitle" style="margin-top: 0.5rem;">
          {{ currentMeta.summary }}
        </p>
        <p class="section-subtitle">
          当前登录人：{{ store.authUser?.name || '未登录' }} · {{ store.authUser?.school || '未识别学校' }}
        </p>
      </div>

      <div class="actions-row" style="margin-top: 0;">
        <el-button type="primary" :disabled="currentRole !== 'teacher'" @click="goTeacherWorkspace">
          进入{{ currentRole === 'teacher' ? '教师工作台' : '当前角色入口待建设' }}
        </el-button>
        <el-button @click="logout">退出登录</el-button>
      </div>
    </article>

    <section class="role-hub-grid">
      <article
        v-for="item in allRoles"
        :key="item.role"
        class="section-card surface-card role-hub-card"
        :class="{ 'role-hub-card-active': item.active }"
      >
        <div class="chip-row">
          <span class="soft-chip">{{ item.label }}</span>
          <span v-if="item.active" class="stat-pill">当前身份</span>
        </div>
        <h3 style="margin-top: 1rem;">{{ item.entryLabel }}</h3>
        <p class="section-subtitle" style="margin-bottom: 0;">{{ item.summary }}</p>
      </article>
    </section>

    <article class="section-card surface-card">
      <h3>当前项目状态</h3>
      <p class="section-subtitle">
        本轮前端原型已经完成教师端 Web 主链路，所以教师账号登录后会直接进入工作台。
        管理员、学生和家长角色已经纳入统一登录认证流程，但对应页面入口还可以继续在后续迭代中补齐。
      </p>
    </article>
  </section>
</template>
