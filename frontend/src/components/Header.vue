<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled } from '@element-plus/icons-vue'
import { getUserInfo, resolveFileUrl } from '../api'
import BrandMark from './BrandMark.vue'

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
    { icon: '◦', label: '首页', path: '/' },
    { icon: '↗', label: 'GitHub', path: '/github' }
  ]
  if (isLoggedIn.value) {
    base.push(
      { icon: '★', label: '收藏', path: '/favorites' },
      { icon: '♥', label: '喜欢', path: '/likes' }
    )
    if (isAdmin.value) {
      base.push({ icon: '◆', label: '管理', path: '/admin' })
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
    }, 380)

    dischargeTimer.value = setTimeout(() => {
      dischargePath.value = ''
    }, 420)

    fadeTimer.value = setTimeout(() => {
      fadingPath.value = ''
    }, 460)

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

watch(navItems, () => {
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
      <button type="button" class="logo" @click="navigateTo('/')" aria-label="回到首页">
        <BrandMark class="logo-mark" :size="40" />
        <span class="logo-copy">
          <span class="logo-eyebrow">personal tech notes</span>
          <span class="logo-text">ERWANG</span>
        </span>
      </button>

      <nav ref="navLinksRef" class="nav-links" aria-label="主导航">
        <span class="nav-indicator" :style="indicatorStyle" aria-hidden="true"></span>
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
        <el-dropdown v-if="isLoggedIn" trigger="click" class="profile-dropdown" popper-class="app-user-dropdown">
          <button class="avatar-button" type="button" aria-label="用户菜单">
            <el-avatar v-if="displayAvatar" :src="displayAvatar" :size="34" class="header-avatar" />
            <span v-else class="avatar-fallback" aria-hidden="true">
              <el-icon><UserFilled /></el-icon>
            </span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goToProfile">
                <span class="menu-icon">◦</span>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <span class="menu-icon">⏻</span>
                登出
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-button
          v-else
          type="primary"
          @click="handleLogin"
          class="header-login-btn"
        >
          登录
        </el-button>
      </div>

      <button
        type="button"
        class="mobile-toggle"
        :aria-expanded="mobileMenuOpen ? 'true' : 'false'"
        aria-label="菜单"
        @click="mobileMenuOpen = !mobileMenuOpen"
      >
        <span class="toggle-bar" :class="{ open: mobileMenuOpen }"></span>
      </button>
    </div>

    <Transition name="mobile-menu">
      <div v-if="mobileMenuOpen" class="mobile-overlay" @click.self="mobileMenuOpen = false">
        <nav class="mobile-nav">
          <div class="mobile-nav-intro">
            <span class="mobile-nav-kicker">导航</span>
            <p class="mobile-nav-title">继续阅读或管理你的内容。</p>
          </div>

          <button
            v-for="item in navItems"
            :key="item.path"
            type="button"
            class="mobile-nav-item"
            :class="{ active: isActiveRoute(item.path) }"
            @click="navigateTo(item.path)"
          >
            <span class="mobile-nav-icon">{{ item.icon }}</span>
            <span>{{ item.label }}</span>
          </button>

          <div class="mobile-nav-divider"></div>

          <button v-if="isLoggedIn" type="button" class="mobile-nav-item" @click="router.push('/profile'); mobileMenuOpen = false">
            <span class="mobile-nav-icon">◦</span>
            <span>个人中心</span>
          </button>
          <button v-if="isLoggedIn" type="button" class="mobile-nav-item logout" @click="handleLogout(); mobileMenuOpen = false">
            <span class="mobile-nav-icon">⏻</span>
            <span>登出</span>
          </button>
          <button v-else type="button" class="mobile-nav-item" @click="router.push('/login'); mobileMenuOpen = false">
            <span class="mobile-nav-icon">→</span>
            <span>登录</span>
          </button>
        </nav>
      </div>
    </Transition>
  </header>
</template>

<style scoped>
.site-header {
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 14px 0;
  background: rgba(250, 249, 247, 0.86);
  border-bottom: 1px solid color-mix(in srgb, var(--border-strong) 82%, white 18%);
  backdrop-filter: blur(16px);
  box-shadow: 0 10px 30px rgba(119, 99, 78, 0.05);
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  align-items: center;
  gap: 18px;
}

.logo {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  border: none;
  background: transparent;
  padding: 0;
  color: inherit;
  text-align: left;
}

.logo:hover {
  transform: translateY(-1px);
}

.logo-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-eyebrow {
  font-size: 11px;
  line-height: 1;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--text-muted);
  font-family: var(--sans);
}

.logo-text {
  font-size: 18px;
  line-height: 1;
  letter-spacing: 0.18em;
  font-family: var(--heading);
  color: var(--text-h);
}

.nav-links {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-self: center;
  gap: 2px;
  padding: 4px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid color-mix(in srgb, var(--border-strong) 82%, white 18%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.75);
}

.nav-indicator {
  position: absolute;
  inset-block: 4px;
  left: 0;
  border-radius: var(--radius-full);
  background: #ffffff;
  border: 1px solid rgba(45, 90, 74, 0.1);
  box-shadow: var(--shadow-sm);
  transition:
    transform var(--duration-slow) var(--ease-emphasis),
    width var(--duration-slow) var(--ease-emphasis);
}

.nav-link {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  min-height: 40px;
  padding: 0 14px;
  border: none;
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--text-muted);
  font-size: 13px;
  letter-spacing: 0.04em;
  font-family: var(--heading);
  transition:
    color var(--duration-normal) var(--ease-out),
    transform var(--duration-fast) var(--ease-out),
    opacity var(--duration-normal) var(--ease-out);
}

