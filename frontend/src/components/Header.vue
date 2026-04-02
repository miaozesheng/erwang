<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled } from '@element-plus/icons-vue'
import { getUserInfo, resolveFileUrl } from '../api'

const router = useRouter()

const normalizeRole = (role) => (role || '').toLowerCase()
const hasToken = () => !!localStorage.getItem('token')

const isLoggedIn = ref(hasToken())
const userRole = ref(normalizeRole(localStorage.getItem('userRole') || ''))
const userInfo = ref({
  avatar: ''
})

const displayAvatar = computed(() => resolveFileUrl(userInfo.value.avatar))

const isAdmin = computed(() => normalizeRole(userRole.value) === 'admin')

const navItems = computed(() => {
  const base = [
    { icon: '◈', label: '首页', path: '/' },
    { icon: '⟐', label: '关于', path: '/about' },
  ]
  if (isLoggedIn.value) {
    base.push(
      { icon: '⭐', label: '收藏', path: '/favorites' },
      { icon: '❤', label: '喜欢', path: '/likes' },
    )
    if (isAdmin.value) {
      base.push({ icon: '⚡', label: '管理', path: '/admin' })
    }
  }
  return base
})

const mobileMenuOpen = ref(false)

const navLinksRef = ref(null)
const navTabRefs = ref([])
const indicatorX = ref(0)
const indicatorWidth = ref(0)
const activatingPath = ref('')
const fadingPath = ref('')
const dischargePath = ref('')
const activationTimer = ref(null)
const fadeTimer = ref(null)
const dischargeTimer = ref(null)

const currentPath = computed(() => router.currentRoute.value.path)

const isActiveRoute = (path) => {
  if (path === '/admin') {
    return currentPath.value.startsWith('/admin')
  }
  return currentPath.value === path
}

const activeNavIndex = computed(() => {
  const index = navItems.value.findIndex((item) => isActiveRoute(item.path))
  return index < 0 ? 0 : index
})

const indicatorStyle = computed(() => ({
  transform: `translateX(${indicatorX.value}px)`,
  width: `${indicatorWidth.value}px`
}))

const setNavTabRef = (el, index) => {
  if (el) {
    navTabRefs.value[index] = el
  }
}

const updateIndicator = async () => {
  await nextTick()
  const container = navLinksRef.value
  const target = navTabRefs.value[activeNavIndex.value]
  if (!container || !target) return
  indicatorX.value = target.offsetLeft
  indicatorWidth.value = target.offsetWidth
}

const navigateTo = (path) => {
  if (currentPath.value !== path) {
    const previousPath = currentPath.value

    if (activationTimer.value) {
      clearTimeout(activationTimer.value)
    }
    if (fadeTimer.value) {
      clearTimeout(fadeTimer.value)
    }
    if (dischargeTimer.value) {
      clearTimeout(dischargeTimer.value)
    }

    activatingPath.value = path
    dischargePath.value = path
    fadingPath.value = previousPath

    activationTimer.value = setTimeout(() => {
      activatingPath.value = ''
    }, 420)

    dischargeTimer.value = setTimeout(() => {
      dischargePath.value = ''
    }, 420)

    fadeTimer.value = setTimeout(() => {
      fadingPath.value = ''
    }, 480)

    router.push(path)
    mobileMenuOpen.value = false
  }
}

const fetchUserInfo = async () => {
  isLoggedIn.value = hasToken()
  if (!isLoggedIn.value) return
  try {
    const res = await getUserInfo()
    const user = res.data?.data || {}
    userInfo.value = {
      ...userInfo.value,
      ...user,
      avatar: user.avatar || ''
    }
    if (user.role) {
      userRole.value = normalizeRole(user.role)
      localStorage.setItem('userRole', normalizeRole(user.role))
    }
  } catch (error) {
    console.error('Failed to fetch user info:', error)
  }
}

