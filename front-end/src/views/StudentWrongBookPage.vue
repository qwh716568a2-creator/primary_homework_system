<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { UploadUserFile } from 'element-plus'
import { CollectionTag, RefreshRight } from '@element-plus/icons-vue'
import StatusTag from '@/components/StatusTag.vue'
import { uploadStudentFile } from '@/api/student'
import { useStudentPortalStore } from '@/stores/studentPortal'
import type { StudentWrongBookPoolType } from '@/types/student-portal'
import {
  formatStudentFullDateTime,
  getStudentWrongBookPoolLabel,
  wrongReasonOptions
} from '@/utils/student-portal-view'

const route = useRoute()
const router = useRouter()
const store = useStudentPortalStore()
const selectedSubject = ref('all')
const selectedStatus = ref<'all' | 'pending_fix' | 'fixed' | 'mastered'>('all')
const selectedPool = ref<'all' | StudentWrongBookPoolType>('all')
const detailVisible = ref(false)
const createVisible = ref(false)
const activeWrongBookId = ref('')
const createFiles = ref<UploadUserFile[]>([])

const createForm = reactive({
  homeworkId: '',
  subjectCode: '',
  subjectName: '',
  questionNo: '',
  questionText: '',
  studentAnswer: '',
  correctAnswer: '',
  analysisText: '',
  wrongReasonCode: ''
})

const fixForm = reactive({
  fixedText: '',
  files: [] as UploadUserFile[]
})

const currentWrongBook = computed(() => store.getWrongBook(activeWrongBookId.value))
const displayWrongBooks = computed(() =>
  store.wrongBooks.filter((item) => selectedPool.value === 'all' || store.resolveWrongBookPoolType(item) === selectedPool.value)
)
const wrongBookPracticeStats = computed(() => [
  {
    label: '活跃错题',
    value: store.activeWrongBookCount,
    hint: '优先进入练习集'
  },
  {
    label: '风险正确',
    value: store.riskyCorrectWrongBookCount,
    hint: '最近做对但未稳定'
  },
  {
    label: '已掌握',
    value: store.masteredArchiveWrongBookCount,
    hint: '连续答对两次'
  }
])

function resetCreateForm() {
  Object.assign(createForm, {
    homeworkId: '',
    subjectCode: '',
    subjectName: '',
    questionNo: '',
    questionText: '',
    studentAnswer: '',
    correctAnswer: '',
    analysisText: '',
    wrongReasonCode: ''
  })
  createFiles.value = []
}

function applyRoutePrefill() {
  const subjectName = `${route.query.subjectName ?? ''}`.trim()
  const questionText = `${route.query.questionText ?? ''}`.trim()
  const analysisText = `${route.query.analysisText ?? ''}`.trim()
  const studentAnswer = `${route.query.studentAnswer ?? ''}`.trim()
  const homeworkId = `${route.query.homeworkId ?? ''}`.trim()

  if (!subjectName && !questionText && !analysisText && !studentAnswer && !homeworkId) {
    return
  }

  createForm.homeworkId = homeworkId
  createForm.questionText = questionText
  createForm.studentAnswer = studentAnswer
  createForm.analysisText = analysisText

  const subjectOption = store.wrongBookSubjects.find((item) => item.subjectName === subjectName)
  if (subjectOption) {
    createForm.subjectCode = subjectOption.subjectCode
    createForm.subjectName = subjectOption.subjectName
  } else {
    createForm.subjectName = subjectName
  }

  createVisible.value = true
}

watch(
  () => route.fullPath,
  () => {
    applyRoutePrefill()
  }
)

async function loadPage() {
  try {
    await Promise.all([
      store.loadWrongBookSubjects(),
      store.loadWrongBooks(selectedSubject.value, selectedStatus.value)
    ])
    applyRoutePrefill()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '错题本加载失败，请稍后重试。')
  }
}

async function refreshList() {
  try {
    await store.loadWrongBooks(selectedSubject.value, selectedStatus.value)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '错题本列表刷新失败。')
  }
}

async function startPractice() {
  try {
    await store.createWrongBookPracticePlan(selectedSubject.value, 10)
    void router.push({
      path: '/student/wrong-book/practice',
      query: {
        subjectCode: selectedSubject.value
      }
    })
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '错题小练习生成失败，请稍后重试。')
  }
}

function openPracticeHistory() {
  void router.push('/student/wrong-book/practice/history')
}

