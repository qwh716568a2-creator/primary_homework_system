<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { DataAnalysis, Histogram, Opportunity, TrendCharts } from '@element-plus/icons-vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatDateTime, percentFromRate } from '@/utils/teacher-portal-view'
import type { HomeworkListItem } from '@/types/teacher-portal'

const TEXT = {
  heroEyebrow: '\u6559\u5b66\u8fd0\u8425\u770b\u677f',
  heroTitle: '\u628a\u73ed\u7ea7\u6267\u884c\u3001\u5b66\u79d1\u8868\u73b0\u548c\u5f85\u8ddf\u8fdb\u4e8b\u9879\u538b\u7f29\u6210\u4e00\u773c\u80fd\u8bfb\u61c2\u7684\u7ed3\u679c\u9875\u3002',
  heroDesc:
    '\u8fd9\u9875\u4e0d\u662f\u628a\u6570\u5b57\u5e73\u94fa\u4e0a\u6765\uff0c\u800c\u662f\u628a\u6559\u5e08\u6700\u5173\u5fc3\u7684\u63d0\u4ea4\u3001\u6279\u6539\u3001\u8986\u76d6\u548c\u98ce\u9669\u73ed\u7ea7\u6574\u7406\u6210\u4e00\u5957\u53ef\u4ee5\u5feb\u901f\u51b3\u7b56\u7684\u6559\u5b66\u89c6\u56fe\u3002',
  avgSubmitRate: '\u5e73\u5747\u63d0\u4ea4\u7387',
  reviewDoneRate: '\u6279\u6539\u5b8c\u6210\u7387',
  classCoverage: '\u8986\u76d6\u73ed\u7ea7',
  allSubjects: '\u5168\u90e8\u5b66\u79d1',
  allClasses: '\u5168\u90e8\u73ed\u7ea7',
  allStatuses: '\u5168\u90e8\u72b6\u6001',
  statusPublished: '\u8fdb\u884c\u4e2d',
  statusDraft: '\u8349\u7a3f',
  statusRevoked: '\u5df2\u64a4\u56de',
  statusClosed: '\u5df2\u7ed3\u675f',
  dateFrom: '\u5f00\u59cb\u65e5\u671f',
  dateTo: '\u7ed3\u675f\u65e5\u671f',
  dateRangeTo: '\u81f3',
  resetFilters: '\u91cd\u7f6e\u7b5b\u9009',
  subjectEyebrow: '\u5b66\u79d1\u8868\u73b0',
  subjectTitle: '\u5b66\u79d1\u70ed\u5ea6\u5206\u5e03',
  classEyebrow: '\u73ed\u7ea7\u6267\u884c',
  classTitle: '\u73ed\u7ea7\u63d0\u4ea4\u5bf9\u6bd4',
  insightEyebrow: '\u8fd0\u8425\u63d0\u793a',
  insightTitle: '\u5efa\u8bae\u4f18\u5148\u8ddf\u8fdb\u7684\u65b9\u5411',
  recentEyebrow: '\u8fd1\u671f\u4f5c\u4e1a',
  recentTitle: '\u6700\u65b0\u4efb\u52a1\u63a8\u8fdb\u60c5\u51b5',
  teachingReminder: '\u6559\u5b66\u63d0\u9192',
  noSubjectData: '\u5f53\u524d\u7b5b\u9009\u6761\u4ef6\u4e0b\u8fd8\u6ca1\u6709\u5b66\u79d1\u6570\u636e\u3002',
  noClassData: '\u5f53\u524d\u7b5b\u9009\u6761\u4ef6\u4e0b\u8fd8\u6ca1\u6709\u73ed\u7ea7\u6267\u884c\u6570\u636e\u3002',
  noRecentData: '\u5f53\u524d\u7b5b\u9009\u6761\u4ef6\u4e0b\u8fd8\u6ca1\u6709\u4f5c\u4e1a\u8bb0\u5f55\u3002',
  assignmentsUnit: '\u4efd\u4f5c\u4e1a',
  needRevision: '\u5f85\u8ba2\u6b63',
  submittedLabel: '\u5df2\u4ea4',
  pendingLabel: '\u672a\u4ea4',
  revisionLabel: '\u5f85\u8ba2\u6b63',
  rowsUnit: '\u6761',
  loadingFailed: '\u7edf\u8ba1\u5206\u6790\u52a0\u8f7d\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u3002',
  cardSubjectsUnit: '\u95e8\u5b66\u79d1',
  cardClassesUnit: '\u4e2a\u73ed\u7ea7'
} as const

