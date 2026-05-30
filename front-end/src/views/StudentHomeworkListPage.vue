<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, Reading } from '@element-plus/icons-vue'
import StudentHomeworkCard from '@/components/StudentHomeworkCard.vue'
import { useStudentPortalStore } from '@/stores/studentPortal'

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

const homeworkStats = computed(() => [
  {
    label: '待完成',
    value: store.homeworks.filter((item) => item.status === 'pending').length,
    hint: '需要按时提交'
  },
  {
    label: '待订正',
    value: store.homeworks.filter((item) => item.status === 'revision').length,
    hint: '老师要求继续修改'
  },
  {
    label: '已提交',
    value: store.homeworks.filter((item) => item.status === 'submitted').length,
    hint: '等待老师反馈'
  },
  {
    label: '已完成',
    value: store.homeworks.filter((item) => item.status === 'completed').length,
    hint: '本阶段完成记录'
  }
])

const nearestHomework = computed(() =>
  [...store.homeworks]
    .filter((item) => item.status === 'pending' || item.status === 'revision')
    .sort((left, right) => new Date(left.deadline).getTime() - new Date(right.deadline).getTime())[0] ?? null
)

async function loadPage() {
  try {
    await store.loadHomeworks()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '作业列表加载失败，请稍后重试。')
  }
}

function openHomework(homeworkId: string) {
  void router.push(`/student/homeworks/${homeworkId}`)
}

function formatDeadline(deadline?: string) {
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
  <div class="page-stack student-homework-page">
    <section class="surface-card student-homework-hero">
      <div class="student-homework-hero__copy">
        <span>我的作业</span>
        <h2>按状态和科目管理所有作业</h2>
        <p>作业清单从学习台拆出后，待完成、待订正、已提交和已完成的任务都在这里集中处理。</p>
      </div>

      <button
        v-if="nearestHomework"
        type="button"
        class="student-homework-next"
        @click="openHomework(nearestHomework.id)"
      >
        <span>
          <el-icon><Calendar /></el-icon>
          最近需要处理
        </span>
        <strong>{{ nearestHomework.title }}</strong>
        <small>{{ nearestHomework.subject }} · {{ formatDeadline(nearestHomework.deadline) }}</small>
      </button>
    </section>

    <section class="student-homework-stat-grid" v-loading="store.loading.homeworks">
      <article v-for="item in homeworkStats" :key="item.label" class="surface-card student-homework-stat">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </article>
    </section>

    <section class="surface-card section-card">
      <div class="student-homework-section-head">
        <div>
          <span>
            <el-icon><Reading /></el-icon>
            作业清单
          </span>
          <h3>{{ filteredHomeworks.length }} 项作业</h3>
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
.student-homework-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 380px);
  gap: 1rem;
  align-items: stretch;
  padding: 1.25rem;
  border-radius: 28px;
  background:
    radial-gradient(circle at right top, rgba(37, 99, 235, 0.1), transparent 30%),
    linear-gradient(135deg, #ffffff, #f6fbff);
}

.student-homework-hero__copy span,
.student-homework-section-head span {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  color: #2563eb;
  font-weight: 800;
}

.student-homework-hero__copy h2 {
  margin: 0.4rem 0 0;
  color: #112640;
  font-size: 1.85rem;
  line-height: 1.2;
}

.student-homework-hero__copy p {
  margin: 0.55rem 0 0;
  color: #63778d;
  line-height: 1.75;
}

.student-homework-next {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 0.7rem;
  min-height: 150px;
  padding: 1rem;
  border: 0;
  border-radius: 22px;
  background: linear-gradient(135deg, #1d4ed8, #2563eb 52%, #22c55e);
  color: white;
  text-align: left;
  cursor: pointer;
  box-shadow: 0 20px 38px rgba(37, 99, 235, 0.22);
}

.student-homework-next span,
.student-homework-next strong,
.student-homework-next small {
  display: block;
}

.student-homework-next span {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 700;
}

.student-homework-next strong {
  font-size: 1.25rem;
  line-height: 1.35;
}

.student-homework-next small {
  color: rgba(255, 255, 255, 0.82);
  font-size: 0.92rem;
}

.student-homework-stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1rem;
}

.student-homework-stat {
  padding: 1rem;
  border-radius: 22px;
}

.student-homework-stat span,
.student-homework-stat small {
  display: block;
  color: #6b7f95;
}

.student-homework-stat strong {
  display: block;
  margin: 0.2rem 0;
  color: #112640;
  font-size: 2rem;
}

.student-homework-section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.student-homework-section-head h3 {
  margin: 0.35rem 0 0;
  color: #112640;
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

@media (max-width: 1080px) {
  .student-homework-hero,
  .student-homework-stat-grid,
  .student-homework-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .student-homework-hero__copy h2 {
    font-size: 1.55rem;
  }
}
</style>
