<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, Calendar, ChatDotRound, Reading } from '@element-plus/icons-vue'
import MetricCard from '@/components/MetricCard.vue'
import StudentHomeworkCard from '@/components/StudentHomeworkCard.vue'
import { useStudentPortalStore } from '@/stores/studentPortal'
import { formatSubmitTypes } from '@/utils/format-labels'

const router = useRouter()
const store = useStudentPortalStore()
const activeStatus = ref<'all' | 'pending' | 'submitted' | 'revision' | 'completed'>('all')
const activeSubject = ref('all')

const statusOptions = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '待完成' },
  { key: 'submitted', label: '已提交' },
  { key: 'revision', label: '待订正' },
  { key: 'completed', label: '已完成' }
] as const

const subjectOptions = computed(() => [
  { key: 'all', label: '全部科目' },
  ...Array.from(new Set(store.homeworks.map((item) => item.subject))).map((subject) => ({
    key: subject,
    label: subject
  }))
])

const filteredHomeworks = computed(() => {
  let list = store.homeworks

  if (activeStatus.value !== 'all') {
    list = list.filter((item) => item.status === activeStatus.value)
  }

  if (activeSubject.value !== 'all') {
    list = list.filter((item) => item.subject === activeSubject.value)
  }

  return list
})

const latestHomeworks = computed(() =>
  [...store.homeworks]
    .sort((left, right) => new Date(right.deadline).getTime() - new Date(left.deadline).getTime())
    .slice(0, 3)
)

const featuredHomework = computed(() => latestHomeworks.value[0] ?? null)
const secondaryHomeworks = computed(() => latestHomeworks.value.slice(1))
const latestMessages = computed(() => store.messages.slice(0, 4))

async function loadPage() {
  try {
    await store.initializeWorkspace()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '学生工作台加载失败，请稍后重试。')
  }
}

function openHomework(homeworkId: string) {
  void router.push(`/student/homeworks/${homeworkId}`)
}

function goMessages() {
  void router.push('/student/messages')
}

function formatDeadline(deadline: string) {
  if (!deadline) return '待定'
  const date = new Date(deadline)
  if (Number.isNaN(date.getTime())) return deadline
  return `${date.getMonth() + 1}月${date.getDate()}日 ${String(date.getHours()).padStart(2, '0')}:${String(
    date.getMinutes()
  ).padStart(2, '0')}`
}

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div class="page-stack student-home-page">
    <header class="page-header student-page-header">
      <div>
        <p class="student-page-header__eyebrow">学生端 · 学习总览</p>
        <h2>学生首页</h2>
      </div>
      <div class="chip-row">
        <span class="soft-chip">{{ store.profile.school }}</span>
        <span class="soft-chip">{{ store.profile.account }}</span>
      </div>
    </header>

    <section class="grid-cards" v-loading="store.loading.homeworks || store.loading.messages || store.loading.wrongBooks">
      <MetricCard
        v-for="card in store.statusOverview"
        :key="card.label"
        :label="card.label"
        :value="card.value"
        :hint="card.hint"
        :tone="card.tone"
      />
    </section>

    <section class="student-home-overview">
      <article class="student-home-hero surface-card">
        <div class="student-home-hero__head">
          <div>
            <span class="student-home-hero__tag">最新作业</span>
            <h3>优先处理最近布置或最近到期的任务</h3>
          </div>
          <el-button text @click="activeStatus = 'all'">查看全部</el-button>
        </div>

        <button
          v-if="featuredHomework"
          type="button"
          class="student-home-featured"
          @click="openHomework(featuredHomework.id)"
        >
          <div class="student-home-featured__meta">
            <span class="soft-chip">{{ featuredHomework.subject }}</span>
            <span class="student-home-featured__deadline">
              <el-icon><Calendar /></el-icon>
              {{ formatDeadline(featuredHomework.deadline) }}
            </span>
          </div>

          <h4>{{ featuredHomework.title }}</h4>
          <p>{{ featuredHomework.summary || featuredHomework.content }}</p>

          <div class="student-home-featured__footer">
            <span>{{ featuredHomework.teacherName }}</span>
            <span>{{ formatSubmitTypes(featuredHomework.submitTypes) }}</span>
          </div>
        </button>

        <div v-else class="empty-state">当前还没有新的作业安排。</div>

        <div v-if="secondaryHomeworks.length" class="student-home-mini-grid">
          <button
            v-for="item in secondaryHomeworks"
            :key="item.id"
            type="button"
            class="student-home-mini-card"
            @click="openHomework(item.id)"
          >
            <div class="student-home-mini-card__icon">
              <el-icon><Reading /></el-icon>
            </div>
            <div class="student-home-mini-card__body">
              <strong>{{ item.title }}</strong>
              <span>{{ item.subject }} · {{ formatDeadline(item.deadline) }}</span>
            </div>
          </button>
        </div>
      </article>

      <article class="student-home-feed surface-card">
        <div class="student-home-feed__head">
          <div>
            <span class="student-home-hero__tag student-home-hero__tag--green">最新消息</span>
            <h3>老师反馈与提醒通知</h3>
          </div>
          <el-button text @click="goMessages">消息中心</el-button>
        </div>

        <div v-if="latestMessages.length" class="student-home-message-list">
          <button
            v-for="item in latestMessages"
            :key="item.id"
            type="button"
            class="student-home-message-item"
            @click="goMessages"
          >
            <div class="student-home-message-item__icon">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="student-home-message-item__body">
              <strong>{{ item.title }}</strong>
              <p>{{ item.content }}</p>
            </div>
            <div class="student-home-message-item__meta">
              <span>{{ item.time }}</span>
              <span v-if="item.unread" class="student-home-message-item__dot"></span>
            </div>
          </button>
        </div>

        <div v-else class="empty-state">当前还没有新的反馈或提醒。</div>
      </article>
    </section>

    <section class="surface-card section-card">
      <div class="dashboard-section-head dashboard-section-head--compact">
        <div>
          <h3>作业清单</h3>
        </div>
      </div>

      <div class="student-filter-strip">
        <div class="student-filter-strip__group">
          <button
            v-for="option in statusOptions"
            :key="option.key"
            type="button"
            :class="['student-filter-chip', { 'student-filter-chip--active': activeStatus === option.key }]"
            @click="activeStatus = option.key"
          >
            {{ option.label }}
          </button>
        </div>

        <div class="student-filter-strip__group">
          <button
            v-for="option in subjectOptions"
            :key="option.key"
            type="button"
            :class="['student-filter-chip', { 'student-filter-chip--active': activeSubject === option.key }]"
            @click="activeSubject = option.key"
          >
            {{ option.label }}
          </button>
        </div>
      </div>

      <div v-if="filteredHomeworks.length" class="student-homework-grid">
        <StudentHomeworkCard
          v-for="item in filteredHomeworks"
          :key="item.id"
          :item="item"
          @open="openHomework"
        />
      </div>

      <div v-else class="empty-state">当前筛选下还没有作业，换个状态或科目看看。</div>
    </section>
  </div>
