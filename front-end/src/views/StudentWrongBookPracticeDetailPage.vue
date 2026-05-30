<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, RefreshRight } from '@element-plus/icons-vue'
import { useStudentPortalStore } from '@/stores/studentPortal'
import {
  formatStudentFullDateTime,
  getStudentWrongBookPoolLabel,
  getStudentWrongBookPracticeResultLabel
} from '@/utils/student-portal-view'

const route = useRoute()
const router = useRouter()
const store = useStudentPortalStore()

const practiceId = computed(() => `${route.params.practiceId ?? ''}`)
const detail = computed(() => (practiceId.value ? store.getWrongBookPracticeDetail(practiceId.value) : null))
const accuracyPercent = computed(() => {
  const value = detail.value?.accuracyRate ?? 0
  return Math.round((value > 1 ? value / 100 : value) * 100)
})

async function loadDetail() {
  if (!practiceId.value) {
    return
  }

  try {
    await store.loadWrongBookPracticeDetail(practiceId.value)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '练习详情加载失败，请稍后重试。')
  }
}

async function startAgain() {
  try {
    await store.createWrongBookPracticePlan('all', 10)
    void router.push('/student/wrong-book/practice')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '练习生成失败，请稍后重试。')
  }
}

watch(
  () => practiceId.value,
  () => {
    void loadDetail()
  }
)

onMounted(() => {
  void loadDetail()
})
</script>

<template>
  <div class="page-stack smart-practice-detail-page">
    <header class="surface-card smart-practice-detail-header">
      <div>
        <button
          type="button"
          class="smart-practice-back"
          title="返回练习记录"
          @click="router.push('/student/wrong-book/practice/history')"
        >
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <div>
          <span>练习详情</span>
          <h2>{{ detail?.practiceName || '错题小练习' }}</h2>
          <p>{{ formatStudentFullDateTime(detail?.submittedAt || detail?.generatedAt) }}</p>
        </div>
      </div>
      <el-button type="primary" :loading="store.loading.wrongBookPractice" @click="startAgain">
        <el-icon><RefreshRight /></el-icon>
        再练一次
      </el-button>
    </header>

    <template v-if="detail">
      <section class="smart-practice-detail-metrics">
        <article class="surface-card smart-practice-detail-metric">
          <span>正确率</span>
          <strong>{{ accuracyPercent }}%</strong>
        </article>
        <article class="surface-card smart-practice-detail-metric">
          <span>答对</span>
          <strong>{{ detail.correctCount }}</strong>
        </article>
        <article class="surface-card smart-practice-detail-metric">
          <span>答错</span>
          <strong>{{ detail.wrongCount }}</strong>
        </article>
        <article class="surface-card smart-practice-detail-metric">
          <span>题目数</span>
          <strong>{{ detail.questionCount }}</strong>
        </article>
      </section>

      <section class="surface-card section-card">
        <div class="smart-practice-detail-section-head">
          <div>
            <h3>逐题回看</h3>
            <p class="section-subtitle">复盘每道题的答案、结果和当时来源题池。</p>
          </div>
        </div>

        <div class="smart-practice-detail-list">
          <article
            v-for="(item, index) in detail.items"
            :key="item.practiceItemId"
            :class="['smart-practice-detail-item', `smart-practice-detail-item--${item.resultStatus || 'unanswered'}`]"
          >
            <div class="smart-practice-detail-index">{{ index + 1 }}</div>
            <div class="smart-practice-detail-body">
              <div class="smart-practice-detail-item__top">
                <span>{{ item.subjectName || item.subjectCode || '错题复练' }}</span>
                <span>{{ getStudentWrongBookPoolLabel(item.itemSourceType) }}</span>
                <strong>{{ getStudentWrongBookPracticeResultLabel(item.resultStatus) }}</strong>
              </div>
              <h4>{{ item.questionNo ? `原第 ${item.questionNo} 题 · ` : '' }}{{ item.questionText }}</h4>
              <div class="smart-practice-answer-grid">
                <div>
                  <span>我的答案</span>
                  <p>{{ item.studentAnswer || '未作答' }}</p>
                </div>
                <div>
                  <span>参考答案</span>
                  <p>{{ item.correctAnswer || '未同步' }}</p>
                </div>
              </div>
            </div>
          </article>
        </div>
      </section>
    </template>

    <section v-else class="surface-card smart-practice-detail-empty" v-loading="store.loading.wrongBookPracticeDetail">
      <h3>正在读取练习详情</h3>
      <p>如果长时间没有加载，请返回练习记录后重试。</p>
    </section>
  </div>
</template>

<style scoped>
.smart-practice-detail-header,
.smart-practice-detail-header > div,
.smart-practice-detail-item__top {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.smart-practice-detail-header {
  justify-content: space-between;
  padding: 1.1rem 1.2rem;
  border-radius: 26px;
}

.smart-practice-back {
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

.smart-practice-detail-header span {
  color: #2563eb;
  font-weight: 800;
}

.smart-practice-detail-header h2 {
  margin: 0.2rem 0 0;
  color: #112640;
}

.smart-practice-detail-header p {
  margin: 0.25rem 0 0;
  color: #6a7d92;
}

.smart-practice-detail-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1rem;
}

.smart-practice-detail-metric {
  padding: 1rem;
  border-radius: 22px;
}

.smart-practice-detail-metric span {
  color: #6a7d92;
  font-weight: 700;
}

.smart-practice-detail-metric strong {
  display: block;
  margin-top: 0.35rem;
  color: #112640;
  font-size: 2rem;
}

.smart-practice-detail-section-head {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
}

.smart-practice-detail-list {
  display: grid;
  gap: 0.85rem;
}

.smart-practice-detail-item {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 0.85rem;
  padding: 1rem;
  border: 1px solid #e2eaf4;
  border-radius: 20px;
  background: #fff;
}

.smart-practice-detail-index {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 16px;
  background: #eef6ff;
  color: #2563eb;
  font-weight: 800;
}

.smart-practice-detail-item--correct .smart-practice-detail-index {
  color: #067647;
  background: #e6f8ef;
}

.smart-practice-detail-item--wrong .smart-practice-detail-index {
  color: #b42318;
  background: #ffe8e5;
}

.smart-practice-detail-body {
  min-width: 0;
}

.smart-practice-detail-item__top {
  flex-wrap: wrap;
  color: #64778d;
  font-size: 0.88rem;
}

.smart-practice-detail-item__top strong {
  color: #132842;
}

.smart-practice-detail-item h4 {
  margin: 0.45rem 0 0.85rem;
  color: #12263f;
  line-height: 1.55;
}

.smart-practice-answer-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.8rem;
}

.smart-practice-answer-grid div {
  padding: 0.85rem;
  border-radius: 16px;
  background: #f7fbff;
  border: 1px solid #e2ebf5;
}

.smart-practice-answer-grid span {
  display: block;
  color: #6a7d92;
  font-size: 0.86rem;
  font-weight: 700;
}

.smart-practice-answer-grid p {
  margin: 0.35rem 0 0;
  color: #253b52;
  line-height: 1.65;
}

.smart-practice-detail-empty {
  padding: 2rem;
  border-radius: 26px;
  text-align: center;
}

.smart-practice-detail-empty h3 {
  margin: 0;
  color: #112640;
}

.smart-practice-detail-empty p {
  color: #6a7d92;
}

@media (max-width: 920px) {
  .smart-practice-detail-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .smart-practice-detail-metrics,
  .smart-practice-answer-grid {
    grid-template-columns: 1fr;
  }
}
</style>
