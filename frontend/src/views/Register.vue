<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Message, Phone, User } from '@element-plus/icons-vue'
import { register as registerUser } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import BrandMark from '../components/BrandMark.vue'

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
const accountPlaceholder = computed(() => (registerMode.value === 'email' ? '常用邮箱地址' : '常用手机号'))
const accountIcon = computed(() => (registerMode.value === 'email' ? Message : Phone))
const accountHint = computed(() => (
  registerMode.value === 'email'
    ? '用来识别你的账号；只在必要时联系你。'
    : '适合想尽快开始阅读和收藏的你。'
))

const switchMode = (mode) => {
  if (registerMode.value === mode) return
  registerMode.value = mode
  form.account = ''
}

const handleRegister = async () => {
  const username = form.username.trim()
  const account = form.account.trim()

  if (!username) {
    ElMessage.warning('请先填写用户名')
    return
  }

  if (!account) {
    ElMessage.warning(`请填写${accountLabel.value}`)
    return
  }

  if (registerMode.value === 'email' && !/^\S+@\S+\.\S+$/.test(account)) {
    ElMessage.warning('邮箱格式看起来不太对')
    return
  }

  if (registerMode.value === 'phone' && !/^1\d{10}$/.test(account)) {
    ElMessage.warning('手机号格式看起来不太对')
    return
  }

  if (!form.password || !form.confirmPassword) {
    ElMessage.warning('请设置密码，并再确认一次')
    return
  }

  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码还不一致')
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
    ElMessage.success('账号创建成功，请登录')
    router.push({ path: '/login', query: { registered: '1', username } })
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '注册暂时没有完成，请稍后再试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page-container">
    <Header />

    <main class="main-content auth-page">
      <section class="auth-container register-shell">
        <div class="auth-sheen" aria-hidden="true"></div>

        <header class="register-header">
          <div class="register-emblem" aria-hidden="true">
            <span class="register-emblem-ring ring-outer"></span>
            <span class="register-emblem-ring ring-inner"></span>
            <span class="register-note note-left"></span>
            <span class="register-note note-right"></span>
            <BrandMark class="register-mark" :size="72" />
          </div>

          <p class="register-kicker">新读者加入</p>
          <h1 class="register-title">把名字留在这里</h1>
          <p class="register-subtitle">用邮箱或手机号创建账号，之后就能收藏文章、继续阅读，也更容易找到你真正想回看的内容。</p>
        </header>

        <div class="register-mode-switch" role="tablist" aria-label="注册方式">
          <button
            type="button"
            class="mode-tab"
            :class="{ active: registerMode === 'email' }"
            @click="switchMode('email')"
          >
            用邮箱
          </button>
          <button
            type="button"
            class="mode-tab"
            :class="{ active: registerMode === 'phone' }"
            @click="switchMode('phone')"
          >
            用手机号
          </button>
        </div>

        <p class="register-hint">{{ accountHint }}</p>

        <el-form :model="form" class="register-form" @submit.prevent="handleRegister">
          <el-form-item>
            <el-input
              v-model="form.username"
              class="auth-input"
              placeholder="你想被怎么称呼"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="form.account"
              class="auth-input"
              :placeholder="accountPlaceholder"
              size="large"
              :prefix-icon="accountIcon"
            />
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="form.password"
              class="auth-input"
              type="password"
              placeholder="设置登录密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="form.confirmPassword"
              class="auth-input"
              type="password"
              placeholder="再输入一次密码"
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
            创建账号
          </el-button>

          <p class="login-entry">
            已经有账号了？
            <router-link to="/login" class="login-link">回到登录</router-link>
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
  width: min(100%, 500px);
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
  inset: -86px auto auto -48px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(45, 90, 74, 0.12), rgba(45, 90, 74, 0));
  pointer-events: none;
}

.register-header {
  position: relative;
  z-index: 1;
  text-align: center;
  margin-bottom: 26px;
}

.register-emblem {
  position: relative;
  width: 172px;
  height: 150px;
  margin: 0 auto 20px;
  display: grid;
  place-items: center;
  animation: emblem-float 4s ease-in-out infinite;
}

.register-mark {
  position: relative;
  z-index: 2;
  box-shadow: var(--shadow-sm);
}

.register-emblem-ring,
.register-note {
  position: absolute;
  border-radius: 999px;
}

.register-emblem-ring {
  inset: 26px;
  border: 1px solid rgba(45, 90, 74, 0.12);
}

.register-emblem-ring.ring-outer {
  inset: 10px;
  border-style: dashed;
  opacity: 0.7;
}

.register-note {
  width: 54px;
  height: 16px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid color-mix(in srgb, var(--border-strong) 82%, white 18%);
  box-shadow: var(--shadow-sm);
}

.register-note::before,
.register-note::after {
  content: '';
  position: absolute;
  left: 10px;
  right: 10px;
  height: 1px;
  background: rgba(45, 90, 74, 0.18);
}

.register-note::before {
  top: 5px;
}

.register-note::after {
  top: 9px;
}

.register-note.note-left {
  top: 34px;
  left: 6px;
  transform: rotate(-10deg);
}

.register-note.note-right {
  right: 4px;
  bottom: 28px;
  transform: rotate(12deg);
}

.register-kicker {
  margin-bottom: 10px;
  font-size: 11px;
  line-height: 1;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: var(--text-muted);
}

.register-title {
  font-size: clamp(30px, 4vw, 36px);
  letter-spacing: -0.03em;
  color: var(--text-h);
}

.register-subtitle {
  margin: 12px auto 0;
  max-width: 30ch;
  color: var(--text-muted);
  font-size: 15px;
  line-height: 1.7;
}

.register-mode-switch {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  padding: 6px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid color-mix(in srgb, var(--border-strong) 84%, white 16%);
}

.mode-tab {
  min-height: 42px;
  border: 1px solid transparent;
  background: transparent;
  color: var(--text-muted);
  font-family: var(--heading);
  font-size: 13px;
  letter-spacing: 0.04em;
  border-radius: 14px;
  transition:
    color var(--duration-normal) var(--ease-out),
    border-color var(--duration-normal) var(--ease-out),
    background-color var(--duration-normal) var(--ease-out),
    transform var(--duration-fast) var(--ease-out);
}

.mode-tab:hover {
  color: var(--text-h);
  background: rgba(255, 255, 255, 0.84);
}

.mode-tab.active {
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 10%, white 90%);
  border-color: color-mix(in srgb, var(--accent) 22%, var(--border-strong));
  box-shadow: var(--shadow-sm);
}

.register-hint {
  margin: 12px 4px 0;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.6;
}

.register-form {
  position: relative;
  z-index: 1;
  margin-top: 18px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.auth-input :deep(.el-input__wrapper) {
  min-height: 52px;
  padding-inline: 14px;
}

.auth-input :deep(.el-input__prefix) {
  color: var(--text-muted);
}

.register-btn {
  width: 100%;
  min-height: 52px;
  margin-top: 6px;
  font-size: 15px;
  letter-spacing: 0.08em;
}

.login-entry {
  margin: 16px 0 0;
  text-align: center;
  color: var(--text-muted);
  font-size: 13px;
}

.login-link {
  margin-left: 4px;
  font-family: var(--heading);
}

@keyframes emblem-float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
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

  .register-emblem {
    width: 154px;
    height: 138px;
    margin-bottom: 16px;
  }

  .register-subtitle {
    font-size: 14px;
  }

  .register-mode-switch {
    gap: 6px;
    padding: 5px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .register-emblem,
  .mode-tab {
    animation: none !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
