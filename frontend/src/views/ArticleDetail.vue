<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Marked } from 'marked'
import { markedHighlight } from 'marked-highlight'
import hljs from 'highlight.js'
import DOMPurify from 'dompurify'
import { ElMessage } from 'element-plus'
import { getArticle, toggleLike, toggleFavorite, getInteractionStatus, getArticles } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import { ArrowLeft, Folder, Calendar, View, Document, Clock, Star, Close } from '@element-plus/icons-vue'

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

// Reading workbench state
const openedArticles = ref([])
const recentArticles = ref([])
const relatedArticles = ref([])
const activeTabId = ref(null)

const addToOpened = (art) => {
  if (!art || !art.id) return
  const existing = openedArticles.value.find(a => a.id === art.id)
  if (existing) {
    // Move to front
    openedArticles.value = [existing, ...openedArticles.value.filter(a => a.id !== art.id)]
  } else {
    openedArticles.value = [{ id: art.id, title: art.title, path: route.fullPath }, ...openedArticles.value]
  }
  // Keep max 8 articles
  if (openedArticles.value.length > 8) {
    openedArticles.value = openedArticles.value.slice(0, 8)
  }
  activeTabId.value = art.id
}

const closeOpenedArticle = (id, event) => {
  event.stopPropagation()
  const idx = openedArticles.value.findIndex(a => a.id === id)
  if (idx > -1) {
    openedArticles.value.splice(idx, 1)
    // If closed active, switch to first
    if (activeTabId.value === id) {
      activeTabId.value = openedArticles.value[0]?.id || null
    }
  }
}

const switchToArticle = (art) => {
  if (art.id === article.value?.id) return
  router.push(`/article/${art.id}`)
}

const fetchRelatedArticles = async (categoryId, excludeId) => {
  try {
    const res = await getArticles({ category: categoryId, size: 5, page: 1 })
    relatedArticles.value = ((res.data.data?.list || [])).filter(a => a.id != excludeId).slice(0, 5)
  } catch (e) {
    console.error('Failed to fetch related articles:', e)
  }
}

const fetchRecentArticles = async () => {
  try {
    const res = await getArticles({ size: 5, page: 1 })
    const list = (res.data.data?.list || []).slice(0, 5)
    recentArticles.value = list.map(a => ({ id: a.id, title: a.title }))
  } catch (e) {
    console.error('Failed to fetch recent articles:', e)
  }
}
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
    // Add to opened articles workbench
    if (article.value) {
      addToOpened(article.value)
      // Fetch related articles
      if (article.value.category) {
        fetchRelatedArticles(article.value.category, articleId)
      } else if (article.value.categoryId) {
        fetchRelatedArticles(article.value.categoryId, articleId)
      }
    }
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
  fetchRecentArticles()
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
      <el-button class="back-btn" @click="router.push('/')">
        <el-icon><ArrowLeft /></el-icon>
        返回首页
      </el-button>

      <el-skeleton :rows="10" animated v-if="loading" />

      <div v-else-if="article" class="content-layout">
        <!-- Left Reading Workbench -->
        <aside class="workbench-sidebar">
          <div class="workbench-section">
            <h3 class="workbench-title">
              <el-icon><Document /></el-icon>
              阅读工作台
            </h3>
            <div v-if="openedArticles.length" class="opened-tabs">
              <div
                v-for="tab in openedArticles"
                :key="tab.id"
                class="opened-tab"
                :class="{ active: tab.id === article.id }"
                @click="switchToArticle(tab)"
              >
                <span class="tab-title">{{ tab.title }}</span>
                <button class="tab-close" @click="closeOpenedArticle(tab.id, $event)">
                  <el-icon><Close /></el-icon>
                </button>
              </div>
            </div>
            <p v-else class="workbench-empty">暂无打开的文章</p>
          </div>

          <div class="workbench-section">
            <h3 class="workbench-title">
              <el-icon><Clock /></el-icon>
              最近阅读
            </h3>
            <ul v-if="recentArticles.length" class="recent-list">
              <li
                v-for="item in recentArticles"
                :key="item.id"
                class="recent-item"
                @click="switchToArticle(item)"
              >
                {{ item.title }}
              </li>
            </ul>
          </div>

          <div v-if="relatedArticles.length" class="workbench-section">
            <h3 class="workbench-title">
              <el-icon><Star /></el-icon>
              相关推荐
            </h3>
            <ul class="related-list">
              <li
                v-for="item in relatedArticles"
                :key="item.id"
                class="related-item"
                @click="switchToArticle(item)"
              >
                {{ item.title }}
              </li>
            </ul>
          </div>
        </aside>

        <article class="article-detail">
          <div class="article-corners" aria-hidden="true"></div>
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

            <div class="interaction-bar">
              <button class="interact-btn" :class="{ active: liked }" @click="handleLike">
                ❤ {{ likeCount }}
              </button>
              <button class="interact-btn favorite" :class="{ active: favorited }" @click="handleFavorite">
                ⭐ {{ favoriteCount }}
              </button>
            </div>
          </header>

          <div
            ref="contentRef"
            class="article-content markdown-body"
            v-html="renderedContent"
          ></div>
        </article>

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
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
  width: 100%;
  box-sizing: border-box;
}

