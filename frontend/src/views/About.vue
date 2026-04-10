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
        <h1 class="page-title">关于我</h1>

        <div class="terminal-box">
          <p>&gt; user.init_profile()</p>
          <p>&gt; status: ONLINE</p>
          <p>&gt; role: Full-Stack Developer</p>
          <p>&gt; mission: 记录技术实践，分享学习路径，持续迭代成长。</p>
        </div>

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
          <p class="contact-item">本站基于 Vue 3 + Spring Boot 构建，采用赛博朋克风格设计。</p>
          <p class="contact-item">前端使用 Vite 构建，Element Plus 组件库，Markdown 渲染支持代码高亮。</p>
          <p class="contact-item">后端使用 Spring Boot + MyBatis-Plus + MySQL，JWT 认证鉴权。</p>
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
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
  padding: var(--sp-8) var(--sp-6) var(--sp-12);
  box-sizing: border-box;
}

.about-card {
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: var(--sp-8);
  background: var(--card-bg);
  box-shadow: var(--shadow);
}

.page-title {
  margin: 0 0 var(--sp-6);
  font-size: 34px;
  color: var(--text-h);
  letter-spacing: 1px;
}

.terminal-box {
  margin-bottom: var(--sp-6);
  padding: var(--sp-4);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-accent);
  background: var(--code-bg);
  font-family: var(--mono);
  color: var(--accent);
  line-height: 1.8;
}

.terminal-box p {
  margin: 0;
}

.about-section {
  margin-bottom: var(--sp-6);
}

.about-section:last-child {
  margin-bottom: 0;
}

.section-title {
  margin: 0 0 var(--sp-3);
  color: var(--text-h);
  font-size: 20px;
  letter-spacing: 0.8px;
}

.badge-list,
.link-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
}

.tech-badge {
  padding: 6px 12px;
  border-radius: var(--radius-full);
  border: 1px solid var(--border);
  background: var(--accent-bg);
  color: var(--text);
  font-size: 12px;
  letter-spacing: 0.4px;
}

.social-link {
  color: var(--text-h);
  text-decoration: none;
  border: 1px solid var(--border);
  background: var(--bg-surface);
  border-radius: var(--radius-full);
  padding: 8px 14px;
  transition:
    border-color var(--duration-normal) var(--ease-out),
    background-color var(--duration-normal) var(--ease-out),
    color var(--duration-normal) var(--ease-out),
    transform var(--duration-normal) var(--ease-out);
}

.social-link:hover {
  color: var(--accent-hover);
  border-color: var(--border-accent);
  background: var(--accent-bg);
  transform: translateY(-1px);
}

.contact-item {
  margin: 0 0 var(--sp-2);
  color: var(--text);
  font-family: var(--mono);
}

.contact-item:last-child {
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
    font-size: 28px;
  }
}
</style>
