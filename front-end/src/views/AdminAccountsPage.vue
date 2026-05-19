<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAdminPortalStore } from '@/stores/adminPortal'
import type { AdminUserItem, AdminUserPayload, AdminUserQuery } from '@/types/admin-portal'
import {
  getAccountDisplay,
  getAdminRoleLabel,
  normalizeStatus
} from '@/utils/admin-portal-view'

interface AdminUserFormModel {
  userName: string
  roleType: AdminUserPayload['roleType']
  schoolId: number | string | ''
  password: string
  loginName: string
  status: string
  teacherNo: string
  mobile: string
  studentNo: string
  gradeId: number | string | ''
  classId: number | string | ''
}

const store = useAdminPortalStore()
const { users, classes, loading } = storeToRefs(store)

const filters = reactive({
  keyword: '',
  roleType: '' as string,
  schoolId: '' as number | string | '',
  status: '' as string
})

const dialogVisible = ref(false)
const submitting = ref(false)
const editingUserId = ref<number | string | null>(null)
const userFormRef = ref<FormInstance>()

const userForm = reactive<AdminUserFormModel>({
  userName: '',
  roleType: 'teacher',
  schoolId: '',
  password: '',
  loginName: '',
  status: 'enabled',
  teacherNo: '',
  mobile: '',
  studentNo: '',
  gradeId: '',
  classId: ''
})

const roleOptions: Array<{ label: string; value: AdminUserPayload['roleType'] }> = [
  { label: '教师', value: 'teacher' },
  { label: '学生', value: 'student' },
  { label: '家长', value: 'parent' },
  { label: '管理员', value: 'admin' }
]

const statusOptions = [
  { label: '启用中', value: 'enabled' },
  { label: '已停用', value: 'disabled' }
]