const store = useTeacherPortalStore()

const filters = reactive({
  subjectCode: '',
  className: '',
  status: '',
  dateRange: [] as string[]
})

const loading = computed(
  () => store.loading.homeworks || store.loading.overview || store.loading.classes
)

const subjectOptions = computed(() => store.subjectOptions)
const classNameOptions = computed(() =>
  Array.from(new Set(store.homeworks.flatMap((item) => item.classNames).filter(Boolean)))
)

function normalizeDate(value?: string) {
  if (!value) return null
  const date = new Date(value.includes('T') ? value : value.replace(' ', 'T'))
  return Number.isNaN(date.getTime()) ? null : date
}

function isWithinDateRange(item: HomeworkListItem) {
  if (filters.dateRange.length !== 2) return true
  const current = normalizeDate(item.deadlineAt)
  const start = normalizeDate(filters.dateRange[0])
  const end = normalizeDate(filters.dateRange[1])

  if (!current || !start || !end) return true
  return current.getTime() >= start.getTime() && current.getTime() <= end.getTime()
}

const filteredHomeworks = computed(() =>
  store.homeworks.filter((item) => {
    const subjectMatch = !filters.subjectCode || item.subjectCode === filters.subjectCode
    const classMatch = !filters.className || item.classNames.includes(filters.className)
    const statusMatch = !filters.status || item.status === filters.status
    const dateMatch = isWithinDateRange(item)

    return subjectMatch && classMatch && statusMatch && dateMatch
  })
)

const summary = computed(() => {
  const totalAssignments = filteredHomeworks.value.length
  const activeAssignments = filteredHomeworks.value.filter((item) => item.status === 'published').length
  const draftAssignments = filteredHomeworks.value.filter((item) => item.status === 'draft').length
  const submitted = filteredHomeworks.value.reduce((sum, item) => sum + Number(item.submittedCount || 0), 0)
  const pending = filteredHomeworks.value.reduce((sum, item) => sum + Number(item.pendingCount || 0), 0)
  const revision = filteredHomeworks.value.reduce(
    (sum, item) => sum + Number(item.revisionRequiredCount || 0),
    0
  )
  const totalStudents = submitted + pending
  const classCount = new Set(filteredHomeworks.value.flatMap((item) => item.classNames)).size

  return {
    totalAssignments,
    activeAssignments,
    draftAssignments,
    submitted,
    pending,
    revision,
    classCount,
    submissionRate: totalStudents
      ? Math.round((submitted / totalStudents) * 100)
      : percentFromRate(store.homeworkOverview.submissionRate),
    reviewRate: percentFromRate(store.homeworkOverview.reviewRate),
    onTimeRate: percentFromRate(store.homeworkOverview.onTimeRate)
  }
})

const heroMetrics = computed(() => [
  {
    key: 'submission',
    label: TEXT.avgSubmitRate,
    value: `${summary.value.submissionRate}%`,
    detail: `${TEXT.submittedLabel} ${summary.value.submitted} · ${TEXT.pendingLabel} ${summary.value.pending}`,
    icon: TrendCharts,
    tone: 'blue'
  },
  {
    key: 'review',
    label: TEXT.reviewDoneRate,
    value: `${summary.value.reviewRate}%`,
    detail: `${TEXT.needRevision} ${summary.value.revision} · 按时 ${summary.value.onTimeRate}%`,
    icon: DataAnalysis,
    tone: 'orange'
  },
  {
    key: 'coverage',
    label: TEXT.classCoverage,
    value: `${summary.value.classCount}`,
    detail: `${TEXT.statusPublished} ${summary.value.activeAssignments} · ${TEXT.statusDraft} ${summary.value.draftAssignments}`,
    icon: Histogram,
    tone: 'teal'
  }
])