</template>

<style scoped>
.student-home-page {
  gap: 1.15rem;
}

.student-page-header {
  align-items: flex-end;
}

.student-page-header__eyebrow {
  margin: 0 0 0.35rem;
  color: #6d82a0;
  font-size: 0.9rem;
  font-weight: 700;
}

.student-home-overview {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(340px, 0.8fr);
  gap: 1rem;
}

.student-home-hero,
.student-home-feed {
  padding: 1.3rem;
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(245, 249, 255, 0.92)),
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.08), transparent 28%);
}

.student-home-hero__head,
.student-home-feed__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.student-home-hero__tag {
  display: inline-flex;
  align-items: center;
  padding: 0.38rem 0.82rem;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
  font-size: 0.82rem;
  font-weight: 700;
}

.student-home-hero__tag--green {
  background: rgba(16, 185, 129, 0.12);
  color: #0f9b79;
}

.student-home-hero__head h3,
.student-home-feed__head h3 {
  margin: 0.75rem 0 0;
  font-size: 1.4rem;
  color: #12263f;
  line-height: 1.3;
}

.student-home-featured {
  width: 100%;
  padding: 1.25rem;
  border: 0;
  border-radius: 24px;
  background: linear-gradient(135deg, #1d4ed8, #2563eb 52%, #22c55e);
  color: #fff;
  text-align: left;
  cursor: pointer;
  box-shadow: 0 22px 40px rgba(37, 99, 235, 0.24);
}

.student-home-featured__meta,
.student-home-featured__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.8rem;
}

.student-home-featured__deadline {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.88rem;
  color: rgba(255, 255, 255, 0.92);
}

.student-home-featured h4 {
  margin: 1rem 0 0.6rem;
  font-size: 1.7rem;
  line-height: 1.2;
}

.student-home-featured p {
  margin: 0;
  color: rgba(255, 255, 255, 0.88);
  line-height: 1.75;
}

.student-home-featured__footer {
  margin-top: 1rem;
  color: rgba(255, 255, 255, 0.88);
  font-size: 0.92rem;
}

.student-home-mini-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.85rem;
  margin-top: 0.95rem;
}

.student-home-mini-card {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 0.8rem;
  width: 100%;
  padding: 0.95rem 1rem;
  border: 1px solid rgba(214, 226, 242, 0.95);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.96);
  text-align: left;
  cursor: pointer;
}

.student-home-mini-card__icon,
.student-home-message-item__icon {
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 15px;
  background: rgba(37, 99, 235, 0.12);
  color: #2563eb;
  font-size: 1.15rem;
}

.student-home-mini-card__body,
.student-home-message-item__body {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.28rem;
}

.student-home-mini-card__body strong,
.student-home-message-item__body strong {
  color: #132842;
  font-size: 0.98rem;
}

.student-home-mini-card__body span {
  color: #6f8398;
  font-size: 0.86rem;
}

.student-home-message-list {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.student-home-message-item {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) auto;
  gap: 0.8rem;
  width: 100%;
  padding: 0.95rem 1rem;
  border: 1px solid rgba(219, 230, 242, 0.95);
  border-radius: 20px;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.student-home-message-item__body p {
  margin: 0;
  color: #6f8398;
  font-size: 0.88rem;
  line-height: 1.55;
}

.student-home-message-item__meta {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  color: #7c8ea3;
  font-size: 0.84rem;
}

.student-home-message-item__dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #ef4444;
}

.student-filter-strip {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.student-filter-strip__group {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}

.student-filter-chip {
  border: 0;
  padding: 0.58rem 1rem;
  border-radius: 999px;
  background: rgba(241, 245, 249, 0.95);
  color: #5f7488;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.18s ease;
}

.student-filter-chip--active,
.student-filter-chip:hover {
  color: white;
  background: linear-gradient(135deg, #2563eb, #22c55e);
}

.student-homework-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
  margin-top: 1.1rem;
}

.dashboard-section-head--compact {
  margin-bottom: 0.9rem;
}

@media (max-width: 1320px) {
  .student-home-overview {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 980px) {
  .student-home-mini-grid,
  .student-homework-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .student-page-header {
    align-items: flex-start;
  }

  .student-home-featured__meta,
  .student-home-featured__footer,
  .student-home-message-item {
    grid-template-columns: 1fr;
  }

  .student-home-message-item__meta {
    justify-content: flex-start;
  }
}
</style>