.tab-icon,
.tab-label {
  line-height: 1;
}

.tab-icon {
  font-size: 12px;
  color: color-mix(in srgb, var(--accent) 62%, var(--text-muted));
}

.nav-link:hover,
.nav-link.active {
  color: var(--text-h);
}

.nav-link.active .tab-icon {
  color: var(--accent);
}

.nav-link.tab-fading {
  opacity: 0.72;
}

.nav-link.tab-activating {
  transform: translateY(-1px);
}

.nav-link.tab-discharge {
  animation: tab-settle 420ms var(--ease-out);
}

.header-actions,
.profile-dropdown {
  display: inline-flex;
  align-items: center;
}

.header-login-btn {
  min-width: 92px;
}

.avatar-button {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-full);
  border: 1px solid color-mix(in srgb, var(--border-strong) 86%, white 14%);
  background: rgba(255, 255, 255, 0.84);
  padding: 0;
  display: inline-grid;
  place-items: center;
}

.avatar-button:hover {
  background: #ffffff;
  border-color: color-mix(in srgb, var(--accent) 26%, var(--border-strong));
}

.header-avatar,
.avatar-fallback {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-full);
}

.avatar-fallback {
  display: inline-grid;
  place-items: center;
  background: color-mix(in srgb, var(--accent) 10%, white 90%);
  color: var(--accent);
  font-size: 16px;
}

.menu-icon {
  display: inline-flex;
  width: 18px;
  justify-content: center;
  color: var(--accent);
}

.mobile-toggle {
  display: none;
  width: 42px;
  height: 42px;
  border: 1px solid color-mix(in srgb, var(--border-strong) 86%, white 14%);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  position: relative;
  padding: 0;
}

.toggle-bar,
.toggle-bar::before,
.toggle-bar::after {
  position: absolute;
  left: 50%;
  width: 18px;
  height: 2px;
  border-radius: 999px;
  background: var(--text-h);
  transform: translateX(-50%);
  transition:
    transform var(--duration-normal) var(--ease-out),
    opacity var(--duration-fast) var(--ease-out),
    top var(--duration-normal) var(--ease-out),
    background-color var(--duration-normal) var(--ease-out);
}

.toggle-bar {
  top: 50%;
  margin-top: -1px;
}

.toggle-bar::before,
.toggle-bar::after {
  content: '';
}

.toggle-bar::before {
  top: -6px;
}

.toggle-bar::after {
  top: 6px;
}

.toggle-bar.open {
  background: transparent;
}

.toggle-bar.open::before {
  top: 0;
  transform: translateX(-50%) rotate(45deg);
}

