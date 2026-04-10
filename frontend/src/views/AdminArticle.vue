<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getArticle, createArticle, updateArticle, getCategories, getTags, uploadFile, resolveFileUrl } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import Vditor from 'vditor'
import 'vditor/dist/index.css'

const DRAFT_KEY = 'erwang_article_draft'

const saveDraft = () => {
  if (isEdit.value) return
  localStorage.setItem(DRAFT_KEY, JSON.stringify({
    title: form.title,
    content: form.content,
    category: form.category,
    tags: form.tags,
    excerpt: form.excerpt,
    savedAt: Date.now()
  }))
}

const loadDraft = () => {
  if (isEdit.value) return false
  try {
    const raw = localStorage.getItem(DRAFT_KEY)
    if (!raw) return false
    const draft = JSON.parse(raw)
    if (Date.now() - draft.savedAt > 7 * 24 * 60 * 60 * 1000) {
      localStorage.removeItem(DRAFT_KEY)
      return false
    }
    form.title = draft.title || ''
    form.content = draft.content || ''
    form.category = draft.category || ''
    form.tags = draft.tags || []
    form.excerpt = draft.excerpt || ''
    return true
  } catch { return false }
}

const clearDraft = () => {
  localStorage.removeItem(DRAFT_KEY)
}

let draftTimer = null

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  title: '',
  content: '',
  category: '',
  tags: [],
  excerpt: ''
})

