<script setup>
import { ref, onMounted, watch } from 'vue'
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
</script>

<template>
  <div class="article-card" @click="goToDetail">
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
  --card-like: hsl(from var(--accent) 352deg s l);
  --card-fav: hsl(from var(--accent) 42deg s l);
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: var(--sp-6);
  cursor: pointer;
  transition: transform 250ms ease, border-color 200ms ease, box-shadow 200ms ease;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
  box-shadow: none;
  will-change: transform;
}

.article-card:hover {
  border-color: color-mix(in srgb, var(--accent) 20%, transparent);
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.article-card:active {
  transform: scale(0.99);
}

.card-header {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  gap: var(--sp-2);
  min-width: 0;
}

.card-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-h);
  margin: 0;
  line-height: 1.35;
  flex: 1;
  font-family: var(--heading);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}

.card-category {
  background: color-mix(in srgb, var(--accent) 10%, transparent);
  color: var(--accent);
  padding: 2px var(--sp-2);
  border-radius: var(--radius-full);
  border: 1px solid color-mix(in srgb, var(--accent) 24%, transparent);
  font-size: 11px;
  white-space: nowrap;
  font-family: var(--mono);
  letter-spacing: 0.3px;
  flex-shrink: 0;
}

.card-excerpt {
  color: var(--text);
  font-size: 14px;
  line-height: 1.55;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  opacity: 0.8;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--sp-2);
  margin-top: var(--sp-1);
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text);
  font-size: 12px;
  font-family: var(--mono);
  opacity: 0.72;
}

.meta-item .el-icon {
  font-size: 13px;
  color: var(--text);
  opacity: 0.58;
}

.meta-sep {
  color: var(--text);
  opacity: 0.3;
  font-size: 12px;
}

.card-actions {
  display: flex;
  gap: var(--sp-2);
}

.card-action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: 1px solid color-mix(in srgb, var(--text-h) 8%, transparent);
  background: transparent;
  color: var(--text);
  font-size: 12px;
  padding: 4px 10px;
  min-height: 26px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: border-color 150ms ease, color 150ms ease, background-color 150ms ease, transform 150ms ease;
  opacity: 0.88;
}

.card-action-btn:hover {
  opacity: 1;
  border-color: color-mix(in srgb, var(--text-h) 16%, transparent);
}

.card-action-btn:active {
  transform: scale(0.95);
}

.card-action-btn.active {
  opacity: 1;
  color: var(--card-like);
  border-color: color-mix(in srgb, var(--card-like) 45%, transparent);
  background: color-mix(in srgb, var(--card-like) 14%, transparent);
}

.card-action-btn.fav.active {
  color: var(--card-fav);
  border-color: color-mix(in srgb, var(--card-fav) 45%, transparent);
  background: color-mix(in srgb, var(--card-fav) 15%, transparent);
}

.card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: var(--sp-1);
}

.tag {
  background: color-mix(in srgb, var(--accent) 8%, transparent);
  color: color-mix(in srgb, var(--accent) 82%, var(--text-h));
  border: 1px solid color-mix(in srgb, var(--accent) 18%, transparent);
  font-family: var(--mono);
  font-size: 11px;
  border-radius: var(--radius-full);
}

@media (max-width: 768px) {
  .article-card {
    padding: var(--sp-5);
  }

  .card-footer {
    flex-wrap: wrap;
    align-items: flex-start;
  }
}

@media (prefers-reduced-motion: reduce) {
  .article-card,
  .card-action-btn {
    transition-duration: 0.01ms;
  }
}
</style>
