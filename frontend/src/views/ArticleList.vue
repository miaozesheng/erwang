<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { getArticles, getCategories, getTags, getInteractionStatuses } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import ArticleCard from '../components/ArticleCard.vue'
import { Search, Filter, Close, ArrowRight } from '@element-plus/icons-vue'

const articles = ref([])
const loading = ref(false)
const categories = ref([])
const tags = ref([])
const keyword = ref('')
const selectedCategory = ref('')
const selectedTag = ref('')
const startDate = ref('')
const endDate = ref('')
const showAdvancedFilters = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

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

const formatDateValue = (date) => {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

const fetchArticles = async () => {
  loading.value = true
  try {
    const res = await getArticles({
      page: currentPage.value,
      size: pageSize.value,
      keyword: trimmedKeyword.value || undefined,
      category: selectedCategory.value || undefined,
      tag: selectedTag.value || undefined,
      startDate: startDate.value || undefined,
      endDate: endDate.value || undefined
    })
    const data = res.data?.data || {}
    articles.value = data.list || data || []
    total.value = Number(data.total ?? (Array.isArray(articles.value) ? articles.value.length : 0))
    currentPage.value = Number(data.page ?? currentPage.value)
    pageSize.value = Number(data.size ?? pageSize.value)
    
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
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const hasDateFilter = computed(() => Boolean(startDate.value && endDate.value))
const trimmedKeyword = computed(() => keyword.value.trim())

const hasActiveFilters = computed(() => {
  return Boolean(trimmedKeyword.value || selectedCategory.value || selectedTag.value || startDate.value || endDate.value)
})

const toggleAdvancedFilters = () => {
  showAdvancedFilters.value = !showAdvancedFilters.value
}

const applyRelativeDateRange = (days) => {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - days)
  startDate.value = formatDateValue(start)
  endDate.value = formatDateValue(end)
  currentPage.value = 1
  fetchArticles()
}

const clearAllFilters = () => {
  keyword.value = ''
  selectedCategory.value = ''
  selectedTag.value = ''
  startDate.value = ''
  endDate.value = ''
  showAdvancedFilters.value = false
  currentPage.value = 1
  fetchArticles()
}

const dateRange = computed({
  get: () => (startDate.value && endDate.value ? [startDate.value, endDate.value] : null),
  set: (val) => {
    if (val && val[0] && val[1]) {
      startDate.value = val[0]
      endDate.value = val[1]
    } else {
      startDate.value = ''
      endDate.value = ''
    }
  }
})

const handleDateChange = () => {
  currentPage.value = 1
  fetchArticles()
}

onMounted(() => {
  fetchFilters()
  fetchArticles()
})
</script>

<template>
  <div class="page-container">
    <Header />

    <main class="main-content">
      <section class="hero-minimal">
        <div class="hero-inner">
          <div class="hero-content">
            <span class="hero-eyebrow">Notes & Thoughts</span>
            <h1 class="hero-title">记录技术探索的<br />每一段旅程</h1>
            <p class="hero-desc">在这里，我分享关于编程、系统架构以及在技术世界中的种种思考。</p>
          </div>
          <div class="hero-stats">
            <div class="stat-item">
              <span class="stat-value">{{ total }}</span>
              <span class="stat-label">文章总数</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">{{ categories.length }}</span>
              <span class="stat-label">专栏分类</span>
            </div>
          </div>
        </div>
      </section>

      <div class="discovery-section">
        <div class="discovery-card">
          <div class="search-bar-row">
            <el-input
              v-model="keyword"
              placeholder="搜索感兴趣的内容..."
              class="search-input"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            >
              <template #prefix>
                <el-icon class="search-icon-svg"><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" class="search-main-btn" @click="handleSearch">搜索</el-button>
            <button
              type="button"
              class="filter-toggle-btn"
              :class="{ active: showAdvancedFilters }"
              @click="toggleAdvancedFilters"
            >
              <el-icon><Filter /></el-icon>
              <span>筛选</span>
            </button>
          </div>

          <div class="category-row">
            <button
              type="button"
              class="cat-chip"
              :class="{ active: !selectedCategory }"
              @click="handleCategoryFilter('')"
            >
              全部
            </button>
            <button
              v-for="cat in categories"
              :key="cat"
              type="button"
              class="cat-chip"
              :class="{ active: selectedCategory === cat }"
              @click="handleCategoryFilter(cat)"
            >
              {{ cat }}
            </button>
          </div>

          <Transition name="expand">
            <div v-if="showAdvancedFilters" class="advanced-filter-drawer">
              <div class="filter-group">
                <span class="filter-label">发布时间</span>
                <div class="filter-options">
                  <el-date-picker
                    v-model="dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    value-format="YYYY-MM-DD"
                    @change="handleDateChange"
                    class="refined-date-picker"
                  />
                  <div class="quick-dates">
                    <button type="button" class="quick-date-link" @click="applyRelativeDateRange(30)">近一月</button>
                    <button type="button" class="quick-date-link" @click="applyRelativeDateRange(180)">近半年</button>
                  </div>
                </div>
              </div>
              <div class="filter-group">
                <span class="filter-label">热门标签</span>
                <div class="tag-cloud">
                  <button
                    v-for="tag in tags"
                    :key="tag"
                    type="button"
                    class="tag-link"
                    :class="{ active: selectedTag === tag }"
                    @click="handleTagFilter(tag)"
                  >
                    #{{ tag }}
                  </button>
                </div>
              </div>
              <div v-if="hasActiveFilters" class="filter-actions">
                <el-button link :icon="Close" @click="clearAllFilters">重置所有筛选</el-button>
              </div>
            </div>
          </Transition>
        </div>
      </div>

      <section class="content-timeline">
        <div class="timeline-container">
          <el-skeleton :rows="6" animated v-if="loading" />

          <div v-else-if="articles.length" class="articles-timeline">
            <div v-for="article in articles" :key="article.id" class="timeline-item">
              <div class="timeline-meta">
                <time class="time-main">
                  {{ article.created_at ? new Date(article.created_at).getDate().toString().padStart(2, '0') : '--' }}
                </time>
                <span class="time-sub">
                  {{ article.created_at ? new Date(article.created_at).toLocaleDateString('en-US', { month: 'short' }).toUpperCase() : '--' }}
                </span>
              </div>
              <div class="timeline-marker">
                <div class="marker-dot"></div>
                <div class="marker-line"></div>
              </div>
              <div class="timeline-content">
                <ArticleCard :article="article" />
              </div>
            </div>
          </div>

          <div v-else class="empty-state-refined">
            <div class="empty-illustration">
              <el-icon><Search /></el-icon>
            </div>
            <h3>没能找到相关文章</h3>
            <p>尝试更换关键词或清除筛选条件</p>
            <el-button plain round @click="clearAllFilters">清除所有筛选</el-button>
          </div>

          <div v-if="total > pageSize" class="pagination-refined">
            <el-pagination
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              v-model:current-page="currentPage"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </section>
    </main>

    <Footer />
  </div>
</template>

<style scoped>
.page-container {
  background-color: var(--bg);
}

.main-content {
  max-width: var(--shell-reading);
  margin: 0 auto;
  padding: 0 var(--page-gutter);
}

/* --- Refined Hero Section --- */
.hero-minimal {
  padding: 80px 0 60px;
  position: relative;
}

.hero-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 40px;
}

.hero-eyebrow {
  display: block;
  font-family: var(--mono);
  font-size: 13px;
  color: var(--accent);
  letter-spacing: 0.2em;
  text-transform: uppercase;
  margin-bottom: 16px;
}

.hero-title {
  font-size: clamp(32px, 5vw, 48px);
  line-height: 1.2;
  color: var(--text-h);
  margin-bottom: 20px;
  font-weight: 800;
}

.hero-desc {
  max-width: 540px;
  font-size: 17px;
  color: var(--text-muted);
  line-height: 1.6;
}

.hero-stats {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 20px 40px;
  background: #ffffff;
  border-radius: var(--radius-full);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-family: var(--heading);
  font-size: 24px;
  font-weight: 700;
  color: var(--text-h);
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
}

.stat-divider {
  width: 1px;
  height: 24px;
  background-color: var(--border);
}

/* --- Discovery Section --- */
.discovery-section {
  margin-bottom: 40px;
}

.discovery-card {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  padding: 24px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.03);
}

