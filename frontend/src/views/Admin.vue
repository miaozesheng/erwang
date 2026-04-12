<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, View } from '@element-plus/icons-vue'
import { getArticles, deleteArticle, getStats, getCategories, getTags, createCategory, createTag, getQuickLinks, createQuickLink, updateQuickLink, deleteQuickLink } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'

const router = useRouter()
const activeTab = ref('articles')
const articles = ref([])
const loading = ref(false)
const stats = ref({ articleCount: 0, totalViews: 0, categoryCount: 0, tagCount: 0 })
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const categories = ref([])
const tagsData = ref([])
const quickLinks = ref([])

const newCategory = ref('')
const newTag = ref('')
const linkForm = reactive({ icon: '', label: '', url: '', sort: 0 })
const editingLinkId = ref(null)

const fetchArticles = async () => {
  loading.value = true
  try {
    const res = await getArticles({ page: currentPage.value, size: pageSize.value })
    const data = res.data.data || {}
    articles.value = data.list || data || []
    total.value = Number(data.total ?? articles.value.length)
  } catch (e) {
    console.error('Failed to fetch articles:', e)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchArticles()
}

const fetchStats = async () => {
  try {
    const res = await getStats()
    stats.value = res.data.data || stats.value
  } catch (e) {
    console.error('Failed to fetch stats:', e)
  }
}

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data?.data || []
  } catch (e) {
    console.error('Failed to fetch categories:', e)
  }
}

const fetchTags = async () => {
  try {
    const res = await getTags()
    tagsData.value = res.data?.data || []
  } catch (e) {
    console.error('Failed to fetch tags:', e)
  }
}

const fetchQuickLinks = async () => {
  try {
    const res = await getQuickLinks()
    quickLinks.value = res.data?.data || []
  } catch (e) {
    console.error('Failed to fetch quick links:', e)
  }
}

