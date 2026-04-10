<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Message, Phone, User } from '@element-plus/icons-vue'
import { register as registerUser } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'

const router = useRouter()
const loading = ref(false)
const registerMode = ref('email')

const form = reactive({
  username: '',
  account: '',
  password: '',
  confirmPassword: ''
})

const accountLabel = computed(() => (registerMode.value === 'email' ? '邮箱' : '手机号'))
const accountPlaceholder = computed(() => (registerMode.value === 'email' ? '请输入邮箱地址' : '请输入手机号'))
const accountIcon = computed(() => (registerMode.value === 'email' ? Message : Phone))

const switchMode = (mode) => {
  if (registerMode.value === mode) return
  registerMode.value = mode
  form.account = ''
}

const handleRegister = async () => {
  const username = form.username.trim()
  const account = form.account.trim()

  if (!username) {
    ElMessage.warning('请输入用户名')
    return
  }

  if (!account) {
    ElMessage.warning(`请输入${accountLabel.value}`)
    return
  }

  if (registerMode.value === 'email' && !/^\S+@\S+\.\S+$/.test(account)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }

  if (registerMode.value === 'phone' && !/^1\d{10}$/.test(account)) {
    ElMessage.warning('手机号格式不正确')
    return
  }

  if (!form.password || !form.confirmPassword) {
    ElMessage.warning('请输入密码并确认密码')
    return
  }

  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    const payload = {
      username,
      password: form.password,
      email: registerMode.value === 'email' ? account : undefined,
      phone: registerMode.value === 'phone' ? account : undefined
    }
    await registerUser(payload)
    ElMessage.success('注册成功，请登录')
    router.push({ path: '/login', query: { registered: '1', username } })
  } catch (error) {
    console.error('Register failed:', error)
    ElMessage.error(error?.response?.data?.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page-container">
    <Header />

    <main class="main-content">
      <section class="register-card">
        <header class="register-header">
          <div class="register-bot" aria-hidden="true">
            <svg class="register-bot-svg" viewBox="0 0 180 170" role="img">
              <defs>
                <radialGradient id="registerBotCore" cx="50%" cy="35%" r="86%">
                  <stop offset="0%" stop-color="rgba(0, 240, 255, 0.24)" />
                  <stop offset="100%" stop-color="rgba(8, 17, 33, 0.95)" />
                </radialGradient>
                <linearGradient id="registerBotShell" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#143052" />
                  <stop offset="100%" stop-color="#0a1b33" />
                </linearGradient>
              </defs>

              <g transform="translate(90 16)">
                <rect x="-2" y="0" width="4" height="18" rx="2" fill="rgba(140, 245, 255, 0.72)" />
                <circle class="bot-signal" cx="0" cy="-4" r="5" fill="var(--accent)" />
              </g>

              <g transform="translate(22 30)">
                <rect x="0" y="0" width="136" height="106" rx="34" fill="url(#registerBotShell)" stroke="var(--accent)" stroke-opacity="0.66" stroke-width="2" />
                <rect x="10" y="10" width="116" height="86" rx="28" fill="url(#registerBotCore)" />

                <g transform="translate(68 52)">
                  <rect x="-44" y="-20" width="88" height="40" rx="20" fill="#031120" stroke="rgba(0, 240, 255, 0.34)" stroke-width="1.5" />
                  <circle class="eye eye-left" cx="-20" cy="0" r="8" fill="var(--accent)" />
                  <circle class="eye eye-right" cx="20" cy="0" r="8" fill="var(--accent-secondary)" />
                </g>
              </g>

              <g class="register-bot-arm left" transform="translate(30 94)">
                <rect x="0" y="0" width="20" height="52" rx="10" fill="#112540" stroke="rgba(0, 240, 255, 0.45)" stroke-width="1.5" />
                <circle cx="10" cy="54" r="8" fill="#102239" stroke="rgba(0, 240, 255, 0.58)" stroke-width="1.4" />
              </g>
              <g class="register-bot-arm right" transform="translate(130 94)">
                <rect x="0" y="0" width="20" height="52" rx="10" fill="#112540" stroke="rgba(0, 240, 255, 0.45)" stroke-width="1.5" />
                <circle cx="10" cy="54" r="8" fill="#102239" stroke="rgba(0, 240, 255, 0.58)" stroke-width="1.4" />
              </g>
            </svg>
          </div>

          <h1 class="register-title">注册</h1>
          <p class="register-terminal">新身份写入协议 / CREATE-CREW-ID</p>
        </header>

        <div class="register-mode-switch" role="tablist" aria-label="注册方式">
          <button
            type="button"
            class="mode-tab"
            :class="{ active: registerMode === 'email' }"
            @click="switchMode('email')"
          >
            邮箱注册
          </button>
          <button
            type="button"
            class="mode-tab"
            :class="{ active: registerMode === 'phone' }"
            @click="switchMode('phone')"
          >
            手机号注册
          </button>
        </div>

        <el-form :model="form" class="register-form" @submit.prevent="handleRegister">
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
              v-model="form.account"
              :placeholder="accountPlaceholder"
              size="large"
              :prefix-icon="accountIcon"
            />
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="确认密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleRegister"
            />
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
          >
            提交注册
          </el-button>

          <p class="login-entry">
            已有账号？
            <router-link to="/login" class="login-link">立即登录</router-link>
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
  padding: 48px 24px;
  position: relative;
  overflow: hidden;
}

.register-card {
  width: 100%;
  max-width: 432px;
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
  background: rgba(10, 20, 38, 0.85);
  box-shadow: var(--shadow-lg);
  padding: 40px;
  animation: register-slide-up 0.42s ease;
}

.register-header {
  text-align: center;
  margin-bottom: 22px;
  position: relative;
  z-index: 1;
}

.register-bot {
  width: 136px;
  height: 128px;
  margin: 0 auto 12px;
  filter: drop-shadow(0 0 16px rgba(0, 240, 255, 0.35));
  animation: register-bot-float 3.4s ease-in-out infinite;
}

.register-bot-svg {
  width: 100%;
  height: 100%;
  overflow: visible;
}

.bot-signal {
  animation: register-bot-pulse 1.8s ease-in-out infinite;
}

.eye {
  transform-origin: center;
  animation: register-eye-blink 4.2s ease-in-out infinite;
  filter: drop-shadow(0 0 8px rgba(0, 240, 255, 0.82));
}

.register-bot-arm {
  transform-origin: 10px 10px;
  animation: register-arm-swing 2.5s ease-in-out infinite alternate;
}

.register-bot-arm.right {
  animation-delay: 0.5s;
}

.register-title {
  margin: 0;
  font-size: 30px;
  font-weight: 700;
  color: var(--text-h);
  letter-spacing: 1px;
  font-family: var(--heading);
}

.register-terminal {
  margin: 10px 0 0;
  color: var(--accent);
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 1px;
  opacity: 0.82;
}

.register-mode-switch {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 18px;
  position: relative;
  z-index: 1;
}

.mode-tab {
  border: 1px solid var(--border);
  background: rgba(8, 17, 31, 0.8);
  color: var(--text);
  font-family: var(--heading);
  font-size: 12px;
  letter-spacing: 0.6px;
  padding: 10px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition:
    color var(--duration-normal) ease,
    border-color var(--duration-normal) ease,
    background var(--duration-normal) ease;
}

.mode-tab:hover {
  border-color: rgba(0, 240, 255, 0.42);
  color: var(--accent-hover);
}

.mode-tab.active {
  background: var(--accent-bg);
  border-color: var(--border-accent);
  color: var(--accent);
}

.register-form {
  margin-top: 8px;
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

.register-btn {
  width: 100%;
  height: 48px;
  margin-top: 10px;
  border: 1px solid transparent;
  background: var(--accent);
  color: #04131a;
  font-family: var(--heading);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  transition: transform var(--duration-normal) ease, background var(--duration-normal) ease;
}

.register-btn:hover {
  background: var(--accent-hover);
}

.register-btn:active {
  transform: scale(0.97);
}

.login-entry {
  margin: 14px 0 0;
  text-align: center;
  color: var(--text);
  font-size: 13px;
  letter-spacing: 0.3px;
}

.login-link {
  color: var(--accent);
  margin-left: 4px;
  font-family: var(--heading);
  letter-spacing: 0.6px;
}

.login-link:hover {
  color: var(--accent-hover);
}

@keyframes register-slide-up {
  from {
    opacity: 0;
    transform: translateY(28px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes register-bot-float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6px);
  }
}

@keyframes register-bot-pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.72;
  }
  50% {
    transform: scale(1.14);
    opacity: 1;
  }
}

@keyframes register-eye-blink {
  0%,
  44%,
  100% {
    transform: scaleY(1);
  }
  46%,
  48% {
    transform: scaleY(0.2);
  }
}

@keyframes register-arm-swing {
  from {
    transform: rotate(-10deg);
  }
  to {
    transform: rotate(10deg);
  }
}

@media (max-width: 480px) {
  .register-card {
    padding: 32px 24px;
  }
}
</style>
