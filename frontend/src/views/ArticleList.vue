<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { getArticles, getCategories, getTags } from '../api'
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

  const particleCount = 8
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
      <section class="hero-section hud-corners">
        <div class="hero-grid" aria-hidden="true"></div>
        <div ref="heroParticlesRef" class="hero-particles" aria-hidden="true"></div>
        <div class="hero-data-rain" aria-hidden="true">
          <span
            v-for="column in dataColumns"
            :key="column"
            class="data-column"
            :style="{
              left: `${(column / (dataColumns.length - 1)) * 100}%`,
              '--stream-delay': `${(column % 7) * 0.3}s`,
              '--stream-duration': `${5.4 + (column % 5) * 1.1}s`,
              '--stream-opacity': `${0.2 + (column % 4) * 0.12}`
            }"
          ></span>
        </div>
        <div class="hero-layout">
          <div class="hero-content">
            <span class="hero-kicker">&gt; /erwang/tech-notes</span>
            <h1 class="hero-title">
              <span class="title-prefix">◈</span>
              <span class="title-text">Erwang Blog</span>
              <span class="title-sub">二王博客</span>
            </h1>
            <p class="hero-subtitle">记录技术探索与编程实践</p>
            <div class="hero-meta">
              <span class="hero-status">持续更新中 · 技术文章 / 编程实践 / 思考记录</span>
            </div>
            <div class="hero-terminal" aria-label="terminal status">
              <span class="terminal-prompt">$</span>
              <span class="terminal-text">{{ typedTerminalText }}</span>
              <span class="typing-cursor terminal-cursor" aria-hidden="true"></span>
            </div>
          </div>
          <div class="hero-hud-panel">
            <div class="hud-header">
              <span class="hud-title">快捷操作</span>
            </div>
            <div class="hud-content">
              <div class="hud-quick-nav">
                <button 
                  class="hud-nav-btn"
                  @click="currentPage = 1; fetchArticles()"
                >
                  查看全部
                </button>
                <button 
                  class="hud-nav-btn"
                  @click="selectedCategory = ''; selectedTag = ''; currentPage = 1; fetchArticles()"
                >
                  清除筛选
                </button>
              </div>
              <div class="hud-filter-status" v-if="hasActiveFilters">
                <span class="filter-status-label">当前筛选:</span>
                <div class="filter-active-tags">
                  <span v-if="keyword" class="active-tag">{{ keyword }}</span>
                  <span v-if="selectedCategory" class="active-tag">{{ selectedCategory }}</span>
                  <span v-if="selectedTag" class="active-tag">#{{ selectedTag }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="hero-glow"></div>
      </section>

      <section v-if="featuredArticles.length" class="featured-section">
        <h2 class="section-title section-title-neon">推荐文章</h2>
        <div class="featured-grid">
          <ArticleCard 
            v-for="article in featuredArticles" 
            :key="article.id" 
            :article="article"
          />
        </div>
      </section>

      <div class="articles-layout">
        <aside class="articles-sidebar-left">
          <!-- 博客概览 -->
          <div class="info-card info-card-overview">
            <div class="info-card-header">
              <span class="info-card-icon">◈</span>
              <span class="info-card-title">博客概览</span>
            </div>
            <div class="overview-stats">
              <div class="overview-stat">
                <span class="overview-value">{{ totalArticleCount }}</span>
                <span class="overview-label">文章</span>
              </div>
              <div class="overview-stat">
                <span class="overview-value">{{ featuredCount }}</span>
                <span class="overview-label">精选</span>
              </div>
              <div class="overview-stat">
                <span class="overview-value">{{ categoryCount }}</span>
                <span class="overview-label">分类</span>
              </div>
              <div class="overview-stat">
                <span class="overview-value">{{ tagCount }}</span>
                <span class="overview-label">标签</span>
              </div>
            </div>
          </div>
          
          <!-- 分类统计 -->
          <div class="info-card info-card-category-stats">
            <div class="info-card-header">
              <span class="info-card-icon">▤</span>
              <span class="info-card-title">分类统计</span>
            </div>
            <div class="category-stats-list">
              <div 
                v-for="(category, index) in categories" 
                :key="category"
                class="category-stat-item"
                :class="{ active: selectedCategory === category }"
                @click="handleCategoryFilter(category)"
              >
                <span class="category-name">{{ category }}</span>
                <span class="category-bar">
                  <span class="category-bar-fill" :style="{ width: `${30 + (index * 15)}%` }"></span>
                </span>
              </div>
              <div v-if="categories.length === 0" class="info-card-empty">加载中...</div>
            </div>
          </div>
          
          <!-- 热门文章 -->
          <div class="info-card info-card-hot">
            <div class="info-card-header">
              <span class="info-card-icon">★</span>
              <span class="info-card-title">热门文章</span>
            </div>
            <ul class="hot-articles-list">
              <li 
                v-for="article in (featuredArticles.length ? featuredArticles : articles.slice(0, 5))" 
                :key="article.id"
                class="hot-article-item"
              >
                <span class="hot-article-title">{{ article.title }}</span>
                <span class="hot-article-meta" v-if="article.category">{{ article.category }} · {{ article.views || 0 }}阅读</span>
              </li>
              <li v-if="!featuredArticles.length && !articles.length" class="info-card-empty">暂无文章</li>
            </ul>
          </div>
        </aside>

        <section class="articles-section">
          <div class="articles-hex-grid" aria-hidden="true"></div>
          <h2 class="section-title section-title-neon">全部文章</h2>

          <div class="search-section">
            <div class="search-panel-corners" aria-hidden="true"></div>

            <div class="search-wrap">
              <span class="search-prefix" aria-hidden="true">&gt;</span>
              <el-input
                v-model="keyword"
                class="search-input"
                placeholder="输入关键词检索文章..."
                clearable
                @keyup.enter="handleSearch"
                @clear="handleSearch"
              >
                <template #suffix>
                  <span class="search-caret" aria-hidden="true"></span>
                </template>
                <template #append>
                  <el-button class="search-btn" @click="handleSearch">检索</el-button>
                </template>
              </el-input>
            </div>

            <div class="filter-group">
              <span class="filter-label">分类</span>
              <div class="filter-pills">
                <button
                  type="button"
                  class="filter-pill"
                  :class="{ active: !selectedCategory }"
                  @click="handleCategoryFilter('')"
                >
                  全部
                </button>
                <button
                  v-for="category in categories"
                  :key="category"
                  type="button"
                  class="filter-pill"
                  :class="{ active: selectedCategory === category }"
                  @click="handleCategoryFilter(category)"
                >
                  {{ category }}
                </button>
              </div>
            </div>

            <div class="filter-group">
              <span class="filter-label">标签</span>
              <div class="filter-pills">
                <button
                  type="button"
                  class="filter-pill"
                  :class="{ active: !selectedTag }"
                  @click="handleTagFilter('')"
                >
                  全部
                </button>
                <button
                  v-for="tag in tags"
                  :key="tag"
                  type="button"
                  class="filter-pill"
                  :class="{ active: selectedTag === tag }"
                  @click="handleTagFilter(tag)"
                >
                  # {{ tag }}
                </button>
              </div>
            </div>

            <div v-if="hasActiveFilters" class="filter-actions">
              <button type="button" class="clear-filter-btn" @click="clearAllFilters">
                清除全部筛选
              </button>
            </div>
          </div>

          <el-skeleton :rows="5" animated v-if="loading" />
          <div v-else-if="articles.length" class="articles-grid">
            <ArticleCard 
              v-for="article in articles" 
              :key="article.id" 
              :article="article"
            />
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
      </div>
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
  max-width: 1680px;
  margin: 0 auto;
  padding: 0 32px;
  width: 100%;
  box-sizing: border-box;
}

