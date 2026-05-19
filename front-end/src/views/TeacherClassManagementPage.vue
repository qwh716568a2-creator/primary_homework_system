<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import type {
  TeacherClassBindingCandidate,
  TeacherClassBindingPayload
} from '@/types/teacher-portal'

const store = useTeacherPortalStore()

const dialogVisible = ref(false)
const candidateKeyword = ref('')
const selectedCandidateId = ref<string>('')

const form = reactive<TeacherClassBindingPayload>({
  classId: '',
  subjectCode: '',
  isHeadTeacher: false
})

const subjectOptions = [
  { label: '语文', value: 'chinese' },
  { label: '数学', value: 'math' },
  { label: '英语', value: 'english' },
  { label: '科学', value: 'science' },
  { label: '道德与法治', value: 'morality' }
]

const uniqueClassCount = computed(
  () => new Set(store.classRelations.map((item) => `${item.classId}`)).size
)

const headTeacherCount = computed(
  () => store.classRelations.filter((item) => item.isHeadTeacher).length
)

const bindingSummary = computed(() =>
  [...store.classRelations].sort((left, right) => {
    const gradeCompare = `${left.gradeName ?? ''}`.localeCompare(`${right.gradeName ?? ''}`)
    if (gradeCompare !== 0) return gradeCompare
    const classCompare = `${left.className}`.localeCompare(`${right.className}`)
    if (classCompare !== 0) return classCompare
    return `${left.subjectName}`.localeCompare(`${right.subjectName}`)
  })
)

const filteredCandidates = computed(() => {
  const keyword = candidateKeyword.value.trim().toLowerCase()

  return store.bindingCandidates.filter((item) => {
    if (!keyword) return true
    return [item.className, item.gradeName, item.schoolName]
      .filter(Boolean)
      .some((value) => `${value}`.toLowerCase().includes(keyword))
  })
})

const selectedCandidate = computed(() =>
  store.bindingCandidates.find((item) => `${item.classId}` === `${form.classId}`)
)

const selectedConflict = computed(() => {
  if (!selectedCandidate.value || !form.subjectCode) return null
  return selectedCandidate.value.subjectBindings?.find((item) => item.subjectCode === form.subjectCode) || null
})

const metrics = computed(() => [
  { label: '已绑定班级', value: uniqueClassCount.value, hint: '去重后的任教班级数量' },
  { label: '学科关系', value: store.classRelations.length, hint: '当前账号名下的班级学科绑定' },
  { label: '班主任配置', value: headTeacherCount.value, hint: '被标记为班主任的关系数' },
  { label: '候选班级', value: filteredCandidates.value.length, hint: '当前仍可查看和绑定的资源' }
])

function subjectLabel(subjectCode: string) {
  return subjectOptions.find((item) => item.value === subjectCode)?.label || subjectCode
}

function bindingDisplay(candidate: TeacherClassBindingCandidate) {
  if (!candidate.subjectBindings?.length) {
    return ['暂无学科绑定']
  }

  return candidate.subjectBindings.map((item) => {
    const teacherName = item.teacherName || '已绑定教师'
    return `${subjectLabel(item.subjectCode)} · ${teacherName}`
  })
}

async function loadPage() {
  try {
    await Promise.all([store.loadTeachingClasses(), store.loadBindingCandidates()])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '班级管理数据加载失败')
  }
}

function resetForm() {
  form.classId = ''
  form.subjectCode = ''
  form.isHeadTeacher = false
  selectedCandidateId.value = ''
}

function openDialog(candidate?: TeacherClassBindingCandidate) {
  resetForm()
  if (candidate) {
    form.classId = candidate.classId
    selectedCandidateId.value = `${candidate.classId}`
  }
  dialogVisible.value = true
}

function selectCandidate(candidate: TeacherClassBindingCandidate) {
  selectedCandidateId.value = `${candidate.classId}`
  form.classId = candidate.classId
}

function validateBinding() {
  if (!form.classId || !form.subjectCode) {
    ElMessage.warning('请先选择班级和学科')
    return false
  }

  const alreadyBoundByCurrentTeacher = store.classRelations.some(
    (item) => `${item.classId}` === `${form.classId}` && item.subjectCode === form.subjectCode
  )

  if (alreadyBoundByCurrentTeacher) {
    ElMessage.warning('当前班级和学科已经绑定到你的账号，无需重复添加')
    return false
  }

  if (selectedConflict.value) {
    const teacherName = selectedConflict.value.teacherName || '其他老师'
    ElMessage.warning(`${selectedCandidate.value?.className || '该班级'} 的 ${subjectLabel(form.subjectCode)} 已绑定给 ${teacherName}`)
    return false
  }

  return true
}

