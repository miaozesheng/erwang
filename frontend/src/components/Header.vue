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
  avatar: localStorage.getItem('userAvatar') || '',
  avatarVersion: localStorage.getItem('avatarVersion') || ''
})

const displayAvatar = computed(() => resolveFileUrl(userInfo.value.avatar, userInfo.value.avatarVersion))

const isAdmin = computed(() => normalizeRole(userRole.value) === 'admin')

const navItems = computed(() => {
  const base = [
    { icon: '◈', label: '首页', path: '/' },
    { icon: '🔥', label: 'GitHub', path: '/github' },
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
    const avatarVersion = user.updatedAt || user.avatarUpdatedAt || ''
    userInfo.value = {
      ...userInfo.value,
      ...user,
      avatar: user.avatar || '',
      avatarVersion
    }
    if (user.avatar) {
      localStorage.setItem('userAvatar', user.avatar)
    } else {
      localStorage.removeItem('userAvatar')
    }
    if (avatarVersion) {
      localStorage.setItem('avatarVersion', avatarVersion)
    } else {
      localStorage.removeItem('avatarVersion')
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
    userInfo.value = { avatar: '', avatarVersion: '' }
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userRole')
  localStorage.removeItem('userAvatar')
  localStorage.removeItem('avatarVersion')
  isLoggedIn.value = false
  userRole.value = ''
  userInfo.value = { avatar: '', avatarVersion: '' }
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
          <span class="tab-icon" aria-hidden="true">{{ item.icon }}</span>
          <span class="tab-label">{{ item.label }}</span>
        </button>
      </nav>
      <div class="header-actions">
        <el-dropdown v-if="isLoggedIn" trigger="click" class="profile-dropdown" popper-class="cyber-user-dropdown">
          <button class="avatar-button" type="button" aria-label="用户菜单">
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
  background: rgba(7, 11, 20, 0.92);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  padding: 14px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(16px);
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: opacity 220ms ease, transform 220ms ease;
}

.logo:hover {
  opacity: 0.95;
  transform: translateY(-1px);
}

.logo-svg {
  width: 30px;
  height: 30px;
  filter: drop-shadow(0 0 5px rgba(0, 240, 255, 0.2));
  transition: filter 220ms ease;
}

.logo:hover .logo-svg {
  filter: drop-shadow(0 0 9px rgba(0, 240, 255, 0.3));
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-h);
  letter-spacing: 3px;
  font-family: var(--heading);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 2px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.01);
}

.nav-link {
  border: 1px solid transparent;
  background: transparent;
  border-radius: 999px;
  padding: 9px 14px;
  color: var(--text);
  font-size: 13px;
  font-family: var(--heading);
  letter-spacing: 0.55px;
  cursor: pointer;
  transition: color 220ms ease, border-color 220ms ease, background-color 220ms ease, box-shadow 220ms ease, transform 220ms ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.tab-icon,
.tab-label {
  line-height: 1;
}

.tab-icon {
  color: color-mix(in srgb, var(--accent) 72%, var(--text) 28%);
  font-size: 12px;
}

.tab-label {
  color: inherit;
}

.nav-link:hover {
  color: var(--text-h);
  border-color: rgba(0, 240, 255, 0.2);
  box-shadow: inset 0 -1px 0 rgba(0, 240, 255, 0.18), 0 0 10px rgba(0, 240, 255, 0.08);
}

.nav-link.active {
  color: var(--text-h);
  background: rgba(0, 240, 255, 0.1);
  border-color: rgba(0, 240, 255, 0.26);
  box-shadow: inset 0 0 0 1px rgba(0, 240, 255, 0.08);
}

.nav-link.tab-fading {
  opacity: 0.72;
}

.nav-link.tab-activating {
  transform: translateY(-1px);
}

.nav-link.tab-discharge {
  animation: tab-discharge-soft 420ms ease-out;
}

.nav-link:active {
  transform: scale(0.97);
}

.login-btn {
  background: rgba(0, 240, 255, 0.14);
  border: 1px solid rgba(0, 240, 255, 0.32);
  color: var(--text-h);
  box-shadow: none;
  font-family: var(--heading);
  text-transform: none;
  transition: background-color 220ms ease, border-color 220ms ease, color 220ms ease, transform 220ms ease;
}

.login-btn:hover {
  background: rgba(0, 240, 255, 0.2);
  border-color: rgba(0, 240, 255, 0.44);
  color: var(--text-h);
}

.login-btn:active {
  transform: scale(0.97);
}

.profile-dropdown {
  display: inline-flex;
}

.header-actions {
  display: inline-flex;
  align-items: center;
}

.avatar-button {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.02);
  padding: 0;
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: border-color 220ms ease, background-color 220ms ease, transform 220ms ease;
}

.avatar-button:hover {
  border-color: rgba(0, 240, 255, 0.34);
  background: rgba(0, 240, 255, 0.08);
}

.header-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
}

.avatar-fallback {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: color-mix(in srgb, var(--accent) 68%, var(--text-h) 32%);
  font-size: 16px;
  background: rgba(0, 240, 255, 0.12);
}

.menu-icon {
  margin-right: 8px;
  color: var(--accent);
  font-size: 14px;
}

@keyframes tab-discharge-soft {
  0% {
    box-shadow: inset 0 0 0 0 rgba(0, 240, 255, 0);
  }
  40% {
    box-shadow: inset 0 0 0 1px rgba(0, 240, 255, 0.22);
  }
  100% {
    box-shadow: inset 0 0 0 1px rgba(0, 240, 255, 0.08);
  }
}

.mobile-toggle {
  display: none;
  width: 36px;
  height: 36px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.02);
  cursor: pointer;
  position: relative;
  flex-shrink: 0;
  transition: border-color 220ms ease, background-color 220ms ease;
}

.mobile-toggle:hover {
  border-color: rgba(0, 240, 255, 0.32);
  background: rgba(0, 240, 255, 0.08);
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
  top: 66px;
  background: rgba(7, 11, 20, 0.58);
  z-index: 99;
  display: flex;
  justify-content: flex-end;
}

.mobile-nav {
  width: 260px;
  height: 100%;
  background: var(--bg);
  border-left: 1px solid rgba(255, 255, 255, 0.08);
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
  border: 1px solid rgba(255, 255, 255, 0.1);
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
  color: var(--text-h);
  border-color: rgba(0, 240, 255, 0.3);
  background: rgba(0, 240, 255, 0.08);
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
  background: rgba(255, 255, 255, 0.08);
  margin: 8px 0;
}

.mobile-menu-enter-active,
.mobile-menu-leave-active {
  transition: opacity 240ms ease;
}

.mobile-menu-enter-active .mobile-nav,
.mobile-menu-leave-active .mobile-nav {
  transition: transform 260ms ease;
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
    font-size: 18px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .logo,
  .logo-svg,
  .nav-link,
  .login-btn,
  .avatar-button,
  .mobile-toggle,
  .mobile-menu-enter-active,
  .mobile-menu-leave-active,
  .mobile-menu-enter-active .mobile-nav,
  .mobile-menu-leave-active .mobile-nav {
    transition-duration: 1ms;
    animation: none;
  }
}
</style>
