<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const isPasswordFocused = ref(false)
const isUsernameFocused = ref(false)

const form = reactive({
  username: '',
  password: ''
})

onMounted(() => {
  if (route.query.username) {
    form.username = route.query.username
  }
  if (route.query.registered) {
    ElMessage.success('注册成功，请登录')
  }
})

const handlePasswordFocus = () => {
  isPasswordFocused.value = true
}

const handlePasswordBlur = () => {
  isPasswordFocused.value = false
}

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
    router.push('/')
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
    
    <main class="main-content">
      <div class="login-bg-grid" aria-hidden="true"></div>
      <div class="login-card">
        <div class="login-card-corners" aria-hidden="true"></div>
        <div class="login-header">
          <div
            class="login-bot"
            :class="{
              'is-password-focused': isUsernameFocused,
              'is-username-focused': isPasswordFocused
            }"
            aria-hidden="true"
          >
            <svg class="login-bot-svg" viewBox="0 0 180 170" role="img">
              <defs>
                <radialGradient id="botCore" cx="50%" cy="30%" r="85%">
                  <stop offset="0%" stop-color="rgba(0, 240, 255, 0.2)" />
                  <stop offset="100%" stop-color="rgba(7, 18, 34, 0.96)" />
                </radialGradient>
                <linearGradient id="botShell" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#10243d" />
                  <stop offset="100%" stop-color="#09172a" />
                </linearGradient>
              </defs>

              <g class="bot-antenna" transform="translate(90 14)">
                <rect x="-2.5" y="0" width="5" height="20" rx="2.5" fill="rgba(140, 245, 255, 0.7)" />
                <circle class="bot-antenna-tip" cx="0" cy="-3" r="6" fill="var(--accent)" />
              </g>

              <g class="bot-body" transform="translate(24 30)">
                <rect x="0" y="0" width="132" height="108" rx="38" fill="url(#botShell)" stroke="var(--accent)" stroke-opacity="0.65" stroke-width="2" />
                <rect x="10" y="10" width="112" height="88" rx="30" fill="url(#botCore)" />

                <g class="bot-face" transform="translate(66 52)">
                  <circle cx="-26" cy="0" r="17" fill="#04111f" stroke="rgba(0, 240, 255, 0.38)" stroke-width="1.6" />
                  <circle cx="26" cy="0" r="17" fill="#04111f" stroke="rgba(0, 240, 255, 0.38)" stroke-width="1.6" />
                  <circle class="bot-eye bot-eye-left" cx="-26" cy="0" r="7" fill="var(--accent)" />
                  <circle class="bot-eye bot-eye-right" cx="26" cy="0" r="7" fill="var(--accent)" />
                  <rect x="-18" y="27" width="36" height="5" rx="2.5" fill="rgba(0, 240, 255, 0.65)" />
                </g>
              </g>

              <g class="bot-arm bot-arm-left" transform="translate(33 95)">
                <rect x="0" y="0" width="20" height="56" rx="10" fill="#0f2136" stroke="rgba(0, 240, 255, 0.48)" stroke-width="1.5" />
                <circle cx="10" cy="58" r="9" fill="#0f2136" stroke="rgba(0, 240, 255, 0.55)" stroke-width="1.5" />
              </g>
              <g class="bot-arm bot-arm-right" transform="translate(127 95)">
                <rect x="0" y="0" width="20" height="56" rx="10" fill="#0f2136" stroke="rgba(0, 240, 255, 0.48)" stroke-width="1.5" />
                <circle cx="10" cy="58" r="9" fill="#0f2136" stroke="rgba(0, 240, 255, 0.55)" stroke-width="1.5" />
              </g>
            </svg>
          </div>
          <h1 class="login-title">登录</h1>
          <p class="login-terminal">身份认证协议 / AUTH-CHANNEL</p>
        </div>
        
        <el-form :model="form" class="login-form" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input 
              v-model="form.username" 
              placeholder="用户名"
              size="large"
              :prefix-icon="User"
              @focus="isUsernameFocused = true"
              @blur="isUsernameFocused = false"
            />
          </el-form-item>
          
          <el-form-item>
            <el-input 
              class="password-input"
              v-model="form.password" 
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              @focus="handlePasswordFocus"
              @blur="handlePasswordBlur"
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
      </div>
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
  padding: 48px 24px;
  position: relative;
  overflow: hidden;
}

.login-bg-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(0, 240, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.08) 1px, transparent 1px);
  background-size: 36px 36px;
  mask-image: radial-gradient(circle at 50% 50%, black 30%, transparent 100%);
  opacity: 0.5;
}

