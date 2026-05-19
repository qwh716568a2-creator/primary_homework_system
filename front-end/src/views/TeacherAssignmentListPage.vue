<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import StatusTag from '@/components/StatusTag.vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatDateTime, getHomeworkDisplayClasses } from '@/utils/teacher-portal-view'

const store = useTeacherPortalStore()
const router = useRouter()

const filters = reactive({
  subject: '',
  status: '',
  keyword: ''
})

const filteredList = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return store.homeworks.filter((item) => {
    if (filters.subject && item.subjectCode !== filters.subject) {
      return false
    }

    if (filters.status && item.status !== filters.status) {
      return false
    }

    if (keyword && !item.title.toLowerCase().includes(keyword)) {
      return false
    }

    return true
  })
})

async function loadPage() {
  try {
    await Promise.all([store.loadTeachingClasses(), store.loadHomeworkList()])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '作业列表加载失败')
  }
}

onMounted(loadPage)
</script>

<template>
  <div class="page-stack">
    <header class="page-header list-page-header">
      <div>
        <h2 class="hero-title">作业列表</h2>
        <p class="page-subtitle">集中查看已发布、草稿和待批改作业，支持按学科和状态快速筛选。</p>
      </div>

      <el-button type="primary" size="large" :icon="Plus" @click="router.push('/assignments/new')">
        新建作业
      </el-button>
    </header>

    <section class="surface-card section-card">
      <div class="list-toolbar">
        <el-input
          v-model="filters.keyword"
          class="toolbar-input"
          clearable
          :prefix-icon="Search"
          placeholder="搜索作业标题"
        />

        <el-select v-model="filters.subject" clearable placeholder="全部学科" style="width: 160px">
          <el-option
            v-for="item in store.subjectOptions"
            :key="item.subjectCode"
            :label="item.subjectName"
            :value="item.subjectCode"
          />
        </el-select>

        <el-select v-model="filters.status" clearable placeholder="全部状态" style="width: 160px">
          <el-option label="进行中" value="published" />
          <el-option label="草稿" value="draft" />
          <el-option label="已撤回" value="revoked" />
          <el-option label="已结束" value="closed" />
        </el-select>
      </div>

      <el-table
        :data="filteredList"
        v-loading="store.loading.homeworks"
        border
        stripe
        class="assignment-table"
        empty-text="暂无作业数据"
      >
        <el-table-column label="作业信息" min-width="280">
          <template #default="{ row }">
            <div class="title-cell">
              <strong>{{ row.title }}</strong>
              <p>{{ row.subjectName }} · {{ getHomeworkDisplayClasses(row) }}</p>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <StatusTag kind="assignment" :value="row.status" />
          </template>
        </el-table-column>

        <el-table-column label="截止时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.deadlineAt) }}
          </template>
        </el-table-column>

        <el-table-column label="提交情况" width="160" align="center">
          <template #default="{ row }">
            <span class="metric-inline">{{ row.submittedCount }}</span>
            <span class="metric-muted"> / {{ row.submittedCount + row.pendingCount }}</span>
          </template>
        </el-table-column>

        <el-table-column label="待订正" width="110" align="center">
          <template #default="{ row }">
            {{ row.revisionRequiredCount }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" @click="router.push(`/assignments/${row.homeworkId}`)">详情</el-button>
              <el-button size="small" @click="router.push(`/assignments/new?edit=${row.homeworkId}`)">编辑</el-button>
              <el-button
                size="small"
                type="primary"
                plain
                :disabled="row.status !== 'published'"
                @click="router.push(`/assignments/${row.homeworkId}/grading`)"
              >
                批改
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.list-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.page-subtitle {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 14px;
}

.list-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 20px;
}

.toolbar-input {
  width: min(320px, 100%);
}

.assignment-table {
  width: 100%;
}

.title-cell strong {
  display: block;
  color: #0f172a;
  font-size: 15px;
}

.title-cell p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.metric-inline {
  font-weight: 700;
  color: #0f172a;
}

.metric-muted {
  color: #94a3b8;
}

.table-actions {
  display: inline-flex;
  gap: 8px;
}
</style>
