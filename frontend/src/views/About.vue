<script setup>
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'

const techStack = [
  'Vue 3',
  'Vite',
  'Element Plus',
  'Spring Boot',
  'MyBatis-Plus',
  'MySQL',
  'JavaScript',
  'Java'
]

const socials = [
  { name: 'GitHub', link: 'https://github.com/erwang-blog' }
]
</script>

<template>
  <div class="page-container">
    <Header />

    <main class="main-content">
      <section class="about-card">
        <header class="about-header">
          <h1 class="page-title">关于我</h1>
          <p class="page-subtitle">Full-Stack Developer</p>
        </header>

        <section class="about-section">
          <p class="intro-text">
            记录技术实践，分享学习路径，持续迭代成长。
          </p>
        </section>

        <section class="about-section">
          <h2 class="section-title">技术栈</h2>
          <div class="badge-list">
            <span v-for="tech in techStack" :key="tech" class="tech-badge">{{ tech }}</span>
          </div>
        </section>

        <section class="about-section">
          <h2 class="section-title">社交链接</h2>
          <div class="link-list">
            <a
              v-for="item in socials"
              :key="item.name"
              :href="item.link"
              target="_blank"
              rel="noopener noreferrer"
              class="social-link"
            >
              {{ item.name }}
            </a>
          </div>
        </section>

        <section class="about-section">
          <h2 class="section-title">联系方式</h2>
          <p class="contact-item">Location: 陕西省西安市</p>
        </section>

        <section class="about-section">
          <h2 class="section-title">关于本站</h2>
          <p class="about-content">
            本站基于 Vue 3 + Spring Boot 构建，采用简洁的阅读友好设计。
          </p>
          <p class="about-content">
            前端使用 Vite 构建，Element Plus 组件库，Markdown 渲染支持代码高亮。
          </p>
          <p class="about-content">
            后端使用 Spring Boot + MyBatis-Plus + MySQL，JWT 认证鉴权。
          </p>
        </section>
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
  max-width: 720px;
  margin: 0 auto;
  width: 100%;
  padding: var(--sp-8) var(--sp-6) var(--sp-12);
  box-sizing: border-box;
}

.about-card {
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: var(--sp-8);
  background: #ffffff;
}

.about-header {
  text-align: center;
  padding: var(--sp-6) 0 var(--sp-8);
  border-bottom: 1px solid var(--border);
  margin-bottom: var(--sp-6);
}

.page-title {
  margin: 0 0 var(--sp-2);
  font-size: 32px;
  font-weight: 600;
  color: var(--text-h);
  letter-spacing: 2px;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
  letter-spacing: 1px;
}

.about-section {
  margin-bottom: var(--sp-6);
}

.about-section:last-child {
  margin-bottom: 0;
}

.intro-text {
  margin: 0;
  font-size: 16px;
  color: var(--text);
  line-height: 1.8;
}

.section-title {
  margin: 0 0 var(--sp-3);
  color: var(--text-h);
  font-size: 18px;
  font-weight: 500;
  letter-spacing: 0.5px;
  padding-bottom: var(--sp-2);
  border-bottom: 1px solid var(--border);
}

.badge-list,
.link-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
}

.tech-badge {
  padding: 6px 14px;
  border-radius: var(--radius-full);
  border: 1px solid var(--border);
  background: var(--bg-surface);
  color: var(--text);
  font-size: 13px;
}

.social-link {
  display: inline-flex;
  align-items: center;
  color: var(--text-secondary);
  text-decoration: none;
  border: 1px solid var(--border);
  border-radius: var(--radius-full);
  padding: 8px 16px;
  font-size: 14px;
  transition: border-color var(--duration-normal) var(--ease-out),
              color var(--duration-normal) var(--ease-out);
}

.social-link:hover {
  color: var(--accent);
  border-color: var(--accent);
}

.contact-item {
  margin: 0;
  color: var(--text);
  font-size: 14px;
}

.about-content {
  margin: 0 0 var(--sp-2);
  color: var(--text);
  line-height: 1.8;
  font-size: 15px;
}

.about-content:last-child {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .main-content {
    padding: var(--sp-5) var(--sp-4) var(--sp-8);
  }

  .about-card {
    padding: var(--sp-6);
  }

  .page-title {
    font-size: 26px;
  }

  .about-header {
    padding: var(--sp-4) 0 var(--sp-6);
  }
}
</style>