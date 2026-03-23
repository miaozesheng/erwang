<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'
import { getArticle, createArticle, updateArticle, getCategories, getTags } from '../api'
import Header from '../components/Header.vue'

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

const categories = ref([])
const tags = ref([])
const previewMode = ref(false)

const renderedPreview = computed(() => {
  return form.content ? marked(form.content) : '<p class="empty">预览区域</p>'
})

const fetchData = async () => {
  loading.value = true
  try {
    const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
    categories.value = catRes.data.categories || catRes.data || []
    tags.value = tagRes.data.tags || tagRes.data || []
    
    if (isEdit.value) {
      const res = await getArticle(route.params.id)
      const article = res.data.article || res.data
      form.title = article.title || ''
      form.content = article.content || ''
      form.category = article.category || ''
      form.tags = article.tags || []
      form.excerpt = article.excerpt || ''
    }
  } catch (e) {
    console.error('Failed to fetch data:', e)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
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
</script>

<template>
  <div class="page-container">
    <Header />
    
    <main class="main-content">
      <div class="editor-header">
        <h1 class="editor-title">{{ isEdit ? '编辑文章' : '新建文章' }}</h1>
        <div class="header-actions">
          <el-button @click="previewMode = !previewMode">
            {{ previewMode ? '编辑' : '预览' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            {{ isEdit ? '更新' : '发布' }}
          </el-button>
        </div>
      </div>

      <div v-loading="loading" class="editor-container">
        <div v-if="!previewMode" class="editor-form">
          <el-form :model="form" label-position="top">
            <el-form-item label="标题" required>
              <el-input 
                v-model="form.title" 
                placeholder="请输入文章标题"
                size="large"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="分类">
              <el-select v-model="form.category" placeholder="选择分类" style="width: 100%">
                <el-option 
                  v-for="cat in categories" 
                  :key="cat" 
                  :label="cat" 
                  :value="cat" 
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="标签">
              <el-select 
                v-model="form.tags" 
                multiple 
                placeholder="选择标签"
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
            
            <el-form-item label="摘要">
              <el-input 
                v-model="form.excerpt" 
                type="textarea" 
                :rows="3"
                placeholder="请输入文章摘要（可选）"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="内容 (Markdown)" required>
              <el-input 
                v-model="form.content" 
                type="textarea" 
                :rows="20"
                placeholder="请输入 Markdown 内容..."
                class="content-editor"
              />
            </el-form-item>
          </el-form>
        </div>

        <div v-else class="preview-container">
          <h2 class="preview-title">{{ form.title || '无标题' }}</h2>
          <div 
            class="preview-content markdown-body"
            v-html="renderedPreview"
          ></div>
        </div>
      </div>
    </main>
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

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
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
  border: none;
  box-shadow: 0 0 15px rgba(170, 59, 255, 0.3);
}

.editor-container {
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 24px;
}

:deep(.el-form-item__label) {
  color: var(--text-h);
  font-weight: 500;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  background: var(--code-bg);
  border: 1px solid var(--border);
  box-shadow: none;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:hover),
:deep(.el-textarea__inner:focus) {
  border-color: var(--accent);
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  color: var(--text-h);
}

:deep(.el-input__inner::placeholder),
:deep(.el-textarea__inner::placeholder) {
  color: var(--text);
}

:deep(.el-select .el-input__wrapper) {
  background: var(--code-bg);
}

.content-editor :deep(.el-textarea__inner) {
  font-family: var(--mono);
  font-size: 14px;
  line-height: 1.6;
  min-height: 400px;
}

.preview-container {
  padding: 24px;
}

.preview-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-h);
  margin: 0 0 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
}

.preview-content {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text);
}

:deep(.markdown-body) {
  color: var(--text);
}

:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3) {
  color: var(--text-h);
  margin: 24px 0 16px;
}

:deep(.markdown-body p) {
  margin: 16px 0;
}

:deep(.markdown-body code) {
  background: var(--code-bg);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: var(--mono);
}

:deep(.markdown-body pre) {
  background: var(--code-bg);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
}

:deep(.markdown-body pre code) {
  background: none;
  padding: 0;
}

:deep(.empty) {
  color: var(--text);
  opacity: 0.5;
  text-align: center;
  padding: 40px;
}

@media (max-width: 768px) {
  .editor-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .main-content {
    padding: 24px 16px;
  }
}
</style>