async function openDetail(wrongBookId: string) {
  activeWrongBookId.value = wrongBookId
  detailVisible.value = true
  fixForm.fixedText = ''
  fixForm.files = []

  try {
    await store.loadWrongBookDetail(wrongBookId)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '错题详情加载失败。')
  }
}

async function uploadFiles(files: UploadUserFile[], bizType: string, assetRole: 'question_image' | 'correction_image') {
  const assets = []

  for (const item of files) {
    if (!item.raw) {
      continue
    }

    const result = await uploadStudentFile(item.raw, bizType)
    assets.push({
      assetRole,
      assetType: 'image',
      assetUrl: result.fileUrl,
      assetName: item.name
    })
  }

  return assets
}

async function submitCreate() {
  if (!createForm.subjectCode && !createForm.subjectName) {
    ElMessage.warning('请先选择科目。')
    return
  }

  if (!createForm.questionText.trim()) {
    ElMessage.warning('请先填写题目内容。')
    return
  }

  try {
    const assets = createFiles.value.length
      ? await uploadFiles(createFiles.value, 'wrongbook_question_image', 'question_image')
      : []

    await store.addWrongBook({
      homeworkId: createForm.homeworkId || undefined,
      subjectCode: createForm.subjectCode || undefined,
      subjectName: createForm.subjectName || undefined,
      questionNo: createForm.questionNo || undefined,
      questionText: createForm.questionText.trim(),
      studentAnswer: createForm.studentAnswer.trim() || undefined,
      correctAnswer: createForm.correctAnswer.trim() || undefined,
      analysisText: createForm.analysisText.trim() || undefined,
      wrongReasonCode: createForm.wrongReasonCode || undefined,
      assets
    })

    ElMessage.success('错题已加入错题本。')
    createVisible.value = false
    resetCreateForm()
    await refreshList()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '新增错题失败，请稍后重试。')
  }
}

async function submitFix() {
  if (!currentWrongBook.value) {
    return
  }

  if (!fixForm.fixedText.trim()) {
    ElMessage.warning('请先填写订正说明。')
    return
  }

  try {
    const assets = fixForm.files.length
      ? await uploadFiles(fixForm.files, 'wrongbook_correction_image', 'correction_image')
      : []

    await store.fixWrongBook(currentWrongBook.value.id, {
      fixedText: fixForm.fixedText.trim(),
      assets
    })

    ElMessage.success('订正内容已提交。')
    fixForm.fixedText = ''
    fixForm.files = []
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '订正提交失败，请稍后重试。')
  }
}

