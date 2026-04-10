<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import BrandMark from './BrandMark.vue'

const router = useRouter()
const currentYear = new Date().getFullYear()
const isAdmin = computed(() => (localStorage.getItem('userRole') || '').toLowerCase() === 'admin')
</script>

<template>
  <footer class="site-footer">
    <div class="footer-container">
      <div class="footer-brand">
        <BrandMark class="footer-mark" :size="44" />

        <div class="footer-brand-copy">
          <span class="brand-text">ERWANG</span>
          <p class="footer-desc">写工程实践，也记下值得回看的阅读线索。希望每次打开，都能安静地读完一篇。</p>
        </div>
      </div>

      <div class="footer-nav" aria-label="页脚导航">
        <button type="button" class="footer-link-btn" @click="router.push('/')">首页</button>
        <button type="button" class="footer-link-btn" @click="router.push('/about')">关于</button>
        <button v-if="isAdmin" type="button" class="footer-link-btn" @click="router.push('/admin')">管理</button>
      </div>

      <div class="footer-bottom">
        <span class="footer-meta">个人技术写作 · 阅读优先</span>
        <p class="copyright">© {{ currentYear }} Erwang. All rights reserved.</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  margin-top: auto;
  padding: 32px 0 28px;
  background: linear-gradient(180deg, rgba(250, 249, 247, 0.3), rgba(245, 243, 239, 0.9));
  border-top: 1px solid color-mix(in srgb, var(--border-strong) 82%, white 18%);
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) auto;
  gap: 24px 32px;
  align-items: end;
}

.footer-brand {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.footer-brand-copy {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.brand-text {
  font-size: 17px;
  line-height: 1;
  letter-spacing: 0.16em;
  font-family: var(--heading);
  color: var(--text-h);
}

.footer-desc {
  max-width: 40ch;
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.7;
}

.footer-nav {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.footer-link-btn {
  min-height: 38px;
  padding: 0 14px;
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--text);
  font-size: 13px;
  font-family: var(--heading);
  letter-spacing: 0.04em;
}

.footer-link-btn:hover {
  color: var(--text-h);
  background: rgba(255, 255, 255, 0.88);
  border-color: color-mix(in srgb, var(--accent) 16%, var(--border-strong));
}

.footer-bottom {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px 18px;
  padding-top: 12px;
  border-top: 1px solid rgba(45, 90, 74, 0.08);
}

.footer-meta,
.copyright {
  color: var(--text-muted);
  font-size: 12px;
  letter-spacing: 0.04em;
}

.copyright {
  margin: 0;
}

@media (max-width: 768px) {
  .site-footer {
    padding-top: 26px;
  }

  .footer-container {
    grid-template-columns: 1fr;
    padding: 0 16px;
  }

  .footer-nav {
    justify-content: flex-start;
  }

  .footer-bottom {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