.search-bar-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
}

:deep(.search-input .el-input__wrapper) {
  border-radius: var(--radius-lg) !important;
  box-shadow: inset 0 0 0 1px var(--border) !important;
  height: 48px;
}

.search-main-btn {
  height: 48px;
  padding: 0 28px;
  border-radius: var(--radius-lg);
  font-family: var(--heading);
  font-weight: 600;
}

.filter-toggle-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 48px;
  padding: 0 18px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text-muted);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-toggle-btn:hover,
.filter-toggle-btn.active {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--accent-bg);
}

.category-row {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
  scrollbar-width: none;
}

.category-row::-webkit-scrollbar { display: none; }

.cat-chip {
  padding: 6px 16px;
  border-radius: var(--radius-full);
  border: 1px solid transparent;
  background: var(--bg-elevated);
  color: var(--text-muted);
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}

.cat-chip:hover {
  background: var(--border);
  color: var(--text-h);
}

.cat-chip.active {
  background: var(--accent);
  color: #ffffff;
  font-weight: 600;
}

.advanced-filter-drawer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px dashed var(--border);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-h);
  font-family: var(--heading);
}

.filter-options {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.quick-dates {
  display: flex;
  gap: 12px;
}

.quick-date-link {
  border: none;
  background: transparent;
  color: var(--accent);
  font-size: 13px;
  cursor: pointer;
  padding: 0;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-link {
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  color: var(--text-muted);
  font-size: 12px;
  font-family: var(--mono);
  cursor: pointer;
}

.tag-link.active {
  background: var(--accent-bg);
  border-color: var(--accent);
  color: var(--accent);
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
}

/* --- Timeline Section --- */
.content-timeline {
  padding-bottom: 80px;
}

.articles-timeline {
  display: flex;
  flex-direction: column;
}

.timeline-item {
  display: grid;
  grid-template-columns: 60px 40px 1fr;
  align-items: stretch;
}

.timeline-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  padding-top: 24px;
  gap: 4px;
}

.time-main {
  font-family: var(--heading);
  font-size: 20px;
  font-weight: 700;
  color: var(--text-h);
  line-height: 1;
}

.time-sub {
  font-family: var(--mono);
  font-size: 11px;
  color: var(--text-muted);
  letter-spacing: 0.1em;
}

.timeline-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.marker-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--border-strong);
  margin-top: 30px;
  z-index: 2;
  transition: all 0.3s var(--ease-out);
}

