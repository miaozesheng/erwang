<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { getArticles, getCategories, getTags, getInteractionStatuses } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import ArticleCard from '../components/ArticleCard.vue'

const articles = ref([])
const loading = ref(false)
const featuredArticles = ref([])
const categories = ref([])
const tags = ref([])
const keyword = ref('')
const selectedCategory = ref('')
const selectedTag = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const heroParticlesRef = ref(null)
const dataColumns = Array.from({ length: 18 }, (_, index) => index)

const terminalText = '> SYSTEM ONLINE_ TECH FEED INITIALIZED'
const typedTerminalText = ref('')

const useTypewriter = (sourceText, targetRef, speed = 64) => {
  let index = 0
  const timer = setInterval(() => {
    targetRef.value = sourceText.slice(0, index + 1)
    index += 1
    if (index >= sourceText.length) {
      clearInterval(timer)
    }
  }, speed)
  return timer
}

const createHeroParticles = () => {
  const container = heroParticlesRef.value
  if (!container) return

  const particleCount = 5
  for (let i = 0; i < particleCount; i += 1) {
    const particle = document.createElement('span')
    particle.className = 'hero-particle'
    const size = 2 + Math.random() * 6
    const opacity = 0.3 + Math.random() * 0.6
    particle.style.left = `${Math.random() * 100}%`
    particle.style.top = `${Math.random() * 100}%`
    particle.style.width = `${size}px`
    particle.style.height = `${size}px`
    particle.style.opacity = `${opacity}`
    particle.style.setProperty('--float-x', `${-12 + Math.random() * 24}px`)
    particle.style.setProperty('--float-y', `${-20 - Math.random() * 45}px`)
    particle.style.setProperty('--particle-scale', `${0.8 + Math.random() * 0.6}`)
    particle.style.animationDelay = `${Math.random() * 4}s`
    particle.style.animationDuration = `${5 + Math.random() * 6}s`
    container.appendChild(particle)
  }
}

let typewriterTimer = null

const normalizeFilterItems = (items) => {
  if (!Array.isArray(items)) return []
  return items
    .map((item) => {
      if (typeof item === 'string') return item
      if (item?.name) return item.name
      if (item?.category) return item.category
      if (item?.tag) return item.tag
      if (item?.title) return item.title
      return ''
    })
    .filter(Boolean)
}

const fetchArticles = async () => {
  loading.value = true
  try {
    const res = await getArticles({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      category: selectedCategory.value || undefined,
      tag: selectedTag.value || undefined
    })
    const data = res.data?.data || {}
    articles.value = data.list || data || []
    total.value = Number(data.total ?? (Array.isArray(articles.value) ? articles.value.length : 0))
    currentPage.value = Number(data.page ?? currentPage.value)
    pageSize.value = Number(data.size ?? pageSize.value)
    const topArticles = articles.value.filter(a => a.is_top || a.isTop)
    featuredArticles.value = topArticles.length > 0 ? topArticles.slice(0, 3) : articles.value.slice(0, 3)
    
    if (articles.value.length > 0 && localStorage.getItem('token')) {
      try {
        const ids = articles.value.map(a => a.id)
        const statusRes = await getInteractionStatuses(ids)
        const statuses = statusRes.data?.data || {}
        articles.value = articles.value.map(a => ({
          ...a,
          _interaction: statuses[a.id] || {}
        }))
      } catch (e) {
        console.error('Failed to fetch interaction statuses:', e)
      }
    }
  } catch (e) {
    console.error('Failed to fetch articles:', e)
  } finally {
    loading.value = false
  }
}

