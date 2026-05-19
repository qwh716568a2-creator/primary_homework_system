<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatDateTime, getHomeworkDisplayClasses, percentFromRate } from '@/utils/teacher-portal-view'

const store = useTeacherPortalStore()

const filters = reactive({
  classId: '' as string | number,
  subjectCode: '',
  dateRange: [] as string[]
})

const filteredHomeworks = computed(() => {
  const [start, end] = filters.dateRange
  const startTime = start ? new Date(start).getTime() : null
  const endTime = end ? new Date(end).getTime() : null

  return store.homeworks.filter((item) => {
    if (filters.subjectCode && item.subjectCode !== filters.subjectCode) return false

    if (
      filters.classId &&
      !store.classRelations.some(
        (relation) =>
          `${relation.classId}` === `${filters.classId}` &&
          item.classNames.includes(relation.className)
      )
    ) {
      return false
    }

    if (startTime || endTime) {
      const deadline = new Date(item.deadlineAt.replace(' ', 'T')).getTime()
      if (startTime && deadline < startTime) return false
      if (endTime && deadline > endTime + 24 * 60 * 60 * 1000 - 1) return false
    }

    return true
  })
})

const metricCards = computed(() => [
  {
    label: '发布作业',
    value: `${store.homeworkOverview.publishCount}`,
    note: '当前筛选范围内的作业数量'
  },
  {
    label: '整体提交率',
    value: `${percentFromRate(store.homeworkOverview.submissionRate)}%`,
    note: '学生是否顺利完成并提交'
  },
  {
    label: '按时率',
    value: `${percentFromRate(store.homeworkOverview.onTimeRate)}%`,
    note: '用于判断提醒是否及时'
  },
  {
    label: '待订正率',
    value: `${percentFromRate(store.homeworkOverview.revisionRequiredRate)}%`,
    note: '需要持续跟进的比例'
  },
  {
    label: '批改完成率',
    value: `${percentFromRate(store.homeworkOverview.reviewRate)}%`,
    note: '老师已处理完成的比例'
  }
])

const detailRows = computed(() =>
  filteredHomeworks.value.map((item) => {
    const total = item.submittedCount + item.pendingCount
    return {
      ...item,
      completionRate: total > 0 ? Math.round((item.submittedCount / total) * 100) : 0,
      revisionRate: item.submittedCount > 0 ? Math.round((item.revisionRequiredCount / item.submittedCount) * 100) : 0
    }
  })
)

const subjectRows = computed(() => {
  const map = new Map<
    string,
    { subjectName: string; homeworkCount: number; submittedCount: number; pendingCount: number; revisionRequiredCount: number }
  >()

  detailRows.value.forEach((item) => {
    const current = map.get(item.subjectCode) ?? {
      subjectName: item.subjectName,
      homeworkCount: 0,
      submittedCount: 0,
      pendingCount: 0,
      revisionRequiredCount: 0
    }

    map.set(item.subjectCode, {
      subjectName: current.subjectName,
      homeworkCount: current.homeworkCount + 1,
      submittedCount: current.submittedCount + item.submittedCount,
      pendingCount: current.pendingCount + item.pendingCount,
      revisionRequiredCount: current.revisionRequiredCount + item.revisionRequiredCount
    })
  })

  return Array.from(map.values()).map((item) => {
    const total = item.submittedCount + item.pendingCount
    return {
      ...item,
      completionRate: total > 0 ? Math.round((item.submittedCount / total) * 100) : 0
    }
  })
})

const trendRows = computed(() =>
  filteredHomeworks.value
    .slice()
    .sort((left, right) => `${left.deadlineAt}`.localeCompare(`${right.deadlineAt}`))
    .slice(-6)
    .map((item) => {
      const total = item.submittedCount + item.pendingCount
      return {
        title: item.title,
        deadlineAt: item.deadlineAt,
        rate: total > 0 ? Math.round((item.submittedCount / total) * 100) : 0
      }
    })
)

async function loadPage() {
  try {
    await Promise.all([store.loadTeachingClasses(), store.loadHomeworkList(), store.loadHomeworkOverview()])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '统计数据加载失败')
  }
}

async function applyFilters() {
  const [startDate, endDate] = filters.dateRange

  try {
    await Promise.all([
      store.loadHomeworkList({
        classId: filters.classId || undefined,
        subjectCode: filters.subjectCode || undefined
      }),
      store.loadHomeworkOverview({
        classId: filters.classId || undefined,
        subjectCode: filters.subjectCode || undefined,
        startDate: startDate ? `${startDate} 00:00:00` : undefined,
        endDate: endDate ? `${endDate} 23:59:59` : undefined
      })
    ])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '筛选统计失败')
  }
}

