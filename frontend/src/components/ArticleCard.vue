<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, View, Star, StarFilled, ChatDotSquare } from '@element-plus/icons-vue'
import { getInteractionStatus, toggleLike, toggleFavorite } from '../api'

const props = defineProps({
  article: { type: Object, required: true }
})

const router = useRouter()
const liked = ref(false)
const favorited = ref(false)
const likeCount = ref(0)
const favCount = ref(0)

const initFromProps = () => {
  const interaction = props.article._interaction || {}
  if (Object.keys(interaction).length > 0) {
    liked.value = !!interaction.liked
    favorited.value = !!interaction.favorited
    likeCount.value = interaction.likeCount || props.article.like_count || 0
    favCount.value = interaction.favoriteCount || props.article.favorite_count || 0
    return true
  }
  likeCount.value = props.article.like_count || 0
  favCount.value = props.article.favorite_count || 0
  return false
}

const hasLocalInteraction = initFromProps()

watch(() => props.article._interaction, initFromProps, { deep: true })

const fetchStatus = async () => {
  if (hasLocalInteraction) return
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

const goToDetail = () => {
  router.push(`/article/${props.article.id}`)
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>

<template>
  <div class="article-card" @click="goToDetail">
    <div class="card-header">
      <div class="card-title-row">
        <h3 class="card-title">{{ article.title }}</h3>
        <span v-if="article.category" class="card-category">{{ article.category }}</span>
      </div>
    </div>

    <p class="card-excerpt">{{ article.excerpt || '暂无摘要' }}</p>

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
        <button
          type="button"
          class="card-action-btn like-btn"
          :class="{ active: liked }"
          @click="handleLike"
          aria-label="点赞"
        >
          <svg
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="heart-icon"
          >
            <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z" />
          </svg>
          <span v-if="likeCount" class="action-count">{{ likeCount }}</span>
        </button>

        <button
          type="button"
          class="card-action-btn fav-btn"
          :class="{ active: favorited }"
          @click="handleFav"
          aria-label="收藏"
        >
          <el-icon v-if="favorited"><StarFilled /></el-icon>
          <el-icon v-else><Star /></el-icon>
          <span v-if="favCount" class="action-count">{{ favCount }}</span>
        </button>
      </div>
    </div>

    <div v-if="article.tags?.length" class="card-tags">
      <span v-for="tag in article.tags.slice(0, 3)" :key="tag" class="simple-tag">#{{ tag }}</span>
    </div>
  </div>
</template>

<style scoped>
.article-card {
  --card-like: #e05e5e;
  --card-fav: #e6a23c;
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 24px;
  cursor: pointer;
  transition:
    transform var(--duration-normal) var(--ease-out),
    border-color var(--duration-normal) var(--ease-out),
    box-shadow var(--duration-normal) var(--ease-out),
    background-color var(--duration-normal) var(--ease-out);
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.article-card:hover {
  border-color: color-mix(in srgb, var(--accent) 32%, var(--border));
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(45, 90, 74, 0.06);
  background: #ffffff;
}

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
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
  font-size: 11px;
  color: var(--accent);
  background: var(--accent-bg);
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-family: var(--mono);
  letter-spacing: 0.04em;
  text-transform: uppercase;
  flex-shrink: 0;
}

.card-excerpt {
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--text-muted);
  font-size: 12px;
  font-family: var(--mono);
}

.meta-item .el-icon {
  font-size: 14px;
  opacity: 0.8;
}

.meta-sep {
  color: var(--border);
}

.card-actions {
  display: flex;
  gap: 8px;
}

.card-action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  height: 32px;
  padding: 0 12px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text-muted);
  font-size: 12px;
  font-family: var(--mono);
  cursor: pointer;
  transition: all 0.2s var(--ease-out);
}

.card-action-btn:hover {
  background: var(--bg-elevated);
  border-color: var(--border-strong);
  color: var(--text-h);
}

.action-count {
  font-weight: 500;
}

.like-btn.active {
  color: var(--card-like);
  border-color: color-mix(in srgb, var(--card-like) 24%, transparent);
  background: color-mix(in srgb, var(--card-like) 8%, transparent);
}

.like-btn.active .heart-icon {
  fill: currentColor;
}

.fav-btn.active {
  color: var(--card-fav);
  border-color: color-mix(in srgb, var(--card-fav) 24%, transparent);
  background: color-mix(in srgb, var(--card-fav) 8%, transparent);
}

.card-tags {
  display: flex;
  gap: 12px;
  margin-top: 4px;
}

.simple-tag {
  font-size: 12px;
  color: var(--text-muted);
  font-family: var(--mono);
  opacity: 0.7;
  transition: opacity 0.2s ease, color 0.2s ease;
}

.simple-tag:hover {
  opacity: 1;
  color: var(--accent);
}

@media (max-width: 640px) {
  .article-card {
    padding: 20px;
  }
}
</style>
