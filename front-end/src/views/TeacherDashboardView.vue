<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataAnalysis, EditPen, Promotion } from '@element-plus/icons-vue'
import MetricCard from '@/components/MetricCard.vue'
import QuickActionCard from '@/components/QuickActionCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatDateTime, getHomeworkDisplayClasses, getRelativeDeadline, percentFromRate } from '@/utils/teacher-portal'

const router = useRouter()
const store = useTeacherPortalStore()

const workspaceLoading = computed(
  () => store.loading.classes || store.loading.homeworks || store.loading.overview
)

const urgentAssignments = computed(() => store.pendingHomeworkList.slice(0, 5))
const recentAssignments = computed(() => store.recentAssignments.slice(0, 5))

const dashboardHighlights = computed(() => [
  `当前共管理 ${store.classOptions.length} 个教学班级，已覆盖 ${store.subjectOptions.length} 个学科。`,
  `作业提交率 ${percentFromRate(store.homeworkOverview.submissionRate)}%，已批改率 ${percentFromRate(store.homeworkOverview.reviewRate)}%。`,
  `待跟进作业 ${store.pendingHomeworkList.length} 份，可优先处理临近截止且仍有未交的任务。`
])

async function loadDashboard() {
  try {
    await store.initializeWorkspace()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '教师工作台加载失败')
  }
}

onMounted(loadDashboard)
</script>

<template>
  <section class="page-stack">
    <header class="page-header">
      <div>
        <h2>今日工作台</h2>
        <p>所有数据均来自正式接口，便于直接联调作业发布、催交、批改和统计流程。</p>
      </div>
      <div class="chip-row">
        <span class="soft-chip">{{ store.teacher.school }}</span>
        <span class="soft-chip">{{ store.teacher.role }}</span>
      </div>
    </header>

    <section class="grid-cards" v-loading="workspaceLoading">
      <MetricCard
        v-for="card in store.dashboardCards"
        :key="card.label"
        :label="card.label"
        :value="card.value"
        :hint="card.hint"
        :tone="card.tone"
      />
    </section>

    <section class="quick-actions">
      <QuickActionCard title="发布作业" subtitle="新建作业并选择多个班级" to="/assignments/new" tone="sky">
        <el-icon><EditPen /></el-icon>
      </QuickActionCard>
      <QuickActionCard title="进入批改" subtitle="查看待批改与待订正任务" to="/assignments" tone="teal">
        <el-icon><Promotion /></el-icon>
      </QuickActionCard>
      <QuickActionCard title="查看统计" subtitle="快速查看执行概况和重点作业" to="/statistics" tone="ink">
        <el-icon><DataAnalysis /></el-icon>
      </QuickActionCard>
    </section>

    <section class="dashboard-summary">
      <article class="section-card surface-card">
        <h3>接口数据概览</h3>
        <p class="section-subtitle">这里不再显示临时图表，改为聚焦当前接口已经提供的核心指标。</p>
        <div class="insight-list">
          <div v-for="item in dashboardHighlights" :key="item" class="insight-item">{{ item }}</div>
        </div>
      </article>

      <article class="section-card surface-card">
        <h3>今日优先处理</h3>
        <p class="section-subtitle">优先关注未交与待订正数量较多的作业。</p>
        <div class="panel-list" v-if="urgentAssignments.length">
          <div v-for="item in urgentAssignments" :key="item.homeworkId" class="panel-list-item">
            <div class="card-row-between">
              <strong>{{ item.title }}</strong>
              <StatusTag kind="assignment" :value="item.status" />
            </div>
            <p>{{ getHomeworkDisplayClasses(item) }}</p>
            <div class="chip-row" style="margin-top: 0.75rem;">
              <span class="stat-pill">未交 {{ item.pendingCount }}</span>
              <span class="stat-pill">待订正 {{ item.revisionRequiredCount }}</span>
              <span class="stat-pill">{{ getRelativeDeadline(item.deadlineAt) }}</span>
            </div>
            <div class="actions-row">
              <el-button type="primary" @click="router.push(`/assignments/${item.homeworkId}`)">查看详情</el-button>
              <el-button @click="router.push(`/assignments/${item.homeworkId}/grading`)">进入批改</el-button>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">当前没有需要优先跟进的作业。</div>
      </article>
    </section>

    <article class="section-card surface-card">
      <h3>最近作业</h3>
      <p class="section-subtitle">按截止时间排序，便于查看最近发布与即将到期的作业。</p>
      <div class="panel-list" v-if="recentAssignments.length">
        <div v-for="item in recentAssignments" :key="item.homeworkId" class="panel-list-item">
          <div class="card-row-between">
            <div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.subjectName }} · {{ getHomeworkDisplayClasses(item) }}</p>
            </div>
            <StatusTag kind="assignment" :value="item.status" />
          </div>
          <div class="chip-row" style="margin-top: 0.75rem;">
            <span class="stat-pill">截止 {{ formatDateTime(item.deadlineAt) }}</span>
            <span class="stat-pill">已交 {{ item.submittedCount }}</span>
            <span class="stat-pill">未交 {{ item.pendingCount }}</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">暂无作业数据，后端接口就绪后可直接从这里看到结果。</div>
    </article>
  </section>
</template>
