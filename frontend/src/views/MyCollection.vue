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
const userRole = computed(() => (localStorage.getItem('userRole') || '').toLowerCase())
const sectionNav = computed(() => [
  { key: 'profile', label: '个人中心', path: '/profile' },
  { key: 'favorites', label: '收藏', path: '/favorites' },
  { key: 'likes', label: '喜欢', path: '/likes' },
  ...(userRole.value === 'admin' ? [{ key: 'admin', label: '管理', path: '/admin' }] : [])
])

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
        <div class="page-header-content">
          <h1 class="page-title">{{ pageTitle }}</h1>
          <p class="page-subtitle">把与你相关的内容收在一处，切换时不用再显式返回上一层。</p>
        </div>

        <nav class="section-nav" aria-label="个人内容导航">
          <button
            v-for="item in sectionNav"
            :key="item.key"
            type="button"
            class="section-nav__item"
            :class="{ active: route.path === item.path }"
            @click="router.push(item.path)"
          >
            {{ item.label }}
          </button>
        </nav>
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
  --page-shell-max: var(--shell-default);
  flex: 1;
  max-width: var(--page-shell-max);
  margin: 0 auto;
  width: 100%;
  padding: var(--sp-8) var(--page-gutter);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: var(--sp-5);
}

.page-header {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: var(--sp-6);
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-header-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-title {
  margin: 0;
  font-size: 28px;
  color: var(--text-h);
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-muted);
}

.section-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.section-nav__item {
  min-height: 42px;
  padding: 0 16px;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: transparent;
  color: var(--text);
  font-size: 13px;
  font-family: var(--heading);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-out);
}

.section-nav__item:hover,
.section-nav__item.active {
  color: var(--accent);
  border-color: color-mix(in srgb, var(--accent) 28%, var(--border));
  background: var(--accent-bg);
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(clamp(280px, 24vw, 360px), 1fr));
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
    padding: var(--sp-5) var(--sp-4) 100px;
  }

  .page-header {
    padding: var(--sp-4);
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }

  .section-nav {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 4px;
    scrollbar-width: none;
  }

  .section-nav::-webkit-scrollbar {
    display: none;
  }

  .section-nav__item {
    flex: 0 0 auto;
    padding: 8px 16px;
    font-size: 13px;
  }

  .collection-grid {
    gap: 12px;
  }
}
</style>
