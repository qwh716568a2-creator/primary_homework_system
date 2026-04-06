<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import {
  formatDateTime,
  formatFullDateTime,
  submissionMethodMap
} from '@/utils/teacher-portal'

const route = useRoute()
const router = useRouter()
const store = useTeacherPortalStore()

const homeworkId = computed(() => `${route.params.id ?? ''}`)
const detail = computed(() => store.getHomeworkDetail(homeworkId.value))
const tasks = computed(() => store.getHomeworkTasks(homeworkId.value))

const classSummaryCards = computed(() =>
  (detail.value?.classList ?? []).map((item) => {
    const submitRate = item.studentCount > 0 ? Math.round((item.submittedCount / item.studentCount) * 100) : 0
    const completionRate = item.studentCount > 0 ? Math.round((item.completedCount / item.studentCount) * 100) : 0

    return {
      ...item,
      submitRate,
      completionRate
    }
  })
)

const submitTypesText = computed(() =>
  detail.value?.baseInfo.submitTypes?.map((item) => submissionMethodMap[item]).join(' / ') || '未配置'
)

async function loadPage() {
  try {
    await Promise.all([store.loadHomeworkDetail(homeworkId.value), store.loadHomeworkTasks(homeworkId.value)])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '作业详情加载失败')
  }
}

async function remind(classId?: number | string) {
  try {
    await store.sendReminder(homeworkId.value, classId)
    ElMessage.success(classId ? '该班级催交通知已发送' : '作业催交通知已发送')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '催交失败')
  }
}

async function revoke() {
  try {
    const { value } = await ElMessageBox.prompt('请输入撤回原因', '撤回作业', {
      confirmButtonText: '确认撤回',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：作业内容需要调整',
      inputValidator: (inputValue) => inputValue.trim().length > 0,
      inputErrorMessage: '请填写撤回原因',
      type: 'warning'
    })

    await store.revokeAssignment(homeworkId.value, value)
    ElMessage.success('作业已撤回')
  } catch (error) {
    if (error === 'cancel') {
      return
    }

    ElMessage.error(error instanceof Error ? error.message : '撤回失败')
  }
}

watch(homeworkId, () => {
  if (homeworkId.value) {
    void loadPage()
  }
})

onMounted(loadPage)
</script>

<template>
  <section v-if="detail" class="page-stack">
    <header class="page-header">
      <div>
        <h2>{{ detail.baseInfo.title }}</h2>
        <p>
          {{ detail.baseInfo.subjectCode }} · 截止时间 {{ formatFullDateTime(detail.baseInfo.deadlineAt) }}
        </p>
      </div>
      <div class="actions-row" style="margin-top: 0;">
        <el-button @click="router.push(`/assignments/new?edit=${detail.baseInfo.homeworkId}`)">编辑</el-button>
        <el-button :disabled="detail.baseInfo.status !== 'published'" @click="remind()">催交</el-button>
        <el-button type="primary" :disabled="detail.baseInfo.status !== 'published'" @click="router.push(`/assignments/${detail.baseInfo.homeworkId}/grading`)">
          进入批改
        </el-button>
        <el-button type="danger" plain :disabled="detail.baseInfo.status !== 'published'" @click="revoke">撤回</el-button>
      </div>
    </header>

    <section class="split-grid">
      <article class="section-card surface-card">
        <h3>作业信息</h3>
        <p class="section-subtitle">以下内容均来自详情接口返回的 `baseInfo` 与 `attachments`。</p>

        <div class="chip-row">
          <StatusTag kind="assignment" :value="detail.baseInfo.status" />
          <span class="soft-chip">提交方式：{{ submitTypesText }}</span>
          <span class="soft-chip">{{ detail.baseInfo.allowLateSubmit ? '允许逾期提交' : '截止后不可提交' }}</span>
          <span class="soft-chip">{{ detail.baseInfo.allowResubmit ? '允许重复提交' : '仅允许提交一次' }}</span>
          <span class="soft-chip">{{ detail.baseInfo.needParentConfirm ? '需要家长确认' : '无需家长确认' }}</span>
        </div>

        <div class="detail-content-block">
          {{ detail.baseInfo.contentText || '暂无作业内容' }}
        </div>

        <div v-if="detail.attachments.length" class="panel-list">
          <div v-for="(item, index) in detail.attachments" :key="`${item.assetUrl}-${index}`" class="panel-list-item">
            <strong>{{ item.assetName || item.assetUrl }}</strong>
            <p>{{ item.assetType }} · {{ item.assetUrl }}</p>
          </div>
        </div>
        <div v-else class="empty-state">当前没有附件信息。</div>
      </article>

      <article class="section-card surface-card">
        <h3>班级执行概况</h3>
        <p class="section-subtitle">支持按班级查看提交与完成进度，也可单独发送催交通知。</p>

        <div v-if="classSummaryCards.length" class="panel-list">
          <div v-for="item in classSummaryCards" :key="item.classId" class="panel-list-item">
            <div class="card-row-between">
              <div>
                <strong>{{ item.className }}</strong>
                <p>{{ item.submittedCount }}/{{ item.studentCount }} 已提交</p>
              </div>
              <el-button text @click="remind(item.classId)">催交本班</el-button>
            </div>
            <div class="chip-row" style="margin-top: 0.75rem;">
              <span class="stat-pill">提交率 {{ item.submitRate }}%</span>
              <span class="stat-pill">完成率 {{ item.completionRate }}%</span>
              <span class="stat-pill">待订正 {{ item.revisionRequiredCount }}</span>
              <span class="stat-pill">逾期 {{ item.overdueCount }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">暂无班级执行数据。</div>
      </article>
    </section>

    <article class="section-card surface-card">
      <div class="card-row-between" style="margin-bottom: 1rem;">
        <div>
          <h3>任务明细</h3>
          <p class="section-subtitle" style="margin-bottom: 0;">任务列表由 `/tasks` 接口直接返回。</p>
        </div>
      </div>

      <el-table
        :data="tasks"
        v-loading="store.loading.tasks"
        style="width: 100%;"
        empty-text="暂无学生任务"
      >
        <el-table-column prop="studentName" label="学生" min-width="120" />
        <el-table-column prop="className" label="班级" min-width="120" />
        <el-table-column label="任务状态" min-width="120">
          <template #default="{ row }">
            <StatusTag kind="submission" :value="row.taskStatus" />
          </template>
        </el-table-column>
        <el-table-column label="批改状态" min-width="120">
          <template #default="{ row }">
            <StatusTag kind="review" :value="row.reviewStatus" />
          </template>
        </el-table-column>
        <el-table-column label="最近提交" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.latestSubmittedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="submissionCount" label="提交次数" min-width="100" />
        <el-table-column label="是否逾期" min-width="100">
          <template #default="{ row }">
            {{ row.isLate ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/assignments/${detail.baseInfo.homeworkId}/grading?task=${row.taskId}`)">
              去批改
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </article>
  </section>

  <div v-else class="empty-state">未找到对应作业，请返回列表重新选择。</div>
</template>
