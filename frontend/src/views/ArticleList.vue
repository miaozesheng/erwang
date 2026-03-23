<script setup>
import { ref, onMounted } from 'vue'
import { getArticles } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import ArticleCard from '../components/ArticleCard.vue'

const articles = ref([])
const loading = ref(false)
const featuredArticles = ref([])

const fetchArticles = async () => {
  loading.value = true
  try {
    const res = await getArticles()
    articles.value = res.data.articles || res.data || []
    featuredArticles.value = articles.value.slice(0, 3)
  } catch (e) {
    console.error('Failed to fetch articles:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchArticles()
})
</script>

<template>
  <div class="page-container">
    <Header />
    
    <main class="main-content">
      <section class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">
            <span class="title-accent">◈</span> Erwang Blog
          </h1>
          <p class="hero-subtitle">探索技术世界，分享Coding乐趣</p>
        </div>
        <div class="hero-glow"></div>
      </section>

      <section v-if="featuredArticles.length" class="featured-section">
        <h2 class="section-title">推荐文章</h2>
        <div class="featured-grid">
          <ArticleCard 
            v-for="article in featuredArticles" 
            :key="article.id" 
            :article="article"
          />
        </div>
      </section>

      <section class="articles-section">
        <h2 class="section-title">全部文章</h2>
        <el-skeleton :rows="5" animated v-if="loading" />
        <div v-else-if="articles.length" class="articles-grid">
          <ArticleCard 
            v-for="article in articles" 
            :key="article.id" 
            :article="article"
          />
        </div>
        <el-empty v-else description="暂无文章" />
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
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  width: 100%;
  box-sizing: border-box;
}

.hero-section {
  position: relative;
  padding: 80px 0 60px;
  text-align: center;
  overflow: hidden;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 56px;
  font-weight: 700;
  color: var(--text-h);
  margin: 0 0 16px;
  letter-spacing: -2px;
}

.title-accent {
  color: var(--accent);
  text-shadow: 0 0 40px var(--accent);
}

.hero-subtitle {
  font-size: 18px;
  color: var(--text);
  margin: 0;
}

.hero-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(170, 59, 255, 0.15) 0%, transparent 70%);
  pointer-events: none;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-h);
  margin: 0 0 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}

.featured-section {
  margin-bottom: 48px;
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.articles-section {
  margin-bottom: 48px;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

@media (max-width: 1024px) {
  .featured-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .articles-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 36px;
  }
  
  .hero-section {
    padding: 48px 0 32px;
  }
  
  .featured-grid {
    grid-template-columns: 1fr;
  }
  
  .main-content {
    padding: 0 16px;
  }
}
</style>