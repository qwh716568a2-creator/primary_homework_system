<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { useAdminPortalStore } from '@/stores/adminPortal'
import {
  getRelationTypeLabel,
  getSubjectLabel,
  withResolvedParentRelation,
  withResolvedTeacherRelation
} from '@/utils/admin-portal-view'

interface TeacherRelationFormModel {
  teacherId: number | string | ''
  classId: number | string | ''
  subjectCode: string
  isHeadTeacher: boolean
}

interface ParentRelationFormModel {
  parentUserId: number | string | ''
  studentId: number | string | ''
  relationType: string
  isPrimary: boolean
}

const store = useAdminPortalStore()
const { teacherRelations, parentRelations, users, classes, loading } = storeToRefs(store)

const refreshing = ref(false)
const teacherDialogVisible = ref(false)
const parentDialogVisible = ref(false)

const teacherForm = reactive<TeacherRelationFormModel>({
  teacherId: '',
  classId: '',
  subjectCode: 'math',
  isHeadTeacher: false
})

const parentForm = reactive<ParentRelationFormModel>({
  parentUserId: '',
  studentId: '',
  relationType: 'guardian',
  isPrimary: true
})

const subjectOptions = [
  { label: '语文', value: 'chinese' },
  { label: '数学', value: 'math' },
  { label: '英语', value: 'english' },
  { label: '科学', value: 'science' },
  { label: '道德与法治', value: 'morality' }
]

const relationTypeOptions = [
  { label: '父亲', value: 'father' },
  { label: '母亲', value: 'mother' },
  { label: '监护人', value: 'guardian' },
  { label: '祖辈', value: 'grandparent' }
]

const resolvedTeacherRelations = computed(() =>
  teacherRelations.value.map((item) => withResolvedTeacherRelation(item, users.value, classes.value))
)

const resolvedParentRelations = computed(() =>
  parentRelations.value.map((item) => withResolvedParentRelation(item, users.value, classes.value))
)

async function loadData() {
  refreshing.value = true

  try {
    await Promise.all([
      store.loadSchools(),
      store.loadClasses(),
      store.loadUsers({ pageNo: 1, pageSize: 300 }),
      store.loadTeacherRelations(),
      store.loadParentRelations()
    ])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '关系数据加载失败，请稍后重试。')
  } finally {
    refreshing.value = false
  }
}

function resetTeacherForm() {
  teacherForm.teacherId = ''
  teacherForm.classId = ''
  teacherForm.subjectCode = 'math'
  teacherForm.isHeadTeacher = false
}

function resetParentForm() {
  parentForm.parentUserId = ''
  parentForm.studentId = ''
  parentForm.relationType = 'guardian'
  parentForm.isPrimary = true
}

async function submitTeacherRelation() {
  if (!teacherForm.teacherId || !teacherForm.classId || !teacherForm.subjectCode) {
    ElMessage.warning('请完整填写教师授课关系。')
    return
  }

  try {
    await store.saveTeacherRelation({
      teacherId: teacherForm.teacherId,
      classId: teacherForm.classId,
      subjectCode: teacherForm.subjectCode,
      isHeadTeacher: teacherForm.isHeadTeacher
    })
    ElMessage.success('教师授课关系已保存。')
    teacherDialogVisible.value = false
    resetTeacherForm()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '教师授课关系保存失败，请稍后重试。')
  }
}