const subjectAnalysis = computed(() => {
  const map = new Map<
    string,
    {
      subjectCode: string
      subjectName: string
      count: number
      submitted: number
      pending: number
      revision: number
    }
  >()

  filteredHomeworks.value.forEach((item) => {
    const current = map.get(item.subjectCode) ?? {
      subjectCode: item.subjectCode,
      subjectName: item.subjectName,
      count: 0,
      submitted: 0,
      pending: 0,
      revision: 0
    }

    current.count += 1
    current.submitted += Number(item.submittedCount || 0)
    current.pending += Number(item.pendingCount || 0)
    current.revision += Number(item.revisionRequiredCount || 0)
    map.set(item.subjectCode, current)
  })

  return Array.from(map.values())
    .map((item) => {
      const total = item.submitted + item.pending
      return {
        ...item,
        submissionRate: total ? Math.round((item.submitted / total) * 100) : 0
      }
    })
    .sort((left, right) => right.count - left.count)
})

const classAnalysis = computed(() => {
  const map = new Map<
    string,
    {
      className: string
      assignmentCount: number
      submitted: number
      pending: number
      revision: number
    }
  >()

  filteredHomeworks.value.forEach((item) => {
    const divisor = item.classNames.length || 1

    item.classNames.forEach((className) => {
      const current = map.get(className) ?? {
        className,
        assignmentCount: 0,
        submitted: 0,
        pending: 0,
        revision: 0
      }

      current.assignmentCount += 1
      current.submitted += Number((item.submittedCount || 0) / divisor)
      current.pending += Number((item.pendingCount || 0) / divisor)
      current.revision += Number((item.revisionRequiredCount || 0) / divisor)
      map.set(className, current)
    })
  })

  return Array.from(map.values())
    .map((item) => {
      const total = item.submitted + item.pending
      return {
        ...item,
        submitted: Math.round(item.submitted),
        pending: Math.round(item.pending),
        revision: Math.round(item.revision),
        submissionRate: total ? Math.round((item.submitted / total) * 100) : 0
      }
    })
    .sort((left, right) => right.assignmentCount - left.assignmentCount)
})

const topSubjects = computed(() => subjectAnalysis.value.slice(0, 4))
const topClasses = computed(() => classAnalysis.value.slice(0, 5))
const recentAssignments = computed(() =>
  [...filteredHomeworks.value]
    .sort((left, right) => `${right.deadlineAt}`.localeCompare(`${left.deadlineAt}`))
    .slice(0, 6)
    .map((item) => {
      const total = Number(item.submittedCount || 0) + Number(item.pendingCount || 0)
      return {
        ...item,
        progressRate: total ? Math.round((Number(item.submittedCount || 0) / total) * 100) : 0
      }
    })
)

const insightList = computed(() => {
  const bestSubject = topSubjects.value[0]
  const focusClass = [...topClasses.value].sort((left, right) => left.submissionRate - right.submissionRate)[0]

  return [
    `当前共有 ${summary.value.totalAssignments} 份作业进入统计范围，覆盖 ${summary.value.classCount} 个班级。`,
    bestSubject
      ? `${bestSubject.subjectName} 当前提交率 ${bestSubject.submissionRate}%，是本阶段最稳定的学科。`
      : '当前还没有形成学科对比数据。', 
    focusClass
      ? `${focusClass.className} 提交率 ${focusClass.submissionRate}%，建议优先跟进未交与待订正学生。`
      : '当前还没有班级执行差异数据。'
  ]
})

function subjectBarStyle(rate: number) {
  return {
    width: `${Math.max(rate, 8)}%`
  }
}

