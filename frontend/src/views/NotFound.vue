<script setup>
import { useRouter } from 'vue-router'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'

const router = useRouter()
</script>

<template>
  <div class="not-found-page">
    <Header />

    <main class="main-content">
      <section class="not-found-card">
        <h1 class="error-code">404</h1>
        <p class="error-message">抱歉，您访问的页面不存在或已被移除。</p>
        <p class="sub-message">您可以返回首页或联系管理员。</p>
        <el-button class="home-btn" @click="router.push('/')">返回首页</el-button>
      </section>
    </main>

    <Footer />
  </div>
</template>

<style scoped>
.not-found-page {
  background: var(--bg);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--sp-6);
}

.not-found-card {
  text-align: center;
  padding: var(--sp-9) var(--sp-6);
}

.error-code {
  margin: 0;
  color: var(--text-h);
  font-size: 120px;
  font-weight: 700;
  line-height: 1;
}

.error-message {
  margin: var(--sp-5) 0 var(--sp-2);
  color: var(--text);
  font-size: 18px;
}

.sub-message {
  margin: 0 0 var(--sp-6);
  color: var(--text-light);
  font-size: 14px;
}

.home-btn {
  background: var(--accent);
  color: #ffffff;
  border: none;
  font-family: var(--heading);
  font-size: 14px;
  padding: 10px 28px;
  border-radius: var(--radius-md);
  transition: all var(--duration-normal) var(--ease-out);
}

.home-btn:hover {
  background: var(--accent-hover);
  transform: translateY(-2px);
}

.home-btn:active {
  transform: scale(0.98);
}

@media (max-width: 768px) {
  .main-content {
    padding: var(--sp-4);
  }

  .not-found-card {
    padding: var(--sp-7) var(--sp-5);
  }

  .error-code {
    font-size: 80px;
  }
}
</style>