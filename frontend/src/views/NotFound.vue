<script setup>
import { useRouter } from 'vue-router'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'

const router = useRouter()
</script>

<template>
  <div class="page-container">
    <Header />

    <main class="main-content">
      <section class="not-found-card">
        <h1 class="glitch" data-text="404">404</h1>
        <p class="terminal-msg">&gt; ERROR_NOT_FOUND: 请求的页面不存在或已被迁移。</p>
        <p class="sub-msg">信号丢失，请返回主站继续探索。</p>
        <el-button class="home-btn" @click="router.push('/')">Return to Home</el-button>
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
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
  display: grid;
  place-items: center;
  padding: var(--sp-6);
  box-sizing: border-box;
}

.not-found-card {
  width: min(680px, 100%);
  text-align: center;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
  background: var(--card-bg);
  box-shadow: var(--shadow);
  padding: 42px var(--sp-6);
}

.glitch {
  position: relative;
  margin: 0;
  font-size: clamp(72px, 16vw, 160px);
  line-height: 1;
  color: var(--text-h);
  letter-spacing: 3px;
  text-shadow: 0 0 24px rgba(0, 240, 255, 0.32);
  animation: flicker 2.2s infinite;
}

.glitch::before,
.glitch::after {
  content: attr(data-text);
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.glitch::before {
  color: rgba(0, 240, 255, 0.7);
  transform: translate(-2px, 0);
  clip-path: polygon(0 12%, 100% 0, 100% 48%, 0 56%);
  animation: glitch-shift 1.8s infinite linear alternate-reverse;
}

.glitch::after {
  color: rgba(114, 255, 220, 0.65);
  transform: translate(2px, 0);
  clip-path: polygon(0 52%, 100% 46%, 100% 100%, 0 100%);
  animation: glitch-shift 1.3s infinite linear alternate;
}

.terminal-msg {
  margin: 14px 0 8px;
  color: var(--accent);
  font-family: var(--mono);
}

.sub-msg {
  margin: 0 0 24px;
  color: var(--text);
}

.home-btn {
  border: 1px solid var(--accent);
  background: var(--accent);
  color: #04131a;
  font-family: var(--heading);
  letter-spacing: 0.7px;
  transition:
    transform var(--duration-normal) var(--ease-out),
    box-shadow var(--duration-normal) var(--ease-out),
    background-color var(--duration-normal) var(--ease-out),
    border-color var(--duration-normal) var(--ease-out);
}

.home-btn:hover {
  background: var(--accent-hover);
  border-color: var(--accent-hover);
  box-shadow: 0 0 18px var(--accent-glow);
  transform: translateY(-1px);
}

.home-btn:active {
  transform: scale(0.97);
}

@keyframes glitch-shift {
  0% { transform: translate(0, 0); }
  20% { transform: translate(-2px, 1px); }
  40% { transform: translate(2px, -1px); }
  60% { transform: translate(1px, 0); }
  80% { transform: translate(-1px, 1px); }
  100% { transform: translate(0, 0); }
}

@keyframes flicker {
  0%, 18%, 22%, 62%, 64%, 100% { opacity: 1; }
  20%, 24%, 63% { opacity: 0.72; }
}

@media (max-width: 768px) {
  .main-content {
    padding: var(--sp-4);
  }

  .not-found-card {
    padding: var(--sp-8) var(--sp-5);
  }
}
</style>
