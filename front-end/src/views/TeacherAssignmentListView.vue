<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Search, View } from '@element-plus/icons-vue'
import StatusTag from '@/components/StatusTag.vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatDateTime, getHomeworkDisplayClasses } from '@/utils/teacher-portal-view'
import type { HomeworkListItem } from '@/types/teacher-portal'

const store = useTeacherPortalStore()
const router = useRouter()

const filters = ref({
  subject: '',
  status: '',
  keyword: ''
})

const deletingId = ref<string | number | null>(null)

const filteredList = computed(() => {
  let list = store.homeworks
  if (filters.value.subject) {
    list = list.filter((item) => item.subjectCode === filters.value.subject)
  }
  if (filters.value.status) {
    list = list.filter((item) => item.status === filters.value.status)
  }
  if (filters.value.keyword) {
    const keyword = filters.value.keyword.toLowerCase()
    list = list.filter((item) => item.title.toLowerCase().includes(keyword))
  }
  return list
})

function goDetail(row: HomeworkListItem) {
  router.push(`/assignments/${row.homeworkId}`)
}

function goEdit(row: HomeworkListItem) {
  router.push(`/assignments/new?edit=${row.homeworkId}`)
}

function goGrading(row: HomeworkListItem) {
  router.push(`/assignments/${row.homeworkId}/grading`)
}

async function handleDelete(row: HomeworkListItem) {
  try {
    await ElMessageBox.confirm(
      `删除后将无法恢复「${row.title}」。如果该作业已经有学生提交记录，建议先撤回后再处理。`,
      '确认删除作业',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  deletingId.value = row.homeworkId
  try {
    await store.deleteAssignment(row.homeworkId)
    ElMessage.success('作业已删除')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败，请稍后重试。')
  } finally {
    deletingId.value = null
  }
}

onMounted(async () => {
  await Promise.allSettled([store.loadTeachingClasses(), store.loadHomeworkList()])
})
</script>

<template>
  <div class="page-stack">
    <header class="page-header header-glow mb-4">
      <div>
        <h2 class="hero-title">作业列表</h2>
        <p class="subtitle mt-2">集中查看和筛选您历史下发的所有作业，并处理详情、编辑、删除与批改。</p>
      </div>
      <el-button type="primary" size="large" class="custom-shadow" :icon="Plus" @click="router.push('/assignments/new')">
        新建作业
      </el-button>
    </header>

    <div class="surface-card section-card">
      <div class="toolbar mb-4">
        <el-input
          v-model="filters.keyword"
          clearable
          :prefix-icon="Search"
          placeholder="搜索作业标题..."
          style="width: 250px"
        />
        <el-select v-model="filters.subject" clearable placeholder="所有学科" style="width: 140px">
          <el-option
            v-for="subject in store.subjectOptions"
            :key="subject.subjectCode"
            :label="subject.subjectName"
            :value="subject.subjectCode"
          />
        </el-select>
        <el-select v-model="filters.status" clearable placeholder="所有状态" style="width: 140px">
          <el-option label="已发布" value="published" />
          <el-option label="草稿" value="draft" />
          <el-option label="已撤回" value="revoked" />
          <el-option label="已结束" value="closed" />
        </el-select>
      </div>

      <el-table :data="filteredList" v-loading="store.loading.homeworks" stripe style="width: 100%" class="modern-table">
        <el-table-column prop="title" label="作业标题" min-width="220">
          <template #default="{ row }">
            <strong>{{ row.title }}</strong>
            <div class="text-secondary assignment-meta">
              {{ row.subjectName }} · {{ getHomeworkDisplayClasses(row) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <StatusTag kind="assignment" :value="row.status" />
          </template>
        </el-table-column>

        <el-table-column label="截止时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.deadlineAt) }}
          </template>
        </el-table-column>

        <el-table-column label="总体进度" width="120">
          <template #default="{ row }">
            <span class="font-mono">
              <b>{{ row.submittedCount }}</b>
              <span class="text-secondary">/{{ row.submittedCount + row.pendingCount }}</span>
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" min-width="260" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-group">
              <el-button size="small" :icon="View" @click="goDetail(row)">详情</el-button>
              <el-button size="small" :icon="Edit" @click="goEdit(row)">编辑</el-button>
              <el-button
                size="small"
                type="primary"
                plain
                :disabled="row.status !== 'published'"
                @click="goGrading(row)"
              >
                批阅
              </el-button>
              <el-button
                size="small"
                type="danger"
                plain
                :icon="Delete"
                :loading="deletingId === row.homeworkId"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.mb-4 { margin-bottom: 24px; }
.mt-2 { margin-top: 8px; }

.header-glow {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.subtitle {
  color: #64748b;
  font-size: 0.95rem;
}

.toolbar {
  display: flex;
  gap: 16px;
  align-items: center;
}

.text-secondary {
  color: #94a3b8;
}

.assignment-meta {
  margin-top: 4px;
  font-size: 0.85rem;
}

.font-mono {
  font-family: 'Inter', monospace;
}

.custom-shadow {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.modern-table {
  border-radius: 12px;
  overflow: hidden;
}

.action-group {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
