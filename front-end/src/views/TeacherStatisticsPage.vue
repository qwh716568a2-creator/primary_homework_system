<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatDateTime, getHomeworkDisplayClasses, percentFromRate } from '@/utils/teacher-portal-view'

const store = useTeacherPortalStore()

const recentPublished = computed(() => store.homeworks.slice(0, 6))

const subjectSummary = computed(() => {
  const summary = new Map<string, { subjectName: string; count: number; pending: number }>()

  store.homeworks.forEach((item) => {
    const current = summary.get(item.subjectCode) ?? {
      subjectName: item.subjectName,
      count: 0,
      pending: 0
    }

    current.count += 1
    current.pending += item.pendingCount
    summary.set(item.subjectCode, current)
  })

  return Array.from(summary.values()).sort((left, right) => right.count - left.count)
})

async function loadPage() {
  try {
    await Promise.all([store.loadTeachingClasses(), store.loadHomeworkList(), store.loadHomeworkOverview()])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '统计数据加载失败')
  }
}

onMounted(loadPage)
</script>

<template>
  <div class="page-stack">
    <header class="page-header">
      <div>
        <h2 class="hero-title">统计分析</h2>
        <p class="page-subtitle">从发布量、提交率、按时率和批改效率四个维度查看教师端执行情况。</p>
      </div>
    </header>

    <section class="stats-hero">
      <div class="stats-card">
        <span>发布作业</span>
        <strong>{{ store.homeworkOverview.publishCount }}</strong>
      </div>
      <div class="stats-card">
        <span>提交率</span>
        <strong>{{ percentFromRate(store.homeworkOverview.submissionRate) }}%</strong>
      </div>
      <div class="stats-card">
        <span>按时率</span>
        <strong>{{ percentFromRate(store.homeworkOverview.onTimeRate) }}%</strong>
      </div>
      <div class="stats-card">
        <span>批改率</span>
        <strong>{{ percentFromRate(store.homeworkOverview.reviewRate) }}%</strong>
      </div>
    </section>

    <div class="stats-grid">
      <section class="surface-card section-card">
        <div class="section-head">
          <h3>最近作业表现</h3>
          <span>按当前接口数据实时展示</span>
        </div>

        <div v-if="recentPublished.length" class="stats-list">
          <article v-for="item in recentPublished" :key="item.homeworkId" class="stats-row">
            <div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.subjectName }} · {{ getHomeworkDisplayClasses(item) }}</p>
            </div>

            <div class="stats-row-meta">
              <span>{{ formatDateTime(item.deadlineAt) }}</span>
              <span>已交 {{ item.submittedCount }}</span>
              <span>未交 {{ item.pendingCount }}</span>
              <span>待订正 {{ item.revisionRequiredCount }}</span>
            </div>
          </article>
        </div>

        <el-empty v-else description="暂无统计数据" />
      </section>

      <section class="surface-card section-card">
        <div class="section-head">
          <h3>学科维度概览</h3>
          <span>按当前教师作业列表聚合</span>
        </div>

        <div v-if="subjectSummary.length" class="subject-summary">
          <article v-for="item in subjectSummary" :key="item.subjectName" class="subject-row">
            <div>
              <strong>{{ item.subjectName }}</strong>
              <p>{{ item.count }} 份作业</p>
            </div>

            <span class="subject-badge">未交 {{ item.pending }}</span>
          </article>
        </div>

        <el-empty v-else description="暂无学科统计" />
      </section>
    </div>
  </div>
</template>

<style scoped>
.page-subtitle {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 14px;
}

.stats-hero {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stats-card {
  padding: 24px;
  border-radius: 22px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(239, 246, 255, 0.9));
  border: 1px solid rgba(191, 219, 254, 0.7);
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.06);
}

.stats-card span {
  display: block;
  color: #64748b;
  font-size: 13px;
}

.stats-card strong {
  display: block;
  margin-top: 10px;
  color: #0f172a;
  font-size: 36px;
  font-weight: 800;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 20px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.section-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
}

.section-head span {
  color: #64748b;
  font-size: 13px;
}

.stats-list,
.subject-summary {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.stats-row,
.subject-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px;
  border-radius: 18px;
  background: rgba(248, 250, 252, 0.9);
}

.stats-row strong,
.subject-row strong {
  display: block;
  color: #0f172a;
}

.stats-row p,
.subject-row p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 13px;
}

.stats-row-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  color: #475569;
  font-size: 13px;
}

.subject-badge {
  padding: 8px 12px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 600;
}

@media (max-width: 1200px) {
  .stats-hero,
  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 900px) {
  .stats-hero,
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