function classStackStyle(value: number, total: number) {
  if (!total) return { width: '0%' }
  return {
    width: `${Math.max(Math.round((value / total) * 100), value > 0 ? 8 : 0)}%`
  }
}

function resetFilters() {
  filters.subjectCode = ''
  filters.className = ''
  filters.status = ''
  filters.dateRange = []
}

async function loadStatisticsPage() {
  try {
    await Promise.all([store.loadTeachingClasses(), store.loadHomeworkList(), store.loadHomeworkOverview()])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : TEXT.loadingFailed)
  }
}

onMounted(loadStatisticsPage)
</script>

<template>
  <div class="teacher-statistics" v-loading="loading">
    <section class="teacher-statistics__hero">
      <div class="teacher-statistics__halo teacher-statistics__halo--blue"></div>
      <div class="teacher-statistics__halo teacher-statistics__halo--orange"></div>

      <div class="teacher-statistics__hero-main">
        <div class="teacher-statistics__hero-copy">
          <span class="teacher-statistics__eyebrow">{{ TEXT.heroEyebrow }}</span>
          <h1>{{ TEXT.heroTitle }}</h1>
          <p>{{ TEXT.heroDesc }}</p>
        </div>

        <div class="teacher-statistics__hero-side">
          <div v-for="metric in heroMetrics" :key="metric.key" class="hero-metric" :class="`hero-metric--${metric.tone}`">
            <div class="hero-metric__icon">
              <el-icon><component :is="metric.icon" /></el-icon>
            </div>
            <div class="hero-metric__text">
              <span>{{ metric.label }}</span>
              <strong>{{ metric.value }}</strong>
              <small>{{ metric.detail }}</small>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="teacher-statistics__filters">
      <div class="filter-grid">
        <el-select v-model="filters.subjectCode" :placeholder="TEXT.allSubjects" clearable>
          <el-option
            v-for="subject in subjectOptions"
            :key="subject.subjectCode"
            :label="subject.subjectName"
            :value="subject.subjectCode"
          />
        </el-select>

        <el-select v-model="filters.className" :placeholder="TEXT.allClasses" clearable>
          <el-option
            v-for="className in classNameOptions"
            :key="className"
            :label="className"
            :value="className"
          />
        </el-select>

        <el-select v-model="filters.status" :placeholder="TEXT.allStatuses" clearable>
          <el-option :label="TEXT.statusPublished" value="published" />
          <el-option :label="TEXT.statusDraft" value="draft" />
          <el-option :label="TEXT.statusRevoked" value="revoked" />
          <el-option :label="TEXT.statusClosed" value="closed" />
        </el-select>

        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          :range-separator="TEXT.dateRangeTo"
          :start-placeholder="TEXT.dateFrom"
          :end-placeholder="TEXT.dateTo"
          value-format="YYYY-MM-DD"
          unlink-panels
        />
      </div>

      <el-button plain @click="resetFilters">{{ TEXT.resetFilters }}</el-button>
    </section>

    <section class="teacher-statistics__body">
      <div class="stats-grid stats-grid--top">
        <article class="stats-panel stats-panel--subject">
          <header class="stats-panel__header">
            <div>
              <span class="stats-panel__eyebrow">{{ TEXT.subjectEyebrow }}</span>
              <h3>{{ TEXT.subjectTitle }}</h3>
            </div>
            <span class="stats-panel__tag">{{ topSubjects.length }} {{ TEXT.cardSubjectsUnit }}</span>
          </header>

          <div class="subject-list">
            <article v-for="subject in topSubjects" :key="subject.subjectCode" class="subject-row">
              <div class="subject-row__meta">
                <strong>{{ subject.subjectName }}</strong>
                <span>{{ subject.count }} {{ TEXT.assignmentsUnit }} · {{ TEXT.needRevision }} {{ subject.revision }}</span>
              </div>

              <div class="subject-row__track">
                <div class="subject-row__bar" :style="subjectBarStyle(subject.submissionRate)"></div>
              </div>

              <strong class="subject-row__value">{{ subject.submissionRate }}%</strong>
            </article>

            <div v-if="!topSubjects.length" class="stats-panel__empty">{{ TEXT.noSubjectData }}</div>
          </div>
        </article>

        <article class="stats-panel stats-panel--class">
          <header class="stats-panel__header">
            <div>
              <span class="stats-panel__eyebrow">{{ TEXT.classEyebrow }}</span>
              <h3>{{ TEXT.classTitle }}</h3>
            </div>
            <span class="stats-panel__tag">{{ topClasses.length }} {{ TEXT.cardClassesUnit }}</span>
          </header>

          <div class="class-list">
            <article v-for="item in topClasses" :key="item.className" class="class-row">
              <div class="class-row__head">
                <strong>{{ item.className }}</strong>
                <span>{{ item.submissionRate }}%</span>
              </div>

              <div class="class-row__stack">
                <div class="class-row__segment class-row__segment--submitted" :style="classStackStyle(item.submitted, item.submitted + item.pending + item.revision)"></div>
                <div class="class-row__segment class-row__segment--pending" :style="classStackStyle(item.pending, item.submitted + item.pending + item.revision)"></div>
                <div class="class-row__segment class-row__segment--revision" :style="classStackStyle(item.revision, item.submitted + item.pending + item.revision)"></div>
              </div>

              <div class="class-row__legend">
                <span>{{ TEXT.submittedLabel }} {{ item.submitted }}</span>
                <span>{{ TEXT.pendingLabel }} {{ item.pending }}</span>
                <span>{{ TEXT.revisionLabel }} {{ item.revision }}</span>
              </div>
            </article>

            <div v-if="!topClasses.length" class="stats-panel__empty">{{ TEXT.noClassData }}</div>
          </div>
        </article>
      </div>

      <div class="stats-grid stats-grid--bottom">
        <article class="stats-panel stats-panel--insight">
          <header class="stats-panel__header">
            <div>
              <span class="stats-panel__eyebrow">{{ TEXT.insightEyebrow }}</span>
              <h3>{{ TEXT.insightTitle }}</h3>
            </div>
            <span class="stats-panel__tag">
              <el-icon><Opportunity /></el-icon>
              {{ TEXT.teachingReminder }}
            </span>
          </header>

          <ul class="insight-list">
            <li v-for="item in insightList" :key="item">{{ item }}</li>
          </ul>
        </article>

        <article class="stats-panel stats-panel--recent">
          <header class="stats-panel__header">
            <div>
              <span class="stats-panel__eyebrow">{{ TEXT.recentEyebrow }}</span>
              <h3>{{ TEXT.recentTitle }}</h3>
            </div>
            <span class="stats-panel__tag">{{ recentAssignments.length }} {{ TEXT.rowsUnit }}</span>
          </header>

          <div class="recent-list">
            <article v-for="item in recentAssignments" :key="item.homeworkId" class="recent-row">
              <div class="recent-row__main">
                <strong>{{ item.title }}</strong>
                <span>{{ item.subjectName }} · {{ item.classNames.join('、') || '未分配班级' }}</span>
              </div>

              <div class="recent-row__meta">
                <span>{{ formatDateTime(item.deadlineAt) }}</span>
                <span>{{ item.progressRate }}%</span>
              </div>

              <div class="recent-row__progress">
                <div class="recent-row__progress-bar" :style="{ width: `${Math.max(item.progressRate, 8)}%` }"></div>
              </div>
            </article>

            <div v-if="!recentAssignments.length" class="stats-panel__empty">{{ TEXT.noRecentData }}</div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.teacher-statistics {
  display: flex;
  flex-direction: column;
  gap: 22px;
  color: #14213d;
}