const fetchFilters = async () => {
  try {
    const [categoryRes, tagRes] = await Promise.all([getCategories(), getTags()])
    categories.value = normalizeFilterItems(categoryRes.data?.data || [])
    tags.value = normalizeFilterItems(tagRes.data?.data || [])
  } catch (e) {
    console.error('Failed to fetch filters:', e)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchArticles()
}

const handleCategoryFilter = (category) => {
  selectedCategory.value = selectedCategory.value === category ? '' : category
  currentPage.value = 1
  fetchArticles()
}

const handleTagFilter = (tag) => {
  selectedTag.value = selectedTag.value === tag ? '' : tag
  currentPage.value = 1
  fetchArticles()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchArticles()
}

const hasActiveFilters = computed(() => {
  return Boolean(keyword.value || selectedCategory.value || selectedTag.value)
})

const clearAllFilters = () => {
  keyword.value = ''
  selectedCategory.value = ''
  selectedTag.value = ''
  currentPage.value = 1
  fetchArticles()
}

const featuredCount = computed(() => featuredArticles.value.length)
const totalArticleCount = computed(() => total.value)
const categoryCount = computed(() => categories.value.length)
const tagCount = computed(() => tags.value.length)
const currentYear = new Date().getFullYear()

onMounted(() => {
  typewriterTimer = useTypewriter(terminalText, typedTerminalText)
  createHeroParticles()
  fetchFilters()
  fetchArticles()
})

onUnmounted(() => {
  if (typewriterTimer) {
    clearInterval(typewriterTimer)
  }
})
</script>

<template>
  <div class="page-container">
    <Header />

    <main class="main-content">
      <section class="hero-section">
        <div class="hero-grid" aria-hidden="true"></div>
        <div ref="heroParticlesRef" class="hero-particles" aria-hidden="true"></div>

        <div class="hero-body">
          <span class="hero-kicker">&gt; /erwang/tech-notes</span>
          <h1 class="hero-title">Erwang Blog</h1>
          <p class="hero-subtitle">记录技术探索与编程实践</p>
          <div class="hero-terminal" aria-label="terminal status">
            <span class="terminal-prompt">$</span>
            <span class="terminal-text">{{ typedTerminalText }}</span>
            <span class="typing-cursor terminal-cursor" aria-hidden="true"></span>
          </div>
        </div>
      </section>

      <section class="feed-shell">
        <div class="content-layout">
          <aside class="sidebar-panel">
            <section class="sidebar-section">
              <h2 class="sidebar-title">分类</h2>
              <div class="sidebar-pills">
                <button
                  type="button"
                  class="sidebar-pill"
                  :class="{ active: !selectedCategory }"
                  @click="handleCategoryFilter('')"
                >
                  全部
                </button>
                <button
                  v-for="category in categories"
                  :key="category"
                  type="button"
                  class="sidebar-pill"
                  :class="{ active: selectedCategory === category }"
                  @click="handleCategoryFilter(category)"
                >
                  {{ category }}
                </button>
              </div>
            </section>

            <section class="sidebar-section">
              <h2 class="sidebar-title">标签</h2>
              <div class="sidebar-pills">
                <button
                  type="button"
                  class="sidebar-pill"
                  :class="{ active: !selectedTag }"
                  @click="handleTagFilter('')"
                >
                  全部
                </button>
                <button
                  v-for="tag in tags"
                  :key="tag"
                  type="button"
                  class="sidebar-pill"
                  :class="{ active: selectedTag === tag }"
                  @click="handleTagFilter(tag)"
                >
                  # {{ tag }}
                </button>
              </div>
            </section>

            <button v-if="hasActiveFilters" type="button" class="clear-filter-btn" @click="clearAllFilters">
              清除全部筛选
            </button>
          </aside>

          <section class="content-panel">
            <div class="search-bar">
              <el-input
                v-model="keyword"
                class="search-input"
                placeholder="搜索文章关键词"
                clearable
                @keyup.enter="handleSearch"
                @clear="handleSearch"
              >
                <template #append>
                  <el-button class="search-btn" @click="handleSearch">检索</el-button>
                </template>
              </el-input>
            </div>

            <section class="timeline-section">
              <el-skeleton :rows="5" animated v-if="loading" />

              <div v-else-if="articles.length" class="timeline-list">
                <article v-for="article in articles" :key="article.id" class="timeline-row">
                  <div class="timeline-date">
                    <span class="date-month">
                      {{ article.created_at ? new Date(article.created_at).toLocaleDateString('en-US', { month: 'short' }).toUpperCase() : '--' }}
                    </span>
                    <span class="date-day">
                      {{ article.created_at ? new Date(article.created_at).toLocaleDateString('en-US', { day: '2-digit' }) : '--' }}
                    </span>
                    <span class="date-year">
                      {{ article.created_at ? new Date(article.created_at).toLocaleDateString('en-US', { year: 'numeric' }) : '----' }}
                    </span>
                  </div>

                  <div class="timeline-axis" aria-hidden="true">
                    <span class="timeline-dot"></span>
                  </div>

                  <div class="timeline-card">
                    <ArticleCard :article="article" />
                  </div>
                </article>
              </div>

              <div v-else class="empty-state">
                <div class="empty-icon">◈</div>
                <p class="empty-title">{{ keyword || selectedCategory || selectedTag ? '未找到相关内容' : '暂无文章' }}</p>
                <p class="empty-desc">{{ keyword || selectedCategory || selectedTag ? '试试其他关键词或清除筛选条件' : '期待第一篇文章的到来' }}</p>
              </div>

              <div class="pagination-wrap" v-if="total > 0">
                <el-pagination
                  background
                  layout="total, prev, pager, next"
                  :current-page="currentPage"
                  :page-size="pageSize"
                  :total="total"
                  @current-change="handlePageChange"
                />
              </div>
            </section>
          </section>
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
}

