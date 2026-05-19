<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { useStudentPortalStore } from '@/stores/studentPortal'
import { formatStudentDateTime } from '@/utils/student-portal-view'

const route = useRoute()
const router = useRouter()
const store = useStudentPortalStore()

const homeworkId = computed(() => `${route.params.id ?? ''}`)
const homework = computed(() => store.getHomework(homeworkId.value))
const reviews = computed(() => store.reviewMap[homeworkId.value] ?? [])
const latestReview = computed(() => reviews.value[0] ?? homework.value?.review ?? null)

async function loadPage() {
  try {
    await Promise.all([store.loadHomeworkDetail(homeworkId.value), store.loadReviews(homeworkId.value)])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '反馈结果加载失败，请稍后重试。')
  }
}

function goToWrongBook() {
  void router.push({
    path: '/student/wrong-book',
    query: {
      homeworkId: homeworkId.value,
      subjectName: homework.value?.subject || '',
      questionText: homework.value?.title || '',
      studentAnswer: homework.value?.latestSubmission?.text || '',
      analysisText: latestReview.value?.comment || ''
    }
  })
}

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div class="page-stack">
    <header class="page-header">
      <div>
        <h2>反馈结果</h2>
        <p>查看老师评语、得分和历次批改记录，需要时可以直接整理进错题本。</p>
      </div>
      <el-button @click="router.push(`/student/homeworks/${homeworkId}`)">返回作业详情</el-button>
    </header>

    <section v-if="homework" class="surface-card section-card student-feedback-hero">
      <span class="soft-chip">{{ homework.subject }}</span>
      <h3>{{ homework.title }}</h3>
      <div class="student-feedback-hero__status">
        <StatusTag
          v-if="latestReview?.status"
          kind="student-review"
          :value="latestReview.status"
        />
        <span class="metric-inline">{{ latestReview?.reviewedAt ? formatStudentDateTime(latestReview.reviewedAt) : '等待老师批改' }}</span>
      </div>
      <p>{{ latestReview?.comment || '老师正在批改中，完成后会第一时间更新到这里。' }}</p>

      <div class="student-feedback-result">
        <div class="surface-card student-feedback-result__item">
          <small>评级</small>
          <strong>{{ latestReview?.level || '待批改' }}</strong>
        </div>
        <div class="surface-card student-feedback-result__item">
          <small>得分</small>
          <strong>{{ latestReview?.score ? `${latestReview.score} 分` : '暂无' }}</strong>
        </div>
      </div>

      <div class="student-feedback-actions">
        <el-button @click="goToWrongBook">加入错题本</el-button>
        <el-button type="primary" @click="router.push(`/student/homeworks/${homeworkId}/submit`)">
          {{ homework.status === 'revision' ? '继续订正' : '再次提交' }}
        </el-button>
      </div>
    </section>

    <section class="surface-card section-card">
      <h3>批改记录</h3>
      <p class="section-subtitle">如果老师多次反馈，这里会保留每次批改结果，方便你对照修改。</p>

      <div v-if="reviews.length" class="panel-list">
        <div v-for="item in reviews" :key="item.id || item.reviewedAt" class="panel-list-item">
          <div class="student-feedback-row">
            <StatusTag kind="student-review" :value="item.status" />
            <span class="metric-inline">{{ formatStudentDateTime(item.reviewedAt) }}</span>
          </div>
          <p>{{ item.comment || '本次批改未填写详细评语。' }}</p>
        </div>
      </div>

      <div v-else class="empty-state">老师完成批改后，这里会出现完整反馈记录。</div>
    </section>
  </div>
</template>

<style scoped>
.student-feedback-hero h3 {
  margin: 0.9rem 0 0;
  font-size: 1.7rem;
  color: #14263d;
}

.student-feedback-hero > p {
  margin: 0.9rem 0 0;
  color: #5f7488;
  line-height: 1.8;
}

.student-feedback-hero__status,
.student-feedback-row,
.student-feedback-actions {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.student-feedback-hero__status {
  margin-top: 1rem;
}

.student-feedback-result {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.student-feedback-result__item {
  padding: 1rem;
  border-radius: 22px;
}

.student-feedback-result__item small {
  color: #6d8196;
}

.student-feedback-result__item strong {
  display: block;
  margin-top: 0.45rem;
  font-size: 1.65rem;
  color: #17304a;
}

.student-feedback-actions {
  margin-top: 1.1rem;
  flex-wrap: wrap;
}
</style>