.hero-section {
  position: relative;
  padding: 56px 0 48px;
  overflow: hidden;
  border: 1px solid rgba(0, 240, 255, 0.12);
  border-radius: 16px;
  margin-top: 20px;
  background: linear-gradient(180deg, rgba(10, 20, 38, 0.52) 0%, rgba(8, 16, 31, 0.28) 100%);
}

/* Hero Layout - Left Content + Right HUD */
.hero-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 56px;
  position: relative;
  z-index: 1;
  align-items: center;
  padding: 0 32px;
}

.hero-content {
  text-align: left;
}

.hero-title {
  font-size: 52px;
  font-weight: 700;
  color: var(--text-h);
  margin: 0 0 14px;
  letter-spacing: 1.8px;
  text-shadow: 0 0 24px rgba(0, 240, 255, 0.18);
  line-height: 1.15;
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.hero-title .title-prefix {
  font-size: 36px;
  color: var(--accent);
  text-shadow: 0 0 20px rgba(0, 240, 255, 0.5);
  line-height: 1;
}

.hero-title .title-text {
  font-size: 48px;
  font-weight: 700;
  background: linear-gradient(90deg, var(--text-h) 0%, var(--accent) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-title .title-sub {
  font-size: 20px;
  color: var(--text);
  opacity: 0.5;
  font-weight: 400;
  margin-left: 8px;
}

.hero-kicker {
  display: block;
  font-family: var(--mono);
  font-size: 12px;
  color: rgba(114, 255, 220, 0.92);
  letter-spacing: 1.8px;
  margin-bottom: 12px;
  opacity: 0.82;
  text-transform: uppercase;
  text-shadow: 0 0 14px rgba(114, 255, 220, 0.24);
}

.hero-subtitle {
  font-size: 17px;
  color: var(--text);
  margin: 0 0 12px;
  opacity: 0.8;
  letter-spacing: 0.45px;
}

.hero-meta {
  display: flex;
  align-items: center;
  margin-bottom: 18px;
}

.hero-status {
  color: rgba(233, 248, 255, 0.62);
  font-family: var(--mono);
  font-size: 11px;
  letter-spacing: 1px;
  opacity: 0.92;
}

.hero-terminal {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  padding: 11px 14px;
  border: 1px solid rgba(114, 255, 220, 0.16);
  border-radius: 10px;
  background: linear-gradient(180deg, rgba(7, 14, 20, 0.88), rgba(10, 20, 38, 0.58));
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.03), 0 12px 28px rgba(0, 0, 0, 0.22);
  font-family: var(--mono);
}

.terminal-prompt {
  color: rgba(114, 255, 220, 0.95);
  font-size: 13px;
  text-shadow: 0 0 12px rgba(114, 255, 220, 0.38);
}

.terminal-text {
  color: rgba(233, 248, 255, 0.84);
  font-size: 12px;
  letter-spacing: 0.9px;
}

.terminal-cursor {
  margin-left: -4px;
}

.hero-glow {
  position: absolute;
  top: 50%;
  left: 36%;
  transform: translate(-50%, -50%);
  width: 380px;
  height: 380px;
  background: radial-gradient(circle, rgba(0, 240, 255, 0.12) 0%, transparent 72%);
  pointer-events: none;
}

.hero-hud-panel {
  background: linear-gradient(165deg, rgba(10, 20, 38, 0.95) 0%, rgba(8, 16, 31, 0.82) 100%);
  border: 1px solid rgba(114, 255, 220, 0.14);
  border-radius: 8px;
  padding: 20px;
  position: relative;
  backdrop-filter: blur(18px) saturate(150%);
}

.hero-hud-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: inherit;
  pointer-events: none;
}

.hud-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.1);
}