.toggle-bar.open::after {
  top: 0;
  transform: translateX(-50%) rotate(-45deg);
}

.mobile-overlay {
  position: fixed;
  inset: 0;
  padding: 82px 14px 14px;
  display: flex;
  justify-content: flex-end;
  background: rgba(62, 47, 28, 0.14);
  backdrop-filter: blur(6px);
  z-index: 99;
}

.mobile-nav {
  width: min(360px, 100%);
  max-height: calc(100vh - 96px);
  overflow: auto;
  padding: 20px;
  border-radius: 24px;
  border: 1px solid color-mix(in srgb, var(--border-strong) 84%, white 16%);
  background: rgba(250, 249, 247, 0.97);
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mobile-nav-intro {
  padding: 4px 4px 10px;
}

.mobile-nav-kicker {
  display: inline-block;
  margin-bottom: 6px;
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--text-muted);
}

.mobile-nav-title {
  color: var(--text-h);
  font-size: 15px;
  line-height: 1.5;
}

.mobile-nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-height: 48px;
  padding: 0 14px;
  border: 1px solid transparent;
  border-radius: 16px;
  background: transparent;
  color: var(--text);
  font-family: var(--heading);
  font-size: 15px;
  letter-spacing: 0.03em;
}

.mobile-nav-item:hover,
.mobile-nav-item.active {
  background: rgba(255, 255, 255, 0.92);
  border-color: color-mix(in srgb, var(--accent) 18%, var(--border-strong));
  color: var(--text-h);
}

.mobile-nav-item.logout {
  color: var(--danger);
}

.mobile-nav-item.logout:hover {
  background: color-mix(in srgb, var(--danger) 8%, white 92%);
  border-color: color-mix(in srgb, var(--danger) 20%, var(--border-strong));
}

.mobile-nav-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  color: var(--accent);
}

.mobile-nav-divider {
  height: 1px;
  margin: 6px 4px;
  background: linear-gradient(90deg, transparent, rgba(45, 90, 74, 0.18), transparent);
}

.mobile-menu-enter-active,
.mobile-menu-leave-active {
  transition: opacity var(--duration-normal) var(--ease-out);
}

.mobile-menu-enter-active .mobile-nav,
.mobile-menu-leave-active .mobile-nav {
  transition: transform var(--duration-slow) var(--ease-emphasis), opacity var(--duration-normal) var(--ease-out);
}

.mobile-menu-enter-from,
.mobile-menu-leave-to {
  opacity: 0;
}

.mobile-menu-enter-from .mobile-nav,
.mobile-menu-leave-to .mobile-nav {
  transform: translateY(-8px) translateX(12px);
  opacity: 0;
}

@keyframes tab-settle {
  0% {
    box-shadow: 0 0 0 0 rgba(45, 90, 74, 0);
  }
  45% {
    box-shadow: 0 0 0 6px rgba(45, 90, 74, 0.06);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(45, 90, 74, 0);
  }
}

@media (max-width: 920px) {
  .header-container {
    grid-template-columns: auto 1fr auto;
  }

  .nav-links,
  .header-actions {
    display: none;
  }

  .mobile-toggle {
    display: inline-block;
    justify-self: end;
  }
}

@media (max-width: 640px) {
  .site-header {
    padding: 12px 0;
  }

  .header-container {
    padding: 0 16px;
    gap: 12px;
  }

  .logo-copy {
    gap: 1px;
  }

  .logo-eyebrow {
    display: none;
  }

  .logo-text {
    font-size: 16px;
  }

  .logo-mark {
    --brand-size: 38px;
    --brand-icon-size: 30px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .site-header,
  .logo,
  .nav-indicator,
  .nav-link,
  .avatar-button,
  .mobile-toggle,
  .toggle-bar,
  .toggle-bar::before,
  .toggle-bar::after,
  .mobile-menu-enter-active,
  .mobile-menu-leave-active,
  .mobile-menu-enter-active .mobile-nav,
  .mobile-menu-leave-active .mobile-nav {
    transition-duration: 0.01ms !important;
    animation: none !important;
  }
}
</style>
