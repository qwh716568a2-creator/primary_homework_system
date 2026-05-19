<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatDateTime, getHomeworkDisplayClasses } from '@/utils/teacher-portal-view'
import type { HomeworkListItem } from '@/types/teacher-portal'

const store = useTeacherPortalStore()
const router = useRouter()

const filters = reactive({
  classId: '' as string | number,
  subject: '',
  status: '',
  keyword: '',
  dateRange: [] as string[]
})

const summaryItems = computed(() => [
  {
    label: '全部作业',
    value: store.homeworks.length,
    note: '当前账号可见的全部记录'
  },
  {
    label: '进行中',
    value: store.homeworks.filter((item) => item.status === 'published').length,
    note: '仍在执行中的作业'
  },
  {
    label: '待批改',
    value: store.homeworks.reduce((sum, item) => sum + item.pendingCount, 0),
    note: '等待老师处理的提交'
  },
  {
    label: '待订正',
    value: store.homeworks.reduce((sum, item) => sum + item.revisionRequiredCount, 0),
    note: '等待学生继续回流'
  }
])

const filteredList = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  const [start, end] = filters.dateRange
  const startTime = start ? new Date(start).getTime() : null
  const endTime = end ? new Date(end).getTime() : null

  return store.homeworks.filter((item) => {
    if (filters.subject && item.subjectCode !== filters.subject) {
      return false
    }

    if (filters.status && item.status !== filters.status) {
      return false
    }

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

    if (keyword) {
      const text = [item.title, item.subjectName, item.classNames.join(' ')].join(' ').toLowerCase()
      if (!text.includes(keyword)) {
        return false
      }
    }

    if (startTime || endTime) {
      const deadline = new Date(item.deadlineAt.replace(' ', 'T')).getTime()
      if (startTime && deadline < startTime) return false
      if (endTime && deadline > endTime + 24 * 60 * 60 * 1000 - 1) return false
    }

    return true
  })
})

async function loadPage() {
  try {
    await Promise.all([store.loadTeachingClasses(), store.loadHomeworkList(), store.loadHomeworkOverview()])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '作业台账加载失败')
  }
}

async function remindHomework(homeworkId: number | string, remindType: 'pending' | 'overdue') {
  try {
    await store.sendTypedReminder(homeworkId, { remindType })
    ElMessage.success(remindType === 'overdue' ? '逾期催交通知已发送' : '提交提醒已发送')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '发送提醒失败')
  }
}

async function revokeHomework(homeworkId: number | string) {
  const result = await ElMessageBox.prompt('请填写撤回原因', '撤回作业', {
    confirmButtonText: '确认撤回',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：内容需要调整',
    inputValidator: (value) => value.trim().length > 0,
    inputErrorMessage: '撤回原因不能为空',
    type: 'warning'
  }).catch(() => null)

  if (!result?.value) return

  try {
    await store.revokeAssignment(homeworkId, result.value)
    ElMessage.success('作业已撤回')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '撤回失败')
  }
}

function canDeleteHomework(row: HomeworkListItem) {
  return row.submittedCount <= 0 && row.revisionRequiredCount <= 0
}

async function deleteHomework(row: HomeworkListItem) {
  if (!canDeleteHomework(row)) {
    ElMessage.warning('已有学生提交记录的作业不能直接删除，请优先使用撤回。')
    return
  }

  const confirmed = await ElMessageBox.confirm(`确定删除“${row.title}”吗？删除后无法恢复。`, '删除作业', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).catch(() => false)

  if (!confirmed) return

  try {
    await store.deleteAssignment(row.homeworkId)
    ElMessage.success('作业已删除')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败')
  }
}

