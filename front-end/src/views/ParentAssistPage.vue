<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { UploadProps, UploadUserFile } from 'element-plus'
import { uploadParentFile } from '@/api/parent'
import { useParentPortalStore } from '@/stores/parentPortal'

const route = useRoute()
const router = useRouter()
const store = useParentPortalStore()

const childId = computed(() => `${route.params.childId ?? ''}`)
const homeworkId = computed(() => `${route.params.id ?? ''}`)
const homework = computed(() => store.getHomework(childId.value, homeworkId.value))
const child = computed(() => store.children.find((item) => item.id === childId.value) ?? null)
const uploadFiles = ref<UploadUserFile[]>([])
const submitting = ref(false)

const form = reactive({
  text: ''
})

const uploadRequest: UploadProps['httpRequest'] = async (options) => {
  try {
    const result = await uploadParentFile(options.file as File, 'parent-homework')
    options.onSuccess?.({
      url: result.fileUrl,
      name: result.fileName
    })
  } catch (error) {
    const uploadError =
      error instanceof Error
        ? Object.assign(error, { status: 500, method: 'POST', url: '/api/files/upload' })
        : Object.assign(new Error('上传失败'), { status: 500, method: 'POST', url: '/api/files/upload' })

    options.onError?.(uploadError as never)
  }
}

async function loadPage() {
  try {
    if (!store.children.length) {
      await store.loadChildren()
    }
    store.selectChild(childId.value)
    await store.loadHomeworkDetail(childId.value, homeworkId.value)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '协助提交页面加载失败，请稍后重试。')
  }
}

async function submitAssist() {
  submitting.value = true

  try {
    const images = uploadFiles.value
      .map((item) => {
        const response = item.response as { url?: string } | { data?: { fileUrl?: string } } | undefined
        if (response && 'data' in response) {
          return response.data?.fileUrl || item.url
        }

        return (response as { url?: string } | undefined)?.url || item.url
      })
      .filter((item): item is string => Boolean(item))

    await store.assistHomework({
      studentId: childId.value,
      homeworkId: homeworkId.value,
      text: form.text,
      images
    })

    ElMessage.success('已提交协助内容。')
    void router.push(`/parent/homeworks/${childId.value}/${homeworkId.value}`)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '协助提交失败，请稍后重试。')
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
    <section v-if="homework" class="surface-card section-card parent-assist-card">
      <div class="parent-assist-head">
        <div>
          <el-button text @click="router.push(`/parent/homeworks/${childId}/${homeworkId}`)">← 返回详情</el-button>
          <h2>协助提交</h2>
          <p>{{ child?.name || '孩子' }} · {{ homework.subject }} · {{ homework.teacherName }}老师</p>
        </div>
        <strong>{{ homework.title }}</strong>
      </div>

      <el-form label-position="top" class="parent-assist-form">
        <el-form-item label="补充说明">
          <el-input v-model="form.text" type="textarea" :rows="7" placeholder="填写本次提交说明，或补充孩子订正情况。" />
        </el-form-item>

        <el-form-item label="上传图片">
          <el-upload
            v-model:file-list="uploadFiles"
            drag
            multiple
            :limit="6"
            :http-request="uploadRequest"
            list-type="picture-card"
          >
            <div class="assist-upload-copy">
              <strong>点击或拖拽上传</strong>
              <p>支持作业照片、订正截图和家长补充材料</p>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>

      <div class="parent-assist-actions">
        <el-button @click="router.push(`/parent/homeworks/${childId}/${homeworkId}`)">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAssist">提交</el-button>
      </div>
    </section>

    <div v-else class="empty-state">当前没有找到这份作业。</div>
  </div>
</template>

<style scoped>
.parent-assist-card {
  max-width: 980px;
}

.parent-assist-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
}

.parent-assist-head h2 {
  margin: 12px 0 0;
  color: #08213f;
  font-size: 30px;
  letter-spacing: -0.04em;
}

.parent-assist-head p {
  margin: 8px 0 0;
  color: #60758a;
}

.parent-assist-head > strong {
  color: #17324d;
  font-size: 18px;
}

.parent-assist-form {
  margin-top: 1rem;
}

.assist-upload-copy p {
  margin: 0.45rem 0 0;
  color: #73879a;
}

.parent-assist-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1rem;
}

@media (max-width: 760px) {
  .parent-assist-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
