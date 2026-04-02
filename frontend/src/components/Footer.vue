<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentYear = new Date().getFullYear()
const isAdmin = computed(() => (localStorage.getItem('userRole') || '').toLowerCase() === 'admin')
</script>

<template>
  <footer class="site-footer">
    <div class="footer-top-neon" aria-hidden="true"></div>
    <div class="footer-data-stream" aria-hidden="true"></div>
    <div class="footer-scanline" aria-hidden="true"></div>
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
          <span class="divider">|</span>
          <button type="button" class="footer-link-btn" @click="router.push('/about')">关于</button>
          <template v-if="isAdmin">
            <span class="divider">|</span>
            <button type="button" class="footer-link-btn" @click="router.push('/admin')">管理</button>
          </template>
        </div>
        <p class="copyright">© {{ currentYear }} Erwang. All rights reserved.<span class="typing-cursor" aria-hidden="true"></span></p>
      </div>
    </div>
    <div class="footer-corners" aria-hidden="true"></div>
  </footer>
</template>

<style scoped>
.site-footer {
  background: linear-gradient(180deg, rgba(8, 16, 31, 0.52), rgba(7, 11, 20, 0.9));
  padding: 48px 0 24px;
  margin-top: auto;
  position: relative;
  overflow: hidden;
}

.footer-top-neon {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 2px;
  background:
    linear-gradient(90deg, transparent, var(--accent), transparent),
    repeating-linear-gradient(
      90deg,
      rgba(0, 240, 255, 0.08) 0 8px,
      rgba(0, 240, 255, 0.65) 8px 12px,
      transparent 12px 20px
    );
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.55);
  animation: footer-line-stream 1.15s linear infinite;
}

.footer-data-stream {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.2;
  background:
    repeating-linear-gradient(
      90deg,
      transparent 0 38px,
      rgba(0, 240, 255, 0.16) 38px 44px,
      transparent 44px 72px
    ),
    repeating-linear-gradient(
      180deg,
      transparent 0 16px,
      rgba(114, 255, 220, 0.12) 16px 18px,
      transparent 18px 30px
    );
  animation: footer-data-move 9s linear infinite;
}

.footer-scanline {
  position: absolute;
  left: -40%;
  top: 0;
  width: 45%;
  height: 100%;
  background: linear-gradient(95deg, transparent, rgba(0, 240, 255, 0.15), transparent);
  animation: footer-scan 7s linear infinite;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-content {
  text-align: center;
  position: relative;
  z-index: 1;
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
  filter: drop-shadow(0 0 8px rgba(0, 240, 255, 0.4));
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
  margin-bottom: 10px;
  opacity: 0.9;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 16px;
}

.footer-links .footer-link-btn {
  color: var(--accent);
  cursor: pointer;
  font-size: 13px;
  font-family: var(--heading);
  text-transform: uppercase;
  letter-spacing: 0.8px;
  transition: opacity 0.3s, text-shadow 0.3s;
  background: transparent;
  border: none;
  padding: 0;
}

.footer-links .footer-link-btn:hover {
  opacity: 1;
  text-shadow: 0 0 10px var(--accent-glow);
}

.footer-links .divider {
  color: var(--border);
}

.copyright {
  color: var(--text);
  font-size: 13px;
  opacity: 0.66;
  letter-spacing: 0.4px;
  font-family: var(--mono);
}

.footer-corners {
  position: absolute;
  inset: 8px 12px;
  pointer-events: none;
}

.footer-corners::before,
.footer-corners::after {
  content: '';
  position: absolute;
  width: 26px;
  height: 14px;
  border-bottom: 1px solid var(--accent-border);
}

.footer-corners::before {
  left: 0;
  top: 0;
  border-left: 1px solid var(--accent-border);
  border-bottom: none;
  border-top: 1px solid var(--accent-border);
}

.footer-corners::after {
  right: 0;
  bottom: 0;
  border-right: 1px solid var(--accent-border);
}

@keyframes footer-scan {
  0% { transform: translateX(0); }
  100% { transform: translateX(380%); }
}

@keyframes footer-line-stream {
  0% {
    background-position: 0 0, 0 0;
  }
  100% {
    background-position: 0 0, 30px 0;
  }
}

@keyframes footer-data-move {
  0% {
    background-position: 0 0, 0 0;
  }
  100% {
    background-position: 72px 0, 0 30px;
  }
}

@media (max-width: 768px) {
  .footer-container {
    padding: 0 16px;
  }
}
</style>