async function submitBinding() {
  if (!validateBinding()) return

  try {
    await store.saveClassBinding({
      classId: form.classId,
      subjectCode: form.subjectCode,
      isHeadTeacher: form.isHeadTeacher
    })
    ElMessage.success('班级绑定成功')
    dialogVisible.value = false
    resetForm()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '班级绑定失败')
  }
}

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div class="class-page">
    <section class="class-page__summary">
      <article v-for="item in metrics" :key="item.label" class="class-page__metric">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </article>
    </section>

    <section class="class-page__layout">
      <article class="class-page__panel">
        <header class="class-page__section-head">
          <div>
            <span class="class-page__eyebrow">任教关系</span>
            <h2>已绑定的班级与学科</h2>
            <p>同一个班级的同一门学科只允许绑定一位老师，这里统一查看当前关系。</p>
          </div>
        </header>

        <div class="class-page__table">
          <div class="class-page__table-head">
            <span>年级 / 班级</span>
            <span>学科</span>
            <span>学生数</span>
            <span>班主任</span>
          </div>

          <div v-if="bindingSummary.length">
            <div
              v-for="row in bindingSummary"
              :key="row.relationId || `${row.classId}-${row.subjectCode}`"
              class="class-page__table-row"
            >
              <div class="class-page__table-title">
                <strong>{{ row.className }}</strong>
                <small>{{ row.gradeName || '未配置年级' }}</small>
              </div>
              <span>{{ row.subjectName }}</span>
              <span>{{ row.studentCount ?? '--' }}</span>
              <span>{{ row.isHeadTeacher ? '是' : '否' }}</span>
            </div>
          </div>
          <div v-else class="class-page__empty">当前还没有任教班级绑定。</div>
        </div>
      </article>

      <article class="class-page__panel">
        <header class="class-page__section-head">
          <div>
            <span class="class-page__eyebrow">候选班级</span>
            <h2>可绑定班级</h2>
            <p>先查看当前班级已有学科绑定，再决定是否继续添加。</p>
          </div>
          <button type="button" class="class-page__primary" @click="openDialog()">绑定新班级</button>
        </header>

        <div class="class-page__toolbar">
          <el-input v-model="candidateKeyword" clearable placeholder="搜索候选班级" />
        </div>

        <div v-if="filteredCandidates.length" class="class-page__candidate-list">
          <button
            v-for="candidate in filteredCandidates"
            :key="candidate.classId"
            type="button"
            class="class-page__candidate"
            @click="openDialog(candidate)"
          >
            <div class="class-page__table-title">
              <strong>{{ candidate.className }}</strong>
              <small>{{ candidate.gradeName || '未配置年级' }} · {{ candidate.schoolName || '未配置学校' }}</small>
            </div>
            <div class="class-page__binding-list">
              <span v-for="binding in bindingDisplay(candidate)" :key="binding" class="class-page__pill">{{ binding }}</span>
            </div>
          </button>
        </div>
        <div v-else class="class-page__empty">当前没有可展示的候选班级。</div>
      </article>
    </section>

    <el-dialog v-model="dialogVisible" title="绑定班级与学科" width="680px">
      <div class="class-page__dialog">
        <div class="class-page__dialog-list">
          <button
            v-for="candidate in filteredCandidates"
            :key="candidate.classId"
            type="button"
            class="class-page__dialog-item"
            :class="{ 'is-active': `${candidate.classId}` === selectedCandidateId }"
            @click="selectCandidate(candidate)"
          >
            <strong>{{ candidate.className }}</strong>
            <small>{{ candidate.gradeName || '未配置年级' }}</small>
          </button>
        </div>

        <div class="class-page__dialog-form">
          <el-select v-model="form.subjectCode" placeholder="选择学科">
            <el-option v-for="item in subjectOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-checkbox v-model="form.isHeadTeacher">同时标记为班主任</el-checkbox>

          <div v-if="selectedConflict" class="class-page__warning">
            当前班级的 {{ subjectLabel(form.subjectCode) }} 已经绑定给 {{ selectedConflict.teacherName || '其他老师' }}。
          </div>
        </div>
      </div>

      <template #footer>
        <div class="class-page__dialog-actions">
          <button type="button" class="class-page__secondary" @click="dialogVisible = false">取消</button>
          <button type="button" class="class-page__primary" @click="submitBinding">确认绑定</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.class-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.class-page__summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.class-page__metric,