const handleEdit = (id) => router.push(`/admin/article/${id}`)
const handleCreate = () => router.push('/admin/article')

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteArticle(id)
      ElMessage.success('删除成功')
      fetchArticles()
      fetchStats()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleAddCategory = async () => {
  const name = newCategory.value.trim()
  if (!name) return
  try {
    await createCategory(name)
    ElMessage.success('分类添加成功')
    newCategory.value = ''
    fetchCategories()
    fetchStats()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

const handleAddTag = async () => {
  const name = newTag.value.trim()
  if (!name) return
  try {
    await createTag(name)
    ElMessage.success('标签添加成功')
    newTag.value = ''
    fetchTags()
    fetchStats()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

const resetLinkForm = () => {
  linkForm.icon = ''
  linkForm.label = ''
  linkForm.url = ''
  linkForm.sort = 0
  editingLinkId.value = null
}

const handleSaveLink = async () => {
  if (!linkForm.label.trim() || !linkForm.url.trim()) {
    ElMessage.warning('名称和链接不能为空')
    return
  }
  try {
    if (editingLinkId.value) {
      await updateQuickLink(editingLinkId.value, { ...linkForm })
      ElMessage.success('更新成功')
    } else {
      await createQuickLink({ ...linkForm })
      ElMessage.success('添加成功')
    }
    resetLinkForm()
    fetchQuickLinks()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleEditLink = (link) => {
  editingLinkId.value = link.id
  linkForm.icon = link.icon
  linkForm.label = link.label
  linkForm.url = link.url
  linkForm.sort = link.sort || 0
}

const handleDeleteLink = async (id) => {
  try {
    await deleteQuickLink(id)
    ElMessage.success('删除成功')
    fetchQuickLinks()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', { year: 'numeric', month: 'short', day: 'numeric' })
}

onMounted(() => {
  fetchArticles()
  fetchStats()
  fetchCategories()
  fetchTags()
  fetchQuickLinks()
})
</script>

<template>
  <div class="page-container">
    <Header />

    <main class="main-content">
      <div class="stats-grid">
        <div class="stat-card"><span class="stat-value">{{ stats.articleCount }}</span><span class="stat-label">文章数</span></div>
        <div class="stat-card"><span class="stat-value">{{ stats.totalViews }}</span><span class="stat-label">总阅读</span></div>
        <div class="stat-card"><span class="stat-value">{{ stats.categoryCount }}</span><span class="stat-label">分类数</span></div>
        <div class="stat-card"><span class="stat-value">{{ stats.tagCount }}</span><span class="stat-label">标签数</span></div>
      </div>

      <section class="admin-panel">
        <div class="tab-bar">
          <button v-for="tab in [{key:'articles',label:'文章'},{key:'categories',label:'分类'},{key:'tags',label:'标签'},{key:'links',label:'快捷链接'}]" :key="tab.key" type="button" class="tab-btn" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">{{ tab.label }}</button>
        </div>

        <div v-if="activeTab === 'articles'">
<div class="admin-header">
         <div class="admin-header-content">
           <button
             v-if="$route.path !== '/profile'"
             class="back-btn"
             @click="$router.push('/profile')"
           >
             ← 返回个人中心
           </button>
           <h2 class="admin-title">文章管理</h2>
         </div>
         <el-button type="primary" @click="handleCreate" class="create-btn"><el-icon><Plus /></el-icon> 新建文章</el-button>
       </div>
          <div class="articles-list" v-loading="loading">
            <div v-for="article in articles" :key="article.id" class="article-row">
              <div class="row-main">
                <h3 class="row-title" @click="router.push(`/article/${article.id}`)">{{ article.title }}</h3>
                <p class="row-excerpt">{{ article.excerpt || article.content?.substring(0, 80) + '...' }}</p>
                <div class="row-meta">
                  <span v-if="article.category" class="meta-badge">{{ article.category }}</span>
                  <span v-for="tag in (article.tags || []).slice(0, 3)" :key="tag" class="meta-tag">#{{ tag }}</span>
                  <span class="meta-date">{{ formatDate(article.created_at) }}</span>
                  <span v-if="article.views" class="meta-views"><el-icon><View /></el-icon> {{ article.views }}</span>
                </div>
              </div>
              <div class="row-actions">
                <button type="button" class="action-icon edit" title="编辑" @click="handleEdit(article.id)"><el-icon><Edit /></el-icon></button>
                <button type="button" class="action-icon delete" title="删除" @click="handleDelete(article.id)"><el-icon><Delete /></el-icon></button>
              </div>
            </div>
          </div>
          <div v-if="!loading && !articles.length" class="empty-state">
            <div class="empty-icon">◈</div>
            <p class="empty-title">暂无文章</p>
            <p class="empty-desc">点击上方「新建文章」开始创作</p>
          </div>
          <div v-if="total > pageSize" class="pagination-row">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              :current-page="currentPage"
              @current-change="handlePageChange"
            />
          </div>
        </div>

        <div v-if="activeTab === 'categories'" class="manage-section">
          <h2 class="admin-title">分类管理</h2>
          <div class="add-row">
            <el-input v-model="newCategory" placeholder="输入分类名称" @keyup.enter="handleAddCategory" />
            <el-button type="primary" @click="handleAddCategory">添加</el-button>
          </div>
          <div class="item-grid">
            <div v-for="cat in categories" :key="cat" class="item-chip">{{ cat }}</div>
          </div>
          <div v-if="!categories.length" class="empty-state"><p class="empty-title">暂无分类</p></div>
        </div>

        <div v-if="activeTab === 'tags'" class="manage-section">
          <h2 class="admin-title">标签管理</h2>
          <div class="add-row">
            <el-input v-model="newTag" placeholder="输入标签名称" @keyup.enter="handleAddTag" />
            <el-button type="primary" @click="handleAddTag">添加</el-button>
          </div>
          <div class="item-grid">
            <div v-for="tag in tagsData" :key="tag" class="item-chip tag-chip">{{ tag }}</div>
          </div>
          <div v-if="!tagsData.length" class="empty-state"><p class="empty-title">暂无标签</p></div>
        </div>

        <div v-if="activeTab === 'links'" class="manage-section">
          <h2 class="admin-title">快捷链接管理</h2>
          <div class="link-form">
            <el-input v-model="linkForm.icon" placeholder="图标(emoji)" style="width:80px" />
            <el-input v-model="linkForm.label" placeholder="名称" style="flex:1" />
            <el-input v-model="linkForm.url" placeholder="链接地址" style="flex:2" />
            <el-input-number v-model="linkForm.sort" :min="0" :max="999" placeholder="排序" style="width:100px" />
            <el-button type="primary" @click="handleSaveLink">{{ editingLinkId ? '更新' : '添加' }}</el-button>
            <el-button v-if="editingLinkId" @click="resetLinkForm">取消</el-button>
          </div>
          <div class="link-list">
            <div v-for="link in quickLinks" :key="link.id" class="link-row">
              <span class="link-icon">{{ link.icon }}</span>
              <span class="link-label">{{ link.label }}</span>
              <a :href="link.url" target="_blank" class="link-url">{{ link.url }}</a>
              <span class="link-sort">{{ link.sort }}</span>
              <div class="row-actions">
                <button type="button" class="action-icon edit" @click="handleEditLink(link)"><el-icon><Edit /></el-icon></button>
                <button type="button" class="action-icon delete" @click="handleDeleteLink(link.id)"><el-icon><Delete /></el-icon></button>
              </div>
            </div>
          </div>
          <div v-if="!quickLinks.length" class="empty-state"><p class="empty-title">暂无链接</p></div>
        </div>
      </section>
    </main>
    <Footer />
  </div>
</template>

<style scoped>
.page-container { min-height: 100vh; display: flex; flex-direction: column; background: var(--bg); }
.main-content { flex: 1; max-width: 1200px; margin: 0 auto; padding: 32px 24px; width: 100%; box-sizing: border-box; }

/* Stats - Clean white cards with subtle border */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }

.back-btn {
   display: inline-flex;
   align-items: center;
   gap: 6px;
   padding: 8px 16px;
   border: 1px solid var(--border);
   border-radius: 6px;
   background: transparent;
   color: var(--text);
   font-size: 13px;
   cursor: pointer;
   transition: all 0.2s ease;
 }

.back-btn:hover {
   border-color: var(--accent);
   color: var(--accent);
   background: rgba(255, 255, 255, 0.96);
   transform: translateY(-1px);
 }

.admin-header-content {
   display: flex;
   justify-content: space-between;
   align-items: center;
   margin-bottom: 20px;
 }
.stat-card { background: #ffffff; border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 20px; display: flex; flex-direction: column; align-items: center; gap: 6px; transition: border-color var(--duration-normal) ease; }
.stat-card:hover { border-color: var(--border-accent); }
.stat-value { font-size: 32px; font-weight: 700; color: var(--accent); font-family: var(--heading); }
.stat-label { font-size: 13px; color: var(--text); font-family: var(--mono); letter-spacing: 0.5px; }

/* Admin Panel - Clean white background */
.admin-panel { position: relative; border: 1px solid var(--border); border-radius: var(--radius-lg); background: #ffffff; padding: 24px; }

/* Tab bar - Clean styling */
.tab-bar { display: flex; gap: 8px; margin-bottom: 24px; position: relative; z-index: 1; border-bottom: 1px solid var(--border); padding-bottom: 16px; }
.tab-btn { border: 1px solid var(--border); background: transparent; color: var(--text); font-family: var(--heading); font-size: 13px; letter-spacing: 0.5px; padding: 8px 20px; border-radius: var(--radius-sm); cursor: pointer; transition: all var(--duration-normal) ease; }
.tab-btn:hover { color: var(--accent); border-color: var(--border-accent); }
.tab-btn.active { color: #ffffff; background: var(--accent); border-color: var(--accent); }

/* Admin Header */
.admin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; position: relative; z-index: 1; }
.admin-title { font-size: 22px; font-weight: 600; color: var(--text-h); margin: 0; }
.create-btn { background: var(--accent); border-color: var(--accent); }
.create-btn:hover { background: var(--accent-hover); border-color: var(--accent-hover); }

/* Articles List */
.articles-list { display: flex; flex-direction: column; gap: 12px; position: relative; z-index: 1; }
.article-row { display: flex; align-items: center; gap: 16px; padding: 18px 20px; border: 1px solid var(--border); border-radius: var(--radius-md); background: #ffffff; transition: border-color var(--duration-normal), background var(--duration-normal); }
.article-row:hover { border-color: var(--border-accent); background: var(--accent-bg); }
.row-main { flex: 1; min-width: 0; }
.row-title { font-size: 16px; font-weight: 600; color: var(--text-h); margin: 0 0 6px; cursor: pointer; transition: color 0.2s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.row-title:hover { color: var(--accent); }
.row-excerpt { font-size: 13px; color: var(--text); margin: 0 0 10px; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; opacity: 0.8; }
.row-meta { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.meta-badge { background: var(--accent-bg); color: var(--accent); border: 1px solid var(--accent-border); padding: 2px 10px; border-radius: 999px; font-size: 11px; font-family: var(--mono); }
.meta-tag { color: var(--accent-secondary); font-size: 12px; font-family: var(--mono); opacity: 0.8; }
.meta-date { color: var(--text); font-size: 12px; font-family: var(--mono); opacity: 0.7; }
.meta-views { display: inline-flex; align-items: center; gap: 4px; color: var(--text); font-size: 12px; opacity: 0.7; }
.meta-views .el-icon { font-size: 13px; color: var(--accent); }
.row-actions { display: flex; gap: 8px; flex-shrink: 0; }
.action-icon { width: 36px; height: 36px; border-radius: var(--radius-sm); border: 1px solid var(--border); background: transparent; color: var(--text); font-size: 16px; cursor: pointer; display: grid; place-items: center; transition: all var(--duration-normal); }
.action-icon.edit:hover { color: var(--accent); border-color: var(--accent); background: var(--accent-bg); }
.action-icon.delete:hover { color: #f56c6c; border-color: #f56c6c; background: rgba(245,108,108,0.08); }

/* Manage Section */
.manage-section { position: relative; z-index: 1; }
.manage-section .admin-title { margin-bottom: 16px; }
.add-row { display: flex; gap: 12px; margin-bottom: 20px; }
.add-row .el-input { flex: 1; }
.item-grid { display: flex; flex-wrap: wrap; gap: 10px; }
.item-chip { padding: 8px 18px; border: 1px solid var(--border); border-radius: var(--radius-full); background: var(--accent-bg); color: var(--text-h); font-family: var(--mono); font-size: 13px; letter-spacing: 0.3px; }
.tag-chip { color: var(--accent-secondary); }

/* Link Form & List */
.link-form { display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap; align-items: center; }
.link-list { display: flex; flex-direction: column; gap: 10px; }
.link-row { display: flex; align-items: center; gap: 12px; padding: 12px 16px; border: 1px solid var(--border); border-radius: var(--radius-md); background: #ffffff; }
.link-icon { font-size: 18px; width: 28px; text-align: center; flex-shrink: 0; }
.link-label { font-family: var(--heading); font-size: 14px; color: var(--text-h); min-width: 80px; }
.link-url { flex: 1; font-size: 12px; color: var(--accent); font-family: var(--mono); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.link-sort { font-size: 12px; color: var(--text); font-family: var(--mono); min-width: 30px; text-align: center; }

/* Empty State */
.empty-state { text-align: center; padding: 48px 24px; }
.empty-icon { font-size: 48px; color: var(--accent); margin-bottom: 16px; opacity: 0.6; }
.empty-title { font-size: 18px; color: var(--text-h); font-family: var(--heading); margin: 0 0 8px; }
.empty-desc { font-size: 14px; color: var(--text); opacity: 0.7; }

/* Pagination */
.pagination-row { display: flex; justify-content: center; margin-top: 20px; position: relative; z-index: 1; }

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .admin-header { flex-direction: column; gap: 16px; align-items: flex-start; }
  .main-content { padding: 24px 16px; }
  .article-row { flex-direction: column; align-items: flex-start; }
  .row-actions { align-self: flex-end; }
  .tab-bar { flex-wrap: wrap; }
  .link-form { flex-direction: column; }
}
</style>
