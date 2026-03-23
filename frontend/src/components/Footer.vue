<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const currentYear = new Date().getFullYear()
</script>

<template>
  <footer class="site-footer">
    <div class="footer-container">
      <div class="footer-content">
        <div class="footer-brand">
          <span class="brand-icon">◈</span>
          <span class="brand-text">Erwang Blog</span>
        </div>
        <p class="footer-desc">探索技术世界，分享Coding乐趣</p>
        <div class="footer-links">
          <a @click="router.push('/')">首页</a>
          <span class="divider">|</span>
          <a @click="router.push('/admin')">管理</a>
        </div>
        <p class="copyright">© {{ currentYear }} Erwang. All rights reserved.</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  background: var(--bg);
  border-top: 1px solid var(--border);
  padding: 48px 0 24px;
  margin-top: auto;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-content {
  text-align: center;
}

.footer-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.brand-icon {
  font-size: 24px;
  color: var(--accent);
  text-shadow: 0 0 15px var(--accent);
}

.brand-text {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-h);
}

.footer-desc {
  color: var(--text);
  font-size: 14px;
  margin-bottom: 16px;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 16px;
}

.footer-links a {
  color: var(--accent);
  cursor: pointer;
  font-size: 14px;
  transition: opacity 0.3s;
}

.footer-links a:hover {
  opacity: 0.8;
}

.footer-links .divider {
  color: var(--border);
}

.copyright {
  color: var(--text);
  font-size: 13px;
  opacity: 0.6;
}

@media (max-width: 768px) {
  .footer-container {
    padding: 0 16px;
  }
}
</style>