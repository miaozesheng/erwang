<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Marked } from 'marked'
import { markedHighlight } from 'marked-highlight'
import hljs from 'highlight.js'
import DOMPurify from 'dompurify'
import { ElMessage } from 'element-plus'
import { getArticle, toggleLike, toggleFavorite, getInteractionStatus } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import { ArrowLeft, Folder, Calendar, View } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const article = ref(null)
const loading = ref(false)
const headings = ref([])
const activeHeadingId = ref('')
const contentRef = ref(null)
const liked = ref(false)
const favorited = ref(false)
const likeCount = ref(0)
const favoriteCount = ref(0)

let headingObserver = null

const resolveInteractionState = (data = {}) => {
  liked.value = Boolean(data.liked ?? data.isLiked ?? false)
  favorited.value = Boolean(data.favorited ?? data.isFavorited ?? false)
  likeCount.value = Number(data.likeCount ?? data.like_count ?? likeCount.value ?? 0)
  favoriteCount.value = Number(data.favoriteCount ?? data.favorite_count ?? favoriteCount.value ?? 0)
}

const fetchInteractionStatus = async (articleId) => {
  try {
    const res = await getInteractionStatus(articleId)
    resolveInteractionState(res.data?.data || {})
  } catch (error) {
    console.error('Failed to fetch interaction status:', error)
  }
}

const marked = new Marked(
  markedHighlight({
    langPrefix: 'hljs language-',
    highlight(code, lang) {
      if (lang && hljs.getLanguage(lang)) {
        return hljs.highlight(code, { language: lang }).value
      }
      return hljs.highlightAuto(code).value
    }
  })
)

marked.setOptions({ breaks: true })

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  const raw = marked.parse(article.value.content)
  return DOMPurify.sanitize(raw)
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
    const articleId = route.params.id
    const res = await getArticle(articleId)
    article.value = res.data.data
    likeCount.value = Number(article.value?.like_count ?? article.value?.likeCount ?? 0)
    favoriteCount.value = Number(article.value?.favorite_count ?? article.value?.favoriteCount ?? 0)
    await fetchInteractionStatus(articleId)
    await nextTick()
    collectHeadings()
    observeHeadingScroll()
  } catch (e) {
    console.error('Failed to fetch article:', e)
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

const handleLike = async () => {
  if (!article.value?.id) return
  try {
    const res = await toggleLike(article.value.id)
    resolveInteractionState(res.data?.data || {})
  } catch (error) {
    console.error('Failed to toggle like:', error)
    ElMessage.error('点赞失败，请稍后重试')
  }
}

const handleFavorite = async () => {
  if (!article.value?.id) return
  try {
    const res = await toggleFavorite(article.value.id)
    resolveInteractionState(res.data?.data || {})
  } catch (error) {
    console.error('Failed to toggle favorite:', error)
    ElMessage.error('收藏失败，请稍后重试')
  }
}

const collectHeadings = () => {
  const container = contentRef.value
  if (!container) {
    headings.value = []
    return
  }

  const headingElements = container.querySelectorAll('h1, h2, h3')
  headings.value = Array.from(headingElements).map((heading, index) => {
    const level = Number(heading.tagName.replace('H', ''))
    const text = heading.textContent?.trim() || `section-${index + 1}`
    const id = heading.id || `heading-${index}-${text.toLowerCase().replace(/\s+/g, '-')}`
    heading.id = id
    return {
      id,
      text,
      level
    }
  })
  activeHeadingId.value = headings.value[0]?.id || ''
}

const scrollToHeading = (id) => {
  const el = document.getElementById(id)
  if (!el) return
  el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const observeHeadingScroll = () => {
  if (headingObserver) {
    headingObserver.disconnect()
    headingObserver = null
  }

  if (!headings.value.length) return

  headingObserver = new IntersectionObserver(
    (entries) => {
      const visible = entries
        .filter((entry) => entry.isIntersecting)
        .sort((a, b) => b.intersectionRatio - a.intersectionRatio)
      if (visible.length) {
        activeHeadingId.value = visible[0].target.id
      }
    },
    {
      root: null,
      rootMargin: '-96px 0px -60% 0px',
      threshold: [0.1, 0.4, 0.7]
    }
  )

  headings.value.forEach((item) => {
    const headingEl = document.getElementById(item.id)
    if (headingEl) headingObserver.observe(headingEl)
  })
}

onMounted(() => {
  fetchArticle()
})

watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    article.value = null
    headings.value = []
    activeHeadingId.value = ''
    if (headingObserver) {
      headingObserver.disconnect()
    }
    fetchArticle()
  }
})

