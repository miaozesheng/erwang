<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const focusedField = ref('')

const form = reactive({
  username: '',
  password: ''
})

const botState = computed(() => ({
  'is-username-focused': focusedField.value === 'username',
  'is-password-focused': focusedField.value === 'password'
}))

const setFocusedField = (field) => {
  focusedField.value = field
}

const clearFocusedField = (field) => {
  if (focusedField.value === field) {
    focusedField.value = ''
  }
}

onMounted(() => {
  if (route.query.username) {
    form.username = route.query.username
  }
  if (route.query.registered) {
    ElMessage.success('注册成功，请登录')
  }
})

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = await login(form)
    if (res.data?.code !== 200) {
      throw new Error(res.data?.message || '登录失败')
    }

    const data = res.data?.data || {}
    const token = data.token
    const role = data.user?.role || data.role || ''

    if (!token) {
      throw new Error('登录凭证缺失')
    }

    localStorage.setItem('token', token)
    if (role) {
      localStorage.setItem('userRole', role.toLowerCase())
    } else {
      localStorage.removeItem('userRole')
    }

    window.dispatchEvent(new Event('auth-changed'))

    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page-container">
    <Header />

    <main class="main-content auth-page">
      <section class="auth-container">
        <div class="auth-sheen" aria-hidden="true"></div>

        <header class="login-header">
          <div class="login-bot" :class="botState" aria-hidden="true">
            <svg class="login-bot-svg" viewBox="0 0 180 180" role="img">
              <defs>
                <linearGradient id="loginBotShell" x1="18%" y1="14%" x2="82%" y2="92%">
                  <stop offset="0%" stop-color="#fffdf8" />
                  <stop offset="100%" stop-color="#f0ece5" />
                </linearGradient>
                <linearGradient id="loginBotFace" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#f7f4ee" />
                  <stop offset="100%" stop-color="#ebe5da" />
                </linearGradient>
              </defs>

              <circle cx="90" cy="88" r="58" fill="rgba(45, 90, 74, 0.07)" />

              <g transform="translate(90 26)">
                <rect x="-2" y="0" width="4" height="18" rx="2" fill="rgba(45, 90, 74, 0.4)" />
                <circle class="bot-antenna-tip" cx="0" cy="-4" r="5" fill="var(--accent)" />
              </g>

               <g class="bot-body" transform="translate(30 38)">
                 <rect x="18" y="76" width="84" height="40" rx="20" fill="rgba(45, 90, 74, 0.08)" />
                 <rect x="8" y="12" width="104" height="86" rx="34" fill="url(#loginBotShell)" stroke="rgba(45, 90, 74, 0.18)" stroke-width="2" />
                 <rect class="bot-face" x="22" y="24" width="76" height="48" rx="24" fill="url(#loginBotFace)" stroke="rgba(45, 90, 74, 0.1)" />
                 <circle class="bot-eye bot-eye-left" cx="50" cy="48" r="7.5" fill="var(--accent)" />
                 <circle class="bot-eye bot-eye-right" cx="70" cy="48" r="7.5" fill="var(--accent)" />
                 <!-- Hands covering eyes when password focused -->
                 <g class="bot-hands" :class="{ 'is-password-focused': focusedField.value === 'password' }">
                   <path class="bot-hand-left" d="M30 40q10 -15 25 0t25 0" stroke="var(--accent)" stroke-width="6" fill="none" stroke-linecap="round"/>
                   <path class="bot-hand-right" d="M70 40q-10 -15 -25 0t-25 0" stroke="var(--accent)" stroke-width="6" fill="none" stroke-linecap="round"/>
                 </g>
                 <path d="M48 66c6 5 18 5 24 0" stroke="rgba(87, 72, 58, 0.72)" stroke-width="2.5" stroke-linecap="round" />
                 <circle cx="35" cy="60" r="4" fill="rgba(184, 135, 112, 0.18)" />
                 <circle cx="85" cy="60" r="4" fill="rgba(184, 135, 112, 0.18)" />
               </g>

              <g class="bot-arm bot-arm-left" transform="translate(42 104)">
                <rect x="0" y="0" width="18" height="54" rx="9" fill="#e8dfd1" stroke="rgba(45, 90, 74, 0.14)" stroke-width="1.5" />
                <circle cx="9" cy="56" r="8" fill="#f3ede3" stroke="rgba(45, 90, 74, 0.14)" stroke-width="1.2" />
              </g>

              <g class="bot-arm bot-arm-right" transform="translate(120 104)">
                <rect x="0" y="0" width="18" height="54" rx="9" fill="#e8dfd1" stroke="rgba(45, 90, 74, 0.14)" stroke-width="1.5" />
                <circle cx="9" cy="56" r="8" fill="#f3ede3" stroke="rgba(45, 90, 74, 0.14)" stroke-width="1.2" />
              </g>
            </svg>
          </div>

          <p class="login-kicker">账户登录</p>
          <h1 class="login-title">欢迎回来</h1>
          <p class="login-subtitle">继续阅读、整理收藏，或回到你的个人主页。</p>
        </header>

        <el-form :model="form" class="login-form" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input
              v-model="form.username"
              class="auth-input"
              placeholder="用户名"
              size="large"
              :prefix-icon="User"
              @focus="setFocusedField('username')"
              @blur="clearFocusedField('username')"
            />
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="form.password"
              class="auth-input password-input"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              @focus="setFocusedField('password')"
              @blur="clearFocusedField('password')"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            登录
          </el-button>

          <p class="register-entry">
            还没有账号？
            <router-link to="/register" class="register-link">立即注册</router-link>
          </p>
        </el-form>
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
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(32px, 6vw, 72px) 24px;
}