const syncAuthState = () => {
  isLoggedIn.value = hasToken()
  userRole.value = normalizeRole(localStorage.getItem('userRole') || '')
  if (isLoggedIn.value) {
    fetchUserInfo()
  } else {
    userInfo.value = { avatar: '' }
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userRole')
  isLoggedIn.value = false
  userRole.value = ''
  userInfo.value = { avatar: '' }
  window.dispatchEvent(new Event('auth-changed'))
  router.push('/')
}

const handleLogin = () => {
  router.push('/login')
}

const goToProfile = () => {
  router.push('/profile')
}

onMounted(() => {
  syncAuthState()
  updateIndicator()
  window.addEventListener('resize', updateIndicator)
  window.addEventListener('storage', syncAuthState)
  window.addEventListener('auth-changed', syncAuthState)
})

watch(currentPath, () => {
  updateIndicator()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateIndicator)
  window.removeEventListener('storage', syncAuthState)
  window.removeEventListener('auth-changed', syncAuthState)
  if (activationTimer.value) clearTimeout(activationTimer.value)
  if (fadeTimer.value) clearTimeout(fadeTimer.value)
  if (dischargeTimer.value) clearTimeout(dischargeTimer.value)
})
</script>

<template>
  <header class="site-header">
    <div class="header-scanline"></div>
    <div class="header-container">
      <div class="logo" @click="navigateTo('/')">
        <svg class="logo-svg" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="2" y="2" width="28" height="28" rx="6" stroke="var(--accent)" stroke-width="2" fill="rgba(0,240,255,0.06)"/>
          <path d="M8 11h16M8 16h12M8 21h8" stroke="var(--accent)" stroke-width="2" stroke-linecap="round"/>
          <circle cx="24" cy="21" r="3" fill="var(--accent)" opacity="0.8"/>
        </svg>
        <span class="logo-text">ERWANG</span>
      </div>
      <nav ref="navLinksRef" class="nav-links" aria-label="主导航">
        <div class="nav-energy-indicator" :style="indicatorStyle" aria-hidden="true">
          <span class="indicator-core"></span>
          <span class="indicator-halo"></span>
        </div>
        <div class="nav-data-stream" aria-hidden="true">
          <span class="stream-dot" v-for="dot in 6" :key="dot" :style="{ '--dot-delay': `${dot * 0.5}s` }"></span>
        </div>
        <button
          v-for="(item, index) in navItems"
          :key="item.path"
          :ref="(el) => setNavTabRef(el, index)"
          type="button"
          class="nav-link"
          :class="{
            active: isActiveRoute(item.path),
            'tab-activating': activatingPath === item.path,
            'tab-fading': fadingPath === item.path,
            'tab-discharge': dischargePath === item.path
          }"
          :aria-current="isActiveRoute(item.path) ? 'page' : undefined"
          @click="navigateTo(item.path)"
        >
          <span class="tab-shimmer" aria-hidden="true"></span>
          <span class="tab-discharge-flash" aria-hidden="true"></span>
          <span class="tab-icon" aria-hidden="true">{{ item.icon }}</span>
          <span class="tab-label">{{ item.label }}</span>
        </button>
      </nav>
      <div class="header-actions">
        <el-dropdown v-if="isLoggedIn" trigger="click" class="profile-dropdown" popper-class="cyber-user-dropdown">
          <button class="avatar-button" type="button" aria-label="用户菜单">
            <span class="avatar-ring"></span>
            <el-avatar v-if="displayAvatar" :src="displayAvatar" :size="32" class="header-avatar" />
            <span v-else class="avatar-fallback" aria-hidden="true">
              <el-icon><UserFilled /></el-icon>
            </span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goToProfile">
                <span class="menu-icon">◈</span> 个人中心
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <span class="menu-icon">⏻</span> 登出
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button 
          v-else 
          type="primary" 
          @click="handleLogin"
          class="login-btn"
        >
          登录
        </el-button>
      </div>
      <button type="button" class="mobile-toggle" @click="mobileMenuOpen = !mobileMenuOpen" aria-label="菜单">
        <span class="toggle-bar" :class="{ open: mobileMenuOpen }"></span>
      </button>
    </div>
    <div class="header-corners" aria-hidden="true"></div>

    <Transition name="mobile-menu">
      <div v-if="mobileMenuOpen" class="mobile-overlay" @click.self="mobileMenuOpen = false">
        <nav class="mobile-nav">
          <button
            v-for="item in navItems"
            :key="item.path"
            type="button"
            class="mobile-nav-item"
            :class="{ active: isActiveRoute(item.path) }"
            @click="navigateTo(item.path)"
          >
            <span class="mobile-nav-icon">{{ item.icon }}</span>
            {{ item.label }}
          </button>
          <div class="mobile-nav-divider"></div>
          <button v-if="isLoggedIn" type="button" class="mobile-nav-item" @click="router.push('/profile'); mobileMenuOpen = false">
            <span class="mobile-nav-icon">◈</span> 个人中心
          </button>
          <button v-if="isLoggedIn" type="button" class="mobile-nav-item logout" @click="handleLogout(); mobileMenuOpen = false">
            <span class="mobile-nav-icon">⏻</span> 登出
          </button>
          <button v-else type="button" class="mobile-nav-item" @click="router.push('/login'); mobileMenuOpen = false">
            <span class="mobile-nav-icon">→</span> 登录
          </button>
        </nav>
      </div>
    </Transition>
  </header>
