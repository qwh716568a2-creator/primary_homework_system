<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatAssetType, formatSubmitTypes } from '@/utils/format-labels'
import { getAttachmentDisplayName, isImageAttachmentLike, resolveAttachmentUrl } from '@/utils/attachment-url'
import {
  formatDateTime,
  formatFullDateTime
} from '@/utils/teacher-portal-view'

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
  formatSubmitTypes(detail.value?.baseInfo.submitTypes)
)

function getAttachmentName(item: { assetName?: string; assetUrl?: string }) {
  return getAttachmentDisplayName(item)
}

function isImageAttachment(item: { assetType?: string; assetName?: string; assetUrl?: string }) {
  return isImageAttachmentLike(item)
}

const imageAttachmentUrls = computed(() =>
  (detail.value?.attachments ?? []).filter(isImageAttachment).map(resolveAttachmentUrl).filter(Boolean)
)

function getImageAttachmentIndex(item: { assetUrl?: string }) {
  return Math.max(0, imageAttachmentUrls.value.findIndex((url) => url === resolveAttachmentUrl(item)))
}

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
  <div v-if="detail" class="page-stack">
    <!-- 顶部页头区域 -->
    <div class="surface-card section-card mb-4" style="padding: 1.5rem;">
      <el-page-header @back="router.back()" title="返回列表">
        <template #content>
          <div class="header-title-area">
            <span class="page-title">{{ detail.baseInfo.title }}</span>
            <el-tag size="small" effect="light" class="ml-2">{{ detail.baseInfo.subjectCode }}</el-tag>
            <StatusTag kind="assignment" :value="detail.baseInfo.status" class="ml-2" />
          </div>
        </template>
        <template #extra>
          <div class="header-actions">
            <el-button @click="router.push(`/assignments/${detail.baseInfo.homeworkId}/print`)">打印作业</el-button>
            <el-button @click="router.push(`/assignments/new?edit=${detail.baseInfo.homeworkId}`)">编辑</el-button>
            <el-button :disabled="detail.baseInfo.status !== 'published'" @click="remind()">催交</el-button>
            <el-button type="primary" :disabled="detail.baseInfo.status !== 'published'" @click="router.push(`/assignments/${detail.baseInfo.homeworkId}/grading`)">
              进入批改
            </el-button>
            <el-button type="danger" plain :disabled="detail.baseInfo.status !== 'published'" @click="revoke">撤回</el-button>
          </div>
        </template>

        <!-- 详细元数据 -->
        <el-descriptions :column="4" size="default" class="mt-4" border>
          <el-descriptions-item label="截止时间" label-align="right" align="center">{{ formatFullDateTime(detail.baseInfo.deadlineAt) }}</el-descriptions-item>
          <el-descriptions-item label="提交方式" label-align="right" align="center">{{ submitTypesText }}</el-descriptions-item>
          <el-descriptions-item label="逾期限制" label-align="right" align="center">
            <el-tag size="small" :type="detail.baseInfo.allowLateSubmit ? 'warning' : 'info'">
              {{ detail.baseInfo.allowLateSubmit ? '允许逾期提交' : '截止后不可提交' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="重复提交" label-align="right" align="center">
            {{ detail.baseInfo.allowResubmit ? '允许重复提交' : '仅限一次' }}
          </el-descriptions-item>
          <el-descriptions-item label="家长签字" label-align="right" align="center">
            {{ detail.baseInfo.needParentConfirm ? '需要家长确认' : '无需家长确认' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-page-header>
    </div>

    <el-row :gutter="24" class="mb-4">
      <!-- 左侧：作业详细内容 -->
      <el-col :span="12">
        <div class="surface-card section-card h-full">
          <div class="card-header pb-4">
            <span class="card-title">作业信息</span>
          </div>
          <div class="content-block">
            {{ detail.baseInfo.contentText || '暂无作业内容' }}
          </div>

          <template v-if="detail.attachments.length">
            <el-divider content-position="left">附件资料</el-divider>
            <div class="attachment-list">
              <div
                v-for="(item, index) in detail.attachments"
                :key="`${resolveAttachmentUrl(item)}-${index}`"
                class="attachment-item"
                :class="{ 'attachment-item--image': isImageAttachment(item) }"
              >
                <el-image
                  v-if="isImageAttachment(item)"
                  class="attachment-preview"
                  :src="resolveAttachmentUrl(item)"
                  :alt="getAttachmentName(item)"
                  :preview-src-list="imageAttachmentUrls"
                  :initial-index="getImageAttachmentIndex(item)"
                  fit="cover"
                  preview-teleported
                  hide-on-click-modal
                />
                <div class="attachment-info">
                  <div class="attachment-name">{{ getAttachmentName(item) }}</div>
                  <div class="attachment-meta">{{ isImageAttachment(item) ? '图片附件' : formatAssetType(item.assetType) }}</div>
                </div>
                <el-link type="primary" :href="resolveAttachmentUrl(item)" target="_blank" :download="getAttachmentName(item)" :underline="false">下载</el-link>
              </div>
            </div>
          </template>
          <el-empty v-else description="当前没有附件信息" :image-size="60" />
        </div>
      </el-col>

      <!-- 右侧：班级执行情况 -->
      <el-col :span="12">
        <div class="surface-card section-card h-full">
          <div class="card-header pb-4">
            <span class="card-title">班级执行概况</span>
            <span class="card-subtitle">支持按班级查看提交进度或催交</span>
          </div>
          <div v-if="classSummaryCards.length" class="class-summary-wrapper">
            <div v-for="item in classSummaryCards" :key="item.classId" class="class-summary-card">
              <div class="class-header">
                <span class="class-name">{{ item.className }}</span>
                <el-button type="primary" link @click="remind(item.classId)">催交本班</el-button>
              </div>
              <div class="class-progress-area mt-2">
                <div class="progress-label">提交进度 ({{ item.submittedCount }}/{{ item.studentCount }})</div>
                <el-progress :percentage="item.submitRate" :color="item.submitRate === 100 ? '#67c23a' : '#0d9488'" />
              </div>
              <div class="class-tags mt-3">
                <el-tag size="small" type="success" effect="plain">完成率 {{ item.completionRate }}%</el-tag>
                <el-tag size="small" type="warning" effect="plain">待订正 {{ item.revisionRequiredCount }}</el-tag>
                <el-tag size="small" type="danger" effect="plain">逾期 {{ item.overdueCount }}</el-tag>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无班级执行数据" :image-size="60" />
        </div>
      </el-col>
    </el-row>

    <!-- 底部：任务明细表格 -->
    <div class="surface-card section-card">
      <div class="card-header pb-4">
        <span class="card-title">任务明细</span>
      </div>
      <el-table
        :data="tasks"
        v-loading="store.loading.tasks"
        style="width: 100%;"
        empty-text="暂无学生任务"
        stripe
        border
      >
        <el-table-column prop="studentName" label="学生" min-width="100" />
        <el-table-column prop="className" label="班级" min-width="100" />
        <el-table-column label="任务状态" min-width="110" align="center">
          <template #default="{ row }">
            <StatusTag kind="submission" :value="row.taskStatus" />
          </template>
        </el-table-column>
        <el-table-column label="批改状态" min-width="110" align="center">
          <template #default="{ row }">
            <StatusTag kind="review" :value="row.reviewStatus" />
          </template>
        </el-table-column>
        <el-table-column label="最近提交" min-width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.latestSubmittedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="submissionCount" label="提交次数" min-width="90" align="center" />
        <el-table-column label="是否逾期" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isLate ? 'danger' : 'info'" size="small">{{ row.isLate ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="router.push(`/assignments/${detail.baseInfo.homeworkId}/grading?task=${row.taskId}`)">
              去批改
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>

  <el-empty v-else description="未找到对应作业，请返回列表重新选择" />
</template>

<style scoped>
.mb-4 { margin-bottom: 24px; }
.pb-4 { padding-bottom: 16px; margin-bottom: 16px; border-bottom: 1px solid rgba(0, 0, 0, 0.05); }
.mt-4 { margin-top: 16px; }
.mt-2 { margin-top: 8px; }
.mt-3 { margin-top: 12px; }
.ml-2 { margin-left: 8px; }
.h-full { height: 100%; }

/* Header */
.header-title-area {
  display: flex;
  align-items: center;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

/* Cards */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.card-subtitle {
  font-size: 13px;
  color: #909399;
  font-weight: normal;
}

/* Content Block */
.content-block {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  min-height: 60px;
}

/* Attachments */
.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.attachment-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(8px);
  padding: 12px 18px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.6);
}
.attachment-item--image {
  align-items: center;
}
.attachment-preview {
  width: 132px;
  height: 92px;
  flex: 0 0 132px;
  display: block;
  overflow: hidden;
  border-radius: 12px;
  border: 1px solid rgba(203, 213, 225, 0.9);
  background: #f4f8fc;
}
.attachment-preview :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}
.attachment-info {
  flex: 1;
  min-width: 0;
}
.attachment-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  word-break: break-all;
}
.attachment-meta {
  font-size: 12px;
  color: #909399;
}

/* Class Summary Cards */
.class-summary-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.class-summary-card {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  padding: 16px;
}
.class-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.class-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.progress-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}
.class-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .attachment-item--image {
    flex-direction: column;
    align-items: stretch;
  }

  .attachment-preview {
    width: 100%;
    height: 180px;
    flex-basis: auto;
  }
}
</style>
