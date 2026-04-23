<script setup>
import { ref, onBeforeUnmount, onMounted, computed, watch } from 'vue'
import { getGithubProjects, getUserInfo, syncGithubProjects } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'

const activeTab = ref('all_time')
const projects = ref([])
const loading = ref(false)
const syncing = ref(false)
const languages = ref([])
const selectedLanguage = ref('')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const latestFetchDate = ref('')

const normalizeRole = (role = '') => role.toLowerCase().replace(/^role_/, '')
const isAdmin = ref(normalizeRole(localStorage.getItem('userRole') || '') === 'admin')
const hasToken = computed(() => !!localStorage.getItem('token'))
const dataHint = computed(() => {
  const leaderboardLabelMap = {
    all_time: '总 star 榜单',
    growth_7d: '最近 7 天增长榜',
    growth_30d: '最近 30 天增长榜'
  }
  const totalText = total.value ? `当前展示 ${total.value} 个去重仓库 · ${leaderboardLabelMap[activeTab.value]}` : '还没有同步到可展示的数据'
  const timeText = latestFetchDate.value ? `最近同步：${latestFetchDate.value}` : '等待首次同步'
  return isAdmin.value
    ? `${totalText} · ${timeText} · 你可以手动刷新榜单。`
    : `${totalText} · ${timeText}`
})

const growthLabel = computed(() => {
  if (activeTab.value === 'growth_7d') return '7天增长'
  if (activeTab.value === 'growth_30d') return '30天增长'
  return ''
})

const tabs = [
  { key: 'all_time', label: '总 Star 榜' },
  { key: 'growth_7d', label: '近 7 天增长' },
  { key: 'growth_30d', label: '近 30 天增长' },
]

const fetchProjects = async () => {
  loading.value = true
  try {
    const params = {
      category: activeTab.value,
      page: currentPage.value,
      size: pageSize.value
    }
    if (selectedLanguage.value) {
      params.language = selectedLanguage.value
    }
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    const res = await getGithubProjects(params)
    const data = res.data?.data || {}
    projects.value = data.list || []
    total.value = data.total || 0
    languages.value = data.languages || []
    latestFetchDate.value = data.latestFetchDate || ''
  } catch (e) {
    console.error('Failed to fetch GitHub projects:', e)
  } finally {
    loading.value = false
  }
}

const syncAdminState = async () => {
  if (!hasToken.value) {
    isAdmin.value = false
    return
  }

  try {
    const res = await getUserInfo()
    const user = res.data?.data || {}
    const role = normalizeRole(user.role || localStorage.getItem('userRole') || '')
    isAdmin.value = role === 'admin'
    if (role) {
      localStorage.setItem('userRole', role)
    }
  } catch (error) {
    console.error('Failed to sync admin state:', error)
    isAdmin.value = normalizeRole(localStorage.getItem('userRole') || '') === 'admin'
  }
}

const handleAuthChanged = () => {
  syncAdminState()
}

const handleSearch = () => {
  currentPage.value = 1
  fetchProjects()
}

const handleSync = async () => {
  syncing.value = true
  try {
    await syncGithubProjects()
    fetchProjects()
  } catch (e) {
    console.error('Failed to sync GitHub projects:', e)
  } finally {
    syncing.value = false
  }
}

const handleTabChange = (tab) => {
  activeTab.value = tab
  currentPage.value = 1
  fetchProjects()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchProjects()
}

const formatStars = (num) => {
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toString()
}

const getLanguageColor = (lang) => {
  const colors = {
    JavaScript: '#f1e05a',
    TypeScript: '#3178c6',
    Python: '#3572A5',
    Java: '#b07219',
    Go: '#00ADD8',
    Rust: '#dea584',
    'C++': '#f34b7d',
    C: '#555555',
    Ruby: '#701516',
    PHP: '#4F5D95',
    Swift: '#F05138',
    Kotlin: '#A97BFF',
    'C#': '#178600',
    HTML: '#e34c26',
    CSS: '#563d7c',
    Vue: '#41b883',
    Shell: '#89e051',
  }
  return colors[lang] || '#8b949e'
}

