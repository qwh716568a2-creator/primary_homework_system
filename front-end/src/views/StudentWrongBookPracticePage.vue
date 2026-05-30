<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, Close, Timer, View } from '@element-plus/icons-vue'
import { useStudentPortalStore } from '@/stores/studentPortal'
import type {
  StudentWrongBookPracticeItem,
  StudentWrongBookPracticeResultStatus
} from '@/types/student-portal'
import {
  getStudentWrongBookPoolLabel,
  getStudentWrongBookPracticeResultLabel
} from '@/utils/student-portal-view'

const route = useRoute()
const router = useRouter()
const store = useStudentPortalStore()
const activeIndex = ref(0)
const answerDrafts = reactive<Record<string, string>>({})
const manualResults = reactive<Record<string, StudentWrongBookPracticeResultStatus | ''>>({})
const answerVisible = reactive<Record<string, boolean>>({})
const startedAt = reactive<Record<string, number>>({})

const plan = computed(() => store.wrongBookPracticePlan)
const practiceItems = computed(() =>
  [...(plan.value?.items ?? [])].sort((a, b) => Number(a.sortNo ?? 0) - Number(b.sortNo ?? 0))
)
const currentItem = computed(() => practiceItems.value[activeIndex.value] ?? null)
const completedCount = computed(
  () => practiceItems.value.filter((item) => getResolvedResult(item) !== 'unanswered').length
)
const progressPercent = computed(() =>
  practiceItems.value.length ? Math.round((completedCount.value / practiceItems.value.length) * 100) : 0
)

function itemKey(item: StudentWrongBookPracticeItem) {
  return `${item.practiceItemId}`
}

function normalizeAnswer(value?: string) {
  return `${value ?? ''}`.replace(/\s+/g, '').toLowerCase()
}

function initItemState() {
  practiceItems.value.forEach((item) => {
    const key = itemKey(item)
    answerDrafts[key] ??= ''
    manualResults[key] ??= ''
    answerVisible[key] ??= false
    startedAt[key] ??= Date.now()
  })
}

function getResolvedResult(item: StudentWrongBookPracticeItem): StudentWrongBookPracticeResultStatus {
  const key = itemKey(item)
  if (manualResults[key]) {
    return manualResults[key] as StudentWrongBookPracticeResultStatus
  }

  const answer = normalizeAnswer(answerDrafts[key])
  const correctAnswer = normalizeAnswer(item.correctAnswer)

  if (!answer) {
    return 'unanswered'
  }

  return correctAnswer && answer === correctAnswer ? 'correct' : 'wrong'
}

function setManualResult(item: StudentWrongBookPracticeItem, result: StudentWrongBookPracticeResultStatus) {
  manualResults[itemKey(item)] = result
}

function showAnswer(item: StudentWrongBookPracticeItem) {
  answerVisible[itemKey(item)] = true
}

function selectItem(index: number) {
  activeIndex.value = index
  const item = practiceItems.value[index]
  if (item) {
    startedAt[itemKey(item)] ??= Date.now()
  }
}

function moveStep(offset: number) {
  const nextIndex = activeIndex.value + offset
  if (nextIndex >= 0 && nextIndex < practiceItems.value.length) {
    selectItem(nextIndex)
  }
}

async function ensurePracticePlan() {
  if (plan.value?.items?.length) {
    initItemState()
    return
  }

  try {
    await store.createWrongBookPracticePlan(`${route.query.subjectCode ?? 'all'}`, 10)
    initItemState()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '练习生成失败，请稍后重试。')
  }
}

async function submitPractice() {
  if (!plan.value || practiceItems.value.length === 0) {
    ElMessage.warning('当前还没有可提交的练习。')
    return
  }

  const payloadItems = practiceItems.value.map((item) => {
    const key = itemKey(item)
    return {
      practiceItemId: item.practiceItemId,
      wrongBookId: item.wrongBookId,
      studentAnswer: answerDrafts[key]?.trim() ?? '',
      resultStatus: getResolvedResult(item),
      usedDurationSeconds: Math.max(1, Math.round((Date.now() - (startedAt[key] ?? Date.now())) / 1000))
    }
  })

  try {
    await store.submitWrongBookPractice({
      practiceId: plan.value.practiceId,
      items: payloadItems
    })
    ElMessage.success('练习结果已提交。')
    void router.push('/student/wrong-book/practice/result')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '练习提交失败，请稍后重试。')
  }
}

