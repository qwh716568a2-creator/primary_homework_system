<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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

const metricItems = computed(() => [
  {
    label: '进行中的作业',
    value: store.homeworks.filter((item) => item.status === 'published').length,
    note: '当前还在执行周期内的作业'
  },
  {
    label: '待批改',
    value: store.homeworks.reduce((sum, item) => sum + item.pendingCount, 0),
    note: '优先需要处理的已提交作业'
  },
  {
    label: '待订正',
    value: store.homeworks.reduce((sum, item) => sum + item.revisionRequiredCount, 0),
    note: '已经流回教师侧的订正任务'
  },
  {
    label: '整体批改率',
    value: `${percentFromRate(store.homeworkOverview.reviewRate)}%`,
    note: '当前范围内已经完成闭环的比例'
  }
])

const focusQueue = computed(() =>
  [...store.pendingHomeworkList]
    .sort(
      (left, right) =>
        right.pendingCount + right.revisionRequiredCount * 2 -
        (left.pendingCount + left.revisionRequiredCount * 2)
    )
    .slice(0, 6)
)

const recentRows = computed(() => [...store.recentAssignments].slice(0, 6))

const insightRows = computed(() => [
  {
    label: '班级覆盖',
    value: `${store.classOptions.length} 个`,
    note: '当前账号能够操作的班级范围'
  },
  {
    label: '提交率',
    value: `${percentFromRate(store.homeworkOverview.submissionRate)}%`,
    note: '学生是否顺利完成并提交'
  },
  {
    label: '按时率',
    value: `${percentFromRate(store.homeworkOverview.onTimeRate)}%`,
    note: '判断是否需要提前提醒或催交'
  },
  {
    label: '待订正率',
    value: `${percentFromRate(store.homeworkOverview.revisionRequiredRate)}%`,
    note: '识别需要持续跟进的作业'
  }
])

async function loadDashboard() {
  try {
    await store.initializeWorkspace()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '教师工作台加载失败')
  }
}

onMounted(() => {
  void loadDashboard()
})
</script>

<template>
  <div class="teacher-page teacher-dashboard-next" v-loading="workspaceLoading">
    <section class="teacher-page__hero">
      <span class="teacher-page__eyebrow">Teacher Operations</span>
      <h2>把发布、批改、提醒和复盘整理成一条清晰的工作流</h2>
      <p>这里不是展示页，而是一块真正帮助老师推进日常事务的操作台。先处理今天的队列，再跟进订正，最后安排新的发布与通知。</p>
    </section>

    <section class="teacher-metrics">
      <article v-for="item in metricItems" :key="item.label" class="teacher-metric">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.note }}</small>
      </article>
    </section>

    <section class="teacher-two-column">
      <article class="teacher-panel">
        <header class="teacher-panel__head">
          <div>
            <span class="teacher-kicker">Priority Queue</span>
            <h3>今天优先处理</h3>
            <p>把今天最应该先处理的作业放到前面，避免在多个页面里来回切换。</p>
          </div>
          <button type="button" class="teacher-link-button" @click="router.push('/grading-center')">
            进入批改中心
          </button>
        </header>

        <div v-if="focusQueue.length" class="teacher-dashboard-next__queue">
          <button
            v-for="item in focusQueue"
            :key="item.homeworkId"
            type="button"
            class="teacher-dashboard-next__queue-item"
            @click="router.push(`/assignments/${item.homeworkId}/grading`)"
          >
            <div class="teacher-table__title">
              <strong>{{ item.title }}</strong>
              <small>{{ item.subjectName }} · {{ getHomeworkDisplayClasses(item) }}</small>
            </div>

            <div class="teacher-dashboard-next__queue-meta">
              <span class="teacher-pill">待批改 {{ item.pendingCount }}</span>
              <span class="teacher-pill">待订正 {{ item.revisionRequiredCount }}</span>
              <small>{{ getRelativeDeadline(item.deadlineAt) }}</small>
            </div>
          </button>
        </div>
        <div v-else class="teacher-empty">当前没有需要优先处理的作业。</div>
      </article>

      <div class="teacher-stack">
        <article class="teacher-panel">
          <header class="teacher-panel__head">
            <div>
              <span class="teacher-kicker">Ledger</span>
              <h3>最近发布</h3>
              <p>从这里直接进入详情、批改，或继续推进执行。</p>
            </div>
            <button type="button" class="teacher-link-button" @click="router.push('/assignments')">
              查看全部
            </button>
          </header>

          <div v-if="recentRows.length" class="teacher-table teacher-dashboard-next__ledger">
            <div class="teacher-table__head teacher-dashboard-next__ledger-head">
              <span>作业</span>
              <span>截止时间</span>
              <span>状态</span>
            </div>

            <div
              v-for="item in recentRows"
              :key="item.homeworkId"
              class="teacher-table__row teacher-dashboard-next__ledger-row"
            >
              <button
                type="button"
                class="teacher-dashboard-next__title-btn"
                @click="router.push(`/assignments/${item.homeworkId}`)"
              >
                <span class="teacher-table__title">
                  <strong>{{ item.title }}</strong>
                  <small>{{ item.subjectName }} · {{ getHomeworkDisplayClasses(item) }}</small>
                </span>
              </button>
              <span class="teacher-inline-note">{{ formatDateTime(item.deadlineAt) }}</span>
              <StatusTag kind="assignment" :value="item.status" />
            </div>
          </div>
          <div v-else class="teacher-empty">当前还没有可以展示的作业记录。</div>
        </article>

        <article class="teacher-panel">
          <header class="teacher-panel__head">
            <div>
              <span class="teacher-kicker">Signals</span>
              <h3>执行信号</h3>
              <p>快速判断今天更适合催交、批改，还是进入复盘。</p>
            </div>
            <button type="button" class="teacher-link-button" @click="router.push('/statistics')">
              查看统计
            </button>
          </header>

          <div class="teacher-dashboard-next__signals">
            <div v-for="item in insightRows" :key="item.label" class="teacher-dashboard-next__signal">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <small>{{ item.note }}</small>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>
