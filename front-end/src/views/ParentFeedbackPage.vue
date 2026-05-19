<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { useParentPortalStore } from '@/stores/parentPortal'
import { formatParentDateTime } from '@/utils/parent-portal-view'

const route = useRoute()
const router = useRouter()
const store = useParentPortalStore()

const childId = computed(() => `${route.params.childId ?? ''}`)
const homeworkId = computed(() => `${route.params.id ?? ''}`)
const child = computed(() => store.children.find((item) => item.id === childId.value) ?? null)
const homework = computed(() => store.getHomework(childId.value, homeworkId.value))

async function loadPage() {
  try {
    if (!store.children.length) {
      await store.loadChildren()
    }
    store.selectChild(childId.value)
    await store.loadHomeworkDetail(childId.value, homeworkId.value)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '反馈页面加载失败，请稍后重试。')
  }
}

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div class="page-stack">
    <section v-if="homework" class="surface-card section-card parent-feedback-page">
      <div class="parent-feedback-title">
        <div>
          <el-button text @click="router.push(`/parent/homeworks/${childId}/${homeworkId}`)">← 返回详情</el-button>
          <h2>老师反馈</h2>
          <p>{{ child?.name || '孩子' }} · {{ homework.title }}</p>
        </div>
      </div>

      <div v-if="homework.review" class="parent-feedback-stack">
        <div class="parent-feedback-result">
          <StatusTag kind="student-review" :value="homework.review.status" />
          <div>
            <span>等级</span>
            <strong>{{ homework.review.level || '已反馈' }}</strong>
          </div>
          <div>
            <span>得分</span>
            <strong>{{ homework.review.score ? `${homework.review.score} 分` : '暂无' }}</strong>
          </div>
          <div>
            <span>时间</span>
            <strong>{{ formatParentDateTime(homework.review.reviewedAt) }}</strong>
          </div>
        </div>

        <article class="feedback-comment">
          <h3>评语</h3>
          <p>{{ homework.review.comment || '老师暂未填写详细评语。' }}</p>
        </article>

        <article class="feedback-comment feedback-comment--action">
          <h3>下一步</h3>
          <p v-if="homework.review.status === 'revision_required'">这份作业需要继续订正，可以返回详情页协助孩子补充提交。</p>
          <p v-else>这份作业已完成反馈，可提醒孩子回看评语并整理错题。</p>
        </article>
      </div>

      <div v-else class="empty-state">暂无老师反馈。</div>
    </section>

    <div v-else class="empty-state">当前没有找到这份作业。</div>
  </div>
</template>

<style scoped>
.parent-feedback-page {
  max-width: 980px;
}

.parent-feedback-title h2 {
  margin: 12px 0 0;
  color: #08213f;
  font-size: 30px;
  letter-spacing: -0.04em;
}

.parent-feedback-title p {
  margin: 8px 0 0;
  color: #60758a;
}

.parent-feedback-stack {
  display: grid;
  gap: 16px;
  margin-top: 22px;
}

.parent-feedback-result {
  display: grid;
  grid-template-columns: auto repeat(3, minmax(0, 1fr));
  gap: 14px;
  align-items: center;
  padding: 18px;
  border-radius: 24px;
  background: linear-gradient(135deg, #f4fbff, #fff7fb);
}

.parent-feedback-result span {
  display: block;
  color: #718599;
  font-size: 13px;
}

.parent-feedback-result strong {
  display: block;
  margin-top: 4px;
  color: #08213f;
}

.feedback-comment {
  padding: 18px;
  border: 1px solid rgba(190, 205, 224, 0.72);
  border-radius: 22px;
  background: #fff;
}

.feedback-comment--action {
  background: linear-gradient(135deg, #eff6ff, #f0fdfa);
}

.feedback-comment h3 {
  margin: 0;
  color: #08213f;
}

.feedback-comment p {
  margin: 10px 0 0;
  color: #536b86;
  line-height: 1.8;
}

@media (max-width: 760px) {
  .parent-feedback-result {
    grid-template-columns: 1fr;
  }
}
</style>