watch(
  () => plan.value?.practiceId,
  () => initItemState()
)

onMounted(() => {
  void ensurePracticePlan()
})
</script>

<template>
  <div class="page-stack smart-practice-page">
    <header class="smart-practice-header surface-card">
      <div>
        <button class="smart-icon-button" type="button" title="返回错题本" @click="router.push('/student/wrong-book')">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <div>
          <span>错题小练习</span>
          <h2>{{ plan?.practiceName || '今日错题小练习' }}</h2>
        </div>
      </div>
      <div class="smart-practice-header__actions">
        <span class="smart-progress-text">{{ completedCount }}/{{ practiceItems.length }} 已核对</span>
        <el-progress :percentage="progressPercent" :show-text="false" class="smart-progress" />
        <el-button type="primary" :loading="store.loading.wrongBookPractice" @click="submitPractice">
          提交练习
        </el-button>
      </div>
    </header>

    <section v-if="practiceItems.length" class="smart-practice-workspace">
      <aside class="surface-card smart-question-rail">
        <button
          v-for="(item, index) in practiceItems"
          :key="item.practiceItemId"
          type="button"
          :class="['smart-question-tab', `smart-question-tab--${getResolvedResult(item)}`, { 'is-active': index === activeIndex }]"
          @click="selectItem(index)"
        >
          <strong>{{ index + 1 }}</strong>
          <span>{{ getStudentWrongBookPoolLabel(item.itemSourceType) }}</span>
        </button>
      </aside>

      <article v-if="currentItem" class="surface-card smart-answer-panel">
        <div class="smart-answer-panel__top">
          <div class="chip-row">
            <span class="soft-chip">{{ currentItem.subjectName || currentItem.subjectCode || '错题复练' }}</span>
            <span class="soft-chip">{{ getStudentWrongBookPoolLabel(currentItem.itemSourceType) }}</span>
          </div>
          <span class="smart-result-pill" :class="`smart-result-pill--${getResolvedResult(currentItem)}`">
            {{ getStudentWrongBookPracticeResultLabel(getResolvedResult(currentItem)) }}
          </span>
        </div>

        <div class="smart-question-body">
          <span>第 {{ activeIndex + 1 }} 题</span>
          <h3>{{ currentItem.questionNo ? `原第 ${currentItem.questionNo} 题 · ` : '' }}{{ currentItem.questionText }}</h3>
        </div>

        <el-input
          v-model="answerDrafts[itemKey(currentItem)]"
          type="textarea"
          :rows="6"
          resize="vertical"
          placeholder="写下你的答案或关键步骤。"
        />

        <div class="smart-check-panel">
          <div>
            <strong>核对结果</strong>
            <p>先独立作答，再查看答案并选择本题结果。</p>
          </div>
          <div class="smart-check-actions">
            <el-button @click="showAnswer(currentItem)">
              <el-icon><View /></el-icon>
              看答案
            </el-button>
            <button
              type="button"
              :class="['smart-mark-button', { 'is-correct': manualResults[itemKey(currentItem)] === 'correct' }]"
              @click="setManualResult(currentItem, 'correct')"
            >
              <el-icon><Check /></el-icon>
              答对
            </button>
            <button
              type="button"
              :class="['smart-mark-button', { 'is-wrong': manualResults[itemKey(currentItem)] === 'wrong' }]"
              @click="setManualResult(currentItem, 'wrong')"
            >
              <el-icon><Close /></el-icon>
              答错
            </button>
          </div>
        </div>

        <div v-if="answerVisible[itemKey(currentItem)]" class="smart-answer-key">
          <strong>参考答案</strong>
          <p>{{ currentItem.correctAnswer || '这道题暂未同步参考答案，请按订正记录自行核对。' }}</p>
        </div>

        <footer class="smart-answer-footer">
          <span>
            <el-icon><Timer /></el-icon>
            本题会记录练习耗时
          </span>
          <div>
            <el-button :disabled="activeIndex === 0" @click="moveStep(-1)">上一题</el-button>
            <el-button :disabled="activeIndex === practiceItems.length - 1" type="primary" @click="moveStep(1)">
              下一题
            </el-button>
          </div>
        </footer>
      </article>
    </section>

    <div v-else class="empty-state">正在生成错题小练习，若长时间没有题目，请先回错题本补充错题。</div>
  </div>
