<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import MetricCard from '@/components/MetricCard.vue'
import ParentHomeworkCard from '@/components/ParentHomeworkCard.vue'
import { useParentPortalStore } from '@/stores/parentPortal'

const router = useRouter()
const store = useParentPortalStore()
const activeStatus = ref<'all' | 'pending' | 'submitted' | 'revision' | 'completed'>('all')
const activeSubject = ref('all')

const statusOptions = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '待完成' },
  { key: 'submitted', label: '已提交' },
  { key: 'revision', label: '待订正' },
  { key: 'completed', label: '已完成' }
] as const

const subjectOptions = computed(() => [
  { key: 'all', label: '全部科目' },
  ...Array.from(new Set(store.currentChildHomeworks.map((item) => item.subject).filter(Boolean))).map((subject) => ({
    key: subject,
    label: subject
  }))
])

const filteredHomeworks = computed(() => {
  let list = store.currentChildHomeworks

  if (activeStatus.value !== 'all') {
    list = list.filter((item) => item.status === activeStatus.value)
  }

  if (activeSubject.value !== 'all') {
    list = list.filter((item) => item.subject === activeSubject.value)
  }

  return list
})

const priorityHomeworks = computed(() =>
  store.currentChildHomeworks
    .filter((item) => item.status === 'pending' || item.status === 'revision' || item.status === 'overdue')
    .slice(0, 3)
)

const latestMessages = computed(() => store.messages.slice(0, 4))

watch(
  () => store.currentChild?.id,
  () => {
    activeStatus.value = 'all'
    activeSubject.value = 'all'
  }
)

async function loadPage() {
  try {
    await store.initializeWorkspace()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '家长首页加载失败，请稍后重试。')
  }
}

async function selectChild(childId: string) {
  store.selectChild(childId)
  try {
    await store.loadHomeworks(childId)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '切换孩子失败，请稍后重试。')
  }
}

function openHomework(homeworkId: string) {
  if (!store.currentChild) return
  void router.push(`/parent/homeworks/${store.currentChild.id}/${homeworkId}`)
}

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div class="page-stack parent-home">
    <section class="parent-home__hero surface-card">
      <div>
        <span class="parent-pill">家长首页</span>
        <h2>{{ store.currentChild?.name || '孩子' }}的今日跟进</h2>
      </div>
      <div class="parent-home__children">
        <button
          v-for="child in store.children"
          :key="child.id"
          type="button"
          :class="['child-switch', { 'child-switch--active': child.id === store.currentChild?.id }]"
          @click="selectChild(child.id)"
        >
          <strong>{{ child.name }}</strong>
          <span>{{ child.gradeName }} · {{ child.className }}</span>
        </button>
      </div>
    </section>

    <section class="grid-cards" v-loading="store.loading.children || store.loading.homeworks || store.loading.messages">
      <MetricCard
        v-for="card in store.statusOverview"
        :key="card.label"
        :label="card.label"
        :value="card.value"
        :hint="card.hint"
        :tone="card.tone"
      />
    </section>

    <section class="parent-focus-grid">
      <article class="surface-card section-card">
        <div class="dashboard-section-head">
          <div>
            <span class="parent-pill">最新作业</span>
            <h3>优先处理</h3>
          </div>
          <el-button text @click="activeStatus = 'all'">查看全部</el-button>
        </div>

        <div v-if="priorityHomeworks.length" class="parent-priority-list">
          <ParentHomeworkCard
            v-for="item in priorityHomeworks"
            :key="item.id"
            :item="item"
            :child-name="store.currentChild?.name"
            @open="openHomework"
          />
        </div>
        <div v-else class="empty-state">当前没有需要家长优先跟进的作业。</div>
      </article>

      <article class="surface-card section-card">
        <div class="dashboard-section-head">
          <div>
            <span class="parent-pill parent-pill--green">最新消息</span>
            <h3>老师反馈与提醒</h3>
          </div>
          <el-button text @click="router.push('/parent/messages')">消息中心</el-button>
        </div>

        <div v-if="latestMessages.length" class="parent-message-list">
          <button
            v-for="item in latestMessages"
            :key="item.id"
            type="button"
            class="parent-message-row"
            @click="router.push('/parent/messages')"
          >
            <span class="parent-message-dot" :class="{ 'parent-message-dot--unread': item.unread }" />
            <div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.content }}</p>
            </div>
            <time>{{ item.time }}</time>
          </button>
        </div>
        <div v-else class="empty-state">暂无老师消息。</div>
      </article>
    </section>

    <section class="surface-card section-card">
      <div class="dashboard-section-head">
        <h3>作业清单</h3>
      </div>

      <div class="parent-filter-strip">
        <button
          v-for="option in statusOptions"
          :key="option.key"
          type="button"
          :class="['parent-filter-chip', { 'parent-filter-chip--active': activeStatus === option.key }]"
          @click="activeStatus = option.key"
        >
          {{ option.label }}
        </button>
      </div>
      <div class="parent-filter-strip">
        <button
          v-for="option in subjectOptions"
          :key="option.key"
          type="button"
          :class="['parent-filter-chip', { 'parent-filter-chip--active': activeSubject === option.key }]"
          @click="activeSubject = option.key"
        >
          {{ option.label }}
        </button>
      </div>

      <div v-if="filteredHomeworks.length" class="parent-homework-grid">
        <ParentHomeworkCard
          v-for="item in filteredHomeworks"
          :key="item.id"
          :item="item"
          :child-name="store.currentChild?.name"
          @open="openHomework"
        />
      </div>
      <div v-else class="empty-state">当前筛选下暂无作业。</div>
    </section>
  </div>
