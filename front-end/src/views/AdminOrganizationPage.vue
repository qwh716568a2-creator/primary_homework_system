<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { useAdminPortalStore } from '@/stores/adminPortal'
import { getSchoolSummary, normalizeStatus } from '@/utils/admin-portal-view'

const store = useAdminPortalStore()
const { schools, classes, users, loading } = storeToRefs(store)

const filters = reactive({
  schoolId: '' as number | string | '',
  keyword: ''
})

const refreshing = ref(false)

const schoolSummary = computed(() => getSchoolSummary(schools.value, classes.value, users.value))

const filteredClasses = computed(() =>
  classes.value.filter((item) => {
    const schoolMatch = !filters.schoolId || `${item.schoolId}` === `${filters.schoolId}`
    const keyword = filters.keyword.trim().toLowerCase()
    const keywordMatch =
      !keyword ||
      [item.className, item.schoolName, item.gradeName, item.homeroomTeacherName]
        .filter(Boolean)
        .some((value) => `${value}`.toLowerCase().includes(keyword))

    return schoolMatch && keywordMatch
  })
)

async function loadData() {
  refreshing.value = true

  try {
    await Promise.all([
      store.loadSchools(),
      store.loadClasses({
        schoolId: filters.schoolId || undefined,
        keyword: filters.keyword.trim() || undefined
      }),
      store.loadUsers({ pageNo: 1, pageSize: 200 })
    ])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '组织数据加载失败')
  } finally {
    refreshing.value = false
  }
}

function resetFilters() {
  filters.schoolId = ''
  filters.keyword = ''
  void loadData()
}

onMounted(() => {
  void loadData()
})
</script>

<template>
  <section class="page-stack">
    <div class="page-header">
      <div>
        <h2>组织管理</h2>
        <p>查看学校组织覆盖、班级结构和班主任配置情况。</p>
      </div>
      <el-button :loading="refreshing" @click="loadData">刷新组织</el-button>
    </div>

    <section class="section-card surface-card">
      <div class="filter-row">
        <el-select v-model="filters.schoolId" clearable placeholder="按学校筛选">
          <el-option
            v-for="item in store.schoolOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-input v-model="filters.keyword" clearable placeholder="搜索班级 / 年级 / 班主任" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </section>

    <section class="section-card surface-card" v-loading="loading.schools || loading.classes || loading.users">
      <h3>学校概览</h3>
      <p class="section-subtitle">按学校维度查看班级、教师和学生规模。</p>

      <div v-if="schoolSummary.length" class="org-school-grid">
        <article v-for="item in schoolSummary" :key="item.schoolId" class="org-school-card">
          <div class="org-school-card__top">
            <div>
              <strong>{{ item.schoolName }}</strong>
              <p>{{ item.schoolCode || '未设置学校编码' }}</p>
            </div>
            <el-tag :type="normalizeStatus(item.status) === 'enabled' ? 'success' : 'info'" effect="plain">
              {{ normalizeStatus(item.status) === 'enabled' ? '启用中' : '已停用' }}
            </el-tag>
          </div>
          <div class="chip-row">
            <span class="soft-chip">班级 {{ item.classCount ?? 0 }}</span>
            <span class="soft-chip">教师 {{ item.teacherCount ?? 0 }}</span>
            <span class="soft-chip">学生 {{ item.studentCount ?? 0 }}</span>
          </div>
        </article>
      </div>
      <div v-else class="empty-state">暂无学校组织数据</div>
    </section>

    <section class="section-card surface-card" v-loading="loading.classes">
      <h3>班级列表</h3>
      <p class="section-subtitle">重点查看年级、班主任和学生规模。</p>

      <el-table v-if="filteredClasses.length" :data="filteredClasses" stripe>
        <el-table-column prop="className" label="班级" min-width="180" />
        <el-table-column prop="schoolName" label="学校" min-width="180" />
        <el-table-column prop="gradeName" label="年级" min-width="120" />
        <el-table-column prop="homeroomTeacherName" label="班主任" min-width="140">
          <template #default="{ row }">
            {{ row.homeroomTeacherName || '未配置' }}
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="学生数" width="110" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="normalizeStatus(row.status) === 'enabled' ? 'success' : 'info'" effect="plain">
              {{ normalizeStatus(row.status) === 'enabled' ? '启用中' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="empty-state">暂无班级数据</div>
    </section>
  </section>
</template>

<style scoped>
.org-school-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1rem;
}

.org-school-card {
  padding: 1.2rem;
  border-radius: 24px;
  background: rgba(248, 251, 255, 0.86);
  border: 1px solid rgba(213, 227, 244, 0.8);
}

.org-school-card__top {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.org-school-card strong {
  display: block;
  font-size: 1.05rem;
}

.org-school-card p {
  margin: 0.35rem 0 0;
  color: #6d8096;
}

@media (max-width: 1100px) {
  .org-school-grid {
    grid-template-columns: 1fr;
  }
}
</style>
