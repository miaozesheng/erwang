<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, House, Connection, Star, StarFilled, Management, Operation, SwitchButton, User } from '@element-plus/icons-vue'
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
    { icon: House, label: '首页', path: '/' },
    { icon: Connection, label: 'GitHub', path: '/github' }
  ]
  if (isLoggedIn.value) {
    base.push(
      { icon: Star, label: '收藏', path: '/favorites' }
    )
    if (isAdmin.value) {
      base.push({ icon: Operation, label: '管理', path: '/admin' })
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
  <header class="site-header" data-site-header>
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
          <el-icon class="tab-icon"><component :is="item.icon" /></el-icon>
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
                <el-icon class="menu-icon"><User /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <el-icon class="menu-icon logout"><SwitchButton /></el-icon>
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
            <el-icon class="mobile-nav-icon"><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </button>

          <div class="mobile-nav-divider"></div>

          <button v-if="isLoggedIn" type="button" class="mobile-nav-item" @click="router.push('/profile'); mobileMenuOpen = false">
            <el-icon class="mobile-nav-icon"><User /></el-icon>
            <span>个人中心</span>
          </button>
          <button v-if="isLoggedIn" type="button" class="mobile-nav-item logout" @click="handleLogout(); mobileMenuOpen = false">
            <el-icon class="mobile-nav-icon"><SwitchButton /></el-icon>
            <span>登出账户</span>
          </button>
          <button v-else type="button" class="mobile-nav-item" @click="router.push('/login'); mobileMenuOpen = false">
            <el-icon class="mobile-nav-icon"><ArrowRight /></el-icon>
            <span>立即登录</span>
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
  background: rgba(255, 255, 255, 0.82);
  border-bottom: 1px solid var(--border);
  backdrop-filter: blur(20px);
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.02);
}

.header-container {
  max-width: var(--shell-default);
  margin: 0 auto;
  padding: 0 var(--page-gutter);
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  align-items: center;
  gap: 24px;
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
  cursor: pointer;
}

.logo-copy {
  display: flex;
  flex-direction: column;
}

.logo-eyebrow {
  font-size: 10px;
  line-height: 1;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--text-muted);
  font-family: var(--mono);
}

.logo-text {
  font-size: 19px;
  line-height: 1.2;
  letter-spacing: 0.15em;
  font-family: var(--heading);
  font-weight: 800;
  color: var(--text-h);
}

.nav-links {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-self: center;
  gap: 4px;
  padding: 4px;
  border-radius: var(--radius-full);
  background: var(--bg-elevated);
  border: 1px solid var(--border);
}

.nav-indicator {
  position: absolute;
  inset-block: 4px;
  left: 0;
  border-radius: var(--radius-full);
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06), 0 0 0 1px rgba(0, 0, 0, 0.02);
  transition:
    transform var(--duration-slow) var(--ease-emphasis),
    width var(--duration-slow) var(--ease-emphasis);
}

.nav-link {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 600;
  font-family: var(--heading);
  cursor: pointer;
  transition: all 0.2s;
}

.tab-icon {
  font-size: 15px;
  color: var(--text-muted);
}

.nav-link:hover {
  color: var(--text-h);
}

.nav-link.active {
  color: var(--accent);
}

.nav-link.active .tab-icon {
  color: var(--accent);
}

.header-actions {
  display: flex;
  align-items: center;
}

.avatar-button {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1.5px solid var(--border);
  background: #ffffff;
  padding: 0;
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: all 0.2s;
}

.avatar-button:hover {
  border-color: var(--accent);
  transform: translateY(-1px);
}

.menu-icon {
  margin-right: 8px;
  font-size: 16px;
  color: var(--text-muted);
}

.menu-icon.logout {
  color: var(--danger);
}

.mobile-toggle {
  display: none;
  width: 40px;
  height: 40px;
  border: 1.5px solid var(--border);
  border-radius: 12px;
  background: #ffffff;
  position: relative;
  padding: 0;
}

/* --- Mobile Menu --- */
.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(8px);
  z-index: 99;
  display: flex;
  justify-content: flex-end;
  padding: 80px 16px 16px;
}

.mobile-nav {
  width: min(320px, 100%);
  background: #ffffff;
  border-radius: 24px;
  border: 1px solid var(--border);
  padding: 24px;
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mobile-nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  border: none;
  background: transparent;
  color: var(--text-muted);
  font-weight: 600;
  font-family: var(--heading);
  text-align: left;
  cursor: pointer;
}

.mobile-nav-item.active {
  background: var(--accent-bg);
  color: var(--accent);
}

.mobile-nav-icon {
  font-size: 18px;
}

@media (max-width: 920px) {
  .nav-links, .header-actions { display: none; }
  .mobile-toggle { display: block; }
}
</style>
