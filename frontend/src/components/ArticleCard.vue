<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, View } from '@element-plus/icons-vue'
import { getInteractionStatus, toggleLike, toggleFavorite } from '../api'

const props = defineProps({
  article: { type: Object, required: true }
})

const router = useRouter()
const liked = ref(false)
const favorited = ref(false)
const likeCount = ref(0)
const favCount = ref(0)

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', { year: 'numeric', month: 'short', day: 'numeric' })
}

const goToDetail = () => {
  router.push(`/article/${props.article.id}`)
}

const fetchStatus = async () => {
  if (!localStorage.getItem('token')) return
  try {
    const res = await getInteractionStatus(props.article.id)
    const d = res.data?.data || {}
    liked.value = !!d.liked
    favorited.value = !!d.favorited
    likeCount.value = d.likeCount || 0
    favCount.value = d.favoriteCount || 0
  } catch (e) {
    console.error('Failed to fetch interaction status:', e)
  }
}

const handleLike = async (e) => {
  e.stopPropagation()
  if (!localStorage.getItem('token')) return router.push('/login')
  try {
    const res = await toggleLike(props.article.id)
    const d = res.data?.data || {}
    liked.value = !!d.liked
    likeCount.value = d.count ?? likeCount.value
  } catch (e) {
    console.error('Failed to toggle like:', e)
  }
}

const handleFav = async (e) => {
  e.stopPropagation()
  if (!localStorage.getItem('token')) return router.push('/login')
  try {
    const res = await toggleFavorite(props.article.id)
    const d = res.data?.data || {}
    favorited.value = !!d.favorited
    favCount.value = d.count ?? favCount.value
  } catch (e) {
    console.error('Failed to toggle favorite:', e)
  }
}

onMounted(fetchStatus)
</script>

<template>
  <div class="article-card" @click="goToDetail">
    <div class="card-tech-bg" aria-hidden="true"></div>
    <div class="card-corners" aria-hidden="true"></div>

    <div class="card-header">
      <h3 class="card-title">{{ article.title }}</h3>
      <span v-if="article.category" class="card-category">{{ article.category }}</span>
    </div>

    <p class="card-excerpt">{{ article.excerpt || article.content?.substring(0, 150) + '...' }}</p>

    <div class="card-footer">
      <div class="card-meta">
        <span class="meta-item">
          <el-icon><Calendar /></el-icon>
          {{ formatDate(article.created_at) }}
        </span>
        <span class="meta-sep">·</span>
        <span v-if="article.views != null" class="meta-item views">
          <el-icon><View /></el-icon>
          {{ article.views }}
        </span>
      </div>

      <div class="card-actions">
        <button type="button" class="card-action-btn" :class="{ active: liked }" @click="handleLike" title="喜欢">
          ❤ <span v-if="likeCount">{{ likeCount }}</span>
        </button>
        <button type="button" class="card-action-btn fav" :class="{ active: favorited }" @click="handleFav" title="收藏">
          ⭐ <span v-if="favCount">{{ favCount }}</span>
        </button>
      </div>
    </div>

    <div v-if="article.tags?.length" class="card-tags">
      <el-tag v-for="tag in article.tags.slice(0, 3)" :key="tag" size="small" class="tag">{{ tag }}</el-tag>
    </div>
  </div>
</template>

<style scoped>
.article-card {
  background: linear-gradient(160deg, rgba(10, 20, 38, 0.9), rgba(8, 16, 31, 0.65));
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 24px;
  cursor: pointer;
  transition: transform 0.32s ease, box-shadow 0.32s ease, border-color 0.32s ease;
  position: relative;
  overflow: hidden;
  isolation: isolate;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.article-card:hover {
  border-color: var(--accent);
  transform: translateY(-4px);
  box-shadow: 0 10px 36px rgba(0, 240, 255, 0.18), inset 0 0 0 1px rgba(0, 240, 255, 0.15);
}

.card-tech-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: radial-gradient(circle at 85% 10%, rgba(0, 240, 255, 0.08), transparent 45%);
  opacity: 0.85;
}

.card-corners {
  position: absolute;
  inset: 9px;
  pointer-events: none;
}

.card-corners::before,
.card-corners::after {
  content: '';
  position: absolute;
  width: 20px;
  height: 14px;
  border-top: 1px solid var(--accent-border);
  border-left: 1px solid var(--accent-border);
  opacity: 0;
  transition: opacity 0.28s ease, transform 0.3s ease;
}

.card-corners::before { left: 0; top: 0; transform: translate(-7px, -7px); }
.card-corners::after { right: 0; bottom: 0; transform: translate(7px, 7px) rotate(180deg); }
.article-card:hover .card-corners::before,
.article-card:hover .card-corners::after { opacity: 0.86; }
.article-card:hover .card-corners::before { transform: translate(0, 0); }
.article-card:hover .card-corners::after { transform: translate(0, 0) rotate(180deg); }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-h);
  margin: 0;
  line-height: 1.4;
  flex: 1;
  font-family: var(--heading);
}

.card-category {
  background: var(--accent-bg);
  color: var(--accent);
  padding: 3px 10px;
  border-radius: 4px;
  border: 1px solid rgba(0, 240, 255, 0.25);
  font-size: 11px;
  white-space: nowrap;
  font-family: var(--mono);
  letter-spacing: 0.5px;
  flex-shrink: 0;
}

.card-excerpt {
  color: var(--text);
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text);
  font-size: 12px;
  font-family: var(--mono);
  opacity: 0.7;
}

.meta-item .el-icon {
  font-size: 13px;
  color: var(--accent);
  opacity: 0.6;
}

.meta-sep {
  color: var(--text);
  opacity: 0.3;
  font-size: 12px;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.card-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 1px solid rgba(0, 240, 255, 0.15);
  background: transparent;
  color: var(--text);
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  opacity: 0.7;
}

.card-action-btn:hover {
  opacity: 1;
  border-color: rgba(0, 240, 255, 0.3);
}

.card-action-btn.active {
  opacity: 1;
  color: #ff6b8a;
  border-color: rgba(255, 107, 138, 0.4);
  background: rgba(255, 107, 138, 0.08);
}

.card-action-btn.fav.active {
  color: #ffb84d;
  border-color: rgba(255, 184, 77, 0.4);
  background: rgba(255, 184, 77, 0.08);
}

.card-tags {
  display: flex;
  gap: 6px;
  position: relative;
  z-index: 1;
}

.tag {
  background: rgba(0, 240, 255, 0.06);
  color: var(--accent);
  border: 1px solid rgba(0, 240, 255, 0.18);
  font-family: var(--mono);
  font-size: 11px;
}
</style>