.class-page__panel {
  border: 1px solid #dde6f2;
  border-radius: 26px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.97) 0%, rgba(248, 251, 255, 0.97) 100%);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.05);
}

.class-page__metric {
  padding: 20px 22px;
  display: grid;
  gap: 8px;
}

.class-page__metric span,
.class-page__metric small,
.class-page__eyebrow {
  color: #6d7d96;
}

.class-page__metric strong {
  font-size: 40px;
  line-height: 1;
  color: #13253d;
}

.class-page__layout {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 18px;
  align-items: start;
}

.class-page__panel {
  padding: 20px;
}

.class-page__section-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.class-page__section-head h2 {
  margin: 6px 0 0;
  color: #13253d;
}

.class-page__section-head p {
  margin: 8px 0 0;
  color: #70819a;
  line-height: 1.6;
}

.class-page__primary,
.class-page__secondary {
  border-radius: 14px;
  padding: 11px 18px;
  border: 1px solid transparent;
  cursor: pointer;
  font-weight: 700;
}

.class-page__primary {
  background: linear-gradient(135deg, #4c74ff 0%, #6b84ff 100%);
  color: #fff;
  box-shadow: 0 12px 24px rgba(76, 116, 255, 0.22);
}

.class-page__secondary {
  background: #fff;
  border-color: #dce5f2;
  color: #30445f;
}

.class-page__toolbar {
  margin-bottom: 16px;
}

.class-page__table {
  border: 1px solid #e2e9f3;
  border-radius: 22px;
  overflow: hidden;
  background: #fff;
}

.class-page__table-head,
.class-page__table-row {
  display: grid;
  grid-template-columns: minmax(180px, 1.4fr) 120px 90px 90px;
  gap: 12px;
  align-items: center;
}

.class-page__table-head {
  padding: 16px 18px;
  background: #f5f8fd;
  color: #6d7d96;
  font-size: 13px;
  font-weight: 700;
}

.class-page__table-row {
  padding: 18px;
  border-top: 1px solid #edf2f8;
  color: #30445f;
}

.class-page__table-title {
  display: grid;
  gap: 4px;
}

.class-page__table-title strong {
  color: #13253d;
  font-size: 22px;
}

.class-page__table-title small {
  color: #73839d;
}

.class-page__candidate-list {
  display: grid;
  gap: 12px;
}

.class-page__candidate {
  display: grid;
  gap: 12px;
  padding: 18px;
  border: 1px solid #dce5f2;
  border-radius: 22px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.class-page__candidate:hover {
  border-color: #5b84ff;
  box-shadow: 0 12px 22px rgba(91, 132, 255, 0.12);
  transform: translateY(-2px);
}

.class-page__binding-list,
.class-page__dialog-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.class-page__pill {
  padding: 6px 10px;
  border-radius: 999px;
  background: #f5f8fd;
  color: #55677f;
  font-size: 13px;
}

.class-page__empty {
  padding: 28px 24px;
  border: 1px dashed #d7e1ef;
  border-radius: 18px;
  color: #7a8da7;
  text-align: center;
}

.class-page__dialog {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 16px;
}

.class-page__dialog-list {
  display: grid;
  gap: 8px;
  max-height: 340px;
  overflow: auto;
}

.class-page__dialog-item {
  display: grid;
  gap: 4px;
  padding: 14px;
  border: 1px solid #dce5f2;
  border-radius: 16px;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.class-page__dialog-item.is-active {
  border-color: #5b84ff;
  background: #eef4ff;
}

.class-page__dialog-item small {
  color: #73839d;
  font-size: 12px;
}

.class-page__dialog-form {
  display: grid;
  gap: 14px;
}

.class-page__warning {
  padding: 12px 14px;
  border: 1px solid #f1c27d;
  border-radius: 14px;
  background: #fff4dd;
  color: #9a5d06;
  line-height: 1.7;
  font-size: 13px;
}

@media (max-width: 1380px) {
  .class-page__layout,
  .class-page__summary,
  .class-page__dialog,
  .class-page__table-head,
  .class-page__table-row {
    grid-template-columns: 1fr;
  }
}
</style>
