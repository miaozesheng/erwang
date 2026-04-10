<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMyFavorites, getMyLikes } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import ArticleCard from '../components/ArticleCard.vue'

const route = useRoute()
const router = useRouter()
const articles = ref([])
const loading = ref(false)

const isFavorites = computed(() => route.path === '/favorites')
const pageTitle = computed(() => isFavorites.value ? '我的收藏' : '我的喜欢')

const fetchData = async () => {
  loading.value = true
  try {
    const res = isFavorites.value ? await getMyFavorites() : await getMyLikes()
    articles.value = res.data?.data || []
  } catch (e) {
    console.error('Failed to fetch:', e)
  } finally {
    loading.value = false
  }
}

watch(() => route.path, () => {
  fetchData()
})

onMounted(fetchData)
</script>

<template>
  <div class="collection-page">
    <Header />
    <main class="main-content">
      <div class="page-header">
        <h1 class="page-title">{{ pageTitle }}</h1>
      </div>
      <div v-loading="loading" class="articles-grid">
        <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
      </div>
      <div v-if="!loading && !articles.length" class="empty-state">
        <div class="empty-icon">{{ isFavorites ? '⭐' : '❤️' }}</div>
        <p class="empty-title">{{ isFavorites ? '还没有收藏文章' : '还没有喜欢的文章' }}</p>
        <p class="empty-desc">去首页看看有没有感兴趣的内容吧</p>
      </div>
    </main>
    <Footer />
  </div>
</template>

<style scoped>
.collection-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: var(--sp-8) var(--sp-6);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: var(--sp-5);
}

.page-header {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: var(--sp-6) var(--sp-6);
}

.page-title {
  margin: 0;
  font-size: 28px;
  color: var(--text-h);
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: var(--sp-5);
}

.empty-state {
  display: grid;
  place-items: center;
  text-align: center;
  padding: 60px var(--sp-6);
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
}

.empty-icon {
  font-size: 44px;
  margin-bottom: var(--sp-4);
  opacity: 0.68;
}

.empty-title {
  margin: 0 0 var(--sp-2);
  font-family: var(--heading);
  font-size: 18px;
  color: var(--text-h);
}

.empty-desc {
  font-size: 14px;
  color: var(--text);
  opacity: 0.78;
}

@media (max-width: 768px) {
  .main-content {
    padding: var(--sp-5) var(--sp-4);
  }

  .page-header {
    padding: var(--sp-5);
  }
}
</style>