</template>

<style scoped>
.site-header {
  background: linear-gradient(180deg, rgba(7, 11, 20, 0.96), rgba(7, 11, 20, 0.78));
  border-bottom: 1px solid var(--border);
  padding: 16px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(14px);
  overflow: visible;
}

.header-scanline {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  clip-path: inset(0);
  background-image: linear-gradient(100deg, transparent, rgba(0, 240, 255, 0.16), transparent);
  background-size: 32% 100%;
  background-repeat: no-repeat;
  background-position: -42% 0;
  animation: header-scan 5.5s linear infinite;
  pointer-events: none;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: opacity 0.3s, transform 0.3s;
}

.logo:hover {
  opacity: 0.95;
  transform: translateY(-1px);
}

.logo-svg {
  width: 30px;
  height: 30px;
  filter: drop-shadow(0 0 6px rgba(0, 240, 255, 0.4));
  transition: filter 0.3s ease;
}

.logo:hover .logo-svg {
  filter: drop-shadow(0 0 12px rgba(0, 240, 255, 0.6));
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-h);
  letter-spacing: 3px;
  font-family: var(--heading);
}

.nav-links {
  --nav-gap: 12px;
  display: flex;
  gap: var(--nav-gap);
  position: relative;
  padding: 6px 0 12px;
}

.nav-link {
  border: 1px solid rgba(0, 240, 255, 0.2);
  background: linear-gradient(135deg, rgba(8, 18, 34, 0.74), rgba(6, 12, 23, 0.88));
  padding: 10px 16px;
  color: var(--text);
  font-size: 13px;
  font-family: var(--heading);
  text-transform: uppercase;
  letter-spacing: 0.9px;
  cursor: pointer;
  transition: color 0.28s ease, text-shadow 0.28s ease, filter 0.28s ease, border-color 0.28s ease, background 0.28s ease;
  position: relative;
  min-width: 96px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  clip-path: polygon(10px 0, calc(100% - 10px) 0, 100% 50%, calc(100% - 10px) 100%, 10px 100%, 0 50%);
  overflow: hidden;
  isolation: isolate;
}

.nav-link::before {
  content: '';
  position: absolute;
  inset: 1px;
  background: linear-gradient(140deg, rgba(0, 240, 255, 0.1), rgba(0, 240, 255, 0.02) 46%, rgba(114, 255, 220, 0.12));
  opacity: 0.56;
  z-index: -1;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--accent), transparent);
  opacity: 0.18;
  transition: opacity 0.3s ease;
}

.tab-icon,
.tab-label {
  position: relative;
  z-index: 2;
}

.tab-icon {
  color: var(--accent-secondary);
  text-shadow: 0 0 12px rgba(114, 255, 220, 0.34);
}

.tab-label {
  color: inherit;
}

.tab-shimmer {
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, transparent 12%, rgba(255, 255, 255, 0.4) 44%, transparent 76%);
  transform: translateX(-150%);
  transition: transform 0.55s ease;
  mix-blend-mode: screen;
  opacity: 0.3;
}

.tab-discharge-flash {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 50% 60%, rgba(255, 255, 255, 0.88), rgba(255, 255, 255, 0.1) 45%, transparent 76%);
  opacity: 0;
  pointer-events: none;
  z-index: 3;
}

.nav-link:hover {
  color: var(--accent-hover);
  text-shadow: 0 0 10px var(--accent-glow), 0 0 18px rgba(0, 240, 255, 0.25);
  border-color: rgba(0, 240, 255, 0.38);
}

.nav-link:hover .tab-shimmer {
  transform: translateX(160%);
}