.content-layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr) 280px;
  gap: 24px;
  align-items: flex-start;
}

.workbench-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.workbench-section {
  border: 1px solid rgba(0, 240, 255, 0.18);
  border-radius: 12px;
  background: linear-gradient(170deg, rgba(10, 20, 38, 0.78), rgba(8, 16, 31, 0.62));
  padding: 14px;
}

.workbench-title {
  margin: 0 0 12px;
  font-size: 14px;
  color: var(--text-h);
  letter-spacing: 0.8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.workbench-title .el-icon {
  color: var(--accent);
}

.workbench-empty {
  margin: 0;
  font-size: 13px;
  color: var(--text-dim);
  font-family: var(--mono);
}

.opened-tabs {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.opened-tab {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border: 1px solid rgba(0, 240, 255, 0.12);
  border-radius: 8px;
  background: rgba(0, 240, 255, 0.04);
  cursor: pointer;
  transition: all 0.2s ease;
}

.opened-tab:hover {
  border-color: var(--accent);
  background: rgba(0, 240, 255, 0.1);
}

.opened-tab.active {
  border-color: var(--accent);
  background: rgba(0, 240, 255, 0.16);
  box-shadow: inset 2px 0 0 0 var(--accent);
}

.tab-title {
  font-size: 13px;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  margin-right: 8px;
}

.tab-close {
  background: none;
  border: none;
  color: var(--text-dim);
  cursor: pointer;
  padding: 2px;
  display: flex;
  align-items: center;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.tab-close:hover {
  color: #ff6b6b;
  background: rgba(255, 107, 107, 0.12);
}

.recent-list,
.related-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.recent-item,
.related-item {
  font-size: 13px;
  color: var(--text);
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.recent-item:hover,
.related-item:hover {
  color: var(--accent);
  background: rgba(0, 240, 255, 0.08);
}

.back-btn {
  margin-bottom: 24px;
  background: linear-gradient(180deg, rgba(10, 23, 40, 0.88), rgba(8, 16, 31, 0.78));
  border: 1px solid var(--border);
  color: var(--text);
  font-family: var(--heading);
  letter-spacing: 0.6px;
}

.back-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  box-shadow: 0 0 14px rgba(0, 240, 255, 0.22);
}

.article-detail {
  animation: fadeIn 0.5s ease;
  border: 1px solid rgba(0, 240, 255, 0.18);
  border-radius: 16px;
  padding: 30px;
  background: linear-gradient(170deg, rgba(10, 20, 38, 0.82), rgba(8, 16, 31, 0.58));
  box-shadow: inset 0 0 0 1px rgba(0, 240, 255, 0.04), var(--shadow);
  position: relative;
  overflow: hidden;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.article-header {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border);
  position: relative;
  z-index: 1;
}

.article-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-h);
  margin: 0 0 16px;
  line-height: 1.3;
  letter-spacing: 1px;
  text-shadow: 0 0 14px rgba(0, 240, 255, 0.16);
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
  font-family: var(--mono);
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
  border: 1px solid rgba(0, 240, 255, 0.25);
  font-family: var(--mono);
}

.article-content {
  font-size: 16px;
  line-height: 1.9;
  color: var(--text);
  position: relative;
  z-index: 1;
}

.interaction-bar {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.interact-btn {
  border: 1px solid rgba(0, 240, 255, 0.38);
  background: rgba(0, 240, 255, 0.06);
  color: var(--accent);
  font-family: var(--mono);
  border-radius: 10px;
  padding: 8px 14px;
  cursor: pointer;
  letter-spacing: 0.6px;
  transition: all 0.24s ease;
}

.interact-btn:hover {
  border-color: var(--accent);
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.26);
}

.interact-btn.active {
  color: #ffe8ef;
  background: linear-gradient(135deg, rgba(255, 67, 112, 0.86), rgba(196, 22, 67, 0.88));
  border-color: rgba(255, 83, 124, 0.95);
  box-shadow: 0 0 16px rgba(255, 59, 108, 0.4), inset 0 0 10px rgba(255, 210, 224, 0.16);
}

.interact-btn.favorite.active {
  color: #fff4dc;
  background: linear-gradient(135deg, rgba(255, 184, 57, 0.9), rgba(223, 129, 0, 0.86));
  border-color: rgba(255, 191, 74, 0.96);
  box-shadow: 0 0 16px rgba(255, 185, 69, 0.42), inset 0 0 10px rgba(255, 240, 192, 0.18);
}

.toc-sidebar {
  position: sticky;
  top: 88px;
  border: 1px solid rgba(0, 240, 255, 0.18);
  border-radius: 12px;
  background: linear-gradient(170deg, rgba(10, 20, 38, 0.78), rgba(8, 16, 31, 0.62));
  padding: 14px 12px;
}

.toc-title {
  margin: 0 0 10px;
  font-size: 16px;
  color: var(--text-h);
  letter-spacing: 0.8px;
}

.toc-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.toc-item {
  margin-bottom: 4px;
}

.toc-item.level-2 .toc-link {
  padding-left: 16px;
}

.toc-item.level-3 .toc-link {
  padding-left: 28px;
}

.toc-link {
  width: 100%;
  text-align: left;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text);
  font-family: var(--mono);
  font-size: 13px;
  line-height: 1.4;
  padding: 8px 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toc-link:hover {
  color: var(--accent-hover);
  background: rgba(0, 240, 255, 0.08);
}

.toc-item.active .toc-link {
  color: var(--accent);
  background: rgba(0, 240, 255, 0.14);
  box-shadow: inset 2px 0 0 0 var(--accent);
}

.article-corners {
  position: absolute;
  inset: 10px;
  pointer-events: none;
}

.article-corners::before,
.article-corners::after {
  content: '';
  position: absolute;
  width: 28px;
  height: 18px;
  border-top: 1px solid var(--accent-border);
  border-left: 1px solid var(--accent-border);
  opacity: 0.66;
}

.article-corners::before {
  left: 0;
  top: 0;
}

.article-corners::after {
  right: 0;
  bottom: 0;
  transform: rotate(180deg);
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
  font-weight: 700;
  font-family: var(--heading);
  letter-spacing: 0.8px;
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
  border: 1px solid rgba(0, 240, 255, 0.18);
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
  box-shadow: inset 0 0 0 1px rgba(0, 240, 255, 0.12);
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
  .content-layout {
    grid-template-columns: 1fr;
  }

  .workbench-sidebar {
    display: none;
  }

  .toc-sidebar {
    display: none;
  }

  .article-title {
    font-size: 28px;
  }
  
  .main-content {
    padding: 24px 16px;
  }

  .article-detail {
    padding: 20px;
  }
}
</style>