onUnmounted(() => {
  if (headingObserver) {
    headingObserver.disconnect()
    headingObserver = null
  }
})
</script>

<template>
  <div class="page-container">
    <Header />
    
    <main class="main-content">
      <el-skeleton :rows="10" animated v-if="loading" />

      <div v-else-if="article" class="content-layout">
        <section class="reader-column">
          <button type="button" class="back-btn" @click="router.push('/')">
            <el-icon><ArrowLeft /></el-icon>
            返回首页
          </button>

          <article class="article-detail">
            <header class="article-header">
              <h1 class="article-title">{{ article.title }}</h1>
              <div class="article-meta">
                <span v-if="article.category" class="meta-item">
                  <el-icon><Folder /></el-icon>
                  {{ article.category }}
                </span>
                <span class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  {{ formatDate(article.created_at) }}
                </span>
                <span v-if="article.views" class="meta-item">
                  <el-icon><View /></el-icon>
                  {{ article.views }} 阅读
                </span>
              </div>

              <div v-if="article.tags?.length" class="article-tags">
                <span v-for="tag in article.tags" :key="tag" class="tag">{{ tag }}</span>
              </div>

              <div class="interaction-bar">
                <button type="button" class="interact-btn like" :class="{ active: liked }" @click="handleLike">
                  <span class="btn-icon" aria-hidden="true">❤</span>
                  <span>{{ likeCount }}</span>
                </button>
                <button type="button" class="interact-btn favorite" :class="{ active: favorited }" @click="handleFavorite">
                  <span class="btn-icon" aria-hidden="true">★</span>
                  <span>{{ favoriteCount }}</span>
                </button>
              </div>
            </header>

            <div
              ref="contentRef"
              class="article-content markdown-body"
              v-html="renderedContent"
            ></div>
          </article>
        </section>

        <aside v-if="headings.length" class="toc-sidebar">
          <h3 class="toc-title">目录</h3>
          <ul class="toc-list">
            <li
              v-for="item in headings"
              :key="item.id"
              class="toc-item"
              :class="[
                `level-${item.level}`,
                { active: activeHeadingId === item.id }
              ]"
            >
              <button type="button" class="toc-link" @click="scrollToHeading(item.id)">
                {{ item.text }}
              </button>
            </li>
          </ul>
        </aside>
      </div>

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
  max-width: 1160px;
  margin: 0 auto;
  padding: 32px 24px 44px;
  width: 100%;
  box-sizing: border-box;
}

.content-layout {
  display: grid;
  grid-template-columns: minmax(0, 760px) 200px;
  gap: 40px;
  justify-content: center;
  align-items: start;
}

.reader-column {
  width: 100%;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  color: var(--text);
  font-family: var(--mono);
  font-size: 13px;
  padding: 0;
  margin: 0 0 20px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.back-btn:hover {
  color: var(--accent);
}

.article-detail {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 40px;
}

.article-header {
  margin-bottom: 30px;
  padding-bottom: 22px;
  border-bottom: 1px solid var(--border);
}

.article-title {
  margin: 0;
  font-size: 32px;
  line-height: 1.3;
  font-weight: 700;
  color: var(--text-h);
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 14px;
  color: var(--text-secondary);
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 0.2px;
}

.meta-item .el-icon {
  color: var(--text-secondary);
  font-size: 13px;
}

.article-tags {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-family: var(--mono);
  font-size: 11px;
}

.interaction-bar {
  margin-top: 18px;
  display: flex;
  gap: 10px;
}

.interact-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 12px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text-secondary);
  font-family: var(--mono);
  font-size: 13px;
  cursor: pointer;
  transition: transform 0.12s ease, border-color 0.2s ease, color 0.2s ease, background-color 0.2s ease;
}

