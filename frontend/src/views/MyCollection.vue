<script setup>
import { ref, onMounted, computed } from 'vue'
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

onMounted(fetchData)
</script>

<template>
  <div class="page-container">
    <Header />
    <main class="main-content">
      <section class="collection-panel">
        <div class="panel-corners" aria-hidden="true"></div>
        <h1 class="page-title">{{ pageTitle }}</h1>
        <div v-loading="loading" class="articles-grid">
          <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
        </div>
        <div v-if="!loading && !articles.length" class="empty-state">
          <div class="empty-icon">{{ isFavorites ? '⭐' : '❤️' }}</div>
          <p class="empty-title">{{ isFavorites ? '还没有收藏文章' : '还没有喜欢的文章' }}</p>
          <p class="empty-desc">去首页看看有没有感兴趣的内容吧</p>
        </div>
      </section>
    </main>
    <Footer />
  </div>
</template>

<style scoped>
.page-container { min-height: 100vh; display: flex; flex-direction: column; }
.main-content { flex: 1; max-width: 1200px; margin: 0 auto; padding: 32px 24px; width: 100%; box-sizing: border-box; }
.collection-panel { position: relative; }
.panel-corners { position: absolute; inset: 0; pointer-events: none; }
.panel-corners::before, .panel-corners::after { content: ''; position: absolute; width: 28px; height: 18px; border-top: 1px solid var(--accent-border); border-left: 1px solid var(--accent-border); opacity: 0.5; }
.panel-corners::before { left: 0; top: 0; }
.panel-corners::after { right: 0; bottom: 0; transform: rotate(180deg); }
.page-title { font-size: 28px; color: var(--text-h); margin: 0 0 24px; }
.articles-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 20px; }
.empty-state { text-align: center; padding: 60px 24px; }
.empty-icon { font-size: 48px; margin-bottom: 16px; opacity: 0.6; }
.empty-title { font-size: 18px; color: var(--text-h); font-family: var(--heading); margin: 0 0 8px; }
.empty-desc { font-size: 14px; color: var(--text); opacity: 0.7; }
</style>
