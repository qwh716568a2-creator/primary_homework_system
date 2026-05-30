<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { CollectionTag, RefreshRight } from '@element-plus/icons-vue'
import { useStudentPortalStore } from '@/stores/studentPortal'
import {
  getStudentWrongBookPoolLabel,
  getStudentWrongBookPracticeResultLabel
} from '@/utils/student-portal-view'

const router = useRouter()
const store = useStudentPortalStore()

const result = computed(() => store.wrongBookPracticeResult)
const reviewItems = computed(() => store.wrongBookPracticeReviewItems)
const accuracyPercent = computed(() => {
  const value = result.value?.accuracyRate ?? 0
  return Math.round((value > 1 ? value / 100 : value) * 100)
})

async function startAgain() {
  await store.createWrongBookPracticePlan('all', 10)
  void router.push('/student/wrong-book/practice')
}
</script>

<template>
  <div class="page-stack smart-result-page">
    <template v-if="result">
      <section class="surface-card smart-result-hero">
        <div>
          <span class="smart-result-eyebrow">练习已完成</span>
          <h2>本次正确率 {{ accuracyPercent }}%</h2>
          <p>系统已把本次练习结果回写到错题状态，连续做对两次的题会进入已掌握归档。</p>
        </div>
        <div class="smart-result-ring" :style="{ '--accuracy': accuracyPercent }">
          <strong>{{ accuracyPercent }}</strong>
          <span>%</span>
        </div>
      </section>

      <section class="smart-result-metrics">
        <article class="surface-card smart-result-metric">
          <span>答对</span>
          <strong>{{ result.correctCount }}</strong>
        </article>
        <article class="surface-card smart-result-metric">
          <span>答错</span>
          <strong>{{ result.wrongCount }}</strong>
        </article>
        <article class="surface-card smart-result-metric">
          <span>进入已掌握</span>
          <strong>{{ result.masteredCount }}</strong>
        </article>
        <article class="surface-card smart-result-metric">
          <span>回到活跃错题</span>
          <strong>{{ result.returnedToActiveCount }}</strong>
        </article>
      </section>

      <section class="surface-card section-card">
        <div class="smart-section-head">
          <div>
            <h3>逐题结果</h3>
            <p class="section-subtitle">查看每道题的作答、核对结果和题池来源。</p>
          </div>
          <div class="smart-result-actions">
            <el-button @click="router.push('/student/wrong-book/practice/history')">
              <el-icon><CollectionTag /></el-icon>
              练习记录
            </el-button>
            <el-button type="primary" :loading="store.loading.wrongBookPractice" @click="startAgain">
              <el-icon><RefreshRight /></el-icon>
              再练一次
            </el-button>
          </div>
        </div>

        <div class="smart-review-list">
          <article
            v-for="(item, index) in reviewItems"
            :key="item.practiceItemId"
            :class="['smart-review-item', `smart-review-item--${item.resultStatus}`]"
          >
            <div class="smart-review-index">{{ index + 1 }}</div>
            <div>
              <div class="smart-review-item__top">
                <span>{{ getStudentWrongBookPoolLabel(item.itemSourceType) }}</span>
                <strong>{{ getStudentWrongBookPracticeResultLabel(item.resultStatus) }}</strong>
              </div>
              <h4>{{ item.questionText || '题目内容未同步' }}</h4>
              <p>我的答案：{{ item.studentAnswer || '未作答' }}</p>
              <p>参考答案：{{ item.correctAnswer || '未同步' }}</p>
            </div>
          </article>
        </div>
      </section>
    </template>

    <section v-else class="surface-card smart-result-empty">
      <h2>还没有练习结果</h2>
      <p>先完成一次错题小练习，再回来查看正确率和题目状态迁移。</p>
      <el-button type="primary" @click="router.push('/student/wrong-book')">返回错题本</el-button>
    </section>
  </div>
</template>

<style scoped>
.smart-result-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.2rem;
  padding: 1.4rem;
  border-radius: 28px;
  background:
    radial-gradient(circle at right top, rgba(34, 197, 94, 0.12), transparent 30%),
    linear-gradient(135deg, #ffffff, #f6fbff);
}

.smart-result-eyebrow {
  color: #0f8a69;
  font-weight: 800;
}

.smart-result-hero h2 {
  margin: 0.45rem 0 0;
  color: #102540;
  font-size: 2rem;
}

.smart-result-hero p {
  margin: 0.5rem 0 0;
  color: #64778d;
}

.smart-result-ring {
  display: flex;
  align-items: baseline;
  justify-content: center;
  width: 128px;
  height: 128px;
  flex: 0 0 auto;
  border-radius: 50%;
  color: #0f766e;
  background:
    radial-gradient(circle, #fff 56%, transparent 58%),
    conic-gradient(#14b8a6 calc(var(--accuracy, 100) * 1%), #e4eef7 0);
  box-shadow: 0 18px 36px rgba(20, 184, 166, 0.14);
}

.smart-result-ring strong {
  margin-top: 42px;
  font-size: 2.2rem;
}

.smart-result-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1rem;
}

.smart-result-metric {
  padding: 1rem;
  border-radius: 22px;
}

.smart-result-metric span {
  color: #6a7d92;
  font-weight: 700;
}

.smart-result-metric strong {
  display: block;
  margin-top: 0.35rem;
  color: #112640;
  font-size: 2rem;
}

.smart-section-head,
.smart-result-actions,
.smart-review-item__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
}

.smart-review-list {
  display: grid;
  gap: 0.8rem;
}

.smart-review-item {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 0.85rem;
  padding: 1rem;
  border: 1px solid #e2eaf4;
  border-radius: 20px;
  background: #fff;
}

.smart-review-index {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 16px;
  background: #eef6ff;
  color: #2563eb;
  font-weight: 800;
}

.smart-review-item--correct .smart-review-index {
  color: #067647;
  background: #e6f8ef;
}

.smart-review-item--wrong .smart-review-index {
  color: #b42318;
  background: #ffe8e5;
}

.smart-review-item__top span,
.smart-review-item__top strong {
  color: #64778d;
  font-size: 0.88rem;
}

.smart-review-item h4 {
  margin: 0.4rem 0;
  color: #12263f;
  line-height: 1.55;
}

.smart-review-item p {
  margin: 0.25rem 0 0;
  color: #65788e;
}

.smart-result-empty {
  padding: 2rem;
  border-radius: 28px;
  text-align: center;
}

.smart-result-empty h2 {
  margin: 0;
  color: #102540;
}

.smart-result-empty p {
  color: #65788e;
}

@media (max-width: 920px) {
  .smart-result-hero,
  .smart-section-head {
    align-items: stretch;
    flex-direction: column;
  }

  .smart-result-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .smart-result-metrics,
  .smart-result-actions {
    grid-template-columns: 1fr;
  }

  .smart-result-actions {
    display: grid;
  }
}
</style>
