<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const handleLogout = () => {
  localStorage.removeItem('token')
  router.push('/')
}

const handleLogin = () => {
  router.push('/login')
}
</script>

<template>
  <header class="site-header">
    <div class="header-container">
      <div class="logo" @click="router.push('/')">
        <span class="logo-icon">◈</span>
        <span class="logo-text">Erwang</span>
      </div>
      <nav class="nav-links">
        <a class="nav-link" @click="router.push('/')">首页</a>
        <a class="nav-link" @click="router.push('/admin')">管理</a>
      </nav>
      <div class="header-actions">
        <el-button 
          v-if="isLoggedIn" 
          type="primary" 
          @click="handleLogout"
          class="login-btn"
        >
          登出
        </el-button>
        <el-button 
          v-else 
          type="primary" 
          @click="handleLogin"
          class="login-btn"
        >
          登录
        </el-button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.site-header {
  background: var(--bg);
  border-bottom: 1px solid var(--border);
  padding: 16px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(10px);
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
  transition: opacity 0.3s;
}

.logo:hover {
  opacity: 0.8;
}

.logo-icon {
  font-size: 28px;
  color: var(--accent);
  text-shadow: 0 0 20px var(--accent);
}

.logo-text {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-h);
  letter-spacing: -0.5px;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-link {
  color: var(--text);
  font-size: 15px;
  cursor: pointer;
  transition: color 0.3s;
  position: relative;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--accent);
  transition: width 0.3s;
}

.nav-link:hover {
  color: var(--text-h);
}

.nav-link:hover::after {
  width: 100%;
}

.login-btn {
  background: var(--accent);
  border: none;
  box-shadow: 0 0 15px rgba(170, 59, 255, 0.3);
}

.login-btn:hover {
  background: var(--accent-hover, #b850ff);
  box-shadow: 0 0 25px rgba(170, 59, 255, 0.5);
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }
  
  .header-container {
    padding: 0 16px;
  }
}
</style>