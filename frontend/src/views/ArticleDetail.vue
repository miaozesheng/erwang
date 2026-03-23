<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import hljs from 'highlight.js'
import { ElMessage } from 'element-plus'
import { getArticle } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import { ArrowLeft, Folder, Calendar, View } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const article = ref(null)
const loading = ref(false)

marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true
})

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  return marked(article.value.content)
})

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const fetchArticle = async () => {
  loading.value = true
  try {
    const res = await getArticle(route.params.id)
    article.value = res.data.article || res.data
  } catch (e) {
    console.error('Failed to fetch article:', e)
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchArticle()
})
</script>

<template>
  <div class="page-container">
    <Header />
    
    <main class="main-content">
      <el-button class="back-btn" @click="router.push('/')">
        <el-icon><ArrowLeft /></el-icon>
        返回首页
      </el-button>

      <el-skeleton :rows="10" animated v-if="loading" />
      
      <article v-else-if="article" class="article-detail">
        <header class="article-header">
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-meta">
            <span v-if="article.category" class="meta-category">
              <el-icon><Folder /></el-icon>
              {{ article.category }}
            </span>
            <span class="meta-date">
              <el-icon><Calendar /></el-icon>
              {{ formatDate(article.created_at) }}
            </span>
            <span v-if="article.views" class="meta-views">
              <el-icon><View /></el-icon>
              {{ article.views }} 阅读
            </span>
          </div>
          <div v-if="article.tags?.length" class="article-tags">
            <el-tag 
              v-for="tag in article.tags" 
              :key="tag"
              class="tag"
            >
              {{ tag }}
            </el-tag>
          </div>
        </header>

        <div 
          class="article-content markdown-body"
          v-html="renderedContent"
        ></div>
      </article>

      <el-empty v-else description="文章不存在" />
    </main>

    <Footer />
  </div>
</template>

<style scoped>
.page-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px;
  width: 100%;
  box-sizing: border-box;
}

.back-btn {
  margin-bottom: 24px;
  background: var(--code-bg);
  border: 1px solid var(--border);
  color: var(--text);
}

.back-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.article-detail {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.article-header {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border);
}

.article-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-h);
  margin: 0 0 16px;
  line-height: 1.3;
}

.article-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.meta-category,
.meta-date,
.meta-views {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text);
  font-size: 14px;
}

.meta-category .el-icon,
.meta-date .el-icon,
.meta-views .el-icon {
  color: var(--accent);
}

.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  background: var(--accent-bg);
  color: var(--accent);
  border: none;
}

.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text);
}

:deep(.markdown-body) {
  color: var(--text);
}

:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3),
:deep(.markdown-body h4) {
  color: var(--text-h);
  margin: 24px 0 16px;
  font-weight: 600;
}

:deep(.markdown-body h1) { font-size: 28px; }
:deep(.markdown-body h2) { font-size: 24px; }
:deep(.markdown-body h3) { font-size: 20px; }

:deep(.markdown-body p) {
  margin: 16px 0;
}

:deep(.markdown-body a) {
  color: var(--accent);
  text-decoration: none;
}

:deep(.markdown-body a:hover) {
  text-decoration: underline;
}

:deep(.markdown-body code) {
  background: var(--code-bg);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: var(--mono);
  font-size: 14px;
}

:deep(.markdown-body pre) {
  background: var(--code-bg);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
}

:deep(.markdown-body pre code) {
  background: none;
  padding: 0;
}

:deep(.markdown-body blockquote) {
  border-left: 4px solid var(--accent);
  margin: 16px 0;
  padding: 8px 16px;
  background: var(--accent-bg);
}

:deep(.markdown-body ul),
:deep(.markdown-body ol) {
  padding-left: 24px;
  margin: 16px 0;
}

:deep(.markdown-body li) {
  margin: 8px 0;
}

:deep(.markdown-body img) {
  max-width: 100%;
  border-radius: 8px;
}

:deep(.markdown-body table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

:deep(.markdown-body th),
:deep(.markdown-body td) {
  border: 1px solid var(--border);
  padding: 12px;
  text-align: left;
}

:deep(.markdown-body th) {
  background: var(--code-bg);
}

@media (max-width: 768px) {
  .article-title {
    font-size: 28px;
  }
  
  .main-content {
    padding: 24px 16px;
  }
}
</style>