.hud-title {
  font-family: var(--mono);
  font-size: 12px;
  color: rgba(114, 255, 220, 0.9);
  letter-spacing: 1.8px;
  text-transform: uppercase;
}

.hud-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hud-blog-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 8px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 6px;
  transition: all 0.2s ease;
}

.summary-item:hover {
  border-color: rgba(114, 255, 220, 0.2);
  background: rgba(114, 255, 220, 0.05);
}

.summary-value {
  font-family: var(--mono);
  font-size: 22px;
  font-weight: 700;
  color: var(--text-h);
}

.summary-label {
  font-size: 10px;
  color: var(--text);
  opacity: 0.6;
  letter-spacing: 0.5px;
}

.hud-quick-nav {
  display: flex;
  gap: 10px;
}

.hud-nav-btn {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.025);
  color: var(--text);
  border-radius: 6px;
  font-size: 12px;
  font-family: var(--mono);
  cursor: pointer;
  transition: all 0.2s ease;
}

.hud-nav-btn:hover {
  border-color: rgba(114, 255, 220, 0.24);
  color: rgba(114, 255, 220, 0.96);
  background: rgba(114, 255, 220, 0.06);
}

.hud-filter-status {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-status-label {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--text);
  opacity: 0.5;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.filter-active-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.active-tag {
  padding: 4px 8px;
  background: rgba(0, 240, 255, 0.08);
  border: 1px solid rgba(0, 240, 255, 0.16);
  border-radius: 4px;
  font-size: 11px;
  font-family: var(--mono);
  color: var(--accent);
}

.hud-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent-secondary);
  box-shadow: 0 0 8px var(--accent-secondary);
  animation: hud-status-blink 2s ease-in-out infinite;
}

