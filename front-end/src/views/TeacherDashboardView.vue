<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  CircleCheck,
  DataAnalysis,
  EditPen,
  Files,
  Promotion,
  RefreshLeft
} from '@element-plus/icons-vue'
import MetricCard from '@/components/MetricCard.vue'
import QuickActionCard from '@/components/QuickActionCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import {
  formatDateTime,
  getHomeworkDisplayClasses,
  getRelativeDeadline,
  percentFromRate
} from '@/utils/teacher-portal-view'

const router = useRouter()
const store = useTeacherPortalStore()

const workspaceLoading = computed(
  () => store.loading.classes || store.loading.homeworks || store.loading.overview
)

const urgentAssignments = computed(() => store.pendingHomeworkList.slice(0, 5))
const recentAssignments = computed(() => store.recentAssignments.slice(0, 5))

const dashboardHighlights = computed(() => [
  `当前共管理 ${store.classOptions.length} 个教学班级，覆盖 ${store.subjectOptions.length} 个学科。`,
  `本周作业提交率 ${percentFromRate(store.homeworkOverview.submissionRate)}%，已批改 ${percentFromRate(store.homeworkOverview.reviewRate)}%。`,
  `当前共有 ${store.pendingHomeworkList.length} 份作业待跟进，建议优先处理临近截止时间的任务。`
])

async function loadDashboard() {
  try {
    await store.initializeWorkspace()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '教师工作台加载失败，请稍后重试。')
  }
}

onMounted(loadDashboard)
</script>

<template>
  <div class="page-stack">
    <section class="grid-cards mb-4" v-loading="workspaceLoading">
      <MetricCard
        v-for="card in store.dashboardCards"
        :key="card.label"
        :label="card.label"
        :value="card.value"
        :hint="card.hint"
        :tone="card.tone"
      >
        <template #icon>
          <el-icon v-if="card.label === '进行中作业'"><Files /></el-icon>
          <el-icon v-else-if="card.label === '待批改'"><EditPen /></el-icon>
          <el-icon v-else-if="card.label === '待订正'"><RefreshLeft /></el-icon>
          <el-icon v-else><CircleCheck /></el-icon>
        </template>
      </MetricCard>
    </section>

    <section class="quick-actions-row mb-4">
      <QuickActionCard title="发布作业" subtitle="新建任务并选择下发班级" to="/assignments/new" tone="sky">
        <el-icon><EditPen /></el-icon>
      </QuickActionCard>
      <QuickActionCard title="批改中心" subtitle="查看待批阅与待订正列表" to="/assignments" tone="teal">
        <el-icon><Promotion /></el-icon>
      </QuickActionCard>
      <QuickActionCard title="学情统计" subtitle="多维度透视班级执行数据" to="/statistics" tone="ink">
        <el-icon><DataAnalysis /></el-icon>
      </QuickActionCard>
    </section>

    <el-row :gutter="24">
      <el-col :span="14">
        <div class="surface-card section-card h-full">
          <div class="card-header pb-4">
            <span class="card-title">优先跟进事项</span>
            <span class="card-subtitle">临近截止或提交率低的任务</span>
          </div>

          <div v-if="urgentAssignments.length" class="panel-list mt-3">
            <div v-for="item in urgentAssignments" :key="item.homeworkId" class="panel-list-item glass-hover">
              <div class="card-row-between mb-2">
                <strong class="item-title">{{ item.title }}</strong>
                <StatusTag kind="assignment" :value="item.status" />
              </div>
              <p class="item-desc">{{ getHomeworkDisplayClasses(item) }}</p>

              <div class="item-footer mt-3">
                <div class="chip-row">
                  <span class="stat-pill glass">未交 <b class="text-danger">{{ item.pendingCount }}</b></span>
                  <span class="stat-pill glass">待订正 <b class="text-warning">{{ item.revisionRequiredCount }}</b></span>
                  <span class="stat-pill glass alert">{{ getRelativeDeadline(item.deadlineAt) }}</span>
                </div>
                <div class="actions-row">
                  <el-button size="small" @click="router.push(`/assignments/${item.homeworkId}`)">详情</el-button>
                  <el-button size="small" type="primary" plain @click="router.push(`/assignments/${item.homeworkId}/grading`)">
                    去批改
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="empty-state mt-4">
            <el-empty description="当前没有需要优先跟进的作业。" :image-size="80" />
          </div>
        </div>
      </el-col>

      <el-col :span="10">
        <div class="page-stack h-full" style="gap: 1.5rem;">
          <div class="surface-card section-card insight-box">
            <div class="card-header pb-3">
              <span class="card-title">数据提示</span>
            </div>
            <div class="insight-list mt-2">
              <div v-for="item in dashboardHighlights" :key="item" class="insight-item">
                <p>{{ item }}</p>
              </div>
            </div>
          </div>

          <div class="surface-card section-card flex-1">
            <div class="card-header pb-3">
              <span class="card-title">最近下发</span>
            </div>
            <div v-if="recentAssignments.length" class="panel-list compact-list mt-3">
              <div v-for="item in recentAssignments" :key="item.homeworkId" class="panel-list-item glass-hover compact">
                <div class="card-row-between">
                  <div class="truncate pr-3">
                    <strong class="item-title truncate block">{{ item.title }}</strong>
                    <p class="item-desc mt-1">{{ item.subjectName }} · {{ formatDateTime(item.deadlineAt).split(' ')[0] }} 截止</p>
                  </div>
                  <StatusTag kind="assignment" :value="item.status" />
                </div>
              </div>
            </div>
            <div v-else class="empty-state mt-4">
              <el-empty description="暂无历史下发记录" :image-size="60" />
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.mb-4 { margin-bottom: 24px; }
.mt-2 { margin-top: 8px; }
.mt-3 { margin-top: 16px; }
.mt-4 { margin-top: 24px; }
.pb-3 { padding-bottom: 12px; }
.pb-4 { padding-bottom: 16px; }
.h-full { height: 100%; display: flex; flex-direction: column; }
.flex-1 { flex: 1; }
.block { display: block; }
.truncate { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 100%; }
.pr-3 { padding-right: 12px; }