const contentStats = computed(() => {
  const content = form.content || ''
  const plainText = content
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/`[^`]*`/g, ' ')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/[#>*_~\-|]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
  const words = plainText ? plainText.split(' ').filter(Boolean).length : 0
  return {
    words,
    chars: content.length
  }
})

const categories = ref([])
const tags = ref([])
const vditorRef = ref(null)
let vditorInstance = null



const initVditor = () => {
  if (vditorInstance) {
    vditorInstance.destroy()
    vditorInstance = null
  }

  vditorInstance = new Vditor('vditor-editor', {
    mode: 'ir',
    lang: 'zh_CN',
    theme: 'dark',
    height: 500,
    placeholder: '开始编写 Markdown 内容...',
    value: form.content,
    cache: { enable: false },
    toolbar: [
      'headings', 'bold', 'italic', 'strike', '|',
      'line', 'quote', 'list', 'ordered-list', 'check', '|',
      'code', 'inline-code', 'table', 'link', 'upload', '|',
      'undo', 'redo', '|',
      'edit-mode', 'outline', 'fullscreen'
    ],
    input: (value) => {
      form.content = value
      saveDraft()
    },
    upload: {
      accept: 'image/*',
      multiple: false,
      handler: async (files) => {
        const file = files[0]
        if (!file) return null
        const maxSize = 10 * 1024 * 1024
        if (file.size > maxSize) {
          ElMessage.warning('图片大小不能超过 10MB')
          return null
        }
        try {
          const res = await uploadFile(file)
          const rawUrl = res.data?.data?.url || res.data?.url || ''
          const displayUrl = resolveFileUrl(rawUrl)
          if (displayUrl && vditorInstance) {
            vditorInstance.insertValue(`![image](${displayUrl})`)
          }
          ElMessage.success('图片上传成功')
        } catch (error) {
          console.error('Upload failed:', error)
          ElMessage.error('图片上传失败')
        }
        return null
      }
    },
    after: () => {
      if (form.content && vditorInstance) {
        vditorInstance.setValue(form.content)
      }
      const editorEl = document.getElementById('vditor-editor')
      if (editorEl) {
        editorEl.classList.add('vditor-cyber')
      }
    }
  })
}

const fetchData = async () => {
  loading.value = true
  try {
    const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
    categories.value = catRes.data.data || []
    tags.value = tagRes.data.data || []

    if (isEdit.value) {
      const res = await getArticle(route.params.id)
      const article = res.data.data || {}
      form.title = article.title || ''
      form.content = article.content || ''
      form.category = article.category || ''
      form.tags = article.tags || []
      form.excerpt = article.excerpt || article.summary || ''
    } else {
      const hasDraft = loadDraft()
      if (hasDraft) {
        ElMessage.info('已恢复上次未保存的草稿')
      }
    }
  } catch (e) {
    console.error('Failed to fetch data:', e)
  } finally {
    loading.value = false
    initVditor()
  }
}

const handleSave = async () => {
  if (vditorInstance) {
    form.content = vditorInstance.getValue()
  }

  if (!form.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }

  saving.value = true
  try {
    const data = {
      title: form.title,
      content: form.content,
      category: form.category,
      tags: form.tags,
      excerpt: form.excerpt
    }

    if (isEdit.value) {
      await updateArticle(route.params.id, data)
      ElMessage.success('更新成功')
    } else {
      await createArticle(data)
      ElMessage.success('创建成功')
      clearDraft()
    }

    router.push('/admin')
  } catch (e) {
    console.error('Failed to save:', e)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  router.push('/admin')
}

onMounted(() => {
  fetchData()
})

onBeforeUnmount(() => {
  if (vditorInstance) {
    vditorInstance.destroy()
    vditorInstance = null
  }
  if (draftTimer) clearInterval(draftTimer)
})
</script>

<template>
  <div class="page-container">
    <Header />

    <main class="main-content">
      <section class="editor-shell">
        <div class="editor-header">
          <h1 class="editor-title">{{ isEdit ? '编辑文章' : '新建文章' }}</h1>
          <div class="header-actions">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" :loading="saving" @click="handleSave">
              {{ isEdit ? '更新' : '发布' }}
            </el-button>
          </div>
        </div>

        <div v-loading="loading" class="editor-container">
          <el-form :model="form" label-position="top">
            <div class="section-label">// META</div>

            <div class="meta-row">
              <el-form-item label="标题" required class="meta-title">
                <el-input
                  v-model="form.title"
                  placeholder="请输入文章标题"
                  size="large"
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="分类" class="meta-category">
                <el-select v-model="form.category" placeholder="选择或输入分类" style="width: 100%" filterable allow-create default-first-option>
                  <el-option
                    v-for="cat in categories"
                    :key="cat"
                    :label="cat"
                    :value="cat"
                  />
                </el-select>
              </el-form-item>
            </div>

            <div class="meta-row">
              <el-form-item label="标签" class="meta-tags">
                <el-select
                  v-model="form.tags"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  placeholder="选择或输入标签"
                  style="width: 100%"
                >
                  <el-option
                    v-for="tag in tags"
                    :key="tag"
                    :label="tag"
                    :value="tag"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="摘要" class="meta-excerpt">
                <el-input
                  v-model="form.excerpt"
                  placeholder="文章摘要（可选）"
                  maxlength="200"
                />
              </el-form-item>
            </div>

            <div class="editor-separator" aria-hidden="true"></div>
            <div class="section-label">// CONTENT</div>

            <el-form-item label="正文" required>
              <div id="vditor-editor" class="vditor-wrap"></div>
            </el-form-item>

            <div class="editor-stats">
              <span>WORDS {{ contentStats.words }}</span>
              <span>CHARS {{ contentStats.chars }}</span>
            </div>
          </el-form>
        </div>
      </section>
    </main>
    <Footer />
  </div>
</template>

<style scoped>
.page-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
  width: 100%;
  box-sizing: border-box;
}

.editor-shell {
  position: relative;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  background: var(--card-bg);
  box-shadow: var(--shadow);
  padding: 24px;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.editor-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-h);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.header-actions .el-button--primary {
  background: var(--accent);
  border: 1px solid var(--border-accent);
  box-shadow: none;
}

.editor-container {
  position: relative;
  z-index: 1;
}

.section-label {
  margin: 4px 0 14px;
  color: var(--accent);
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  opacity: 0.88;
}

.editor-separator {
  margin: 8px 0 16px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 240, 255, 0.2), transparent);
}

.meta-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.meta-title {
  grid-column: 1;
}

.meta-category {
  grid-column: 2;
}

:deep(.el-form-item__label) {
  color: var(--text-h);
  font-weight: 500;
  font-family: var(--mono);
  text-transform: uppercase;
  letter-spacing: 0.9px;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  background: var(--code-bg);
  border: 1px solid var(--border);
  box-shadow: none;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  border-color: var(--accent);
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  color: var(--text-h);
}

:deep(.el-select .el-input__wrapper) {
  background: var(--code-bg);
}

.vditor-wrap {
  width: 100%;
  border-radius: 10px;
  overflow: hidden;
}

.editor-stats {
  margin-top: 10px;
  display: flex;
  gap: 18px;
  color: rgba(114, 255, 220, 0.88);
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 0.7px;
}

:deep(.vditor) {
  --panel-background-color: #0b1728 !important;
  --toolbar-background-color: rgba(8, 16, 31, 0.95) !important;
  --textarea-background-color: #0b1728 !important;
  border: 1px solid rgba(0, 240, 255, 0.22) !important;
  border-radius: 10px !important;
}

:deep(.vditor-toolbar) {
  background: rgba(8, 16, 31, 0.95) !important;
  border-bottom: 1px solid rgba(0, 240, 255, 0.15) !important;
  padding: 6px 8px !important;
}

:deep(.vditor-toolbar__item) {
  border-radius: 6px !important;
}

:deep(.vditor-toolbar__item:hover) {
  background: rgba(0, 240, 255, 0.1) !important;
}

:deep(.vditor-toolbar__item--current) {
  background: rgba(0, 240, 255, 0.15) !important;
  color: #00f0ff !important;
}

:deep(.vditor-toolbar svg) {
  color: #97a8b8 !important;
  fill: #97a8b8 !important;
}

:deep(.vditor-toolbar__item:hover svg),
:deep(.vditor-toolbar__item--current svg) {
  color: #00f0ff !important;
  fill: #00f0ff !important;
}

:deep(.vditor-ir .vditor-reset) {
  color: #c8dce8 !important;
  font-family: var(--sans) !important;
  font-size: 15px !important;
  line-height: 1.8 !important;
  padding: 24px !important;
}

:deep(.vditor-ir .vditor-reset h1),
:deep(.vditor-ir .vditor-reset h2),
:deep(.vditor-ir .vditor-reset h3) {
  color: #e9f8ff !important;
  font-family: var(--heading) !important;
}

:deep(.vditor-ir .vditor-reset pre.vditor-reset) {
  background: #081022 !important;
  border: 1px solid rgba(0, 240, 255, 0.15) !important;
  border-radius: 8px !important;
}

:deep(.vditor-ir .vditor-reset code) {
  background: rgba(0, 240, 255, 0.08) !important;
  color: #00f0ff !important;
  border-radius: 4px !important;
  padding: 2px 6px !important;
  font-family: var(--mono) !important;
}

:deep(.vditor-ir .vditor-reset blockquote) {
  border-left: 4px solid #00f0ff !important;
  background: rgba(0, 240, 255, 0.05) !important;
  padding: 8px 16px !important;
}

:deep(.vditor-ir .vditor-reset a) {
  color: #00f0ff !important;
}

:deep(.vditor-ir .vditor-reset table td),
:deep(.vditor-ir .vditor-reset table th) {
  border-color: rgba(0, 240, 255, 0.2) !important;
}

:deep(.vditor-ir .vditor-reset table th) {
  background: rgba(0, 240, 255, 0.06) !important;
}

:deep(.vditor-outline) {
  background: rgba(8, 16, 31, 0.9) !important;
  border-left: 1px solid rgba(0, 240, 255, 0.15) !important;
}

:deep(.vditor-hint),
:deep(.vditor-tip) {
  background: rgba(8, 16, 31, 0.96) !important;
  border: 1px solid rgba(0, 240, 255, 0.2) !important;
  color: #c8dce8 !important;
}

:deep(.vditor-input) {
  background: #0b1728 !important;
  color: #e9f8ff !important;
  border-color: rgba(0, 240, 255, 0.22) !important;
}

@media (max-width: 768px) {
  .editor-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .meta-row {
    grid-template-columns: 1fr;
  }

  .editor-shell {
    padding: 18px;
  }

  .main-content {
    padding: 24px 16px;
  }
}
</style>