/* New HUD blog content styles */
.hud-blog-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 8px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 6px;
  transition: all 0.2s ease;
}

.summary-item:hover {
  border-color: rgba(114, 255, 220, 0.2);
  background: rgba(114, 255, 220, 0.05);
}

.summary-icon {
  font-size: 15px;
  color: rgba(114, 255, 220, 0.88);
  text-shadow: 0 0 10px rgba(114, 255, 220, 0.3);
}

.summary-text {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.summary-value {
  font-family: var(--mono);
  font-size: 22px;
  font-weight: 700;
  color: var(--text-h);
  line-height: 1;
}

.summary-label {
  font-size: 10px;
  color: var(--text);
  opacity: 0.6;
  letter-spacing: 0.5px;
}

.hud-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 240, 255, 0.12), transparent);
  margin: 16px 0;
}

.hud-quick-nav {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quick-nav-title {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--text);
  opacity: 0.5;
  letter-spacing: 0.6px;
  text-transform: uppercase;
}

.quick-nav-items {
  display: flex;
  gap: 10px;
}

.quick-nav-btn {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid rgba(0, 240, 255, 0.16);
  background: rgba(0, 240, 255, 0.04);
  color: var(--text);
  border-radius: 6px;
  font-size: 11px;
  font-family: var(--mono);
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-nav-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: rgba(0, 240, 255, 0.12);
}

.hud-filter-status {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-status-title {
  font-family: var(--mono);
  font-size: 11px;
  color: var(--text);
  opacity: 0.6;
}

.filter-active-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.active-tag {
  padding: 4px 8px;
  background: rgba(0, 240, 255, 0.1);
  border: 1px solid rgba(0, 240, 255, 0.24);
  border-radius: 4px;
  font-size: 11px;
  font-family: var(--mono);
  color: var(--accent);
}

.hud-chrome-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
  margin-top: 12px;
}

@keyframes hud-status-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-h);
  margin: 0 0 22px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.1);
  letter-spacing: 1px;
}

.section-title-neon {
  display: flex;
  align-items: center;
  gap: 14px;
  border-bottom: none;
  padding-bottom: 0;
  text-shadow: 0 0 12px rgba(0, 240, 255, 0.22);
}

.section-title-neon::before,
.section-title-neon::after {
  content: '';
  height: 1px;
  flex: 1;
  background:
    linear-gradient(90deg, transparent, rgba(0, 240, 255, 0.6), transparent),
    repeating-linear-gradient(
      90deg,
      rgba(0, 240, 255, 0.06) 0 8px,
      rgba(0, 240, 255, 0.32) 8px 12px,
      transparent 12px 18px
    );
  box-shadow: 0 0 8px rgba(0, 240, 255, 0.16);
  animation: neon-line-stream 1.2s linear infinite;
}

