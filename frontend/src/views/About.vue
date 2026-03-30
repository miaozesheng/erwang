<script setup>
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'

const techStack = [
  'Vue 3',
  'Vite',
  'Element Plus',
  'JavaScript',
  'Node.js',
  'MySQL'
]

const socials = [
  { name: 'GitHub', link: 'https://github.com' },
  { name: '掘金', link: 'https://juejin.cn' },
  { name: '知乎', link: 'https://zhihu.com' }
]
</script>

<template>
  <div class="page-container">
    <Header />

    <main class="main-content">
      <section class="about-card">
        <div class="card-corners" aria-hidden="true"></div>
        <h1 class="page-title">关于我</h1>

        <div class="terminal-box">
          <p>&gt; user.init_profile()</p>
          <p>&gt; status: ONLINE</p>
          <p>&gt; role: Full-Stack Developer (placeholder)</p>
          <p>&gt; mission: 记录技术实践，分享学习路径，持续迭代成长。</p>
          <p>&gt; note: 请在 About.vue 中配置你的真实信息</p>
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
          <p class="contact-item">Email: your-email@example.com</p>
          <p class="contact-item">WeChat: your-wechat-id</p>
          <p class="contact-item">Location: 请配置你的位置</p>
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
  padding: 28px 24px 48px;
  box-sizing: border-box;
}

.about-card {
  position: relative;
  border: 1px solid rgba(0, 240, 255, 0.2);
  border-radius: 16px;
  padding: 28px;
  background: linear-gradient(165deg, rgba(10, 20, 38, 0.78), rgba(8, 16, 31, 0.56));
  box-shadow: inset 0 0 0 1px rgba(0, 240, 255, 0.04), var(--shadow);
  overflow: hidden;
}

.card-corners {
  position: absolute;
  inset: 10px;
  pointer-events: none;
}

.card-corners::before,
.card-corners::after {
  content: '';
  position: absolute;
  width: 28px;
  height: 18px;
  border-top: 1px solid var(--accent-border);
  border-left: 1px solid var(--accent-border);
  opacity: 0.66;
}

.card-corners::before {
  left: 0;
  top: 0;
}

.card-corners::after {
  right: 0;
  bottom: 0;
  transform: rotate(180deg);
}

.page-title {
  margin: 0 0 18px;
  font-size: 34px;
  color: var(--text-h);
  letter-spacing: 1px;
  text-shadow: 0 0 16px rgba(0, 240, 255, 0.2);
}

.terminal-box {
  margin-bottom: 24px;
  padding: 16px;
  border-radius: 10px;
  border: 1px solid rgba(0, 240, 255, 0.22);
  background: rgba(6, 13, 24, 0.82);
  font-family: var(--mono);
  color: var(--accent);
  line-height: 1.8;
}

.terminal-box p {
  margin: 0;
}

.about-section {
  margin-bottom: 24px;
}

.about-section:last-child {
  margin-bottom: 0;
}

.section-title {
  margin: 0 0 12px;
  color: var(--text-h);
  font-size: 20px;
  letter-spacing: 0.8px;
}

.badge-list,
.link-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tech-badge {
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid rgba(0, 240, 255, 0.24);
  background: rgba(0, 240, 255, 0.08);
  color: var(--text);
  font-size: 12px;
  letter-spacing: 0.4px;
}

.social-link {
  color: var(--accent);
  text-decoration: none;
  border: 1px solid rgba(0, 240, 255, 0.24);
  background: rgba(0, 240, 255, 0.05);
  border-radius: 8px;
  padding: 8px 12px;
  transition: all 0.25s ease;
}

.social-link:hover {
  border-color: var(--accent);
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.2);
  transform: translateY(-1px);
}

.contact-item {
  margin: 0 0 8px;
  color: var(--text);
  font-family: var(--mono);
}

@media (max-width: 768px) {
  .main-content {
    padding: 20px 16px 32px;
  }

  .about-card {
    padding: 20px;
  }

  .page-title {
    font-size: 28px;
  }
}
</style>