async function markMastered() {
  if (!currentWrongBook.value) {
    return
  }

  try {
    await store.masteredWrongBook(currentWrongBook.value.id)
    ElMessage.success('这条错题已标记为掌握。')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败，请稍后重试。')
  }
}

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div class="page-stack">
    <header class="page-header">
      <div>
        <h2>错题本</h2>
        <p>把老师标记的错题和自己主动整理的题集中管理，订正后继续跟踪掌握状态。</p>
      </div>
      <div class="student-wrongbook-header-actions">
        <el-button @click="openPracticeHistory">
          <el-icon><CollectionTag /></el-icon>
          练习记录
        </el-button>
        <el-button type="primary" :loading="store.loading.wrongBookPractice" @click="startPractice">
          <el-icon><RefreshRight /></el-icon>
          开始练习
        </el-button>
        <el-button @click="createVisible = true">新增错题</el-button>
      </div>
    </header>

    <section class="surface-card student-wrongbook-hero">
      <div class="student-wrongbook-hero__copy">
        <span>智能错题本</span>
        <h2>错题小练习</h2>
        <p>默认按 80% 活跃错题和 20% 风险正确题生成练习，连续做对两次后会移出活跃错题本。</p>
      </div>
      <div class="student-wrongbook-hero__stats">
        <article v-for="item in wrongBookPracticeStats" :key="item.label" class="student-wrongbook-stat">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.hint }}</small>
        </article>
      </div>
      <div class="student-wrongbook-hero__actions">
        <el-button @click="openPracticeHistory">最近练习</el-button>
        <el-button type="primary" :loading="store.loading.wrongBookPractice" @click="startPractice">开始错题小练习</el-button>
      </div>
    </section>

    <section class="surface-card section-card">
      <div class="student-filter-strip">
        <div class="student-filter-strip__group">
          <button
            type="button"
            :class="['student-filter-chip', { 'student-filter-chip--active': selectedStatus === 'all' }]"
            @click="selectedStatus = 'all'; refreshList()"
          >
            全部状态
          </button>
          <button
            type="button"
            :class="['student-filter-chip', { 'student-filter-chip--active': selectedStatus === 'pending_fix' }]"
            @click="selectedStatus = 'pending_fix'; refreshList()"
          >
            待订正
          </button>
          <button
            type="button"
            :class="['student-filter-chip', { 'student-filter-chip--active': selectedStatus === 'fixed' }]"
            @click="selectedStatus = 'fixed'; refreshList()"
          >
            已订正
          </button>
          <button
            type="button"
            :class="['student-filter-chip', { 'student-filter-chip--active': selectedStatus === 'mastered' }]"
            @click="selectedStatus = 'mastered'; refreshList()"
          >
            已掌握
          </button>
        </div>

        <div class="student-filter-strip__group">
          <button
            type="button"
            :class="['student-filter-chip', { 'student-filter-chip--active': selectedPool === 'all' }]"
            @click="selectedPool = 'all'"
          >
            全部题池
          </button>
          <button
            type="button"
            :class="['student-filter-chip', { 'student-filter-chip--active': selectedPool === 'active_wrong' }]"
            @click="selectedPool = 'active_wrong'"
          >
            活跃错题
          </button>
          <button
            type="button"
            :class="['student-filter-chip', { 'student-filter-chip--active': selectedPool === 'risky_correct' }]"
            @click="selectedPool = 'risky_correct'"
          >
            风险正确
          </button>
          <button
            type="button"
            :class="['student-filter-chip', { 'student-filter-chip--active': selectedPool === 'mastered_archive' }]"
            @click="selectedPool = 'mastered_archive'"
          >
            已掌握归档
          </button>
        </div>

        <div class="student-filter-strip__group">
          <button
            type="button"
            :class="['student-filter-chip', { 'student-filter-chip--active': selectedSubject === 'all' }]"
            @click="selectedSubject = 'all'; refreshList()"
          >
            全部科目
          </button>
          <button
            v-for="item in store.wrongBookSubjects"
            :key="item.subjectCode"
            type="button"
            :class="['student-filter-chip', { 'student-filter-chip--active': selectedSubject === item.subjectCode }]"
            @click="selectedSubject = item.subjectCode; refreshList()"
          >
            {{ item.subjectName }}
          </button>
        </div>
      </div>

      <div v-if="displayWrongBooks.length" class="student-wrongbook-grid">
        <article
          v-for="item in displayWrongBooks"
          :key="item.id"
          class="surface-card student-wrongbook-card"
          @click="openDetail(item.id)"
        >
          <div class="student-wrongbook-card__top">
            <span class="soft-chip">{{ item.subjectName }}</span>
            <StatusTag kind="wrong-book" :value="item.status" />
          </div>
          <span class="student-pool-pill">{{ getStudentWrongBookPoolLabel(store.resolveWrongBookPoolType(item)) }}</span>
          <h3>{{ item.questionNo ? `第 ${item.questionNo} 题 · ` : '' }}{{ item.questionText }}</h3>
          <p>{{ item.analysisText || item.wrongReasonLabel || '点击查看题目详情和订正内容。' }}</p>
          <div class="student-wrongbook-card__meta">
            <span>{{ item.teacherName || '我的错题整理' }}</span>
            <span>{{ formatStudentFullDateTime(item.createdAt) }}</span>
          </div>
          <div class="student-wrongbook-practice-meta">
            <span>连续正确 {{ item.correctStreak ?? 0 }} 次</span>
            <span>练习 {{ item.practiceCount ?? 0 }} 次</span>
          </div>
        </article>
      </div>

      <div v-else class="empty-state">当前筛选下还没有错题记录，可以从作业详情或反馈页继续加入。</div>
    </section>

    <el-drawer
      v-model="detailVisible"
      size="520px"
      :destroy-on-close="false"
      :with-header="false"
      append-to-body
    >
      <div v-if="currentWrongBook" class="page-stack student-wrongbook-drawer">
        <div>
          <div class="chip-row">
            <span class="soft-chip">{{ currentWrongBook.subjectName }}</span>
            <StatusTag kind="wrong-book" :value="currentWrongBook.status" />
            <span class="soft-chip">{{ getStudentWrongBookPoolLabel(store.resolveWrongBookPoolType(currentWrongBook)) }}</span>
          </div>
          <h3>{{ currentWrongBook.questionText }}</h3>
          <p class="section-subtitle">{{ formatStudentFullDateTime(currentWrongBook.createdAt) }}</p>
        </div>

        <section class="surface-card section-card">
          <h3>练习状态</h3>
          <div class="student-practice-summary">
            <div>
              <span>连续正确</span>
              <strong>{{ currentWrongBook.correctStreak ?? 0 }}</strong>
            </div>
            <div>
              <span>练习次数</span>
              <strong>{{ currentWrongBook.practiceCount ?? 0 }}</strong>
            </div>
            <div>
              <span>最近练习</span>
              <strong>{{ formatStudentFullDateTime(currentWrongBook.lastPracticedAt) }}</strong>
            </div>
          </div>
        </section>

        <section class="surface-card section-card">
          <h3>错题信息</h3>
          <p class="section-subtitle">保留原错答、正确答案和老师给出的解释，方便你对照订正。</p>
          <div class="panel-list">
            <div class="panel-list-item">
              <strong>我的答案</strong>
              <p>{{ currentWrongBook.studentAnswer || '未记录' }}</p>
            </div>
            <div class="panel-list-item">
              <strong>正确答案</strong>
              <p>{{ currentWrongBook.correctAnswer || '待补充' }}</p>
            </div>
            <div class="panel-list-item">
              <strong>解析与提醒</strong>
              <p>{{ currentWrongBook.analysisText || '暂未补充解析。' }}</p>
            </div>
          </div>
        </section>

        <section class="surface-card section-card" v-if="currentWrongBook.status !== 'mastered'">
          <h3>提交订正</h3>
          <p class="section-subtitle">完成重做后，把新的思路和订正截图一起提交。</p>
          <el-input
            v-model="fixForm.fixedText"
            type="textarea"
            :rows="5"
            resize="vertical"
            placeholder="写下这次重新理解后的解题方法或订正说明。"
          />
          <el-upload
            v-model:file-list="fixForm.files"
            drag
            multiple
            :auto-upload="false"
            :limit="4"
            accept="image/*"
            class="student-wrongbook-upload"
          >
            <div class="student-upload-copy">
              <strong>上传订正图片</strong>
              <span>支持手写订正、步骤草稿或解析截图。</span>
            </div>
          </el-upload>
          <div class="student-wrongbook-actions">
            <el-button @click="markMastered">我已经掌握</el-button>
            <el-button type="primary" @click="submitFix">提交订正</el-button>
          </div>
        </section>
      </div>
    </el-drawer>

    <el-dialog v-model="createVisible" title="新增错题" width="680px" @closed="resetCreateForm">
      <div class="page-stack">
        <div class="split-grid">
          <el-form-item label="科目">
            <el-select
              v-model="createForm.subjectCode"
              placeholder="请选择科目"
              @change="
                createForm.subjectName =
                  store.wrongBookSubjects.find((item) => item.subjectCode === createForm.subjectCode)?.subjectName || ''
              "
            >
              <el-option
                v-for="item in store.wrongBookSubjects"
                :key="item.subjectCode"
                :label="item.subjectName"
                :value="item.subjectCode"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="题号">
            <el-input v-model="createForm.questionNo" placeholder="如：1、2-1" />
          </el-form-item>
        </div>

        <el-form-item label="题目内容">
          <el-input v-model="createForm.questionText" type="textarea" :rows="4" resize="vertical" />
        </el-form-item>

        <div class="split-grid">
          <el-form-item label="我的答案">
            <el-input v-model="createForm.studentAnswer" type="textarea" :rows="4" resize="vertical" />
          </el-form-item>
          <el-form-item label="正确答案">
            <el-input v-model="createForm.correctAnswer" type="textarea" :rows="4" resize="vertical" />
          </el-form-item>
        </div>

        <div class="split-grid">
          <el-form-item label="错因标签">
            <el-select v-model="createForm.wrongReasonCode" placeholder="请选择错因">
              <el-option
                v-for="item in wrongReasonOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关联作业 ID">
            <el-input v-model="createForm.homeworkId" placeholder="可不填" />
          </el-form-item>
        </div>

        <el-form-item label="解析与提醒">
          <el-input v-model="createForm.analysisText" type="textarea" :rows="4" resize="vertical" />
        </el-form-item>

        <el-upload
          v-model:file-list="createFiles"
          drag
          multiple
          :auto-upload="false"
          :limit="4"
          accept="image/*"
          class="student-wrongbook-upload"
        >
          <div class="student-upload-copy">
            <strong>上传题目截图</strong>
            <span>如果这道题来自纸面作业，可以上传照片方便后续回看。</span>
          </div>
        </el-upload>
      </div>

      <template #footer>
        <div class="student-dialog-actions">
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreate">加入错题本</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.student-wrongbook-header-actions,
.student-wrongbook-hero,
.student-wrongbook-hero__stats,
.student-wrongbook-hero__actions,
.student-wrongbook-practice-meta,
.student-practice-summary {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.student-wrongbook-header-actions {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.student-wrongbook-hero {
  justify-content: space-between;
  padding: 1.25rem;
  border-radius: 28px;
  background:
    radial-gradient(circle at right top, rgba(34, 197, 94, 0.12), transparent 30%),
    linear-gradient(135deg, #ffffff, #f6fbff);
}

.student-wrongbook-hero__copy {
  min-width: 220px;
}

.student-wrongbook-hero__copy span {
  color: #2563eb;
  font-weight: 800;
}

.student-wrongbook-hero__copy h2 {
  margin: 0.35rem 0 0;
  color: #112640;
  font-size: 1.8rem;
}

.student-wrongbook-hero__copy p {
  margin: 0.45rem 0 0;
  max-width: 36rem;
  color: #63778d;
}

.student-wrongbook-hero__stats {
  flex: 1;
  justify-content: flex-end;
}

.student-wrongbook-stat {
  min-width: 116px;
  padding: 0.9rem;
  border: 1px solid #e0eaf5;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
}

.student-wrongbook-stat span,
.student-wrongbook-stat small {
  display: block;
  color: #6b7f95;
}

.student-wrongbook-stat strong {
  display: block;
  margin: 0.2rem 0;
  color: #112640;
  font-size: 1.7rem;
}

.student-filter-strip {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.student-filter-strip__group {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}

.student-filter-chip {
  border: 0;
  padding: 0.55rem 1rem;
  border-radius: 999px;
  background: rgba(241, 245, 249, 0.95);
  color: #5f7488;
  font-weight: 700;
  cursor: pointer;
}

.student-filter-chip--active {
  color: white;
  background: linear-gradient(135deg, #2563eb, #22c55e);
}

.student-wrongbook-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.student-wrongbook-card {
  padding: 1.2rem;
  border-radius: 26px;
  cursor: pointer;
}

.student-wrongbook-card__top,
.student-wrongbook-card__meta,
.student-wrongbook-actions,
.student-dialog-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.8rem;
}

.student-wrongbook-card h3 {
  margin: 0.95rem 0 0;
  font-size: 1.2rem;
  color: #14263d;
}

.student-wrongbook-card p {
  margin: 0.7rem 0 0;
  color: #617589;
  line-height: 1.7;
}

.student-pool-pill {
  display: inline-flex;
  width: fit-content;
  margin-top: 0.85rem;
  padding: 0.36rem 0.72rem;
  border-radius: 999px;
  color: #0f766e;
  background: #e6f8ef;
  font-size: 0.82rem;
  font-weight: 800;
}

.student-wrongbook-card__meta {
  margin-top: 1rem;
  color: #6d8196;
  font-size: 0.88rem;
}

.student-wrongbook-practice-meta {
  justify-content: flex-start;
  flex-wrap: wrap;
  margin-top: 0.8rem;
  color: #6a7e94;
  font-size: 0.86rem;
}

.student-practice-summary {
  align-items: stretch;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.student-practice-summary div {
  padding: 0.9rem;
  border: 1px solid #e2ebf5;
  border-radius: 16px;
  background: #fff;
}

.student-practice-summary span,
.student-practice-summary strong {
  display: block;
}

.student-practice-summary span {
  color: #6b7f95;
  font-size: 0.86rem;
}

.student-practice-summary strong {
  margin-top: 0.25rem;
  color: #13253d;
  font-size: 1.15rem;
}

.student-wrongbook-drawer h3 {
  margin: 0.9rem 0 0;
  color: #13253d;
}

.student-wrongbook-upload {
  margin-top: 0.5rem;
}

.student-upload-copy {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  color: #607589;
}

.student-wrongbook-actions {
  justify-content: flex-end;
}

@media (max-width: 1080px) {
  .student-wrongbook-hero {
    align-items: stretch;
    flex-direction: column;
  }

  .student-wrongbook-hero__stats {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .student-wrongbook-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .student-wrongbook-hero__stats,
  .student-wrongbook-hero__actions {
    align-items: stretch;
    flex-direction: column;
  }

  .student-practice-summary {
    grid-template-columns: 1fr;
  }
}
</style>