onMounted(() => {
  syncAdminState()
  fetchProjects()
  window.addEventListener('auth-changed', handleAuthChanged)
  window.addEventListener('storage', handleAuthChanged)
})

onBeforeUnmount(() => {
  window.removeEventListener('auth-changed', handleAuthChanged)
  window.removeEventListener('storage', handleAuthChanged)
})

watch(activeTab, () => {
  selectedLanguage.value = ''
})

watch(searchKeyword, () => {
  currentPage.value = 1
})
</script>

<template>
  <div class="page-container">
    <Header />

    <main class="main-content">
      <section class="hero-section">
        <div class="hero-grid" aria-hidden="true"></div>
        <div class="hero-body">
          <span class="hero-kicker">&gt; /erwang/github-trending</span>
          <h1 class="hero-title">GitHub 排行榜</h1>
          <p class="hero-subtitle">看总 star 榜单，也看最近一周与一个月增长最快的项目</p>
        </div>
      </section>

      <section class="content-section">
        <div class="tabs-container">
          <div class="tabs-header">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              type="button"
              class="tab-btn"
              :class="{ active: activeTab === tab.key }"
              @click="handleTabChange(tab.key)"
            >
              {{ tab.label }}
            </button>
          </div>

          <div class="filter-bar">
            <div class="search-input-wrap">
              <input
                v-model="searchKeyword"
                type="text"
                class="search-input"
                placeholder="搜索项目..."
                @keyup.enter="handleSearch"
              />
            </div>
            <select v-model="selectedLanguage" class="language-select" @change="fetchProjects">
              <option value="">所有语言</option>
              <option v-for="lang in languages" :key="lang" :value="lang">{{ lang }}</option>
            </select>
            <button
              v-if="isAdmin"
              type="button"
              class="sync-btn"
              :class="{ syncing: syncing }"
              :disabled="syncing"
              @click="handleSync"
            >
              <span v-if="syncing" class="sync-spinner">⟳</span>
              <span v-else class="sync-icon">↻</span>
              {{ syncing ? '同步中...' : '立即同步榜单' }}
            </button>
          </div>

          <p class="sync-hint">{{ dataHint }}</p>

          <div class="projects-grid">
            <div v-if="loading" class="loading-state">
              <el-skeleton :rows="3" animated />
            </div>
            
            <div v-else-if="projects.length === 0" class="empty-state">
              <div class="empty-icon">◈</div>
              <p class="empty-title">暂无数据</p>
              <p class="empty-desc">请稍后刷新页面，或等待定时任务抓取数据</p>
            </div>

            <a
              v-else
              v-for="(project, index) in projects"
              :key="project.id"
              :href="project.url"
              target="_blank"
              rel="noopener noreferrer"
              class="project-card"
            >
              <div class="project-header">
                <span class="rank-badge">#{{ (currentPage - 1) * pageSize + index + 1 }}</span>
                <img
                  v-if="project.ownerAvatar"
                  :src="project.ownerAvatar"
                  class="owner-avatar"
                  alt=""
                />
                <span class="owner-name">{{ project.ownerName }}</span>
                <span class="repo-name">/{{ project.repoName.split('/')[1] }}</span>
              </div>

              <p class="project-desc">{{ project.description || '暂无描述' }}</p>

              <div class="project-meta">
                <span v-if="project.language" class="language-tag">
                  <span class="lang-dot" :style="{ background: getLanguageColor(project.language) }"></span>
                  {{ project.language }}
                </span>
                <span class="stat-item">
                  <span class="stat-icon">★</span>
                  {{ formatStars(project.stars) }}
                </span>
                <span v-if="growthLabel && typeof project.starGrowth === 'number'" class="stat-item stat-item--growth">
                  <span class="stat-icon">↗</span>
                  {{ growthLabel }} +{{ formatStars(project.starGrowth) }}
                </span>
                <span class="stat-item">
                  <span class="stat-icon">⑂</span>
                  {{ formatStars(project.forks) }}
                </span>
              </div>
            </a>
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
  --page-shell-max: var(--shell-wide);
  flex: 1;
  width: 100%;
  max-width: var(--page-shell-max);
  margin: 0 auto;
  padding: 0 var(--page-gutter);
}

