<script setup lang="ts">
import { computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatSubmitTypes } from '@/utils/format-labels'
import { formatDateTime, formatFullDateTime } from '@/utils/teacher-portal-view'

const route = useRoute()
const router = useRouter()
const store = useTeacherPortalStore()

const filters = reactive({
  keyword: '',
  classId: '' as string | number,
  taskStatus: '',
  reviewStatus: ''
})

const homeworkId = computed(() => `${route.params.id ?? ''}`)
const detail = computed(() => store.getHomeworkDetail(homeworkId.value))
const tasks = computed(() => store.getHomeworkTasks(homeworkId.value))

const metricItems = computed(() => {
  const total = tasks.value.length
  const submitted = tasks.value.filter((item) => item.taskStatus === 'submitted').length
  const completed = tasks.value.filter((item) => item.taskStatus === 'completed').length
  const revisionRequired = tasks.value.filter((item) => item.reviewStatus === 'revision_required').length

  return [
    { label: '任务总数', value: total, note: '当前作业覆盖的全部学生任务' },
    { label: '待批改', value: submitted, note: '已提交但还没有完成批改' },
    { label: '已完成', value: completed, note: '已经形成最终批改结果' },
    { label: '待订正', value: revisionRequired, note: '仍需要继续跟进的学生' }
  ]
})

const submitTypesText = computed(() => formatSubmitTypes(detail.value?.baseInfo.submitTypes))

const filteredTasks = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return tasks.value.filter((item) => {
    if (filters.classId && `${item.classId}` !== `${filters.classId}`) return false
    if (filters.taskStatus && item.taskStatus !== filters.taskStatus) return false
    if (filters.reviewStatus && item.reviewStatus !== filters.reviewStatus) return false

    if (keyword) {
      const text = `${item.studentName} ${item.className}`.toLowerCase()
      if (!text.includes(keyword)) return false
    }

    return true
  })
})

async function loadPage() {
  try {
    await Promise.all([store.loadHomeworkDetail(homeworkId.value), store.loadHomeworkTasks(homeworkId.value)])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '作业详情加载失败')
  }
}

async function remind(remindType: 'pending' | 'overdue', classId?: number | string) {
  try {
    await store.sendTypedReminder(homeworkId.value, { remindType, classId })
    ElMessage.success(remindType === 'overdue' ? '逾期催交已发送' : '提交提醒已发送')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '发送提醒失败')
  }
}

async function revoke() {
  const result = await ElMessageBox.prompt('请填写撤回原因', '撤回作业', {
    confirmButtonText: '确认撤回',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：题目需要调整',
    inputValidator: (value) => value.trim().length > 0,
    inputErrorMessage: '撤回原因不能为空',
    type: 'warning'
  }).catch(() => null)

  if (!result?.value) return

  try {
    await store.revokeAssignment(homeworkId.value, result.value)
    ElMessage.success('作业已撤回')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '撤回失败')
  }
}

const canDeleteHomework = computed(() =>
  (detail.value?.classList ?? []).every(
    (item) =>
      Number(item.submittedCount ?? 0) <= 0 &&
      Number(item.completedCount ?? 0) <= 0 &&
      Number(item.revisionRequiredCount ?? 0) <= 0
  )
)

async function removeHomework() {
  if (!detail.value) return

  if (!canDeleteHomework.value) {
    ElMessage.warning('已有学生提交记录的作业不能直接删除，请优先使用撤回。')
    return
  }

  const confirmed = await ElMessageBox.confirm(`确定删除“${detail.value.baseInfo.title}”吗？删除后无法恢复。`, '删除作业', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).catch(() => false)

  if (!confirmed) return

  try {
    await store.deleteAssignment(homeworkId.value)
    ElMessage.success('作业已删除')
    await router.push('/assignments')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败')
  }
}

