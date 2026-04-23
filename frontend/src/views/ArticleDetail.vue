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
import { ArrowLeft, Folder, Calendar, View, Star, StarFilled } from '@element-plus/icons-vue'

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
  const offset = 80
  const elementPosition = el.getBoundingClientRect().top
  const offsetPosition = elementPosition + window.pageYOffset - offset
  window.scrollTo({
    top: offsetPosition,
    behavior: 'smooth'
  })
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

      <div v-else-if="article" class="article-layout">
        <aside class="toc-sidebar">
          <div class="toc-sticky">
            <h3 class="toc-title">目录索引</h3>
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
                  <span class="toc-dot"></span>
                  <span class="toc-text">{{ item.text }}</span>
                </button>
              </li>
            </ul>
          </div>
        </aside>

        <section class="article-column">
          <nav class="article-nav">
            <button type="button" class="refined-back-btn" @click="router.push('/')">
              <el-icon><ArrowLeft /></el-icon>
              <span>返回列表</span>
            </button>
          </nav>

          <article class="article-viewer">
            <header class="article-header">
              <h1 class="article-title">{{ article.title }}</h1>
              
              <div class="article-meta-row">
                <div class="meta-main">
                  <span v-if="article.category" class="meta-tag category">
                    <el-icon><Folder /></el-icon>
                    {{ article.category }}
                  </span>
                  <span class="meta-info">
                    <el-icon><Calendar /></el-icon>
                    {{ formatDate(article.created_at) }}
                  </span>
                  <span v-if="article.views" class="meta-info">
                    <el-icon><View /></el-icon>
                    {{ article.views }} 阅读
                  </span>
                </div>
                
                <div class="interaction-pill">
                  <button type="button" class="i-btn like" :class="{ active: liked }" @click="handleLike" aria-label="点赞">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" class="heart-svg">
                      <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z" />
                    </svg>
                    <span>{{ likeCount }}</span>
                  </button>
                  <div class="i-divider"></div>
                  <button type="button" class="i-btn fav" :class="{ active: favorited }" @click="handleFavorite" aria-label="收藏">
                    <el-icon v-if="favorited"><StarFilled /></el-icon>
                    <el-icon v-else><Star /></el-icon>
                    <span>{{ favoriteCount }}</span>
                  </button>
                </div>
              </div>

              <div v-if="article.tags?.length" class="article-tags-cloud">
                <span v-for="tag in article.tags" :key="tag" class="refined-tag">#{{ tag }}</span>
              </div>
            </header>

            <div
              ref="contentRef"
              class="article-content-body markdown-body"
              v-html="renderedContent"
            ></div>
          </article>
        </section>
      </div>

      <el-empty v-else description="没能找到该文章" />
    </main>

    <Footer />
  </div>
</template>

<style scoped>
.page-container {
  min-height: 100vh;
}

.main-content {
  max-width: var(--shell-default);
  margin: 0 auto;
  padding: 40px var(--page-gutter) 80px;
  width: 100%;
}

.article-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 60px;
  align-items: start;
}

/* --- TOC Sidebar --- */
.toc-sidebar {
  position: relative;
}

.toc-sticky {
  position: sticky;
  top: 100px;
}

.toc-title {
  font-family: var(--heading);
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: var(--text-muted);
  margin-bottom: 24px;
}

.toc-list {
  list-style: none;
  padding: 0;
  margin: 0;
  border-left: 1.5px solid var(--border);
}

.toc-item {
  position: relative;
}

.toc-link {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: 100%;
  padding: 10px 16px;
  border: none;
  background: transparent;
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.4;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s;
  margin-left: -1.5px;
  border-left: 1.5px solid transparent;
}

.toc-link:hover {
  color: var(--text-h);
}

.toc-item.active .toc-link {
  color: var(--accent);
  border-left-color: var(--accent);
  background: var(--accent-bg);
}

.toc-item.level-3 .toc-link {
  padding-left: 32px;
  font-size: 13px;
}

/* --- Article Column --- */
.article-column {
  min-width: 0;
}

.article-nav {
  margin-bottom: 24px;
}

.refined-back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: var(--radius-full);
  border: 1px solid var(--border);
  background: #ffffff;
  color: var(--text-muted);
  font-size: 13px;
  font-family: var(--mono);
  cursor: pointer;
  transition: all 0.2s;
}

.refined-back-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  transform: translateX(-4px);
}

.article-viewer {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  padding: 60px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
}

.article-header {
  margin-bottom: 48px;
  padding-bottom: 32px;
  border-bottom: 1px solid var(--border);
}

.article-title {
  font-size: 40px;
  line-height: 1.25;
  margin-bottom: 24px;
  font-weight: 800;
  color: var(--text-h);
}

.article-meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-main {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-tag.category {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: var(--accent-bg);
  color: var(--accent);
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  font-family: var(--mono);
}

.meta-info {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--text-muted);
  font-size: 13px;
  font-family: var(--mono);
}

.interaction-pill {
  display: inline-flex;
  align-items: center;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: var(--radius-full);
  padding: 2px;
}

.i-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border: none;
  background: transparent;
  color: var(--text-muted);
  font-size: 13px;
  font-family: var(--mono);
  font-weight: 600;
  cursor: pointer;
  border-radius: var(--radius-full);
  transition: all 0.2s;
}

.i-btn:hover {
  color: var(--text-h);
}

.i-btn.active.like { color: #e05e5e; background: #fee; }
.i-btn.active.like .heart-svg { fill: currentColor; stroke: #e05e5e; }
.i-btn.active.fav { color: #e6a23c; background: #fff7e6; }

.i-divider {
  width: 1px;
  height: 16px;
  background: var(--border);
}

.article-tags-cloud {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

.refined-tag {
  color: var(--text-muted);
  font-size: 13px;
  font-family: var(--mono);
  opacity: 0.8;
}

/* --- Content Typography --- */
.article-content-body {
  font-size: 17px;
  line-height: 1.75;
  color: #333;
}

:deep(.markdown-body) {
  font-family: var(--sans);
}

:deep(.markdown-body h1, .markdown-body h2, .markdown-body h3) {
  font-family: var(--heading);
  margin-top: 2em;
  margin-bottom: 0.8em;
  color: var(--text-h);
}

:deep(.markdown-body blockquote) {
  border-left: 4px solid var(--accent);
  background: var(--accent-bg);
  padding: 16px 24px;
  margin: 2em 0;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  color: #555;
  font-style: italic;
}

:deep(.markdown-body pre) {
  background: #f8f9fa;
  border: 1px solid var(--border);
  padding: 24px;
  border-radius: var(--radius-lg);
  margin: 2em 0;
}

:deep(.markdown-body code) {
  background: var(--bg-elevated);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}

@media (max-width: 1024px) {
  .article-layout {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  .toc-sidebar { display: none; }
}

@media (max-width: 768px) {
  .main-content { padding: 20px 16px 60px; }
  .article-viewer { padding: 32px 20px; }
  .article-title { font-size: 28px; }
  .article-meta-row { flex-direction: column; align-items: flex-start; }
}
</style>
