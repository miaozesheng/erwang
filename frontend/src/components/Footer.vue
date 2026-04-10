<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentYear = new Date().getFullYear()
const isAdmin = computed(() => (localStorage.getItem('userRole') || '').toLowerCase() === 'admin')
</script>

<template>
  <footer class="site-footer">
    <div class="footer-container">
      <div class="footer-content">
        <div class="footer-brand">
          <svg class="footer-logo-svg" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="2" y="2" width="28" height="28" rx="6" stroke="var(--accent)" stroke-width="2" fill="rgba(0,240,255,0.06)"/>
            <path d="M8 11h16M8 16h12M8 21h8" stroke="var(--accent)" stroke-width="2" stroke-linecap="round"/>
            <circle cx="24" cy="21" r="3" fill="var(--accent)" opacity="0.8"/>
          </svg>
          <span class="brand-text">ERWANG</span>
        </div>
        <p class="footer-desc">探索技术世界，分享Coding乐趣</p>
        <div class="footer-links">
          <button type="button" class="footer-link-btn" @click="router.push('/')">首页</button>
          <span class="divider" aria-hidden="true"></span>
          <button type="button" class="footer-link-btn" @click="router.push('/about')">关于</button>
          <template v-if="isAdmin">
            <span class="divider" aria-hidden="true"></span>
            <button type="button" class="footer-link-btn" @click="router.push('/admin')">管理</button>
          </template>
        </div>
        <p class="copyright">© {{ currentYear }} Erwang. All rights reserved.</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  background: var(--bg);
  border-top: 1px solid rgba(0, 240, 255, 0.12);
  padding: 42px 0 24px;
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

.footer-logo-svg {
  width: 24px;
  height: 24px;
  filter: drop-shadow(0 0 6px rgba(0, 240, 255, 0.22));
}

.brand-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-h);
  font-family: var(--heading);
  letter-spacing: 3px;
}

.footer-desc {
  color: var(--text);
  font-size: 14px;
  margin-bottom: 14px;
  opacity: 0.9;
}

.footer-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 16px;
}

.footer-links .footer-link-btn {
  color: var(--text);
  cursor: pointer;
  font-size: 13px;
  font-family: var(--heading);
  letter-spacing: 0.45px;
  transition: color 220ms ease, text-shadow 220ms ease, transform 220ms ease;
  background: transparent;
  border: none;
  border-radius: 999px;
  padding: 6px 10px;
}

.footer-links .footer-link-btn:hover {
  color: var(--text-h);
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.2);
}

.footer-links .footer-link-btn:active {
  transform: scale(0.97);
}

.footer-links .divider {
  width: 1px;
  height: 12px;
  background: rgba(255, 255, 255, 0.16);
  border-radius: 999px;
}

.copyright {
  color: var(--text);
  font-size: 13px;
  opacity: 0.7;
  letter-spacing: 0.4px;
  font-family: var(--mono);
}

@media (max-width: 768px) {
  .footer-container {
    padding: 0 16px;
  }

  .footer-links {
    gap: 8px;
  }
}
</style>