watch(homeworkId, () => {
  if (homeworkId.value) {
    void loadPage()
  }
})

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div v-if="detail" class="teacher-page teacher-detail-next">
    <section class="teacher-page__hero">
      <span class="teacher-page__eyebrow">Assignment Detail</span>
      <h2>{{ detail.baseInfo.title }}</h2>
      <p>{{ detail.baseInfo.subjectCode }} · 截止于 {{ formatFullDateTime(detail.baseInfo.deadlineAt) }}</p>
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
            <span class="teacher-kicker">Overview</span>
            <h3>作业明细</h3>
            <p>在这里查看作业要求、覆盖班级和后续处理动作。</p>
          </div>
          <div class="teacher-detail-next__actions">
            <button type="button" class="teacher-button" @click="router.push(`/assignments/new?edit=${detail.baseInfo.homeworkId}`)">编辑</button>
            <button type="button" class="teacher-button" @click="remind('pending')">提醒</button>
            <button type="button" class="teacher-button--primary" @click="router.push(`/assignments/${detail.baseInfo.homeworkId}/grading`)">进入批改</button>
            <button type="button" class="teacher-button" @click="revoke">撤回</button>
            <button type="button" class="teacher-button" @click="removeHomework">删除</button>
          </div>
        </header>

        <div class="teacher-detail-next__summary">
          <div class="teacher-detail-next__meta">
            <div>
              <span class="teacher-inline-note">提交方式</span>
              <strong>{{ submitTypesText }}</strong>
            </div>
            <div>
              <span class="teacher-inline-note">逾期提交</span>
              <strong>{{ detail.baseInfo.allowLateSubmit ? '允许' : '不允许' }}</strong>
            </div>
            <div>
              <span class="teacher-inline-note">重复提交</span>
              <strong>{{ detail.baseInfo.allowResubmit ? '允许' : '不允许' }}</strong>
            </div>
            <div>
              <span class="teacher-inline-note">家长确认</span>
              <strong>{{ detail.baseInfo.needParentConfirm ? '需要' : '不需要' }}</strong>
            </div>
          </div>

          <div class="teacher-detail-next__content">
            <h4>作业说明</h4>
            <p>{{ detail.baseInfo.contentText || '暂无说明' }}</p>
          </div>

          <div class="teacher-detail-next__classes">
            <div v-for="item in detail.classList" :key="item.classId" class="teacher-detail-next__class-row">
              <div class="teacher-table__title">
                <strong>{{ item.className }}</strong>
                <small>学生 {{ item.studentCount }} 人</small>
              </div>
              <div class="teacher-detail-next__class-metrics">
                <span class="teacher-pill">已交 {{ item.submittedCount }}</span>
                <span class="teacher-pill">完成 {{ item.completedCount }}</span>
                <span class="teacher-pill">待订正 {{ item.revisionRequiredCount }}</span>
                <button type="button" class="teacher-link-button" @click="remind('pending', item.classId)">提醒本班</button>
              </div>
            </div>
          </div>
        </div>
      </article>

      <aside class="teacher-stack">
        <article class="teacher-toolbar">
          <div class="teacher-toolbar__row">
            <el-input v-model="filters.keyword" clearable placeholder="搜索学生或班级" />
            <el-select v-model="filters.classId" clearable placeholder="全部班级">
              <el-option
                v-for="item in detail.classList"
                :key="item.classId"
                :label="item.className"
                :value="item.classId"
              />
            </el-select>
            <el-select v-model="filters.taskStatus" clearable placeholder="任务状态">
              <el-option label="未提交" value="pending" />
              <el-option label="待批改" value="submitted" />
              <el-option label="待订正" value="revision_required" />
              <el-option label="已完成" value="completed" />
            </el-select>
            <el-select v-model="filters.reviewStatus" clearable placeholder="批改状态">
              <el-option label="未批改" value="unreviewed" />
              <el-option label="已批改" value="completed" />
              <el-option label="待订正" value="revision_required" />
            </el-select>
          </div>
        </article>

        <article class="teacher-table">
          <div class="teacher-table__head teacher-detail-next__task-head">
            <span>学生</span>
            <span>任务状态</span>
            <span>批改状态</span>
            <span>最近提交</span>
          </div>

          <div v-if="filteredTasks.length">
            <button
              v-for="item in filteredTasks"
              :key="item.taskId"
              type="button"
              class="teacher-table__row teacher-detail-next__task-row"
              @click="router.push(`/assignments/${detail.baseInfo.homeworkId}/grading?task=${item.taskId}`)"
            >
              <span class="teacher-table__title">
                <strong>{{ item.studentName }}</strong>
                <small>{{ item.className }}</small>
              </span>
              <StatusTag kind="submission" :value="item.taskStatus" />
              <StatusTag kind="review" :value="item.reviewStatus" />
              <span class="teacher-inline-note">{{ item.latestSubmittedAt ? formatDateTime(item.latestSubmittedAt) : '暂无' }}</span>
            </button>
          </div>
          <div v-else class="teacher-empty">当前筛选条件下没有学生任务。</div>
        </article>
      </aside>
    </section>
  </div>
</template>

<style scoped>
.teacher-detail-next__actions,
.teacher-detail-next__class-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.teacher-detail-next__summary {
  display: grid;
  gap: 18px;
  padding: 18px 20px 20px;
}

.teacher-detail-next__meta {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  padding-bottom: 18px;
  border-bottom: 1px solid #ede7db;
}

.teacher-detail-next__meta strong {
  display: block;
  margin-top: 6px;
  color: var(--teacher-ink);
  font-size: 15px;
}

.teacher-detail-next__content {
  display: grid;
  gap: 8px;
}

.teacher-detail-next__content h4 {
  margin: 0;
  color: var(--teacher-ink);
  font-size: 14px;
}

.teacher-detail-next__content p {
  margin: 0;
  color: var(--teacher-muted);
  line-height: 1.8;
}

.teacher-detail-next__classes {
  border-top: 1px solid #ede7db;
}

.teacher-detail-next__class-row {
  display: grid;
  gap: 10px;
  padding: 16px 0;
  border-bottom: 1px solid #ede7db;
}

.teacher-detail-next__class-row:last-child {
  border-bottom: 0;
}

.teacher-detail-next__task-head,
.teacher-detail-next__task-row {
  grid-template-columns: minmax(0, 1.2fr) 92px 92px 120px;
}

.teacher-detail-next__task-row {
  width: 100%;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.teacher-detail-next__task-row:hover {
  background: #f4efe4;
}

@media (max-width: 1280px) {
  .teacher-detail-next__meta,
  .teacher-detail-next__task-head,
  .teacher-detail-next__task-row {
    grid-template-columns: 1fr;
  }
}
</style>