.hero-section {
  position: relative;
  margin-top: 20px;
  min-height: 180px;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  background: #ffffff;
  overflow: hidden;
}


.hero-body {
  position: relative;
  z-index: 1;
  height: 100%;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  gap: 8px;
  padding: 26px var(--space-4);
}

.hero-kicker {
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 1.4px;
  color: var(--text);
  opacity: 0.68;
}

.hero-title {
  margin: 0;
  font-family: var(--heading);
  font-size: clamp(28px, 5vw, 42px);
  line-height: 1.08;
  letter-spacing: -0.02em;
  color: var(--text-h);
}

.hero-subtitle {
  margin: 0;
  color: var(--text);
  font-size: clamp(14px, 2.1vw, 17px);
  letter-spacing: 0.2px;
}

.content-section {
  margin: 32px auto 48px;
}

.tabs-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.tabs-header {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tab-btn {
  padding: 10px 20px;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: transparent;
  color: var(--text);
  font-size: 14px;
  font-family: var(--heading);
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.22s ease;
}

.tab-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.tab-btn.active {
  background: var(--accent-bg);
  border-color: var(--accent);
  color: var(--accent);
}

.filter-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.search-input-wrap {
  flex: 1;
  min-width: 180px;
}

.search-input {
  width: 100%;
  padding: 8px 16px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--card-bg);
  color: var(--text);
  font-size: 13px;
}

.search-input:focus {
  border-color: var(--accent);
  outline: none;
}

.search-input::placeholder {
  color: var(--text);
  opacity: 0.6;
}

.language-select {
  padding: 8px 16px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--card-bg);
  color: var(--text);
  font-size: 13px;
  cursor: pointer;
}

.language-select:focus {
  border-color: var(--accent);
  outline: none;
}

.sync-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: transparent;
  color: var(--text);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.22s ease;
}

.sync-btn:hover:not(:disabled) {
  border-color: var(--accent);
  color: var(--accent);
}

.sync-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.sync-btn.syncing {
  pointer-events: none;
}

.sync-hint {
  margin: -6px 0 2px;
  color: var(--text);
  opacity: 0.7;
  font-size: 13px;
}

.sync-icon {
  font-size: 14px;
}

.sync-spinner {
  font-size: 14px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(clamp(280px, 24vw, 360px), 1fr));
  gap: 16px;
}

.project-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: #ffffff;
  text-decoration: none;
  transition: all 0.22s ease;
}

.project-card:hover {
  border-color: var(--accent);
}

.project-header {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: var(--accent-bg);
  color: var(--accent);
  font-size: 12px;
  font-family: var(--heading);
}

.owner-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
}

.owner-name {
  font-size: 13px;
  color: var(--accent);
}

.repo-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-h);
}

.project-desc {
  margin: 0;
  font-size: 13px;
  color: var(--text);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.language-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text);
}

.lang-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text);
}

.stat-item--growth {
  color: var(--accent);
}

.stat-icon {
  color: var(--accent-secondary);
}

.loading-state {
  padding: 40px;
}

.empty-state {
  text-align: center;
  padding: 56px 24px;
  grid-column: 1 / -1;
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

@media (max-width: 768px) {
  .main-content {
    padding: 20px 16px 100px;
  }

  .projects-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .project-card {
    padding: 14px;
  }

  .tabs-header {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 4px;
  }

  .tab-btn {
    flex-shrink: 0;
    padding: 8px 14px;
    font-size: 13px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }

  .page-title {
    font-size: 20px;
  }
}
</style>