.main-content {
  --space-1: 8px;
  --space-2: 12px;
  --space-3: 16px;
  --space-4: 24px;
  --space-5: 32px;
  --space-6: 40px;
  flex: 1;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 clamp(14px, 2.8vw, 32px);
}

.hero-section {
  position: relative;
  margin-top: 20px;
  min-height: 240px;
  max-height: 280px;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  background: #ffffff;
  overflow: hidden;
}

.hero-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.06;
  background-image:
    linear-gradient(rgba(0, 0, 0, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 0, 0, 0.08) 1px, transparent 1px);
  background-size: 28px 28px;
}

.hero-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero-body {
  position: relative;
  z-index: 1;
  height: 100%;
  min-height: 240px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  gap: var(--space-2);
  padding: clamp(26px, 4.5vw, 40px) var(--space-4);
}

.hero-kicker {
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 1.4px;
  color: var(--accent);
  opacity: 0.9;
}

.hero-title {
  margin: 0;
  font-family: var(--heading);
  font-size: clamp(34px, 6vw, 54px);
  line-height: 1.08;
  letter-spacing: -0.02em;
  color: var(--text-h);
}

.hero-subtitle {
  margin: 0;
  color: var(--text);
  font-size: clamp(16px, 2.1vw, 19px);
  letter-spacing: 0.2px;
}

.hero-terminal {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: #ffffff;
  font-family: var(--mono);
  font-size: 12px;
}

.terminal-prompt {
  color: var(--accent);
}

.terminal-text {
  color: var(--text);
  letter-spacing: 0.6px;
}

.terminal-cursor {
  margin-left: -3px;
}

.feed-shell {
  width: 100%;
  margin: var(--space-5) auto var(--space-6);
}

.content-layout {
  display: flex;
  align-items: flex-start;
  gap: 32px;
}

.sidebar-panel {
  width: 240px;
  flex: 0 0 240px;
  position: sticky;
  top: 80px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: #ffffff;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar-title {
  margin: 0;
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--accent);
}

.sidebar-pills {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.sidebar-pill {
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text);
  border-radius: 999px;
  padding: 5px 11px;
  font-size: 11px;
  font-family: var(--mono);
  letter-spacing: 0.25px;
  cursor: pointer;
  white-space: nowrap;
  transition: border-color 0.22s ease, color 0.22s ease, background-color 0.22s ease;
}

.sidebar-pill:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.sidebar-pill.active {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--accent-bg);
}

.clear-filter-btn {
  width: fit-content;
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text);
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 11px;
  font-family: var(--mono);
  cursor: pointer;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}

.clear-filter-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

:deep(.search-input .el-input__wrapper) {
  border-radius: var(--radius-md);
  border-color: var(--border);
  background: #ffffff !important;
  box-shadow: none;
}

:deep(.search-input .el-input__wrapper.is-focus) {
  border-color: var(--accent);
}

:deep(.search-input .el-input-group__append) {
  border-color: var(--border);
  background: transparent;
}

.search-btn {
  border: none;
  background: var(--accent);
  color: #ffffff;
  font-family: var(--heading);
  letter-spacing: 0.3px;
}

.content-panel {
  flex: 1;
  min-width: 0;
}

.search-bar {
  margin-bottom: var(--space-4);
}

.timeline-section {
  position: relative;
}