const userFormRules: FormRules<AdminUserFormModel> = {
  userName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleType: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const filteredClasses = computed(() =>
  classes.value.filter((item) => !userForm.schoolId || `${item.schoolId}` === `${userForm.schoolId}`)
)

const roleSummary = computed(() =>
  roleOptions.map((item) => ({
    ...item,
    count: users.value.filter((user) => user.roleType === item.value).length
  }))
)

const dialogTitle = computed(() => (editingUserId.value === null ? '新建账号' : '编辑账号'))

function buildUserQuery(): AdminUserQuery {
  return {
    keyword: filters.keyword.trim() || undefined,
    roleType: filters.roleType || undefined,
    schoolId: filters.schoolId || undefined,
    status: filters.status || undefined,
    pageNo: 1,
    pageSize: 200
  }
}

async function loadData() {
  try {
    await Promise.all([
      store.loadSchools(),
      store.loadClasses(),
      store.loadUsers(buildUserQuery())
    ])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '账号数据加载失败')
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.roleType = ''
  filters.schoolId = ''
  filters.status = ''
  void loadData()
}

function resetUserForm() {
  editingUserId.value = null
  userForm.userName = ''
  userForm.roleType = 'teacher'
  userForm.schoolId = ''
  userForm.password = ''
  userForm.loginName = ''
  userForm.status = 'enabled'
  userForm.teacherNo = ''
  userForm.mobile = ''
  userForm.studentNo = ''
  userForm.gradeId = ''
  userForm.classId = ''
  userFormRef.value?.clearValidate()
}

function openCreate(roleType: AdminUserPayload['roleType'] = 'teacher') {
  resetUserForm()
  userForm.roleType = roleType
  dialogVisible.value = true
}

function openEdit(user: AdminUserItem) {
  editingUserId.value = user.userId
  userForm.userName = user.userName
  userForm.roleType = user.roleType as AdminUserPayload['roleType']
  userForm.schoolId = user.schoolId ?? ''
  userForm.password = ''
  userForm.loginName = user.loginName || user.account || ''
  userForm.status = normalizeStatus(user.status)
  userForm.teacherNo = user.teacherNo || ''
  userForm.mobile = user.mobile || ''
  userForm.studentNo = user.studentNo || ''
  userForm.gradeId = user.gradeId ?? ''
  userForm.classId = user.classId ?? ''
  dialogVisible.value = true
}

function validateRoleFields() {
  if (userForm.roleType !== 'admin' && !userForm.schoolId) {
    ElMessage.warning('请先选择所属学校')
    return false
  }

  if (editingUserId.value === null && !userForm.password.trim()) {
    ElMessage.warning('新建账号时请设置初始密码')
    return false
  }

  if (userForm.roleType === 'admin' && !userForm.loginName.trim()) {
    ElMessage.warning('管理员账号需要填写登录账号')
    return false
  }

  if (userForm.roleType === 'teacher' && !userForm.mobile.trim()) {
    ElMessage.warning('教师账号需要填写手机号')
    return false
  }

  if (userForm.roleType === 'student' && !userForm.studentNo.trim()) {
    ElMessage.warning('学生账号需要填写学号')
    return false
  }

  if (userForm.roleType === 'student' && !userForm.classId) {
    ElMessage.warning('学生账号需要绑定班级')
    return false
  }

  if (userForm.roleType === 'parent' && !userForm.mobile.trim()) {
    ElMessage.warning('家长账号需要填写手机号')
    return false
  }

  return true
}

function buildPayload(): AdminUserPayload {
  const payload: AdminUserPayload = {
    userName: userForm.userName.trim(),
    roleType: userForm.roleType,
    schoolId: userForm.roleType === 'admin' ? null : userForm.schoolId || null,
    status: userForm.status
  }

  if (userForm.password.trim()) {
    payload.password = userForm.password.trim()
  }

  if (userForm.roleType === 'admin') {
    payload.loginName = userForm.loginName.trim()
  }

  const profile: NonNullable<AdminUserPayload['profile']> = {}

  if (userForm.roleType === 'teacher') {
    profile.mobile = userForm.mobile.trim()
    if (userForm.teacherNo.trim()) {
      profile.teacherNo = userForm.teacherNo.trim()
    }
  }

  if (userForm.roleType === 'student') {
    profile.studentNo = userForm.studentNo.trim()
    if (userForm.gradeId) {
      profile.gradeId = userForm.gradeId
    }
    if (userForm.classId) {
      profile.classId = userForm.classId
    }
  }

  if (userForm.roleType === 'parent') {
    profile.mobile = userForm.mobile.trim()
  }

  if (Object.keys(profile).length > 0) {
    payload.profile = profile
  }

  return payload
}

async function submitUser() {
  const valid = (await userFormRef.value?.validate().catch(() => false)) ?? true

  if (!valid || !validateRoleFields()) {
    return
  }

  submitting.value = true

  try {
    await store.saveUser(buildPayload(), editingUserId.value ?? undefined)
    await store.loadUsers(buildUserQuery())
    ElMessage.success(editingUserId.value === null ? '账号创建成功' : '账号已更新')
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '账号保存失败')
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(user: AdminUserItem) {
  const nextStatus = normalizeStatus(user.status) === 'enabled' ? 'disabled' : 'enabled'

  try {
    await store.saveUser({ status: nextStatus }, user.userId)
    await store.loadUsers(buildUserQuery())
    ElMessage.success(nextStatus === 'enabled' ? '账号已启用' : '账号已停用')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '账号状态更新失败')
  }
}

onMounted(() => {
  void loadData()
})
</script>

<template>
  <section class="page-stack">
    <div class="page-header">
      <div>
        <h2>账号管理</h2>
        <p>统一维护教师、学生、家长和管理员账号。</p>
      </div>
      <div class="account-header-actions">
        <el-button @click="openCreate('teacher')">新建教师</el-button>
        <el-button @click="openCreate('student')">新建学生</el-button>
        <el-button type="primary" @click="openCreate('admin')">新建管理员</el-button>
      </div>
    </div>

    <div class="grid-cards">
      <article v-for="item in roleSummary" :key="item.value" class="section-card surface-card account-summary-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.count }}</strong>
        <small>当前角色账号数</small>
      </article>
    </div>

    <section class="section-card surface-card">
      <div class="filter-row">
        <el-input v-model="filters.keyword" clearable placeholder="搜索姓名 / 账号 / 手机号 / 学号" />
        <el-select v-model="filters.roleType" clearable placeholder="角色">
          <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="filters.schoolId" clearable placeholder="学校">
          <el-option
            v-for="item in store.schoolOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select v-model="filters.status" clearable placeholder="状态">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </section>

    <section class="section-card surface-card" v-loading="loading.users">
      <el-table v-if="users.length" :data="users" stripe>
        <el-table-column prop="userName" label="姓名" min-width="140" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            {{ getAdminRoleLabel(row.roleType) }}
          </template>
        </el-table-column>
        <el-table-column prop="schoolName" label="学校" min-width="180">
          <template #default="{ row }">
            {{ row.schoolName || (row.roleType === 'admin' ? '平台管理中心' : '未关联学校') }}
          </template>
        </el-table-column>
        <el-table-column label="账号" min-width="180">
          <template #default="{ row }">
            {{ getAccountDisplay(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" min-width="140">
          <template #default="{ row }">
            {{ row.className || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="normalizeStatus(row.status) === 'enabled' ? 'success' : 'info'" effect="plain">
              {{ normalizeStatus(row.status) === 'enabled' ? '启用中' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="account-table-actions">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link @click="toggleStatus(row)">
                {{ normalizeStatus(row.status) === 'enabled' ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="empty-state">暂无账号数据</div>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form ref="userFormRef" :model="userForm" :rules="userFormRules" label-position="top">
        <div class="account-form-grid">
          <el-form-item label="姓名" prop="userName">
            <el-input v-model="userForm.userName" placeholder="请输入姓名" />
          </el-form-item>

          <el-form-item label="角色" prop="roleType">
            <el-select v-model="userForm.roleType" placeholder="请选择角色">
              <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="所属学校">
            <el-select v-model="userForm.schoolId" clearable placeholder="请选择学校">
              <el-option
                v-for="item in store.schoolOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="账号状态">
            <el-select v-model="userForm.status">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>

          <el-form-item v-if="userForm.roleType === 'admin'" label="登录账号">
            <el-input v-model="userForm.loginName" placeholder="请输入管理员账号" />
          </el-form-item>

          <el-form-item v-if="userForm.roleType === 'teacher'" label="手机号">
            <el-input v-model="userForm.mobile" placeholder="请输入教师手机号" />
          </el-form-item>

          <el-form-item v-if="userForm.roleType === 'teacher'" label="教师工号">
            <el-input v-model="userForm.teacherNo" placeholder="可选" />
          </el-form-item>

          <el-form-item v-if="userForm.roleType === 'student'" label="学号">
            <el-input v-model="userForm.studentNo" placeholder="请输入学生学号" />
          </el-form-item>

          <el-form-item v-if="userForm.roleType === 'student'" label="班级">
            <el-select v-model="userForm.classId" clearable placeholder="请选择班级">
              <el-option
                v-for="item in filteredClasses"
                :key="item.classId"
                :label="item.className"
                :value="item.classId"
              />
            </el-select>
          </el-form-item>

          <el-form-item v-if="userForm.roleType === 'parent'" label="手机号">
            <el-input v-model="userForm.mobile" placeholder="请输入家长手机号" />
          </el-form-item>

          <el-form-item label="密码">
            <el-input
              v-model="userForm.password"
              type="password"
              show-password
              :placeholder="editingUserId === null ? '请输入初始密码' : '留空则不修改密码'"
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitUser">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.account-header-actions,
.account-table-actions,
.dialog-footer {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.account-summary-card {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.account-summary-card span {
  color: #6f8397;
  font-size: 0.88rem;
}

.account-summary-card strong {
  font-size: 2rem;
  color: #123454;
}

.account-summary-card small {
  color: #71849b;
}

.account-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 1rem;
}

@media (max-width: 1100px) {
  .grid-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .account-header-actions {
    width: 100%;
    flex-wrap: wrap;
    justify-content: flex-start;
  }

  .account-form-grid,
  .grid-cards {
    grid-template-columns: 1fr;
  }
}
</style>