.grid-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1.5rem;
}

.quick-actions-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.text-danger { color: #e11d48; }
.text-warning { color: #d97706; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.card-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: #0f172a;
}

.card-subtitle {
  font-size: 0.85rem;
  color: #64748b;
}

.surface-card:hover {
  transform: translateY(-3px);
}

.panel-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-list.compact-list {
  gap: 8px;
}

.panel-list-item {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  padding: 1.25rem;
  transition: all 0.3s ease;
}

.panel-list-item.compact {
  padding: 1rem;
  border-radius: 12px;
}

.panel-list-item.glass-hover:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: translateY(-2px);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.08);
}

.item-title {
  font-size: 1.05rem;
  color: #1e293b;
}

.item-desc {
  margin: 0;
  font-size: 0.88rem;
  color: #64748b;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-pill.glass {
  background: rgba(241, 245, 249, 0.6);
  border: 1px solid rgba(226, 232, 240, 0.8);
  color: #475569;
  border-radius: 8px;
  padding: 4px 10px;
  font-size: 0.8rem;
}

.stat-pill.glass.alert {
  background: rgba(254, 242, 242, 0.6);
  border-color: rgba(254, 226, 226, 0.8);
  color: #b91c1c;
}

.actions-row {
  display: flex;
  gap: 8px;
}

.insight-box {
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.7) 0%, rgba(248, 250, 252, 0.85) 100%);
}

.insight-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.insight-item {
  position: relative;
  padding-left: 1rem;
}

.insight-item::before {
  content: '•';
  position: absolute;
  left: 0;
  top: -1px;
  color: #6366f1;
  font-size: 1rem;
  font-weight: 700;
}

.insight-item p {
  margin: 0;
  font-size: 0.9rem;
  color: #334155;
  line-height: 1.5;
}
</style>