.nav-link.active {
  color: var(--accent);
  text-shadow: 0 0 12px rgba(0, 240, 255, 0.55), 0 0 24px rgba(0, 240, 255, 0.3);
  border-color: rgba(0, 240, 255, 0.62);
  background: linear-gradient(135deg, rgba(11, 29, 46, 0.86), rgba(6, 16, 31, 0.9));
  box-shadow: inset 0 0 0 1px rgba(0, 240, 255, 0.16), 0 0 20px rgba(0, 240, 255, 0.18);
}

.nav-link.active::after {
  opacity: 1;
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.5);
  animation: nav-active-pulse 1.8s ease-in-out infinite;
}

.nav-link.tab-fading {
  animation: tab-flicker-dim 0.46s steps(3, end);
}

.nav-link.tab-activating {
  animation: tab-power-up 0.4s ease-out;
}

.nav-link.tab-discharge .tab-discharge-flash {
  animation: tab-discharge-flash 0.4s ease-out;
}

.nav-energy-indicator {
  position: absolute;
  left: 0;
  bottom: 0;
  height: 4px;
  pointer-events: none;
  z-index: 4;
  transition: transform 0.52s cubic-bezier(0.22, 1.35, 0.35, 1), width 0.34s ease;
}

.indicator-core,
.indicator-halo {
  position: absolute;
  inset: 0;
  border-radius: 999px;
}

.indicator-core {
  background: linear-gradient(90deg, rgba(0, 240, 255, 0.24), var(--accent), rgba(114, 255, 220, 0.94));
  box-shadow: 0 0 18px rgba(0, 240, 255, 0.48), 0 0 30px rgba(0, 240, 255, 0.26);
}

.indicator-halo {
  filter: blur(7px);
  opacity: 0.75;
  background: linear-gradient(90deg, transparent, rgba(0, 240, 255, 0.9), transparent);
}

.nav-data-stream {
  position: absolute;
  left: 0;
  right: 0;
  bottom: -3px;
  height: 10px;
  pointer-events: none;
  overflow: hidden;
}

.stream-dot {
  position: absolute;
  left: -10px;
  top: 4px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(114, 255, 220, 0.9);
  box-shadow: 0 0 10px rgba(114, 255, 220, 0.8);
  animation: nav-data-flow 3.4s linear infinite;
  animation-delay: var(--dot-delay);
}

.login-btn {
  background: var(--accent);
  border: 1px solid transparent;
  color: #061018;
  box-shadow: 0 0 16px rgba(0, 240, 255, 0.3);
  font-family: var(--heading);
  text-transform: uppercase;
}

.login-btn:hover {
  background: var(--accent-hover);
  box-shadow: 0 0 24px rgba(0, 240, 255, 0.45);
}

.profile-dropdown {
  display: inline-flex;
}

.avatar-button {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: transparent;
  padding: 0;
  display: grid;
  place-items: center;
  cursor: pointer;
  position: relative;
  transition: transform 0.24s ease;
}

.avatar-button:hover {
  transform: scale(1.08);
}

.avatar-ring {
  position: absolute;
  inset: -2px;
  border-radius: 50%;
  border: 2px solid var(--accent);
  box-shadow: 0 0 12px var(--accent-glow), inset 0 0 8px rgba(0, 240, 255, 0.15);
  animation: ring-pulse 2.5s ease-in-out infinite;
  pointer-events: none;
}

.avatar-button:hover .avatar-ring {
  box-shadow: 0 0 22px var(--accent-glow), 0 0 40px rgba(0, 240, 255, 0.2), inset 0 0 12px rgba(0, 240, 255, 0.2);
}

.header-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
}

.avatar-fallback {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: var(--accent);
  font-size: 18px;
  background: radial-gradient(circle, rgba(0, 240, 255, 0.18), rgba(7, 11, 20, 0.95));
}

.menu-icon {
  margin-right: 8px;
  color: var(--accent);
  font-size: 14px;
}

@keyframes ring-pulse {
  0%, 100% { opacity: 0.7; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.05); }
}

.header-corners {
  position: absolute;
  inset: 6px 12px;
  pointer-events: none;
}

.header-corners::before,
.header-corners::after {
  content: '';
  position: absolute;
  width: 28px;
  height: 14px;
  border-top: 1px solid var(--accent-border);
}

.header-corners::before {
  left: 0;
  top: 0;
  border-left: 1px solid var(--accent-border);
}