.teacher-statistics__hero {
  position: relative;
  overflow: hidden;
  padding: 30px 32px;
  border-radius: 30px;
  background:
    radial-gradient(circle at top left, rgba(73, 140, 255, 0.24), transparent 34%),
    radial-gradient(circle at bottom right, rgba(46, 196, 182, 0.18), transparent 38%),
    linear-gradient(135deg, #0f172a 0%, #13213d 55%, #18273f 100%);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.22);
}

.teacher-statistics__halo {
  position: absolute;
  border-radius: 999px;
  filter: blur(18px);
  opacity: 0.8;
}

.teacher-statistics__halo--blue {
  top: -48px;
  right: 180px;
  width: 220px;
  height: 220px;
  background: rgba(74, 144, 255, 0.3);
}

.teacher-statistics__halo--orange {
  bottom: -64px;
  left: 28%;
  width: 180px;
  height: 180px;
  background: rgba(255, 154, 79, 0.18);
}

.teacher-statistics__hero-main {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(420px, 0.95fr);
  gap: 28px;
  align-items: stretch;
}

.teacher-statistics__hero-copy {
  display: flex;
  flex-direction: column;
  gap: 14px;
  color: rgba(255, 255, 255, 0.92);
}

.teacher-statistics__eyebrow {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  padding: 6px 12px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.teacher-statistics__hero-copy h1 {
  margin: 0;
  font-size: 38px;
  line-height: 1.18;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.teacher-statistics__hero-copy p {
  max-width: 640px;
  margin: 0;
  color: rgba(255, 255, 255, 0.72);
  font-size: 15px;
  line-height: 1.75;
}

.teacher-statistics__hero-side {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.hero-metric {
  position: relative;
  overflow: hidden;
  min-height: 180px;
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.hero-metric::after {
  content: '';
  position: absolute;
  inset: auto -18px -28px auto;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  opacity: 0.12;
}

.hero-metric--blue::after {
  background: #2f7cf7;
}

.hero-metric--orange::after {
  background: #ff8a4c;
}

.hero-metric--teal::after {
  background: #11b6b3;
}

.hero-metric__icon {
  width: 48px;
  height: 48px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  font-size: 22px;
  color: white;
  margin-bottom: 28px;
}

.hero-metric--blue .hero-metric__icon {
  background: linear-gradient(135deg, #2f7cf7, #5da5ff);
}

.hero-metric--orange .hero-metric__icon {
  background: linear-gradient(135deg, #ff8a4c, #ff6a3d);
}

.hero-metric--teal .hero-metric__icon {
  background: linear-gradient(135deg, #14b8a6, #29d1cc);
}

.hero-metric__text {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hero-metric__text span {
  color: #6b7b9b;
  font-size: 13px;
}

.hero-metric__text strong {
  font-size: 36px;
  line-height: 1;
  font-weight: 800;
  color: #12213f;
}

.hero-metric__text small {
  color: #7d8dad;
  font-size: 13px;
  line-height: 1.5;
}

.teacher-statistics__filters {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: 16px;
  padding: 18px;
  border: 1px solid rgba(205, 218, 236, 0.86);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 16px 38px rgba(36, 67, 122, 0.08);
}

.filter-grid {
  display: grid;
  grid-template-columns: 160px 160px 160px minmax(320px, 1fr);
  gap: 12px;
}

.filter-grid > * {
  min-width: 0;
}

.teacher-statistics__filters :deep(.el-button) {
  min-width: 112px;
  min-height: 46px;
  border-radius: 16px;
  white-space: nowrap;
}

:deep(.teacher-statistics__filters .el-input__wrapper),
:deep(.teacher-statistics__filters .el-select__wrapper) {
  min-height: 46px;
  border-radius: 16px;
  box-shadow: 0 0 0 1px rgba(196, 210, 230, 0.8) inset;
}

:deep(.teacher-statistics__filters .el-date-editor) {
  width: 100%;
  min-width: 0;
}

.teacher-statistics__body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-grid {
  display: grid;
  gap: 20px;
}

.stats-grid--top {
  grid-template-columns: 1.15fr 1fr;
}

.stats-grid--bottom {
  grid-template-columns: 0.92fr 1.08fr;
}

.stats-panel {
  padding: 24px;
  border: 1px solid rgba(205, 218, 236, 0.88);
  border-radius: 28px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.97), rgba(245, 249, 255, 0.92));
  box-shadow: 0 18px 44px rgba(36, 67, 122, 0.08);
}

.stats-panel__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.stats-panel__eyebrow {
  display: block;
  margin-bottom: 8px;
  color: #88a0c6;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.stats-panel__header h3 {
  margin: 0;
  font-size: 28px;
  line-height: 1.2;
  font-weight: 800;
  color: #16284a;
}

.stats-panel__tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 999px;
  background: #edf4ff;
  color: #376db7;
  font-size: 13px;
  font-weight: 700;
}

.subject-list,
.class-list,
.recent-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.subject-row,
.class-row,
.recent-row {
  padding: 16px 18px;
  border-radius: 20px;
  background: white;
  border: 1px solid rgba(224, 233, 246, 0.92);
}

.subject-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 150px 60px;
  gap: 16px;
  align-items: center;
}

.subject-row__meta strong,
.class-row__head strong,
.recent-row__main strong {
  display: block;
  color: #16284a;
  font-size: 18px;
  font-weight: 800;
}

.subject-row__meta span,
.class-row__legend,
.recent-row__main span,
.recent-row__meta span {
  color: #70809e;
  font-size: 13px;
}

.subject-row__track,
.recent-row__progress {
  height: 10px;
  border-radius: 999px;
  background: #ebf1fb;
  overflow: hidden;
}

.subject-row__bar,
.recent-row__progress-bar {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #2f7cf7, #59a6ff);
}

.subject-row__value {
  justify-self: end;
  color: #18315c;
  font-size: 20px;
  font-weight: 800;
}

.class-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.class-row__head,
.recent-row__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.class-row__head span {
  color: #3771d6;
  font-weight: 800;
}

.class-row__stack {
  display: flex;
  width: 100%;
  height: 12px;
  overflow: hidden;
  border-radius: 999px;
  background: #edf2fa;
}

.class-row__segment {
  min-width: 0;
  height: 100%;
}

.class-row__segment--submitted {
  background: linear-gradient(90deg, #2f7cf7, #59a6ff);
}

.class-row__segment--pending {
  background: linear-gradient(90deg, #ff9d57, #ffc266);
}

.class-row__segment--revision {
  background: linear-gradient(90deg, #18b4aa, #41d0c7);
}

.class-row__legend {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
}

.insight-list {
  display: grid;
  gap: 12px;
  padding: 0;
  margin: 0;
  list-style: none;
}

.insight-list li {
  position: relative;
  padding: 18px 18px 18px 52px;
  border-radius: 22px;
  background: white;
  border: 1px solid rgba(224, 233, 246, 0.92);
  color: #44536f;
  line-height: 1.75;
}

.insight-list li::before {
  content: '';
  position: absolute;
  top: 19px;
  left: 18px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2f7cf7, #59a6ff);
  box-shadow: 0 0 0 6px rgba(47, 124, 247, 0.12);
}

.recent-row {
  display: grid;
  gap: 12px;
}

.stats-panel__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 160px;
  border-radius: 22px;
  border: 1px dashed rgba(196, 210, 230, 0.92);
  color: #8a9ab8;
  background: rgba(246, 249, 255, 0.8);
}

@media (max-width: 1440px) {
  .teacher-statistics__hero-main,
  .stats-grid--top,
  .stats-grid--bottom {
    grid-template-columns: 1fr;
  }

  .teacher-statistics__hero-side {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1080px) {
  .teacher-statistics__hero {
    padding: 24px 20px;
  }

  .teacher-statistics__hero-copy h1 {
    font-size: 30px;
  }

  .teacher-statistics__hero-side,
  .filter-grid {
    grid-template-columns: 1fr;
  }

  .teacher-statistics__filters {
    flex-direction: column;
    align-items: stretch;
  }

  .subject-row {
    grid-template-columns: 1fr;
  }

  .subject-row__value {
    justify-self: start;
  }
}
</style>

