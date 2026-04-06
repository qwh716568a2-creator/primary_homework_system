<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatDateTime, getHomeworkDisplayClasses } from '@/utils/teacher-portal'

const router = useRouter()
const store = useTeacherPortalStore()

const filters = reactive({
  keyword: '',
  classId: '',
  subjectCode: '',
  status: ''
})

const query = computed(() => ({
  keyword: filters.keyword.trim() || undefined,
  classId: filters.classId || undefined,
  subjectCode: filters.subjectCode || undefined,
  status: filters.status || undefined
}))

async function loadAssignments() {
  try {
    if (!store.classRelations.length) {
      await store.loadTeachingClasses()
    }

    await store.loadHomeworkList(query.value)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '作业列表加载失败')
  }
}

async function remind(homeworkId: number | string) {
  try {
    await store.sendReminder(homeworkId)
    ElMessage.success('催交通知已发送')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '催交失败')
  }
}

async function revoke(homeworkId: number | string) {
  try {
    const { value } = await ElMessageBox.prompt('请输入撤回原因', '撤回作业', {
      confirmButtonText: '确认撤回',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：发布时间需要调整',
      inputValidator: (inputValue) => inputValue.trim().length > 0,
      inputErrorMessage: '请填写撤回原因',
      type: 'warning'
    })

    await store.revokeAssignment(homeworkId, value)
    ElMessage.success('作业已撤回')
  } catch (error) {
    if (error === 'cancel') {
      return
    }

    ElMessage.error(error instanceof Error ? error.message : '撤回失败')
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.classId = ''
  filters.subjectCode = ''
  filters.status = ''
  void loadAssignments()
}

onMounted(loadAssignments)
</script>

<template>
  <section class="page-stack">
    <header class="page-header">
      <div>
        <h2>作业列表</h2>
        <p>列表直接请求教师作业接口，支持按学科、班级和状态筛选。</p>
      </div>
      <el-button type="primary" @click="router.push('/assignments/new')">新建作业</el-button>
    </header>

    <article class="section-card surface-card">
      <h3>筛选条件</h3>
      <p class="section-subtitle">这里不再做本地过滤，而是按条件重新请求后端数据。</p>
      <div class="filter-row">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索作业标题"
          @keyup.enter="loadAssignments"
        />
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
        <el-select v-model="filters.status" clearable placeholder="状态">
          <el-option label="草稿" value="draft" />
          <el-option label="已发布" value="published" />
          <el-option label="已撤回" value="revoked" />
          <el-option label="已结束" value="closed" />
        </el-select>
        <div class="actions-row" style="margin-top: 0;">
          <el-button type="primary" @click="loadAssignments">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>
    </article>

    <article class="section-card surface-card">
      <div class="card-row-between" style="margin-bottom: 1rem;">
        <div>
          <h3>作业结果</h3>
          <p class="section-subtitle" style="margin-bottom: 0;">共 {{ store.homeworks.length }} 条作业记录</p>
        </div>
      </div>

      <el-table
        :data="store.homeworks"
        v-loading="store.loading.homeworks"
        style="width: 100%;"
        empty-text="暂无作业数据"
      >
        <el-table-column prop="title" label="作业标题" min-width="220" />
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
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <StatusTag kind="assignment" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="submittedCount" label="已交" min-width="90" />
        <el-table-column prop="pendingCount" label="未交" min-width="90" />
        <el-table-column prop="revisionRequiredCount" label="待订正" min-width="100" />
        <el-table-column label="操作" min-width="310" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="router.push(`/assignments/${row.homeworkId}`)">详情</el-button>
              <el-button link @click="router.push(`/assignments/new?edit=${row.homeworkId}`)">编辑</el-button>
              <el-button
                link
                :disabled="row.status !== 'published'"
                @click="router.push(`/assignments/${row.homeworkId}/grading`)"
              >
                批改
              </el-button>
              <el-button link :disabled="row.status !== 'published'" @click="remind(row.homeworkId)">催交</el-button>
              <el-button link type="danger" :disabled="row.status !== 'published'" @click="revoke(row.homeworkId)">
                撤回
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </article>
  </section>
</template>