.login-card {
  background: linear-gradient(170deg, rgba(10, 20, 38, 0.9), rgba(8, 16, 31, 0.7));
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 56px;
  width: 100%;
  max-width: 440px;
  animation: slideUp 0.4s ease;
  box-shadow: 0 18px 42px rgba(0, 0, 0, 0.42), 0 0 30px rgba(0, 240, 255, 0.14);
  position: relative;
  overflow: hidden;
}

.login-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.06), transparent 42%, rgba(114, 255, 220, 0.07));
  pointer-events: none;
}

.login-card-corners {
  position: absolute;
  inset: 10px;
  pointer-events: none;
}

.login-card-corners::before,
.login-card-corners::after {
  content: '';
  position: absolute;
  width: 24px;
  height: 14px;
  border-top: 1px solid var(--accent-border);
  border-left: 1px solid var(--accent-border);
}

.login-card-corners::before {
  top: 0;
  left: 0;
}

.login-card-corners::after {
  right: 0;
  bottom: 0;
  transform: rotate(180deg);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.login-bot {
  width: 170px;
  height: 165px;
  margin: 0 auto 16px;
  filter: drop-shadow(0 0 16px rgba(0, 240, 255, 0.38));
  animation: botFloat 3.2s ease-in-out infinite;
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
  transform-origin: 66px 56px;
  transition: transform 0.38s ease;
}

.bot-face {
  transition: transform 0.3s ease;
}

.bot-eye {
  transform-origin: center;
  transition: transform 0.28s ease, opacity 0.28s ease, filter 0.28s ease;
  filter: drop-shadow(0 0 8px rgba(0, 240, 255, 0.8));
}

.bot-antenna-tip {
  animation: botPulse 1.8s ease-in-out infinite;
}

.bot-arm {
  transform-box: fill-box;
  transform-origin: center top;
  transition: transform 0.36s cubic-bezier(0.32, 0.84, 0.32, 1.08);
}

.bot-arm-left {
  transform: rotate(12deg);
}

.bot-arm-right {
  transform: rotate(-12deg);
}

.login-bot.is-username-focused .bot-face {
  transform: translateY(-6px);
}

.login-bot.is-username-focused .bot-eye {
  transform: translateY(-4px);
}

.login-bot.is-username-focused .bot-arm-right {
  transform: translate(2px, -4px) rotate(-18deg);
  animation: none;
}

.login-bot.is-username-focused .bot-arm-left {
  transform: translate(-1px, -2px) rotate(10deg);
}

.login-bot.is-password-focused .bot-body {
  transform: rotate(-6deg) translateY(4px);
}

.login-bot.is-password-focused .bot-arm-left {
  transform: translate(34px, -72px) rotate(-128deg);
}

.login-bot.is-password-focused .bot-arm-right {
  transform: translate(-34px, -72px) rotate(128deg);
}

.login-bot.is-password-focused .bot-eye {
  transform: scaleY(0.2);
  opacity: 0.01;
}

@keyframes botFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

@keyframes botPulse {
  0%, 100% { opacity: 0.72; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.14); }
}

@keyframes botWave {
  from { transform: translate(-8px, -10px) rotate(-44deg); }
  to { transform: translate(-12px, -8px) rotate(-24deg); }
}

.login-title {
  font-size: 30px;
  font-weight: 700;
  color: var(--text-h);
  margin: 0;
  font-family: var(--heading);
  letter-spacing: 1px;
}

.login-terminal {
  margin: 10px 0 0;
  color: var(--accent);
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 1px;
  opacity: 0.82;
}

.login-form {
  margin-top: 24px;
  position: relative;
  z-index: 1;
}

:deep(.el-input__wrapper) {
  background: var(--code-bg);
  border: 1px solid var(--border);
  box-shadow: none;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  border-color: var(--accent);
}

:deep(.el-input__inner) {
  color: var(--text-h);
}

:deep(.el-input__inner::placeholder) {
  color: var(--text);
}

.login-btn {
  width: 100%;
  height: 48px;
  background: var(--accent);
  border: 1px solid transparent;
  font-size: 16px;
  font-weight: 700;
  margin-top: 16px;
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.3);
  color: #04131a;
  letter-spacing: 0.8px;
  font-family: var(--heading);
  text-transform: uppercase;
}

.login-btn:hover {
  background: var(--accent-hover);
  box-shadow: 0 0 30px rgba(0, 240, 255, 0.5);
}

.register-entry {
  margin: 14px 0 0;
  text-align: center;
  color: var(--text);
  font-size: 13px;
  letter-spacing: 0.3px;
}

.register-link {
  color: var(--accent);
  margin-left: 4px;
  font-family: var(--heading);
  letter-spacing: 0.6px;
}

.register-link:hover {
  color: var(--accent-hover);
}

@media (max-width: 480px) {
  .login-card {
    padding: 32px 24px;
  }

  .login-bot {
    width: 130px;
    height: 126px;
  }
}
</style>
