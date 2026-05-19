<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { UploadUserFile } from 'element-plus'
import { useStudentPortalStore } from '@/stores/studentPortal'
import { uploadStudentFile } from '@/api/student'

const route = useRoute()
const router = useRouter()
const store = useStudentPortalStore()

const homeworkId = computed(() => `${route.params.id ?? ''}`)
const homework = computed(() => store.getHomework(homeworkId.value))
const text = ref('')
const fileList = ref<UploadUserFile[]>([])
const submitting = ref(false)

async function loadPage() {
  try {
    await store.loadHomeworkDetail(homeworkId.value)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '作业信息加载失败，请稍后重试。')
  }
}

async function uploadSelectedFiles() {
  const uploaded: string[] = []

  for (const item of fileList.value) {
    if (!item.raw) {
      continue
    }

    const result = await uploadStudentFile(item.raw, 'submission_image')
    uploaded.push(result.fileUrl)
  }

  return uploaded
}

async function submitHomework() {
  if (!text.value.trim() && fileList.value.length === 0) {
    ElMessage.warning('请先填写说明或上传图片后再提交。')
    return
  }

  submitting.value = true

  try {
    const images = fileList.value.length ? await uploadSelectedFiles() : []
    await store.submitHomework({
      homeworkId: homeworkId.value,
      text: text.value.trim(),
      images
    })

    ElMessage.success('作业提交成功，已同步到反馈页。')
    void router.replace(`/student/homeworks/${homeworkId.value}/feedback`)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '提交失败，请稍后重试。')
  } finally {
    submitting.value = false
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
        <h2>提交作业</h2>
        <p>补充文字说明、上传图片后直接提交，成功后会跳到反馈页继续查看结果。</p>
      </div>
      <el-button @click="router.push(`/student/homeworks/${homeworkId}`)">返回作业详情</el-button>
    </header>

    <section class="surface-card section-card">
      <div v-if="homework" class="student-submit-hero">
        <span class="soft-chip">{{ homework.subject }}</span>
        <h3>{{ homework.title }}</h3>
        <p class="section-subtitle">{{ homework.summary }}</p>
      </div>

      <div class="page-stack">
        <div>
          <h3>文字说明</h3>
          <p class="section-subtitle">可以补充解题过程、朗读说明或实验观察结论。</p>
          <el-input
            v-model="text"
            type="textarea"
            :rows="7"
            resize="vertical"
            placeholder="写下本次作业的说明、思路或补充内容。"
          />
        </div>

        <div>
          <h3>图片上传</h3>
          <p class="section-subtitle">支持上传答题照片或手写过程，最多 6 张。</p>
          <el-upload
            v-model:file-list="fileList"
            drag
            multiple
            :auto-upload="false"
            :limit="6"
            accept="image/*"
          >
            <div class="student-upload-copy">
              <strong>把作业照片拖到这里，或点击选择图片</strong>
              <span>支持 png / jpg / jpeg，提交时会自动上传。</span>
            </div>
          </el-upload>
        </div>

        <div class="student-submit-actions">
          <el-button size="large" @click="router.push(`/student/homeworks/${homeworkId}`)">稍后再交</el-button>
          <el-button type="primary" size="large" :loading="submitting" @click="submitHomework">
            {{ submitting ? '正在提交...' : '提交作业' }}
          </el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.student-submit-hero h3 {
  margin: 0.9rem 0 0;
  font-size: 1.7rem;
  color: #14263d;
}

.student-upload-copy {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  color: #607589;
}

.student-submit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.8rem;
}
</style>
