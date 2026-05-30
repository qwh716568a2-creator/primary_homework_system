<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, CollectionTag, RefreshRight, View } from '@element-plus/icons-vue'
import { useStudentPortalStore } from '@/stores/studentPortal'
import type { StudentWrongBookPracticeHistoryRecord } from '@/types/student-portal'
import {
  formatStudentFullDateTime,
  getStudentWrongBookPoolLabel,
  getStudentWrongBookPracticeResultLabel
} from '@/utils/student-portal-view'

const router = useRouter()
const store = useStudentPortalStore()
const detailVisible = ref(false)
const activePracticeId = ref('')

const historyItems = computed(() => store.wrongBookPracticeHistory)
const activeDetail = computed(() => store.wrongBookPracticeDetailMap[activePracticeId.value] ?? null)

function getPracticeId(item: StudentWrongBookPracticeHistoryRecord) {
  return `${item.practiceId ?? item.id ?? ''}`
}

function accuracyPercent(value?: number) {
  const rate = value ?? 0
  return Math.round((rate > 1 ? rate / 100 : rate) * 100)
}

function statusLabel(status?: string) {
  const statusMap: Record<string, string> = {
    generated: '已生成',
    in_progress: '练习中',
    completed: '已完成',
    abandoned: '已放弃'
  }
  return status ? statusMap[status] ?? status : '未同步'
}

async function loadHistory() {
  try {
    await store.loadWrongBookPracticeHistory()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '练习记录加载失败，请稍后重试。')
  }
}

async function openDetail(item: StudentWrongBookPracticeHistoryRecord) {
  const practiceId = getPracticeId(item)
  if (!practiceId) {
    ElMessage.warning('这条练习记录缺少编号，暂时无法查看详情。')
    return
  }

  activePracticeId.value = practiceId
  detailVisible.value = true

  if (store.wrongBookPracticeDetailMap[practiceId]) {
    return
  }

  try {
    await store.loadWrongBookPracticeDetail(practiceId)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '练习详情加载失败，请稍后重试。')
  }
}

async function startPractice() {
  try {
    await store.createWrongBookPracticePlan('all', 10)
    void router.push('/student/wrong-book/practice')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '练习生成失败，请稍后重试。')
  }
}

function openDetailPage() {
  if (!activePracticeId.value) {
    return
  }

  detailVisible.value = false
  void router.push(`/student/wrong-book/practice/${activePracticeId.value}`)
}

onMounted(() => {
  void loadHistory()
})
</script>

<template>
  <div class="page-stack smart-history-page">
    <header class="surface-card smart-history-header">
      <div class="smart-history-title">
        <button class="smart-icon-button" type="button" title="返回错题本" @click="router.push('/student/wrong-book')">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <div>
          <span>错题小练习</span>
          <h2>最近练习记录</h2>
        </div>
      </div>

      <el-button type="primary" :loading="store.loading.wrongBookPractice" @click="startPractice">
        <el-icon><RefreshRight /></el-icon>
        开始练习
      </el-button>
    </header>

    <section v-if="historyItems.length" class="smart-history-list" v-loading="store.loading.wrongBookPracticeHistory">
      <article
        v-for="item in historyItems"
        :key="getPracticeId(item)"
        class="surface-card smart-history-card"
      >
        <button type="button" class="smart-history-card__button" @click="openDetail(item)">
          <div class="smart-history-card__main">
            <div>
              <strong>{{ item.practiceName || '错题小练习' }}</strong>
              <span>{{ formatStudentFullDateTime(item.submittedAt || item.generatedAt || item.updatedAt || item.createdAt) }}</span>
            </div>
            <span class="smart-history-status">{{ statusLabel(item.status) }}</span>
          </div>

          <div class="smart-history-metrics">
            <div>
              <span>题数</span>
              <strong>{{ item.questionCount }}</strong>
            </div>
            <div>
              <span>答对</span>
              <strong>{{ item.correctCount }}</strong>
            </div>
            <div>
              <span>答错</span>
              <strong>{{ item.wrongCount }}</strong>
            </div>
            <div>
              <span>正确率</span>
              <strong>{{ accuracyPercent(item.accuracyRate) }}%</strong>
            </div>
          </div>
        </button>
      </article>
    </section>

    <section v-else class="surface-card smart-history-empty" v-loading="store.loading.wrongBookPracticeHistory">
      <el-icon><CollectionTag /></el-icon>
      <h3>还没有练习记录</h3>
      <p>完成第一次错题小练习后，这里会沉淀正确率和题目迁移结果。</p>
      <el-button type="primary" :loading="store.loading.wrongBookPractice" @click="startPractice">开始第一套练习</el-button>
    </section>

    <el-drawer v-model="detailVisible" title="练习详情" size="560px">
      <div v-if="activeDetail" class="smart-detail-panel">
        <div class="smart-detail-toolbar">
          <el-button type="primary" text @click="openDetailPage">打开详情页</el-button>
        </div>

        <section class="smart-detail-summary">
          <div>
            <span>练习名称</span>
            <strong>{{ activeDetail.practiceName || '错题小练习' }}</strong>
          </div>
          <div>
            <span>正确率</span>
            <strong>{{ accuracyPercent(activeDetail.accuracyRate) }}%</strong>
          </div>
          <div>
            <span>完成时间</span>
            <strong>{{ formatStudentFullDateTime(activeDetail.submittedAt || activeDetail.generatedAt || activeDetail.updatedAt) }}</strong>
          </div>
        </section>

        <div class="smart-detail-list">
          <article
            v-for="(item, index) in activeDetail.items"
            :key="item.practiceItemId"
            :class="['smart-detail-item', `smart-detail-item--${item.resultStatus}`]"
          >
            <div class="smart-detail-item__index">{{ index + 1 }}</div>
            <div class="smart-detail-item__body">
              <div class="smart-detail-item__meta">
                <span>{{ item.subjectName || item.subjectCode || '错题' }}</span>
                <span>{{ getStudentWrongBookPoolLabel(item.itemSourceType) }}</span>
                <strong>{{ getStudentWrongBookPracticeResultLabel(item.resultStatus) }}</strong>
              </div>
              <h4>{{ item.questionText || '题目内容未同步' }}</h4>
              <p>我的答案：{{ item.studentAnswer || '未作答' }}</p>
              <p>参考答案：{{ item.correctAnswer || '未同步' }}</p>
            </div>
          </article>
        </div>
      </div>

      <div v-else class="smart-detail-loading" v-loading="store.loading.wrongBookPracticeDetail">正在加载练习详情...</div>
    </el-drawer>
  </div>
