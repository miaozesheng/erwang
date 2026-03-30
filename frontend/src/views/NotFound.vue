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
        <div class="card-corners" aria-hidden="true"></div>
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
  padding: 24px;
  box-sizing: border-box;
}

.not-found-card {
  width: min(680px, 100%);
  position: relative;
  text-align: center;
  border-radius: 16px;
  border: 1px solid rgba(0, 240, 255, 0.22);
  background: linear-gradient(165deg, rgba(10, 20, 38, 0.82), rgba(8, 16, 31, 0.62));
  box-shadow: inset 0 0 0 1px rgba(0, 240, 255, 0.05), var(--shadow);
  padding: 42px 24px;
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
  border: 1px solid transparent;
  background: linear-gradient(130deg, rgba(0, 240, 255, 0.9), rgba(0, 210, 225, 0.84));
  color: #04131a;
  font-family: var(--heading);
  letter-spacing: 0.7px;
}

.home-btn:hover {
  box-shadow: 0 0 18px rgba(0, 240, 255, 0.34);
  transform: translateY(-1px);
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
    padding: 16px;
  }

  .not-found-card {
    padding: 32px 18px;
  }
}
</style>