.featured-section {
  margin-bottom: 48px;
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.articles-section {
  margin-bottom: 48px;
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  padding: 10px 14px 4px;
}

.articles-hex-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    radial-gradient(circle at 25% 22%, rgba(0, 240, 255, 0.1), transparent 42%),
    linear-gradient(30deg, rgba(0, 240, 255, 0.07) 1px, transparent 1px),
    linear-gradient(150deg, rgba(0, 240, 255, 0.07) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.05) 1px, transparent 1px);
  background-size: 100% 100%, 28px 16px, 28px 16px, 28px 16px;
  background-position: 0 0, 0 0, 0 0, 14px 8px;
  opacity: 0.22;
  animation: hex-grid-float 9s linear infinite;
}

.articles-section > *:not(.articles-hex-grid) {
  position: relative;
  z-index: 1;
}

.search-section {
  margin-bottom: 24px;
  padding: 18px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  border-radius: 12px;
  background: linear-gradient(165deg, rgba(10, 20, 38, 0.72), rgba(8, 16, 31, 0.5));
  box-shadow: inset 0 0 0 1px rgba(0, 240, 255, 0.06);
  position: relative;
  overflow: hidden;
}

.search-section::before {
  content: '';
  position: absolute;
  inset: 0;
  border: 1px solid rgba(0, 240, 255, 0.34);
  border-radius: inherit;
  pointer-events: none;
  box-shadow: 0 0 22px rgba(0, 240, 255, 0.18), inset 0 0 26px rgba(0, 240, 255, 0.08);
}

.search-section::after {
  content: '';
  position: absolute;
  left: -40%;
  top: 0;
  width: 44%;
  height: 100%;
  background: linear-gradient(95deg, transparent, rgba(0, 240, 255, 0.16), transparent);
  animation: panel-scan 4.8s linear infinite;
  pointer-events: none;
}

.search-panel-corners {
  position: absolute;
  inset: 8px;
  pointer-events: none;
  z-index: 1;
}

.search-panel-corners::before,
.search-panel-corners::after {
  content: '';
  position: absolute;
  width: 20px;
  height: 14px;
  border-top: 1px solid rgba(0, 240, 255, 0.48);
  border-left: 1px solid rgba(0, 240, 255, 0.48);
  opacity: 0.75;
}

.search-panel-corners::before {
  left: 0;
  top: 0;
}

.search-panel-corners::after {
  right: 0;
  bottom: 0;
  transform: rotate(180deg);
}

.search-wrap {
  margin-bottom: 16px;
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  z-index: 1;
}

.search-prefix {
  color: var(--accent);
  font-family: var(--mono);
  font-size: 14px;
  line-height: 1;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.38);
  user-select: none;
}

.search-caret {
  display: inline-block;
  width: 8px;
  height: 14px;
  background: rgba(0, 240, 255, 0.92);
  animation: terminal-caret-blink 1s steps(1, end) infinite;
}

.search-btn {
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.88), rgba(0, 210, 225, 0.82));
  color: #041319;
  border: none;
  font-family: var(--heading);
  letter-spacing: 0.6px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  position: relative;
  z-index: 1;
}

.filter-group:last-child {
  margin-bottom: 0;
}

.filter-label {
  min-width: 44px;
  font-size: 13px;
  color: var(--text);
  font-family: var(--mono);
  margin-top: 0;
  white-space: nowrap;
}

.filter-pills {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  overflow-y: hidden;
  white-space: nowrap;
  padding-bottom: 4px;
  flex: 1;
}

.filter-pills::-webkit-scrollbar {
  height: 6px;
}

.filter-pills::-webkit-scrollbar-track {
  background: rgba(0, 240, 255, 0.08);
  border-radius: 999px;
}

.filter-pills::-webkit-scrollbar-thumb {
  background: rgba(0, 240, 255, 0.35);
  border-radius: 999px;
}

.filter-pill {
  border: 1px solid rgba(0, 240, 255, 0.22);
  background: rgba(0, 240, 255, 0.06);
  color: var(--text);
  border-radius: 4px;
  padding: 4px 10px;
  font-size: 11px;
  font-family: var(--mono);
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.25s ease;
  white-space: nowrap;
}