</template>

<style scoped>
.smart-practice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.2rem;
  border-radius: 24px;
}

.smart-practice-header > div:first-child,
.smart-practice-header__actions,
.smart-answer-panel__top,
.smart-check-panel,
.smart-check-actions,
.smart-answer-footer {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.smart-practice-header span {
  color: #6b7f95;
  font-weight: 700;
}

.smart-practice-header h2 {
  margin: 0.2rem 0 0;
  color: #112640;
}

.smart-icon-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border: 0;
  border-radius: 14px;
  background: #edf6ff;
  color: #2563eb;
  cursor: pointer;
}

.smart-progress {
  width: 160px;
}

.smart-progress-text {
  white-space: nowrap;
}

.smart-practice-workspace {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 1rem;
}

.smart-question-rail {
  position: sticky;
  top: 1rem;
  align-self: start;
  display: grid;
  gap: 0.65rem;
  padding: 0.85rem;
  border-radius: 24px;
}

.smart-question-tab {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr);
  gap: 0.7rem;
  align-items: center;
  min-height: 58px;
  padding: 0.7rem;
  border: 1px solid #e2ebf5;
  border-radius: 18px;
  background: #fff;
  color: #536a82;
  text-align: left;
  cursor: pointer;
}

.smart-question-tab strong {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 14px;
  background: #eef6ff;
  color: #2563eb;
}

.smart-question-tab span {
  font-size: 0.88rem;
  font-weight: 700;
}

.smart-question-tab.is-active {
  border-color: rgba(37, 99, 235, 0.42);
  box-shadow: 0 14px 28px rgba(37, 99, 235, 0.1);
}

.smart-question-tab--correct strong {
  color: #067647;
  background: #e6f8ef;
}

.smart-question-tab--wrong strong {
  color: #c2410c;
  background: #fff1e7;
}

.smart-answer-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-width: 0;
  padding: 1.2rem;
  border-radius: 26px;
}

.smart-answer-panel__top,
.smart-check-panel,
.smart-answer-footer {
  justify-content: space-between;
}

.smart-question-body span {
  color: #2563eb;
  font-weight: 800;
}

.smart-question-body h3 {
  margin: 0.55rem 0 0;
  color: #12263f;
  font-size: 1.35rem;
  line-height: 1.55;
}

.smart-result-pill,
.smart-mark-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  border-radius: 999px;
  font-weight: 800;
}

.smart-result-pill {
  padding: 0.45rem 0.8rem;
  white-space: nowrap;
}

.smart-result-pill--correct {
  color: #067647;
  background: #e6f8ef;
}

.smart-result-pill--wrong {
  color: #b42318;
  background: #ffe8e5;
}

.smart-result-pill--unanswered {
  color: #5f6f82;
  background: #eef2f7;
}

.smart-check-panel,
.smart-answer-key {
  padding: 1rem;
  border-radius: 20px;
  background: #f6faff;
  border: 1px solid #dfeaf6;
}

.smart-check-panel p,
.smart-answer-key p {
  margin: 0.3rem 0 0;
  color: #62768c;
}

.smart-mark-button {
  min-height: 32px;
  padding: 0.55rem 0.9rem;
  border: 1px solid #d8e4f0;
  background: #fff;
  color: #52687f;
  cursor: pointer;
}

.smart-mark-button.is-correct {
  color: #067647;
  border-color: #9ee8c8;
  background: #e6f8ef;
}

.smart-mark-button.is-wrong {
  color: #b42318;
  border-color: #ffc6bd;
  background: #ffe8e5;
}

.smart-answer-key strong,
.smart-check-panel strong {
  color: #132842;
}

.smart-answer-footer {
  color: #6b7f95;
}

.smart-answer-footer > span {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
}

@media (max-width: 980px) {
  .smart-practice-header,
  .smart-practice-header__actions,
  .smart-check-panel,
  .smart-answer-footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .smart-practice-workspace {
    grid-template-columns: 1fr;
  }

  .smart-question-rail {
    position: static;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .smart-question-rail,
  .smart-check-actions {
    grid-template-columns: 1fr;
  }

  .smart-check-actions {
    display: grid;
    width: 100%;
  }
}
</style>