.marker-line {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 1.5px;
  background-color: var(--border);
  z-index: 1;
}

.timeline-item:first-child .marker-line {
  top: 30px;
}

.timeline-item:last-child .marker-line {
  bottom: calc(100% - 30px);
}

.timeline-content {
  padding: 12px 0 32px;
}

.timeline-item:hover .marker-dot {
  background: var(--accent);
  transform: scale(1.4);
  box-shadow: 0 0 0 4px var(--accent-bg);
}

/* --- Empty State --- */
.empty-state-refined {
  text-align: center;
  padding: 80px 0;
}

.empty-illustration {
  font-size: 48px;
  color: var(--border-strong);
  margin-bottom: 16px;
}

.empty-state-refined h3 {
  margin-bottom: 8px;
}

.empty-state-refined p {
  color: var(--text-muted);
  margin-bottom: 24px;
}

/* --- Pagination --- */
.pagination-refined {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* --- Transitions --- */
.expand-enter-active, .expand-leave-active {
  transition: all 0.3s ease-in-out;
  max-height: 400px;
  overflow: hidden;
}
.expand-enter-from, .expand-leave-to {
  max-height: 0;
  opacity: 0;
}

@media (max-width: 768px) {
  .hero-minimal { padding: 40px 0; }
  .hero-stats { display: none; }
  
  .timeline-item {
    grid-template-columns: 1fr;
    gap: 12px;
    margin-bottom: 24px;
  }
  
  .timeline-meta {
    flex-direction: row;
    align-items: baseline;
    gap: 8px;
    padding-top: 0;
    justify-content: flex-start;
  }
  
  .timeline-marker { display: none; }
  .timeline-content { padding: 0; }
  
  .search-bar-row { flex-direction: column; }
  .search-main-btn { width: 100%; }
}
</style>
