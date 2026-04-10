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
      <div class="auth-container">
        <div class="login-header">
          <h1 class="login-title">登录</h1>
          <p class="login-subtitle">欢迎回来，请登录您的账户</p>
        </div>
        
        <el-form :model="form" class="login-form" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input 
              v-model="form.username" 
              placeholder="用户名"
              size="large"
              :prefix-icon="User"
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
}

.auth-page {
  background: var(--bg);
  min-height: 100vh;
}

.auth-container {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 56px;
  width: 100%;
  max-width: 440px;
  animation: slideUp 0.4s ease;
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
  transform: translateY(-3px);
}

.login-bot.is-username-focused .bot-eye {
  transform: scale(1.15);
  filter: drop-shadow(0 0 12px rgba(0, 240, 255, 1));
}

.login-bot.is-username-focused .bot-arm-right {
  transform: rotate(-18deg);
}

.login-bot.is-username-focused .bot-arm-left {
  transform: rotate(18deg);
}

.login-bot.is-password-focused .bot-body {
  transform: rotate(-4deg) translateY(2px);
}

.login-bot.is-password-focused .bot-arm-left {
  transform: translate(34px, -72px) rotate(-128deg);
}

.login-bot.is-password-focused .bot-arm-right {
  transform: translate(-34px, -72px) rotate(128deg);
}

.login-bot.is-password-focused .bot-eye {
  transform: scaleY(0.1);
  opacity: 0.01;
  filter: none;
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

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-h);
  margin: 0;
  font-family: var(--heading);
}

.login-subtitle {
  margin: 8px 0 0;
  color: var(--text);
  font-size: 14px;
}

.login-form {
  margin-top: 24px;
  position: relative;
  z-index: 1;
}

:deep(.el-input__wrapper) {
  background: #ffffff !important;
  border: 1px solid var(--border) !important;
  box-shadow: none !important;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  border-color: var(--accent) !important;
}

:deep(.el-input__inner) {
  color: var(--text-h);
}

:deep(.el-input__inner::placeholder) {
  color: var(--text-dimmed, #9ca3af);
}

.login-btn {
  width: 100%;
  height: 48px;
  background: var(--accent);
  border-color: var(--accent);
  font-size: 16px;
  font-weight: 600;
  margin-top: 16px;
  color: #ffffff;
  letter-spacing: 0.8px;
  font-family: var(--heading);
  transition: all var(--duration-normal) ease;
}

.login-btn:hover {
  background: var(--accent-hover);
  border-color: var(--accent-hover);
}

.login-btn:active {
  transform: scale(0.97);
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