.timeline-list {
  --date-col: 108px;
  --axis-col: 52px;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.timeline-list::before {
  content: '';
  position: absolute;
  top: 4px;
  bottom: 4px;
  left: calc(var(--date-col) + (var(--axis-col) / 2));
  width: 1px;
  background: var(--border);
  transform: translateX(-0.5px);
}

.timeline-row {
  display: grid;
  grid-template-columns: var(--date-col) var(--axis-col) minmax(0, 1fr);
  align-items: start;
  column-gap: 0;
}

.timeline-date {
  padding-top: 12px;
  text-align: right;
  font-family: var(--mono);
  line-height: 1.1;
  display: flex;
  flex-direction: column;
  gap: 3px;
  color: var(--text);
}

.date-month {
  font-size: 11px;
  opacity: 0.82;
  letter-spacing: 1px;
}

.date-day {
  font-size: 20px;
  color: var(--text-h);
}

.date-year {
  font-size: 11px;
  opacity: 0.58;
}

.timeline-axis {
  display: flex;
  justify-content: center;
  padding-top: 18px;
}

.timeline-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent);
  transition: transform 0.25s ease;
}

.timeline-row:hover .timeline-dot {
  transform: scale(1.1);
}

.timeline-row:hover .timeline-dot {
  transform: scale(1.12);
  box-shadow: 0 0 0 4px rgba(0, 240, 255, 0.16), 0 0 12px rgba(0, 240, 255, 0.35);
}

.timeline-card {
  min-width: 0;
}

.empty-state {
  text-align: center;
  padding: 56px 24px;
}

.empty-icon {
  font-size: 40px;
  color: var(--accent);
  margin-bottom: 12px;
  opacity: 0.68;
}

.empty-title {
  margin: 0 0 8px;
  font-size: 18px;
  color: var(--text-h);
  font-family: var(--heading);
}

.empty-desc {
  margin: 0;
  font-size: 14px;
  color: var(--text);
  opacity: 0.78;
}

.pagination-wrap {
  margin-top: 28px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .el-pager li) {
  background: #ffffff;
  border: 1px solid var(--border);
  color: var(--text);
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--accent-bg);
  color: var(--accent);
  border-color: var(--accent);
}

:deep(.timeline-card .article-card) {
  min-height: 100%;
}

:deep(.timeline-card .article-card:hover) {
  transform: translateY(-2px);
}

:deep(.hero-particle) {
  position: absolute;
  border-radius: 999px;
  background: var(--border);
  animation-name: hero-particle-float;
  animation-timing-function: cubic-bezier(0.22, 1, 0.36, 1);
  animation-iteration-count: infinite;
}

@keyframes hero-particle-float {
  0% {
    transform: translate3d(0, 0, 0) scale(1);
    opacity: 0;
  }
  18% {
    opacity: var(--particle-opacity, 0.65);
  }
  100% {
    transform: translate3d(var(--float-x), var(--float-y), 0) scale(var(--particle-scale));
    opacity: 0;
  }
}

@media (max-width: 1024px) {
  .content-layout {
    gap: 24px;
  }

  .sidebar-panel {
    width: 220px;
    flex-basis: 220px;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 0 14px;
  }

  .hero-section {
    min-height: 210px;
    max-height: 260px;
    border-radius: 12px;
  }

  .hero-body {
    min-height: 210px;
    padding: 22px 14px;
  }

  .hero-kicker {
    font-size: 11px;
  }

  .hero-terminal {
    font-size: 11px;
    width: 100%;
    max-width: 100%;
    justify-content: center;
  }

  .feed-shell {
    margin-top: 24px;
  }

  .content-layout {
    flex-direction: column;
    gap: 16px;
  }

  .sidebar-panel {
    width: 100%;
    flex-basis: auto;
    position: static;
    top: auto;
  }

  .sidebar-section {
    gap: 8px;
  }

  .sidebar-pills {
    flex-wrap: nowrap;
    overflow-x: auto;
    padding-bottom: 2px;
  }

  .clear-filter-btn {
    align-self: flex-start;
  }

  .timeline-list {
    gap: 14px;
  }

  .timeline-list::before,
  .timeline-date,
  .timeline-axis {
    display: none;
  }

  .timeline-row {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (prefers-reduced-motion: reduce) {
  .timeline-dot,
  :deep(.hero-particle) {
    animation: none;
    transition: none;
  }
}
</style>
