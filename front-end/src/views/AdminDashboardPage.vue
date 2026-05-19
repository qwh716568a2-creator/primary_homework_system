<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { useAdminPortalStore } from '@/stores/adminPortal'
import { getSchoolSummary, getAdminRoleLabel } from '@/utils/admin-portal-view'

const store = useAdminPortalStore()
const { overview, schools, classes, users, loading } = storeToRefs(store)
const refreshing = ref(false)

const metricCards = computed(() => [
  {
    label: '学校数量',
    value: schools.value.length,
    caption: '已接入的学校组织'
  },
  {
    label: '班级数量',
    value: classes.value.length,
    caption: '当前纳入管理的班级'
  },
  {
    label: '今日发布',
    value: overview.value.publishCountToday,
    caption: '当日教师发布作业数'
  },
  {
    label: '活跃教师',
    value: overview.value.activeTeacherCount,
    caption: '最近活跃教师数'
  }
])

const executionMetrics = computed(() => [
  {
    label: '提交率',
    value: overview.value.submissionRate
  },
  {
    label: '逾期率',
    value: overview.value.overdueRate
  },
  {
    label: '学生活跃度',
    value:
      users.value.filter((item) => item.roleType === 'student').length === 0
        ? 0
        : overview.value.activeStudentCount /
          users.value.filter((item) => item.roleType === 'student').length
  }
])

const roleDistribution = computed(() => {
  const roleTypes = ['admin', 'teacher', 'student', 'parent']

  return roleTypes.map((roleType) => ({
    roleType,
    label: getAdminRoleLabel(roleType),
    count: users.value.filter((item) => item.roleType === roleType).length
  }))
})

const schoolSummary = computed(() =>
  getSchoolSummary(schools.value, classes.value, users.value).slice(0, 6)
)

const classRanking = computed(() =>
  [...classes.value]
    .sort((left, right) => (Number(right.studentCount ?? 0) || 0) - (Number(left.studentCount ?? 0) || 0))
    .slice(0, 6)
)

async function loadDashboard() {
  refreshing.value = true

  try {
    await Promise.all([
      store.loadOverview(),
      store.loadSchools(),
      store.loadClasses(),
      store.loadUsers({ pageNo: 1, pageSize: 200 })
    ])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '管理员数据加载失败')
  } finally {
    refreshing.value = false
  }
}

onMounted(() => {
  void loadDashboard()
})
</script>

<template>
  <section class="page-stack">
    <div class="page-header">
      <div>
        <h2>数据看板</h2>
        <p>聚合查看学校组织、作业执行和账号活跃情况。</p>
      </div>
      <el-button :loading="refreshing" @click="loadDashboard">刷新数据</el-button>
    </div>

    <div class="grid-cards">
      <article v-for="item in metricCards" :key="item.label" class="section-card surface-card admin-metric">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.caption }}</small>
      </article>
    </div>

    <div class="content-grid">
      <section class="section-card surface-card" v-loading="loading.overview">
        <h3>执行指标</h3>
        <p class="section-subtitle">按平台口径展示当前作业执行表现。</p>

        <div class="admin-progress-list">
          <div v-for="item in executionMetrics" :key="item.label" class="admin-progress-item">
            <div class="admin-progress-item__meta">
              <strong>{{ item.label }}</strong>
              <span>{{ Math.round(item.value * 100) }}%</span>
            </div>
            <el-progress :percentage="Math.round(item.value * 100)" :stroke-width="10" />
          </div>
        </div>
      </section>

      <section class="section-card surface-card" v-loading="loading.users">
        <h3>账号分布</h3>
        <p class="section-subtitle">当前管理范围内账号角色构成。</p>

        <div class="panel-list">
          <div v-for="item in roleDistribution" :key="item.roleType" class="panel-list-item admin-role-item">
            <div>
              <strong>{{ item.label }}</strong>
              <p>{{ item.count }} 个账号</p>
            </div>
            <span>{{ item.count }}</span>
          </div>
        </div>
      </section>
    </div>

    <div class="split-grid">
      <section class="section-card surface-card" v-loading="loading.schools || loading.classes || loading.users">
        <h3>学校覆盖</h3>
        <p class="section-subtitle">按学校维度查看班级、教师与学生规模。</p>

        <div v-if="schoolSummary.length" class="panel-list">
          <div v-for="item in schoolSummary" :key="item.schoolId" class="panel-list-item">
            <div class="admin-list-row">
              <div>
                <strong>{{ item.schoolName }}</strong>
                <p>{{ item.classCount ?? 0 }} 个班级</p>
              </div>
              <el-tag type="info" effect="plain">{{ item.schoolCode || '未设置编码' }}</el-tag>
            </div>
            <div class="chip-row">
              <span class="soft-chip">教师 {{ item.teacherCount ?? 0 }}</span>
              <span class="soft-chip">学生 {{ item.studentCount ?? 0 }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">暂无学校数据</div>
      </section>

      <section class="section-card surface-card" v-loading="loading.classes">
        <h3>班级规模</h3>
        <p class="section-subtitle">按学生人数查看重点班级。</p>

        <div v-if="classRanking.length" class="panel-list">
          <div v-for="item in classRanking" :key="item.classId" class="panel-list-item">
            <div class="admin-list-row">
              <div>
                <strong>{{ item.className }}</strong>
                <p>{{ item.schoolName || '未关联学校' }}</p>
              </div>
              <span class="mini-number">{{ item.studentCount ?? 0 }}</span>
            </div>
            <div class="chip-row">
              <span class="soft-chip">{{ item.gradeName || '未设置年级' }}</span>
              <span class="soft-chip">{{ item.homeroomTeacherName || '未设置班主任' }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">暂无班级数据</div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.admin-metric {
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
}

.admin-metric span {
  color: #6b7f93;
  font-size: 0.88rem;
}

.admin-metric strong {
  font-size: 2.1rem;
  color: #103251;
}

.admin-metric small {
  color: #72849a;
}

.admin-progress-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.admin-progress-item {
  padding: 1rem 1.1rem;
  border-radius: 20px;
  background: rgba(248, 251, 255, 0.82);
}

.admin-progress-item__meta,
.admin-list-row,
.admin-role-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.admin-progress-item__meta {
  margin-bottom: 0.75rem;
}

.admin-progress-item__meta span,
.admin-role-item span {
  font-weight: 700;
  color: #17538a;
}

@media (max-width: 1100px) {
  .grid-cards,
  .content-grid,
  .split-grid {
    grid-template-columns: 1fr;
  }
}
</style>