function resetFilters() {
  filters.classId = ''
  filters.subject = ''
  filters.status = ''
  filters.keyword = ''
  filters.dateRange = []
}

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div class="teacher-page teacher-ledger-next">
    <section class="teacher-page__hero">
      <span class="teacher-page__eyebrow">Assignment Ledger</span>
      <h2>把作业从“发布”到“收口”全部放回同一张台账里。</h2>
      <p>筛选、提醒、撤回、进入批改都在当前页完成，减少在多个页面之间往返。</p>
    </section>

    <section class="teacher-metrics">
      <article v-for="item in summaryItems" :key="item.label" class="teacher-metric">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.note }}</small>
      </article>
    </section>

    <section class="teacher-toolbar">
      <div class="teacher-toolbar__row">
        <el-input v-model="filters.keyword" clearable placeholder="搜索作业标题、班级或学科" />
        <el-select v-model="filters.classId" clearable placeholder="全部班级">
          <el-option
            v-for="item in store.classOptions"
            :key="item.classId"
            :label="item.className"
            :value="item.classId"
          />
        </el-select>
        <el-select v-model="filters.subject" clearable placeholder="全部学科">
          <el-option
            v-for="item in store.subjectOptions"
            :key="item.subjectCode"
            :label="item.subjectName"
            :value="item.subjectCode"
          />
        </el-select>
        <el-select v-model="filters.status" clearable placeholder="全部状态">
          <el-option label="进行中" value="published" />
          <el-option label="草稿" value="draft" />
          <el-option label="已撤回" value="revoked" />
          <el-option label="已结束" value="closed" />
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
        <button type="button" class="teacher-button" @click="resetFilters">重置筛选</button>
        <button type="button" class="teacher-button--primary" @click="router.push('/assignments/new')">新建作业</button>
      </div>
    </section>

    <section class="teacher-table teacher-ledger-next__table">
      <div class="teacher-table__head teacher-ledger-next__head">
        <span>作业信息</span>
        <span>当前状态</span>
        <span>提交 / 订正</span>
        <span>截止时间</span>
        <span>操作</span>
      </div>

      <div v-if="filteredList.length">
        <div v-for="row in filteredList" :key="row.homeworkId" class="teacher-table__row teacher-ledger-next__row">
          <div class="teacher-table__title">
            <strong>{{ row.title }}</strong>
            <small>{{ row.subjectName }} · {{ getHomeworkDisplayClasses(row) }}</small>
          </div>

          <StatusTag kind="assignment" :value="row.status" />

          <div class="teacher-ledger-next__progress">
            <span class="teacher-pill">已交 {{ row.submittedCount }}</span>
            <span class="teacher-pill">待批改 {{ row.pendingCount }}</span>
            <span class="teacher-pill">待订正 {{ row.revisionRequiredCount }}</span>
          </div>

          <span class="teacher-inline-note">{{ formatDateTime(row.deadlineAt) }}</span>

          <div class="teacher-ledger-next__actions">
            <button type="button" class="teacher-link-button" @click="router.push(`/assignments/${row.homeworkId}`)">详情</button>
            <button type="button" class="teacher-link-button" @click="router.push(`/assignments/new?edit=${row.homeworkId}`)">编辑</button>
            <button type="button" class="teacher-link-button" @click="router.push(`/assignments/${row.homeworkId}/grading`)">批改</button>
            <button type="button" class="teacher-link-button" @click="remindHomework(row.homeworkId, 'pending')">提醒</button>
            <button type="button" class="teacher-link-button teacher-ledger-next__danger" @click="revokeHomework(row.homeworkId)">撤回</button>
            <button type="button" class="teacher-link-button teacher-ledger-next__danger" @click="deleteHomework(row)">删除</button>
          </div>
        </div>
      </div>
      <div v-else class="teacher-empty">当前筛选条件下没有作业记录。</div>
    </section>
  </div>
</template>

<style scoped>
.teacher-ledger-next__head,
.teacher-ledger-next__row {
  grid-template-columns: minmax(0, 1.5fr) 92px 260px 120px minmax(240px, 0.8fr);
}

.teacher-ledger-next__progress,
.teacher-ledger-next__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.teacher-ledger-next__danger {
  color: #c2410c;
}

@media (max-width: 1280px) {
  .teacher-ledger-next__head,
  .teacher-ledger-next__row {
    grid-template-columns: 1fr;
  }
}
</style>
