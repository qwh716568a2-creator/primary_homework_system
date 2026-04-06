<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import MetricCard from '@/components/MetricCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatDateTime, getHomeworkDisplayClasses, percentFromRate } from '@/utils/teacher-portal'

const store = useTeacherPortalStore()

const filters = reactive({
  classId: '',
  subjectCode: '',
  dateRange: [] as string[]
})

const overviewCards = computed(() => [
  {
    label: '发布数量',
    value: `${store.homeworkOverview.publishCount}`,
    hint: '统计范围内累计发布的作业数',
    tone: 'sky' as const
  },
  {
    label: '提交率',
    value: `${percentFromRate(store.homeworkOverview.submissionRate)}%`,
    hint: '学生任务提交比例',
    tone: 'teal' as const
  },
  {
    label: '按时率',
    value: `${percentFromRate(store.homeworkOverview.onTimeRate)}%`,
    hint: '按截止时间前提交的比例',
    tone: 'amber' as const
  },
  {
    label: '已批改率',
    value: `${percentFromRate(store.homeworkOverview.reviewRate)}%`,
    hint: '教师已完成批改的比例',
    tone: 'rose' as const
  }
])

const visibleHomeworks = computed(() =>
  store.homeworks.filter((item) => {
    if (filters.dateRange.length !== 2) {
      return true
    }

    const [start, end] = filters.dateRange
    return item.deadlineAt >= start && item.deadlineAt <= end
  })
)

const focusAssignments = computed(() =>
  [...visibleHomeworks.value]
    .sort(
      (left, right) =>
        right.pendingCount + right.revisionRequiredCount - (left.pendingCount + left.revisionRequiredCount)
    )
    .slice(0, 5)
)

const subjectSummary = computed(() => {
  const subjectMap = new Map<string, { subjectName: string; count: number }>()

  visibleHomeworks.value.forEach((item) => {
    const current = subjectMap.get(item.subjectCode)

    if (current) {
      current.count += 1
      return
    }

    subjectMap.set(item.subjectCode, {
      subjectName: item.subjectName,
      count: 1
    })
  })

  return Array.from(subjectMap.values()).sort((left, right) => right.count - left.count)
})

function buildQuery() {
  return {
    classId: filters.classId || undefined,
    subjectCode: filters.subjectCode || undefined,
    startDate: filters.dateRange[0] || undefined,
    endDate: filters.dateRange[1] || undefined
  }
}

async function loadStatistics() {
  try {
    if (!store.classRelations.length) {
      await store.loadTeachingClasses()
    }

    await Promise.all([
      store.loadHomeworkOverview(buildQuery()),
      store.loadHomeworkList({
        classId: filters.classId || undefined,
        subjectCode: filters.subjectCode || undefined
      })
    ])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '统计数据加载失败')
  }
}

function resetFilters() {
  filters.classId = ''
  filters.subjectCode = ''
  filters.dateRange = []
  void loadStatistics()
}

onMounted(loadStatistics)
</script>

<template>
  <section class="page-stack">
    <header class="page-header">
      <div>
        <h2>统计分析</h2>
        <p>统计页改为真实接口聚合，不再展示本地拼接趋势图和临时班级画像。</p>
      </div>
    </header>

    <article class="section-card surface-card">
      <h3>筛选条件</h3>
      <p class="section-subtitle">概览走统计接口，作业明细走作业列表接口。</p>
      <div class="filter-row">
        <el-select v-model="filters.classId" clearable placeholder="班级">
          <el-option
            v-for="item in store.classOptions"
            :key="item.classId"
            :label="item.className"
            :value="`${item.classId}`"
          />
        </el-select>
        <el-select v-model="filters.subjectCode" clearable placeholder="学科">
          <el-option
            v-for="item in store.subjectOptions"
            :key="item.subjectCode"
            :label="item.subjectName"
            :value="item.subjectCode"
          />
        </el-select>
        <el-date-picker
          v-model="filters.dateRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
        />
        <div class="actions-row" style="margin-top: 0;">
          <el-button type="primary" @click="loadStatistics">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>
    </article>

    <section class="grid-cards" v-loading="store.loading.overview">
      <MetricCard
        v-for="card in overviewCards"
        :key="card.label"
        :label="card.label"
        :value="card.value"
        :hint="card.hint"
        :tone="card.tone"
      />
    </section>

    <section class="content-grid">
      <article class="section-card surface-card">
        <h3>重点跟进作业</h3>
        <p class="section-subtitle">按照未交与待订正总量排序，便于快速定位风险作业。</p>
        <div v-if="focusAssignments.length" class="panel-list">
          <div v-for="item in focusAssignments" :key="item.homeworkId" class="panel-list-item">
            <div class="card-row-between">
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ getHomeworkDisplayClasses(item) }}</p>
              </div>
              <StatusTag kind="assignment" :value="item.status" />
            </div>
            <div class="chip-row" style="margin-top: 0.75rem;">
              <span class="stat-pill">已交 {{ item.submittedCount }}</span>
              <span class="stat-pill">未交 {{ item.pendingCount }}</span>
              <span class="stat-pill">待订正 {{ item.revisionRequiredCount }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">当前筛选范围内暂无重点跟进作业。</div>
      </article>

      <article class="section-card surface-card">
        <h3>学科分布</h3>
        <p class="section-subtitle">基于当前查询范围内的作业明细做简单聚合。</p>
        <div v-if="subjectSummary.length" class="panel-list">
          <div v-for="item in subjectSummary" :key="item.subjectName" class="panel-list-item">
            <strong>{{ item.subjectName }}</strong>
            <p>作业数量 {{ item.count }}</p>
          </div>
        </div>
        <div v-else class="empty-state">暂无学科分布数据。</div>
      </article>
    </section>

    <article class="section-card surface-card">
      <h3>作业明细</h3>
      <p class="section-subtitle">每条记录都来自作业列表接口，便于后续继续扩展导出与更细颗粒度统计。</p>
      <el-table
        :data="visibleHomeworks"
        v-loading="store.loading.homeworks"
        style="width: 100%;"
        empty-text="暂无统计明细"
      >
        <el-table-column prop="title" label="作业" min-width="220" />
        <el-table-column prop="subjectName" label="学科" min-width="120" />
        <el-table-column label="班级" min-width="180">
          <template #default="{ row }">
            {{ getHomeworkDisplayClasses(row) }}
          </template>
        </el-table-column>
        <el-table-column label="截止时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.deadlineAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="submittedCount" label="已交" min-width="90" />
        <el-table-column prop="pendingCount" label="未交" min-width="90" />
        <el-table-column prop="revisionRequiredCount" label="待订正" min-width="100" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <StatusTag kind="assignment" :value="row.status" />
          </template>
        </el-table-column>
      </el-table>
    </article>
  </section>
</template>