.filter-pill:hover {
  border-color: var(--accent);
  color: var(--accent-hover);
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.18);
}

.filter-pill.active {
  background: rgba(0, 240, 255, 0.14);
  border-color: var(--accent);
  color: var(--accent);
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.2);
}

:deep(.search-input .el-input__wrapper) {
  background: rgba(7, 11, 20, 0.9);
  border: 1px solid rgba(0, 240, 255, 0.22);
  box-shadow: none;
  border-radius: 6px;
}

:deep(.search-input .el-input-group__append) {
  background: transparent;
  border-color: rgba(0, 240, 255, 0.22);
}

:deep(.search-input .el-input__inner) {
  color: var(--text);
  font-family: var(--mono);
}

:deep(.search-input .el-input__wrapper.is-focus) {
  border-color: var(--accent);
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.26);
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
  position: relative;
  z-index: 1;
}

.clear-filter-btn {
  border: 1px solid rgba(114, 255, 220, 0.42);
  background: rgba(114, 255, 220, 0.1);
  color: var(--accent-secondary);
  border-radius: 4px;
  padding: 5px 10px;
  font-size: 11px;
  font-family: var(--mono);
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.clear-filter-btn:hover {
  box-shadow: 0 0 12px rgba(114, 255, 220, 0.26);
  border-color: var(--accent-secondary);
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

/* Articles Layout - Left/Right Sidebars */
.articles-layout {
  display: grid;
  grid-template-columns: minmax(260px, 300px) minmax(0, 1fr);
  gap: 32px;
  align-items: start;
}

.articles-sidebar-left {
  display: flex;
  flex-direction: column;
  gap: 18px;
  position: sticky;
  top: 100px;
  width: 280px;
  flex-shrink: 0;
}

.articles-section {
  flex: 1;
  min-width: 0;
}

.info-card {
  background: linear-gradient(165deg, rgba(10, 20, 38, 0.82) 0%, rgba(8, 16, 31, 0.62) 100%);
  border: 1px solid rgba(0, 240, 255, 0.12);
  border-radius: 8px;
  padding: 16px;
  transition: all 0.25s ease;
}

.info-card:hover {
  border-color: rgba(0, 240, 255, 0.26);
  box-shadow: 0 4px 20px rgba(0, 240, 255, 0.08);
}

.info-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.1);
}

.info-card-icon {
  font-size: 13px;
  color: var(--accent);
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.28);
}

.info-card-title {
  font-family: var(--mono);
  font-size: 11px;
  color: var(--text-h);
  letter-spacing: 1px;
  text-transform: uppercase;
}

.info-card-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.info-card-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
  color: var(--text);
}

.info-card-item:hover {
  background: rgba(0, 240, 255, 0.06);
  color: var(--accent);
}

.info-card-item.active {
  background: rgba(0, 240, 255, 0.1);
  color: var(--accent);
}

.item-marker {
  color: var(--accent);
  opacity: 0.6;
  font-size: 10px;
}

.item-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.info-card-empty {
  font-size: 12px;
  color: var(--text);
  opacity: 0.5;
  text-align: center;
  padding: 12px 0;
}

/* Overview stats in left sidebar */
.overview-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.overview-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 8px;
  background: rgba(0, 240, 255, 0.04);
  border: 1px solid rgba(0, 240, 255, 0.08);
  border-radius: 6px;
}

.overview-value {
  font-family: var(--heading);
  font-size: 20px;
  font-weight: 700;
  color: var(--text-h);
}

.overview-label {
  font-size: 10px;
  color: var(--text);
  opacity: 0.6;
  font-family: var(--mono);
}

/* Hot articles in left sidebar */
.hot-articles-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.info-tag {
  display: inline-block;
  padding: 3px 8px;
  border: 1px solid rgba(0, 240, 255, 0.14);
  background: rgba(0, 240, 255, 0.04);
  border-radius: 4px;
  font-size: 11px;
  font-family: var(--mono);
  color: var(--text);
  cursor: pointer;
  transition: all 0.2s ease;
}