.auth-page {
  position: relative;
  background: var(--bg);
}

.auth-container {
  position: relative;
  width: min(100%, 480px);
  padding: clamp(30px, 5vw, 48px);
  overflow: hidden;
  border-radius: 28px;
  border: 1px solid color-mix(in srgb, var(--border-strong) 84%, white 16%);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 245, 239, 0.94));
  box-shadow: var(--shadow-lg);
}

.auth-container::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.75);
  pointer-events: none;
}

.auth-sheen {
  position: absolute;
  inset: -80px auto auto -40px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(45, 90, 74, 0.12), rgba(45, 90, 74, 0));
  pointer-events: none;
}

.login-header {
  position: relative;
  z-index: 1;
  text-align: center;
  margin-bottom: 28px;
}

.login-bot {
  width: 172px;
  height: 168px;
  margin: 0 auto 18px;
  filter: drop-shadow(0 18px 26px rgba(113, 92, 68, 0.14));
  animation: bot-float 3.2s ease-in-out infinite;
}

.login-bot.is-password-focused {
  animation-play-state: paused;
}

.login-bot-svg {
  width: 100%;
  height: 100%;
  overflow: visible;
}

.bot-body {
  transform-origin: 82px 92px;
  transition: transform var(--duration-slow) var(--ease-emphasis);
}

.bot-face {
  transition: transform var(--duration-normal) var(--ease-out);
}

.bot-eye {
  transform-origin: center;
  transition:
    transform var(--duration-normal) var(--ease-out),
    opacity var(--duration-normal) var(--ease-out),
    filter var(--duration-normal) var(--ease-out);
  filter: drop-shadow(0 2px 8px rgba(45, 90, 74, 0.28));
}

.bot-antenna-tip {
  animation: bot-pulse 1.8s ease-in-out infinite;
}

.bot-arm {
   transform-box: fill-box;
   transform-origin: center 8px;
   transition: transform var(--duration-slow) var(--ease-emphasis);
 }

.bot-hands {
   opacity: 0;
   transition: opacity var(--duration-normal) var(--ease-out);
 }

.bot-hands.is-password-focused {
   opacity: 1;
}

.bot-hand-left,
.bot-hand-right {
   transform-origin: bottom;
   transition: transform var(--duration-normal) var(--ease-out);
}

.bot-hands.is-password-focused .bot-hand-left {
   transform: rotate(-15deg) translateX(-5px) translateY(-10px);
}

.bot-hands.is-password-focused .bot-hand-right {
   transform: rotate(15deg) translateX(5px) translateY(-10px);
}

.bot-arm-left {
  transform: rotate(10deg);
}

.bot-arm-right {
  transform: rotate(-10deg);
}

.login-bot.is-username-focused .bot-face {
  transform: translateY(-2px);
}

.login-bot.is-username-focused .bot-eye {
  transform: scale(1.12);
  filter: drop-shadow(0 2px 10px rgba(45, 90, 74, 0.4));
}

.login-bot.is-username-focused .bot-arm-left {
  transform: rotate(16deg) translateY(-1px);
}

.login-bot.is-username-focused .bot-arm-right {
  transform: rotate(-16deg) translateY(-1px);
}

.login-bot.is-password-focused .bot-body {
  transform: rotate(-2deg) translateY(2px);
}

.login-bot.is-password-focused .bot-arm-left {
  transform: translate(32px, -68px) rotate(-118deg);
}

.login-bot.is-password-focused .bot-arm-right {
  transform: translate(-32px, -68px) rotate(118deg);
}

.login-bot.is-password-focused .bot-eye {
  transform: scaleY(0.12);
  opacity: 0.04;
  filter: none;
}

.login-kicker {
  margin-bottom: 10px;
  font-size: 11px;
  line-height: 1;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: var(--text-muted);
}

.login-title {
  font-size: clamp(30px, 4vw, 36px);
  letter-spacing: -0.03em;
  color: var(--text-h);
}

.login-subtitle {
  margin: 12px auto 0;
  max-width: 28ch;
  color: var(--text-muted);
  font-size: 15px;
  line-height: 1.7;
}

.login-form {
  position: relative;
  z-index: 1;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.auth-input :deep(.el-input__wrapper) {
  min-height: 52px;
  padding-inline: 14px;
}

.auth-input :deep(.el-input__prefix) {
  color: var(--text-muted);
}

.login-btn {
  width: 100%;
  min-height: 52px;
  margin-top: 6px;
  font-size: 15px;
  letter-spacing: 0.08em;
}

.register-entry {
  margin: 16px 0 0;
  text-align: center;
  color: var(--text-muted);
  font-size: 13px;
}

.register-link {
  margin-left: 4px;
  font-family: var(--heading);
}

@keyframes bot-float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6px);
  }
}

@keyframes bot-pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.72;
  }
  50% {
    transform: scale(1.12);
    opacity: 1;
  }
}

@media (max-width: 560px) {
  .main-content {
    padding-inline: 16px;
  }

  .auth-container {
    border-radius: 24px;
    padding: 28px 22px;
  }

  .login-bot {
    width: 146px;
    height: 144px;
    margin-bottom: 14px;
  }

  .login-subtitle {
    font-size: 14px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .login-bot,
  .bot-body,
  .bot-face,
  .bot-eye,
  .bot-arm,
  .bot-antenna-tip {
    animation: none !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