</template>

<style scoped>
.smart-history-header,
.smart-history-title,
.smart-history-card__main,
.smart-history-metrics {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.smart-history-header {
  padding: 1.2rem;
  border-radius: 26px;
}

.smart-history-title {
  justify-content: flex-start;
}

.smart-history-header span,
.smart-history-empty > .el-icon {
  color: #2563eb;
  font-weight: 800;
}

.smart-history-header h2 {
  margin: 0.35rem 0 0;
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

.smart-history-list {
  display: grid;
  gap: 0.9rem;
}

.smart-history-card {
  overflow: hidden;
  border-radius: 22px;
}

.smart-history-card__button {
  display: block;
  width: 100%;
  padding: 1rem;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.smart-history-card__button:hover {
  background: #f8fbff;
}

.smart-history-card__main strong,
.smart-history-card__main span {
  display: block;
}

.smart-history-card__main strong {
  color: #12263f;
  font-size: 1.08rem;
}

.smart-history-card__main span {
  margin-top: 0.25rem;
  color: #6c7f95;
}

.smart-history-status {
  padding: 0.42rem 0.78rem;
  border-radius: 999px;
  color: #067647;
  background: #e6f8ef;
  font-weight: 800;
  white-space: nowrap;
}

.smart-history-metrics {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e5edf6;
}

.smart-history-metrics div {
  min-width: 0;
}

.smart-history-metrics span {
  display: block;
  color: #718299;
  font-size: 0.86rem;
}

.smart-history-metrics strong {
  display: block;
  margin-top: 0.25rem;
  color: #112640;
  font-size: 1.35rem;
}

.smart-history-empty {
  padding: 2rem;
  border-radius: 26px;
  text-align: center;
}

.smart-history-empty > .el-icon {
  font-size: 2rem;
}

.smart-history-empty h3 {
  margin: 0.8rem 0 0;
  color: #112640;
}

.smart-history-empty p {
  color: #6c7f95;
}

.smart-detail-panel,
.smart-detail-list {
  display: grid;
  gap: 0.9rem;
}

.smart-detail-toolbar {
  display: flex;
  justify-content: flex-end;
}

.smart-detail-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.7rem;
}

.smart-detail-summary div,
.smart-detail-item {
  border: 1px solid #e2eaf4;
  background: #fff;
}

.smart-detail-summary div {
  padding: 0.85rem;
  border-radius: 18px;
}

.smart-detail-summary span {
  display: block;
  color: #718299;
  font-size: 0.82rem;
}

.smart-detail-summary strong {
  display: block;
  margin-top: 0.3rem;
  color: #112640;
  line-height: 1.35;
}

.smart-detail-item {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 0.8rem;
  padding: 0.9rem;
  border-radius: 18px;
}

.smart-detail-item__index {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 15px;
  background: #eef6ff;
  color: #2563eb;
  font-weight: 800;
}

.smart-detail-item--correct .smart-detail-item__index {
  color: #067647;
  background: #e6f8ef;
}

.smart-detail-item--wrong .smart-detail-item__index {
  color: #b42318;
  background: #ffe8e5;
}

.smart-detail-item__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  align-items: center;
}

.smart-detail-item__meta span,
.smart-detail-item__meta strong {
  padding: 0.26rem 0.58rem;
  border-radius: 999px;
  background: #f1f6fb;
  color: #61758a;
  font-size: 0.8rem;
}

.smart-detail-item h4 {
  margin: 0.55rem 0;
  color: #12263f;
  line-height: 1.55;
}

.smart-detail-item p {
  margin: 0.25rem 0 0;
  color: #65788e;
}

.smart-detail-loading {
  min-height: 160px;
  color: #65788e;
}

@media (max-width: 720px) {
  .smart-history-header,
  .smart-history-card__main {
    align-items: flex-start;
    flex-direction: column;
  }

  .smart-history-metrics,
  .smart-detail-summary {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 520px) {
  .smart-history-metrics,
  .smart-detail-summary {
    grid-template-columns: 1fr;
  }
}
</style>
