<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  loading.value = true
  try {
    const res = await login(form)
    localStorage.setItem('token', res.data.token)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    console.error('Login failed:', e)
    ElMessage.error('用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page-container">
    <Header />
    
    <main class="main-content">
      <div class="login-card">
        <div class="login-header">
          <span class="login-icon">◈</span>
          <h1 class="login-title">登录</h1>
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
        </el-form>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script>
import { User, Lock } from '@element-plus/icons-vue'
export default {
  components: { User, Lock }
}
</script>

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

.login-card {
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 48px;
  width: 100%;
  max-width: 400px;
  animation: slideUp 0.4s ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-icon {
  font-size: 48px;
  color: var(--accent);
  text-shadow: 0 0 30px var(--accent);
  display: block;
  margin-bottom: 16px;
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-h);
  margin: 0;
}

.login-form {
  margin-top: 24px;
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
  border: none;
  font-size: 16px;
  font-weight: 600;
  margin-top: 16px;
  box-shadow: 0 0 20px rgba(170, 59, 255, 0.3);
}

.login-btn:hover {
  background: var(--accent-hover, #b850ff);
  box-shadow: 0 0 30px rgba(170, 59, 255, 0.5);
}

@media (max-width: 480px) {
  .login-card {
    padding: 32px 24px;
  }
}
</style>