async function submitParentRelation() {
  if (!parentForm.parentUserId || !parentForm.studentId || !parentForm.relationType) {
    ElMessage.warning('请完整填写家长绑定关系。')
    return
  }

  try {
    await store.saveParentRelation({
      parentUserId: parentForm.parentUserId,
      studentId: parentForm.studentId,
      relationType: parentForm.relationType,
      isPrimary: parentForm.isPrimary
    })
    ElMessage.success('家长绑定关系已保存。')
    parentDialogVisible.value = false
    resetParentForm()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '家长绑定关系保存失败，请稍后重试。')
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
        <h2>关系配置</h2>
        <p>维护教师授课关系与家长学生绑定关系。</p>
      </div>
      <el-button :loading="refreshing" @click="loadData">刷新关系</el-button>
    </div>

    <div class="grid-cards">
      <article class="section-card surface-card relation-summary-card">
        <span>教师授课关系</span>
        <strong>{{ resolvedTeacherRelations.length }}</strong>
        <small>已配置条目</small>
      </article>
      <article class="section-card surface-card relation-summary-card">
        <span>家长绑定关系</span>
        <strong>{{ resolvedParentRelations.length }}</strong>
        <small>已配置条目</small>
      </article>
      <article class="section-card surface-card relation-summary-card">
        <span>可选教师</span>
        <strong>{{ store.teacherOptions.length }}</strong>
        <small>可参与授课配置</small>
      </article>
      <article class="section-card surface-card relation-summary-card">
        <span>可选学生</span>
        <strong>{{ store.studentOptions.length }}</strong>
        <small>可参与家长绑定</small>
      </article>
    </div>

    <div class="split-grid">
      <section class="section-card surface-card" v-loading="loading.teacherRelations || loading.users || loading.classes">
        <div class="relation-section-header">
          <div>
            <h3>教师授课关系</h3>
            <p class="section-subtitle">配置教师与班级、学科、班主任身份。</p>
          </div>
          <el-button type="primary" @click="teacherDialogVisible = true">新增关系</el-button>
        </div>

        <el-table v-if="resolvedTeacherRelations.length" :data="resolvedTeacherRelations" stripe>
          <el-table-column prop="teacherName" label="教师" min-width="140" />
          <el-table-column prop="className" label="班级" min-width="140" />
          <el-table-column label="学科" min-width="140">
            <template #default="{ row }">
              {{ row.subjectName || getSubjectLabel(row.subjectCode) }}
            </template>
          </el-table-column>
          <el-table-column label="班主任" width="100">
            <template #default="{ row }">
              {{ row.isHeadTeacher ? '是' : '否' }}
            </template>
          </el-table-column>
        </el-table>
        <div v-else class="empty-state">暂无教师授课关系</div>
      </section>

      <section class="section-card surface-card" v-loading="loading.parentRelations || loading.users || loading.classes">
        <div class="relation-section-header">
          <div>
            <h3>家长绑定关系</h3>
            <p class="section-subtitle">配置家长与学生的对应关系。</p>
          </div>
          <el-button type="primary" @click="parentDialogVisible = true">新增绑定</el-button>
        </div>

        <el-table v-if="resolvedParentRelations.length" :data="resolvedParentRelations" stripe>
          <el-table-column prop="parentName" label="家长" min-width="140" />
          <el-table-column prop="parentMobile" label="手机号" min-width="150" />
          <el-table-column prop="studentName" label="学生" min-width="140" />
          <el-table-column prop="className" label="班级" min-width="140" />
          <el-table-column label="关系" width="120">
            <template #default="{ row }">
              {{ row.relationTypeLabel || getRelationTypeLabel(row.relationType) }}
            </template>
          </el-table-column>
          <el-table-column label="主联系人" width="100">
            <template #default="{ row }">
              {{ row.isPrimary ? '是' : '否' }}
            </template>
          </el-table-column>
        </el-table>
        <div v-else class="empty-state">暂无家长绑定关系</div>
      </section>
    </div>

    <el-dialog v-model="teacherDialogVisible" title="新增教师授课关系" width="560px">
      <div class="relation-form-grid">
        <el-form-item label="教师">
          <el-select
            v-model="teacherForm.teacherId"
            placeholder="请选择教师"
            filterable
            clearable
            no-match-text="没有匹配的教师"
            no-data-text="暂无教师数据"
          >
            <el-option
              v-for="item in store.teacherOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="班级">
          <el-select
            v-model="teacherForm.classId"
            placeholder="请选择班级"
            filterable
            clearable
            no-match-text="没有匹配的班级"
            no-data-text="暂无班级数据"
          >
            <el-option v-for="item in store.classOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="学科">
          <el-select v-model="teacherForm.subjectCode" filterable>
            <el-option v-for="item in subjectOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="班主任配置">
          <el-switch v-model="teacherForm.isHeadTeacher" />
        </el-form-item>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="teacherDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="loading.action" @click="submitTeacherRelation">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="parentDialogVisible" title="新增家长绑定关系" width="560px">
      <div class="relation-form-grid">
        <el-form-item label="家长">
          <el-select
            v-model="parentForm.parentUserId"
            placeholder="请选择家长"
            filterable
            clearable
            no-match-text="没有匹配的家长"
            no-data-text="暂无家长数据"
          >
            <el-option
              v-for="item in store.parentOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="学生">
          <el-select
            v-model="parentForm.studentId"
            placeholder="请选择学生"
            filterable
            clearable
            no-match-text="没有匹配的学生"
            no-data-text="暂无学生数据"
          >
            <el-option
              v-for="item in store.studentOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="关系">
          <el-select v-model="parentForm.relationType" filterable>
            <el-option
              v-for="item in relationTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="主联系人">
          <el-switch v-model="parentForm.isPrimary" />
        </el-form-item>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="parentDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="loading.action" @click="submitParentRelation">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.relation-summary-card {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.relation-summary-card span {
  color: #6d8194;
  font-size: 0.88rem;
}

.relation-summary-card strong {
  color: #123556;
  font-size: 2rem;
}

.relation-summary-card small {
  color: #73869d;
}

.relation-section-header,
.dialog-footer {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
}

.relation-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 1rem;
}

@media (max-width: 1100px) {
  .grid-cards,
  .split-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .relation-section-header {
    flex-direction: column;
    align-items: stretch;
  }

  .relation-form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