.interact-btn:active {
  transform: scale(0.97);
}

.interact-btn:hover,
.interact-btn.active {
  color: var(--accent);
  border-color: var(--accent);
  background: var(--accent-bg);
}

.btn-icon {
  font-size: 12px;
}

.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text);
}

.toc-sidebar {
  position: sticky;
  top: 88px;
  width: 200px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--bg);
  padding: 14px 10px;
}

.toc-title {
  margin: 0 0 10px;
  color: var(--text-h);
  font-size: 13px;
  font-family: var(--mono);
  letter-spacing: 0.4px;
}

.toc-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.toc-item {
  margin-bottom: 2px;
}

.toc-item.level-2 .toc-link {
  padding-left: 16px;
}

.toc-item.level-3 .toc-link {
  padding-left: 28px;
}

.toc-link {
  width: 100%;
  border: 0;
  border-left: 2px solid transparent;
  border-radius: 0;
  background: transparent;
  color: var(--text-secondary);
  text-align: left;
  font-family: var(--mono);
  font-size: 13px;
  line-height: 1.4;
  padding: 6px 8px;
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease, background-color 0.2s ease;
}

.toc-link:hover {
  color: var(--text);
  background: var(--bg-secondary);
}

.toc-item.active .toc-link {
  color: var(--accent);
  border-left-color: var(--accent);
  background: var(--accent-bg);
}

:deep(.markdown-body) {
  color: var(--text);
}

:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3),
:deep(.markdown-body h4) {
  color: var(--text-h);
  font-family: var(--heading);
  font-weight: 700;
  line-height: 1.35;
  margin: 2.1em 0 0.75em;
}

:deep(.markdown-body h1) { font-size: 30px; }
:deep(.markdown-body h2) { font-size: 25px; }
:deep(.markdown-body h3) { font-size: 21px; }
:deep(.markdown-body h4) { font-size: 18px; }

:deep(.markdown-body p) {
  margin: 0 0 1.1em;
}

:deep(.markdown-body a) {
  color: var(--accent);
  text-decoration: underline;
  text-underline-offset: 3px;
}

:deep(.markdown-body code) {
  background: var(--code-bg);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 2px 6px;
  font-size: 14px;
  font-family: var(--mono);
}

:deep(.markdown-body pre) {
  margin: 1.3em 0;
  padding: 16px;
  background: var(--code-bg);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  overflow-x: auto;
}

:deep(.markdown-body pre code) {
  border: none;
  padding: 0;
  background: transparent;
}

:deep(.markdown-body blockquote) {
  margin: 1.2em 0;
  padding: 8px 0 8px 14px;
  border-left: 3px solid var(--accent);
  color: color-mix(in srgb, var(--text) 88%, var(--text-h) 12%);
}

:deep(.markdown-body ul),
:deep(.markdown-body ol) {
  margin: 0 0 1.2em;
  padding-left: 24px;
}

:deep(.markdown-body li) {
  margin: 0.45em 0;
}

:deep(.markdown-body img) {
  display: block;
  max-width: 100%;
  border-radius: 10px;
  margin: 1.2em auto;
}

:deep(.markdown-body table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1.3em 0;
}

:deep(.markdown-body th),
:deep(.markdown-body td) {
  border: 1px solid var(--border);
  padding: 10px 12px;
  text-align: left;
}

:deep(.markdown-body th) {
  color: var(--text-h);
  background: var(--bg-secondary);
}

@media (max-width: 768px) {
  .content-layout {
    grid-template-columns: minmax(0, 760px);
  }

  .toc-sidebar {
    display: none;
  }

  .main-content {
    padding: 22px 14px 34px;
  }

  .article-detail {
    padding: 22px 16px;
  }

  .article-title {
    font-size: 24px;
  }
}
</style>
