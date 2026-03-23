<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getArticles, deleteArticle } from '../api'
import Header from '../components/Header.vue'

const router = useRouter()
const articles = ref([])
const loading = ref(false)

const fetchArticles = async () => {
  loading.value = true
  try {
    const res = await getArticles()
    articles.value = res.data.articles || res.data || []
  } catch (e) {
    console.error('Failed to fetch articles:', e)
  } finally {
    loading.value = false
  }
}

const handleEdit = (id) => {
  router.push(`/admin/article/${id}`)
}

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
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleCreate = () => {
  router.push('/admin/article')
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

onMounted(() => {
  fetchArticles()
})
</script>

<template>
  <div class="page-container">
    <Header />
    
    <main class="main-content">
      <div class="admin-header">
        <h1 class="admin-title">文章管理</h1>
        <el-button type="primary" @click="handleCreate" class="create-btn">
          <el-icon><Plus /></el-icon>
          新建文章
        </el-button>
      </div>

      <el-table 
        :data="articles" 
        v-loading="loading"
        class="articles-table"
      >
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <span class="article-title" @click="router.push(`/article/${row.id}`)">
              {{ row.title }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="category" label="分类" width="120" />
        
        <el-table-column prop="created_at" label="发布时间" width="140">
          <template #default="{ row }">
            {{ formatDate(row.created_at) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="views" label="阅读量" width="100" />
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row.id)">
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !articles.length" description="暂无文章" />
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

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.admin-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-h);
  margin: 0;
}

.create-btn {
  background: var(--accent);
  border: none;
  box-shadow: 0 0 15px rgba(170, 59, 255, 0.3);
}

.create-btn:hover {
  background: var(--accent-hover, #b850ff);
  box-shadow: 0 0 25px rgba(170, 59, 255, 0.5);
}

.articles-table {
  background: var(--card-bg);
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table) {
  --el-table-bg-color: var(--card-bg);
  --el-table-tr-bg-color: var(--card-bg);
  --el-table-header-bg-color: var(--code-bg);
  --el-table-row-hover-bg-color: var(--accent-bg);
  --el-table-border-color: var(--border);
  --el-table-text-color: var(--text);
  --el-table-header-text-color: var(--text-h);
}

.article-title {
  cursor: pointer;
  color: var(--accent);
}

.article-title:hover {
  text-decoration: underline;
}

:deep(.el-button--primary) {
  background: var(--accent);
  border: none;
}

:deep(.el-button--danger) {
  background: #f56c6c;
  border: none;
}

@media (max-width: 768px) {
  .admin-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .main-content {
    padding: 24px 16px;
  }
}
</style>