.info-tag:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: rgba(0, 240, 255, 0.1);
}

.info-tag.active {
  background: rgba(0, 240, 255, 0.14);
  border-color: var(--accent);
  color: var(--accent);
}

.sidebar-stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.sidebar-stat {
  text-align: center;
  padding: 10px 8px;
  background: rgba(0, 240, 255, 0.04);
  border-radius: 6px;
  border: 1px solid rgba(0, 240, 255, 0.08);
}

.sidebar-stat-value {
  display: block;
  font-family: var(--heading);
  font-size: 24px;
  font-weight: 700;
  color: var(--text-h);
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.2);
}

.sidebar-stat-label {
  display: block;
  font-size: 10px;
  color: var(--text);
  opacity: 0.6;
  font-family: var(--mono);
  letter-spacing: 0.5px;
  margin-top: 4px;
}

.filter-status-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  background: rgba(0, 240, 255, 0.04);
  border-radius: 4px;
  font-size: 12px;
}

.status-label {
  font-family: var(--mono);
  color: var(--text);
  opacity: 0.6;
}

.status-value {
  color: var(--accent);
  font-family: var(--mono);
}

.filter-status-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  color: var(--text);
  opacity: 0.5;
  font-size: 12px;
}

.filter-status-empty .empty-icon {
  color: var(--accent);
}

.clear-filter-btn-full {
  width: 100%;
  margin-top: 12px;
  padding: 8px;
  border: 1px solid rgba(114, 255, 220, 0.32);
  background: rgba(114, 255, 220, 0.08);
  color: var(--accent-secondary);
  border-radius: 6px;
  font-size: 12px;
  font-family: var(--mono);
  cursor: pointer;
  transition: all 0.2s ease;
}

.clear-filter-btn-full:hover {
  background: rgba(114, 255, 220, 0.14);
  border-color: var(--accent-secondary);
  box-shadow: 0 0 12px rgba(114, 255, 220, 0.2);
}

.system-clock {
  text-align: center;
  padding: 12px 0;
}

.clock-time {
  display: block;
  font-family: var(--mono);
  font-size: 28px;
  font-weight: 700;
  color: var(--accent);
  text-shadow: 0 0 16px rgba(0, 240, 255, 0.4);
  letter-spacing: 2px;
}

.clock-date {
  display: block;
  font-family: var(--mono);
  font-size: 14px;
  color: var(--text);
  opacity: 0.7;
  margin-top: 4px;
  letter-spacing: 1px;
}

/* Hot article items */
.hot-article-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.hot-article-item:hover {
  background: rgba(0, 240, 255, 0.06);
}

.hot-article-title {
  font-size: 13px;
  color: var(--text-h);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hot-article-meta {
  font-size: 10px;
  font-family: var(--mono);
  color: var(--accent);
  opacity: 0.7;
}

.category-stats-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-stat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.category-stat-item:hover {
  background: rgba(0, 240, 255, 0.06);
}

.category-stat-item.active {
  background: rgba(0, 240, 255, 0.1);
}

.category-name {
  font-size: 12px;
  color: var(--text);
  min-width: 60px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.category-bar {
  flex: 1;
  height: 4px;
  background: rgba(0, 240, 255, 0.08);
  border-radius: 2px;
  overflow: hidden;
}

.category-bar-fill {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, var(--accent), var(--accent-secondary));
  border-radius: 2px;
}

.page-info-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 0;
}

.page-info-text {
  font-family: var(--mono);
  font-size: 16px;
  color: var(--text-h);
  font-weight: 600;
}

.page-info-detail {
  font-size: 11px;
  color: var(--text);
  opacity: 0.6;
  font-family: var(--mono);
}

.pagination-wrap {
  margin-top: 28px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .el-pager li) {
  background: rgba(7, 11, 20, 0.86);
  border: 1px solid rgba(0, 240, 255, 0.18);
  color: var(--text);
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: rgba(0, 240, 255, 0.14);
  color: var(--accent);
  border-color: var(--accent);
}