function resetFilters() {
  filters.classId = ''
  filters.subjectCode = ''
  filters.dateRange = []
  void loadPage()
}

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div class="teacher-page teacher-analytics-next">
    <section class="teacher-page__hero">
      <span class="teacher-page__eyebrow">Execution Analytics</span>
      <h2>把作业执行效果、回流情况和学科差异放回一张运营看板里。</h2>
      <p>先看提交率和待订正率，再决定今天应该先催交、先批改，还是先做班级复盘。</p>
    </section>

    <section class="teacher-metrics teacher-metrics--five">
      <article v-for="item in metricCards" :key="item.label" class="teacher-metric">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.note }}</small>
      </article>
    </section>

    <section class="teacher-toolbar">
      <div class="teacher-toolbar__row">
        <el-select v-model="filters.classId" clearable placeholder="全部班级">
          <el-option
            v-for="item in store.classOptions"
            :key="item.classId"
            :label="item.className"
            :value="item.classId"
          />
        </el-select>

        <el-select v-model="filters.subjectCode" clearable placeholder="全部学科">
          <el-option
            v-for="item in store.subjectOptions"
            :key="item.subjectCode"
            :label="item.subjectName"
            :value="item.subjectCode"
          />
        </el-select>

        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </div>
      <div class="teacher-toolbar__row">
        <button type="button" class="teacher-button" @click="resetFilters">重置</button>
        <button type="button" class="teacher-button--primary" @click="applyFilters">更新看板</button>
      </div>
    </section>

    <section class="teacher-two-column">
      <div class="teacher-stack">
        <article class="teacher-panel">
          <header class="teacher-panel__head">
            <div>
              <span class="teacher-kicker">Trend</span>
              <h3>最近作业执行趋势</h3>
              <p>观察最近几次作业的提交率变化，判断节奏是否稳定。</p>
            </div>
          </header>

          <div v-if="trendRows.length" class="teacher-analytics-next__trend">
            <article v-for="item in trendRows" :key="`${item.title}-${item.deadlineAt}`" class="teacher-analytics-next__trend-item">
              <div class="teacher-analytics-next__trend-head">
                <strong>{{ item.title }}</strong>
                <span>{{ item.rate }}%</span>
              </div>
              <small>{{ formatDateTime(item.deadlineAt) }}</small>
              <div class="teacher-analytics-next__track">
                <div class="teacher-analytics-next__fill" :style="{ width: `${item.rate}%` }"></div>
              </div>
            </article>
          </div>
          <div v-else class="teacher-empty">当前筛选下暂无趋势数据。</div>
        </article>

        <article class="teacher-table">
          <div class="teacher-table__head teacher-analytics-next__ledger-head">
            <span>作业</span>
            <span>执行概况</span>
            <span>提交率</span>
            <span>待订正率</span>
            <span>截止时间</span>
          </div>
          <div v-if="detailRows.length">
            <div v-for="item in detailRows" :key="item.homeworkId" class="teacher-table__row teacher-analytics-next__ledger-row">
              <div class="teacher-table__title">
                <strong>{{ item.title }}</strong>
                <small>{{ item.subjectName }} · {{ getHomeworkDisplayClasses(item) }}</small>
              </div>
              <div class="teacher-analytics-next__chips">
                <span class="teacher-pill">已交 {{ item.submittedCount }}</span>
                <span class="teacher-pill">未交 {{ item.pendingCount }}</span>
                <span class="teacher-pill">待订正 {{ item.revisionRequiredCount }}</span>
              </div>
              <strong>{{ item.completionRate }}%</strong>
              <strong>{{ item.revisionRate }}%</strong>
              <span class="teacher-inline-note">{{ formatDateTime(item.deadlineAt) }}</span>
            </div>
          </div>
          <div v-else class="teacher-empty">当前筛选下暂无统计明细。</div>
        </article>
      </div>

      <div class="teacher-stack">
        <article class="teacher-panel">
          <header class="teacher-panel__head">
            <div>
              <span class="teacher-kicker">Subject Signals</span>
              <h3>学科表现</h3>
              <p>先判断哪门学科最值得排优先级。</p>
            </div>
          </header>

          <div v-if="subjectRows.length" class="teacher-analytics-next__subjects">
            <div v-for="item in subjectRows" :key="item.subjectName" class="teacher-analytics-next__subject-row">
              <div class="teacher-table__title">
                <strong>{{ item.subjectName }}</strong>
                <small>{{ item.homeworkCount }} 份作业</small>
              </div>
              <div class="teacher-analytics-next__subject-meta">
                <span class="teacher-pill">提交率 {{ item.completionRate }}%</span>
                <span class="teacher-pill">待订正 {{ item.revisionRequiredCount }}</span>
              </div>
            </div>
          </div>
          <div v-else class="teacher-empty">暂无学科统计。</div>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.teacher-analytics-next__trend,
.teacher-analytics-next__subjects {
  display: grid;
}

.teacher-analytics-next__trend-item,
.teacher-analytics-next__subject-row {
  display: grid;
  gap: 8px;
  padding: 16px 20px;
  border-top: 1px solid #ede7db;
}

.teacher-analytics-next__trend-item:first-child,
.teacher-analytics-next__subject-row:first-child {
  border-top: 0;
}

.teacher-analytics-next__trend-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.teacher-analytics-next__trend-head strong,
.teacher-analytics-next__subject-row strong {
  color: var(--teacher-ink);
  font-size: 15px;
}

.teacher-analytics-next__track {
  height: 8px;
  background: #ece6d8;
}

.teacher-analytics-next__fill {
  height: 100%;
  background: var(--teacher-accent);
}

.teacher-analytics-next__ledger-head,
.teacher-analytics-next__ledger-row {
  grid-template-columns: minmax(0, 1.4fr) minmax(200px, 1fr) 80px 96px 120px;
}

.teacher-analytics-next__chips,
.teacher-analytics-next__subject-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 1280px) {
  .teacher-analytics-next__ledger-head,
  .teacher-analytics-next__ledger-row {
    grid-template-columns: 1fr;
  }
}
</style>
