<script setup>
import { useRouter } from 'vue-router'
import { Calendar, View } from '@element-plus/icons-vue'

const props = defineProps({
  article: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const goToDetail = () => {
  router.push(`/article/${props.article.id}`)
}
</script>

<template>
  <div class="article-card" @click="goToDetail">
    <div class="card-header">
      <h3 class="card-title">{{ article.title }}</h3>
      <span v-if="article.category" class="card-category">
        {{ article.category }}
      </span>
    </div>
    <p class="card-excerpt">{{ article.excerpt || article.content?.substring(0, 150) + '...' }}</p>
    <div class="card-footer">
      <div class="card-meta">
        <span class="meta-item">
          <el-icon><Calendar /></el-icon>
          {{ formatDate(article.created_at) }}
        </span>
        <span v-if="article.views" class="meta-item">
          <el-icon><View /></el-icon>
          {{ article.views }}
        </span>
      </div>
      <div v-if="article.tags?.length" class="card-tags">
        <el-tag 
          v-for="tag in article.tags.slice(0, 3)" 
          :key="tag" 
          size="small"
          class="tag"
        >
          {{ tag }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<style scoped>
.article-card {
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.article-card:hover {
  border-color: var(--accent);
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(170, 59, 255, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 12px;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-h);
  margin: 0;
  line-height: 1.4;
  flex: 1;
}

.card-category {
  background: var(--accent-bg);
  color: var(--accent);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  white-space: nowrap;
}

.card-excerpt {
  color: var(--text);
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.card-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text);
  font-size: 13px;
}

.meta-item .el-icon {
  font-size: 14px;
  color: var(--accent);
}

.card-tags {
  display: flex;
  gap: 8px;
}

.tag {
  background: var(--code-bg);
  color: var(--text);
  border: none;
}
</style>