@media (max-width: 1024px) {
  .featured-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .articles-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .hero-layout {
    grid-template-columns: 1fr;
    gap: 28px;
    padding: 0 20px;
  }

  .hero-content {
    text-align: center;
  }

  .hero-title {
    font-size: 44px;
  }

  .hero-hud-panel {
    max-width: 420px;
    margin: 0 auto;
  }

  .articles-layout {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .articles-sidebar-left,
  .articles-sidebar-right {
    position: static;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 12px;
  }

  .info-card {
    flex: 1;
    min-width: 180px;
    padding: 14px;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 34px;
    letter-spacing: 0.8px;
  }
  
  .hero-section {
    padding: 40px 0 28px;
    margin-top: 14px;
    border-radius: 12px;
  }

  .hero-layout {
    padding: 0 16px;
    gap: 22px;
  }

  .hero-subtitle {
    font-size: 15px;
  }

  .hero-terminal {
    font-size: 11px;
  }

  .frame-readout {
    display: none;
  }

  .hero-hud-frame::before,
  .hero-hud-frame::after {
    top: 14px;
    bottom: 14px;
  }

  .hero-hud-panel {
    padding: 16px;
  }
  
  .featured-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .featured-section {
    margin-bottom: 32px;
  }

  .articles-section {
    padding: 8px 10px 4px;
    margin-bottom: 32px;
  }

  .section-title {
    font-size: 18px;
    margin-bottom: 16px;
  }

  .articles-sidebar-left,
  .articles-sidebar-right {
    flex-direction: column;
    gap: 12px;
  }

  .info-card {
    min-width: 100%;
    padding: 14px;
  }

  .main-content {
    padding: 0 14px;
  }
}

@keyframes data-rain {
  0% {
    transform: translateY(-22%);
    opacity: 0;
  }
  12% {
    opacity: 1;
  }
  100% {
    transform: translateY(32%);
    opacity: 0;
  }
}

@keyframes panel-scan {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(330%);
  }
}

@keyframes terminal-caret-blink {
  0%,
  49% {
    opacity: 1;
  }
  50%,
  100% {
    opacity: 0;
  }
}

@keyframes hero-frame-pulse {
  0%,
  100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}

@keyframes neon-line-stream {
  0% {
    background-position: 0 0, 0 0;
  }
  100% {
    background-position: 0 0, 28px 0;
  }
}

@keyframes hex-grid-float {
  0% {
    background-position: 0 0, 0 0, 0 0, 14px 8px;
  }
  100% {
    background-position: 0 0, 28px 16px, -28px 16px, 42px 24px;
  }
}

.empty-state {
  text-align: center;
  padding: 60px 24px;
}

.empty-icon {
  font-size: 48px;
  color: var(--accent);
  text-shadow: 0 0 20px var(--accent-glow);
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-title {
  font-size: 18px;
  color: var(--text-h);
  font-family: var(--heading);
  margin: 0 0 8px;
}

.empty-desc {
  font-size: 14px;
  color: var(--text);
  opacity: 0.7;
}

.quick-links-bar {
  position: fixed;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 6px;
  z-index: 50;
  padding: 10px 8px;
  background: rgba(8, 16, 31, 0.9);
  border: 1px solid rgba(0, 240, 255, 0.15);
  border-radius: 12px;
  backdrop-filter: blur(12px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
}

.quick-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  color: var(--text);
  font-size: 13px;
  text-decoration: none;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.quick-link:hover {
  color: var(--accent);
  background: rgba(0, 240, 255, 0.08);
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.15);
}

.quick-link-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
  flex-shrink: 0;
}

.quick-link-label {
  font-family: var(--mono);
  font-size: 11px;
  letter-spacing: 0.3px;
}

@media (max-width: 1400px) {
  .quick-links-bar {
    right: 8px;
  }
  .quick-link-label {
    display: none;
  }
}

@media (max-width: 768px) {
  .quick-links-bar {
    display: none;
  }
}
</style>