</template>

<style scoped>
.parent-home__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 24px 28px;
  border-radius: 28px;
}

.parent-home__hero h2 {
  margin: 10px 0 0;
  color: #08213f;
  font-size: 30px;
  letter-spacing: -0.04em;
}

.parent-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 7px 12px;
  background: #e7f2ff;
  color: #1766c2;
  font-size: 13px;
  font-weight: 900;
}

.parent-pill--green {
  background: #e8fbf3;
  color: #098060;
}

.parent-home__children {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.child-switch {
  min-width: 150px;
  border: 1px solid rgba(190, 205, 224, 0.72);
  border-radius: 18px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.84);
  text-align: left;
  cursor: pointer;
}

.child-switch strong,
.child-switch span {
  display: block;
}

.child-switch span {
  margin-top: 4px;
  color: #668099;
  font-size: 13px;
}

.child-switch--active {
  border-color: transparent;
  color: #fff;
  background: linear-gradient(135deg, #2f7cff, #20bfa6);
}

.child-switch--active span {
  color: rgba(255, 255, 255, 0.78);
}

.parent-focus-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(360px, 0.9fr);
  gap: 18px;
}

.parent-priority-list,
.parent-homework-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.parent-priority-list {
  grid-template-columns: 1fr;
}

.parent-message-list {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.parent-message-row {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  border: 1px solid rgba(201, 214, 231, 0.74);
  border-radius: 18px;
  padding: 14px;
  background: linear-gradient(135deg, #fff, #f7fbff);
  text-align: left;
  cursor: pointer;
}

.parent-message-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #cbd5e1;
}

.parent-message-dot--unread {
  background: #ef4444;
}

.parent-message-row strong {
  color: #08213f;
}

.parent-message-row p {
  display: -webkit-box;
  margin: 5px 0 0;
  overflow: hidden;
  color: #5b7088;
  line-height: 1.55;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

.parent-message-row time {
  color: #71839a;
  font-size: 13px;
}

.parent-filter-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.parent-filter-chip {
  border: 0;
  border-radius: 999px;
  padding: 10px 16px;
  background: #eef4fb;
  color: #50667e;
  font-weight: 900;
  cursor: pointer;
}

.parent-filter-chip--active {
  color: #fff;
  background: linear-gradient(135deg, #2f7cff, #20bfa6);
}

@media (max-width: 1180px) {
  .parent-focus-grid,
  .parent-homework-grid {
    grid-template-columns: 1fr;
  }

  .parent-home__hero {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
