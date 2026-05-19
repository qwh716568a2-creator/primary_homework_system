<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, Check, Message } from '@element-plus/icons-vue'
import { useStudentPortalStore } from '@/stores/studentPortal'
import type { StudentMessageItem } from '@/types/student-portal'
import { studentMessageKindMap } from '@/utils/student-portal-view'

const store = useStudentPortalStore()
const activeFilter = ref<'all' | 'unread'>('all')
const selectedMessage = ref<StudentMessageItem | null>(null)
const detailVisible = ref(false)

const filteredMessages = computed(() => {
  const list = activeFilter.value === 'unread' ? store.messages.filter((item) => item.unread) : store.messages
  return [...list].sort((left, right) => new Date(right.time).getTime() - new Date(left.time).getTime())
})

const unreadCount = computed(() => store.messages.filter((item) => item.unread).length)

async function loadPage() {
  try {
    await store.loadMessages(activeFilter.value)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '消息加载失败，请稍后重试。')
  }
}

function switchFilter(filter: 'all' | 'unread') {
  activeFilter.value = filter
  void loadPage()
}

async function openMessage(item: StudentMessageItem) {
  selectedMessage.value = { ...item, unread: false }
  detailVisible.value = true

  if (item.unread) {
    await store.markMessageRead(item.id)
  }
}

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div class="student-message-page">
    <section class="message-hero">
      <div>
        <span class="message-hero__eyebrow">消息中心</span>
        <h2>老师反馈与提醒通知</h2>
      </div>
      <div class="message-hero__stats">
        <span>全部 {{ store.messages.length }}</span>
        <strong>未读 {{ unreadCount }}</strong>
      </div>
    </section>

    <section class="message-toolbar">
      <button
        type="button"
        :class="['message-filter', { 'message-filter--active': activeFilter === 'all' }]"
        @click="switchFilter('all')"
      >
        全部消息
      </button>
      <button
        type="button"
        :class="['message-filter', { 'message-filter--active': activeFilter === 'unread' }]"
        @click="switchFilter('unread')"
      >
        仅看未读
      </button>
    </section>

    <section v-if="filteredMessages.length" class="message-list">
      <article
        v-for="item in filteredMessages"
        :key="item.id"
        class="message-card"
        :class="{ 'message-card--unread': item.unread }"
        role="button"
        tabindex="0"
        @click="openMessage(item)"
        @keydown.enter.prevent="openMessage(item)"
        @keydown.space.prevent="openMessage(item)"
      >
        <div class="message-card__icon">
          <el-icon><Bell v-if="item.unread" /><Check v-else /></el-icon>
        </div>
        <div class="message-card__content">
          <div class="message-card__meta">
            <span>{{ studentMessageKindMap[item.kind] || item.kind || '消息通知' }}</span>
            <span>{{ item.time }}</span>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.content }}</p>
        </div>
        <span class="message-card__status">{{ item.unread ? '未读' : '已读' }}</span>
      </article>
    </section>

    <section v-else class="message-empty">
      <el-icon><Message /></el-icon>
      <strong>暂无消息</strong>
      <span>当前筛选下没有老师反馈或提醒通知。</span>
    </section>

    <el-dialog v-model="detailVisible" title="消息详情" width="560px" class="message-detail-dialog">
      <div v-if="selectedMessage" class="message-detail">
        <div class="message-detail__meta">
          <span>{{ studentMessageKindMap[selectedMessage.kind] || selectedMessage.kind || '消息通知' }}</span>
          <span>{{ selectedMessage.time }}</span>
          <strong>{{ selectedMessage.unread ? '未读' : '已读' }}</strong>
        </div>
        <h3>{{ selectedMessage.title }}</h3>
        <p>{{ selectedMessage.content }}</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.student-message-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.message-hero,
.message-toolbar,
.message-list,
.message-empty {
  border: 1px solid rgba(190, 205, 224, 0.72);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 44px rgba(34, 66, 112, 0.08);
}

.message-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  padding: 24px 28px;
  border-radius: 28px;
}

.message-hero__eyebrow {
  display: inline-flex;
  padding: 7px 12px;
  border-radius: 999px;
  background: #e7f2ff;
  color: #1766c2;
  font-weight: 800;
  font-size: 13px;
}

.message-hero h2 {
  margin: 12px 0 0;
  color: #08213f;
  font-size: 30px;
  letter-spacing: -0.04em;
}

.message-hero__stats {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #61758f;
}

.message-hero__stats strong {
  padding: 9px 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, #2f7cff, #20bfa6);
  color: #fff;
}

.message-toolbar {
  display: flex;
  gap: 10px;
  padding: 14px;
  border-radius: 22px;
}

.message-filter {
  border: 0;
  border-radius: 999px;
  padding: 10px 18px;
  background: #eef4fb;
  color: #50667e;
  font-weight: 800;
  cursor: pointer;
}

.message-filter--active {
  color: #fff;
  background: linear-gradient(135deg, #2f7cff, #20bfa6);
}

.message-list {
  display: grid;
  gap: 14px;
  padding: 18px;
  border-radius: 28px;
}

.message-card {
  display: grid;
  grid-template-columns: 54px minmax(0, 1fr) auto;
  gap: 16px;
  align-items: flex-start;
  padding: 18px;
  border: 1px solid rgba(201, 214, 231, 0.74);
  border-radius: 22px;
  background: linear-gradient(135deg, #ffffff, #f7fbff);
  cursor: pointer;
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}

.message-card:hover,
.message-card:focus-visible {
  transform: translateY(-2px);
  border-color: rgba(47, 124, 255, 0.48);
  box-shadow: 0 16px 34px rgba(43, 91, 148, 0.12);
  outline: none;
}

.message-card--unread {
  border-color: rgba(47, 124, 255, 0.38);
  box-shadow: inset 4px 0 0 #2f7cff;
}

.message-card__icon {
  display: grid;
  place-items: center;
  width: 54px;
  height: 54px;
  border-radius: 18px;
  color: #1766c2;
  background: #e7f2ff;
  font-size: 22px;
}

.message-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  color: #71839a;
  font-size: 13px;
}

.message-card h3 {
  margin: 8px 0 0;
  color: #09213f;
  font-size: 20px;
}

.message-card p {
  margin: 8px 0 0;
  color: #38536f;
  line-height: 1.75;
}

.message-card__status {
  border-radius: 999px;
  padding: 7px 12px;
  background: #eef4fb;
  color: #50667e;
  font-weight: 800;
  font-size: 13px;
}

.message-card--unread .message-card__status {
  color: #fff;
  background: #2f7cff;
}

.message-detail {
  display: grid;
  gap: 14px;
}

.message-detail__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: #667b94;
}

.message-detail__meta strong {
  color: #2f7cff;
}

.message-detail h3 {
  margin: 0;
  color: #08213f;
  font-size: 24px;
}

.message-detail p {
  margin: 0;
  color: #36516f;
  line-height: 1.8;
  white-space: pre-wrap;
}

.message-empty {
  display: grid;
  place-items: center;
  gap: 10px;
  min-height: 280px;
  border-radius: 28px;
  color: #6c7d92;
}

.message-empty .el-icon {
  font-size: 48px;
  color: #9bb4d4;
}

.message-empty strong {
  color: #132b48;
  font-size: 22px;
}

@media (max-width: 760px) {
  .message-card {
    grid-template-columns: 1fr;
  }

  .message-hero {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