.header-corners::after {
  right: 0;
  bottom: 0;
  border-right: 1px solid var(--accent-border);
  border-top: none;
  border-bottom: 1px solid var(--accent-border);
}

@keyframes header-scan {
  0% {
    background-position: -42% 0;
  }
  100% {
    background-position: 142% 0;
  }
}

@keyframes nav-active-pulse {
  0%,
  100% {
    opacity: 0.75;
  }
  50% {
    opacity: 1;
  }
}

@keyframes tab-discharge-flash {
  0% {
    opacity: 0;
    transform: scale(0.95);
  }
  25% {
    opacity: 0.94;
    transform: scale(1.02);
  }
  100% {
    opacity: 0;
    transform: scale(1.08);
  }
}

@keyframes tab-flicker-dim {
  0% { opacity: 1; filter: brightness(1); }
  30% { opacity: 0.42; filter: brightness(0.72); }
  55% { opacity: 0.88; filter: brightness(0.95); }
  100% { opacity: 0.56; filter: brightness(0.76); }
}

@keyframes tab-power-up {
  0% {
    transform: translateY(2px) scale(0.98);
    filter: brightness(0.9);
  }
  55% {
    transform: translateY(-1px) scale(1.03);
    filter: brightness(1.15);
  }
  100% {
    transform: translateY(0) scale(1);
    filter: brightness(1);
  }
}

@keyframes nav-data-flow {
  0% {
    transform: translateX(0) scale(0.7);
    opacity: 0;
  }
  12% {
    opacity: 0.75;
  }
  85% {
    opacity: 0.35;
  }
  100% {
    transform: translateX(520px) scale(1.15);
    opacity: 0;
  }
}

.mobile-toggle {
  display: none;
  width: 36px;
  height: 36px;
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  position: relative;
  flex-shrink: 0;
}

.toggle-bar,
.toggle-bar::before,
.toggle-bar::after {
  display: block;
  width: 18px;
  height: 2px;
  background: var(--accent);
  border-radius: 1px;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  transition: all 0.3s ease;
}

.toggle-bar { top: 50%; margin-top: -1px; }
.toggle-bar::before { content: ''; top: -6px; left: 0; }
.toggle-bar::after { content: ''; top: 6px; left: 0; }

.toggle-bar.open { background: transparent; }
.toggle-bar.open::before { top: 0; transform: rotate(45deg); }
.toggle-bar.open::after { top: 0; transform: rotate(-45deg); }

.mobile-overlay {
  position: fixed;
  inset: 0;
  top: 60px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(8px);
  z-index: 99;
  display: flex;
  justify-content: flex-end;
}

.mobile-nav {
  width: 260px;
  height: 100%;
  background: linear-gradient(180deg, rgba(7, 11, 20, 0.98), rgba(8, 16, 31, 0.96));
  border-left: 1px solid rgba(0, 240, 255, 0.2);
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mobile-nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid rgba(0, 240, 255, 0.12);
  border-radius: 10px;
  background: transparent;
  color: var(--text);
  font-family: var(--heading);
  font-size: 15px;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.mobile-nav-item:hover,
.mobile-nav-item.active {
  color: var(--accent);
  border-color: rgba(0, 240, 255, 0.4);
  background: rgba(0, 240, 255, 0.06);
}

.mobile-nav-item.active {
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.15);
}

.mobile-nav-item.logout {
  color: #f89898;
}

.mobile-nav-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.mobile-nav-divider {
  height: 1px;
  background: rgba(0, 240, 255, 0.12);
  margin: 8px 0;
}

.mobile-menu-enter-active,
.mobile-menu-leave-active {
  transition: opacity 0.25s ease;
}

.mobile-menu-enter-active .mobile-nav,
.mobile-menu-leave-active .mobile-nav {
  transition: transform 0.3s ease;
}

.mobile-menu-enter-from,
.mobile-menu-leave-to {
  opacity: 0;
}

.mobile-menu-enter-from .mobile-nav {
  transform: translateX(100%);
}

.mobile-menu-leave-to .mobile-nav {
  transform: translateX(100%);
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }

  .mobile-toggle {
    display: block;
  }

  .header-actions {
    display: none;
  }

  .header-container {
    padding: 0 16px;
  }

  .logo-text {
    font-size: 20px;
  }
}
</style>
