<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useStudentPortalStore } from '@/stores/studentPortal'

const router = useRouter()
const store = useStudentPortalStore()

function logout() {
  store.setAuthenticatedUser(null)
  store.resetState()
  void router.push('/login')
}
</script>

<template>
  <div class="page-stack">
    <header class="page-header">
      <div>
        <h2>我的</h2>
        <p>查看当前账号信息、学习进度和桌面端常用入口。</p>
      </div>
    </header>

    <section class="surface-card student-profile-hero">
      <img class="student-profile-hero__logo" src="/system-logo.png" alt="小学课后作业系统 Logo" />
      <div>
        <h3>{{ store.profile.name }}</h3>
        <p>{{ store.profile.school }}</p>
        <span class="soft-chip">{{ store.profile.account }}</span>
      </div>
    </section>

    <section class="grid-cards">
      <article class="section-card surface-card">
        <h3>待完成作业</h3>
        <p class="section-subtitle">今天仍需继续完成的学习任务。</p>
        <div class="mini-number">{{ store.pendingCount }}</div>
      </article>
      <article class="section-card surface-card">
        <h3>待订正作业</h3>
        <p class="section-subtitle">老师反馈后还需要修改的内容。</p>
        <div class="mini-number">{{ store.revisionCount }}</div>
      </article>
      <article class="section-card surface-card">
        <h3>错题本待处理</h3>
        <p class="section-subtitle">还没有完成订正的错题记录。</p>
        <div class="mini-number">{{ store.pendingWrongBookCount }}</div>
      </article>
      <article class="section-card surface-card">
        <h3>未读消息</h3>
        <p class="section-subtitle">老师反馈、提醒和系统通知。</p>
        <div class="mini-number">{{ store.unreadMessageCount }}</div>
      </article>
    </section>

    <section class="surface-card section-card">
      <h3>常用入口</h3>
      <p class="section-subtitle">从桌面端继续查看作业、错题本和消息，不需要来回跳转。</p>

      <div class="panel-list">
        <div class="panel-list-item student-profile-entry" @click="router.push('/student/home')">
          <strong>回到学习台</strong>
          <p>查看全部作业、科目筛选和老师最新提醒。</p>
        </div>
        <div class="panel-list-item student-profile-entry" @click="router.push('/student/wrong-book')">
          <strong>打开错题本</strong>
          <p>继续整理错题、提交订正并标记已掌握。</p>
        </div>
        <div class="panel-list-item student-profile-entry" @click="router.push('/student/messages')">
          <strong>查看消息中心</strong>
          <p>集中查看老师反馈、截止提醒和系统通知。</p>
        </div>
      </div>
    </section>

    <section class="surface-card section-card">
      <h3>账号操作</h3>
      <p class="section-subtitle">如需切换身份或重新登录，可以从这里直接退出当前会话。</p>
      <el-button type="danger" @click="logout">退出登录</el-button>
    </section>
  </div>
</template>

<style scoped>
.student-profile-hero {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  border-radius: 30px;
}

.student-profile-hero__logo {
  width: 88px;
  height: 88px;
  border-radius: 26px;
  object-fit: cover;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 18px 32px rgba(37, 99, 235, 0.14);
}

.student-profile-hero h3 {
  margin: 0;
  font-size: 1.75rem;
  color: #14263d;
}

.student-profile-hero p {
  margin: 0.45rem 0 0.8rem;
  color: #607589;
}

.student-profile-entry {
  cursor: pointer;
}
</style>
