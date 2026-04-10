<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock, UserFilled, Star, StarFilled, Setting, SwitchButton } from '@element-plus/icons-vue'
import { changePassword, getUserInfo, updateProfile, uploadAvatar, resolveFileUrl } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
const router = useRouter()

const loading = ref(false)
const profileSaving = ref(false)
const passwordSaving = ref(false)
const avatarUploading = ref(false)
const avatarInputRef = ref(null)

const profileEditing = ref(false)
const profileSavedFlash = ref(false)
const securitySavedFlash = ref(false)
const logoutCountdown = ref(0)

const userRole = computed(() => (localStorage.getItem('userRole') || '').toLowerCase())

const sidebarNav = computed(() => [
  { key: 'profile', label: '我的资料', icon: UserFilled, path: '/profile' },
  { key: 'favorites', label: '收藏的文章', icon: Star, path: '/favorites' },
  { key: 'likes', label: '喜欢的文章', icon: StarFilled, path: '/likes' },
  ...(userRole.value === 'admin' ? [{ key: 'admin', label: '管理后台', icon: Setting, path: '/admin' }] : []),
  { key: 'logout', label: '退出登录', icon: SwitchButton, action: 'logout' }
])

const handleSidebarAction = async (item) => {
  if (item.action === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      localStorage.removeItem('token')
      localStorage.removeItem('userRole')
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {
      // User cancelled
    }
  } else if (item.path) {
    if (item.key === 'profile') {
      // Already on profile, do nothing
    } else {
      router.push(item.path)
    }
  }
}

let logoutCountdownTimer = null

const profileForm = reactive({
  nickname: '',
  birthday: '',
  phone: '',
  email: '',
  address: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const userProfile = ref({
  avatar: '',
  avatarVersion: '',
  username: '',
  role: '',
  memberSince: '',
  articlesCount: '--'
})

const displayAvatar = computed(() => resolveFileUrl(userProfile.value.avatar, userProfile.value.avatarVersion))

const formatDisplayDate = (value) => {
  if (!value) return '未设置'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const year = `${date.getFullYear()}`
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}.${month}.${day}`
}

const displayName = computed(() => profileForm.nickname || userProfile.value.username || '未命名用户')
const displayRole = computed(() => userProfile.value.role || '普通成员')

const fillProfileForm = (user) => {
  profileForm.nickname = user.nickname || ''
  profileForm.birthday = user.birthday || ''
  profileForm.phone = user.phone || ''
  profileForm.email = user.email || ''
  profileForm.address = user.address || ''

  const avatarVersion = user.updatedAt || user.avatarUpdatedAt || ''
  userProfile.value.avatar = user.avatar || ''
  userProfile.value.avatarVersion = avatarVersion
  userProfile.value.username = user.username || user.nickname || '未命名用户'
  userProfile.value.role = user.role || user.title || '普通成员'
  userProfile.value.memberSince = user.createdAt || user.created_at || ''
  userProfile.value.articlesCount = user.articleCount ?? user.articlesCount ?? '--'
}

const triggerFlash = (target) => {
  target.value = true
  setTimeout(() => {
    target.value = false
  }, 700)
}

const fetchUserProfile = async () => {
  loading.value = true
  try {
    const res = await getUserInfo()
    const user = res.data?.data || {}
    fillProfileForm(user)
  } catch (error) {
    console.error('Failed to fetch user profile:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const handleAvatarClick = () => {
  avatarInputRef.value?.click()
}

const handleAvatarUpload = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('头像图片大小不能超过 5MB')
    event.target.value = ''
    return
  }

  avatarUploading.value = true
  try {
    const res = await uploadAvatar(file)
    const data = res.data?.data || {}
    const avatarUrl = data.url || ''
    const avatarVersion = data.updatedAt || ''
    if (avatarUrl) {
      userProfile.value.avatar = avatarUrl
      userProfile.value.avatarVersion = avatarVersion
      localStorage.setItem('userAvatar', avatarUrl)
      if (avatarVersion) {
        localStorage.setItem('avatarVersion', avatarVersion)
      } else {
        localStorage.removeItem('avatarVersion')
      }
      window.dispatchEvent(new Event('auth-changed'))
    }
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error('Failed to upload avatar:', error)
    ElMessage.error('头像上传失败')
  } finally {
    avatarUploading.value = false
    event.target.value = ''
  }
}

const handleSaveProfile = async () => {
  profileSaving.value = true
  try {
    await updateProfile({
      nickname: profileForm.nickname,
      birthday: profileForm.birthday,
      phone: profileForm.phone,
      email: profileForm.email,
      address: profileForm.address
    })
    ElMessage.success('资料保存成功')
    profileEditing.value = false
    triggerFlash(profileSavedFlash)
    fetchUserProfile()
  } catch (error) {
    console.error('Failed to update profile:', error)
    ElMessage.error('资料保存失败')
  } finally {
    profileSaving.value = false
  }
}

const handleChangePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写完整的密码信息')
    return
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  passwordSaving.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''

    ElMessage.success('密码修改成功，请重新登录')
    triggerFlash(securitySavedFlash)

    localStorage.removeItem('token')
    logoutCountdown.value = 3
    if (logoutCountdownTimer) {
      clearInterval(logoutCountdownTimer)
    }
    logoutCountdownTimer = setInterval(() => {
      logoutCountdown.value -= 1
      if (logoutCountdown.value <= 0) {
        clearInterval(logoutCountdownTimer)
        logoutCountdownTimer = null
        router.push('/login')
      }
    }, 1000)
  } catch (error) {
    console.error('Failed to change password:', error)
    ElMessage.error('密码修改失败')
  } finally {
    passwordSaving.value = false
  }
}

onMounted(() => {
  fetchUserProfile()
})

onUnmounted(() => {
  if (logoutCountdownTimer) {
    clearInterval(logoutCountdownTimer)
  }
})
</script>

<template>
  <div class="page-container">
    <Header />

    <main class="main-content">
      <section class="profile-layout" v-loading="loading">
        <aside class="profile-sidebar">
          <button
            v-for="item in sidebarNav"
            :key="item.key"
            class="sidebar-item"
            :class="{ 'is-logout': item.action === 'logout', 'is-active': item.key === 'profile' }"
            @click="handleSidebarAction(item)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </button>
        </aside>

        <div class="profile-main">
          <article class="profile-header">
            <button
              type="button"
              class="avatar-btn"
              :disabled="avatarUploading"
              @click="handleAvatarClick"
            >
              <el-avatar v-if="displayAvatar" :src="displayAvatar" :size="88" class="avatar-img" />
              <div v-else class="avatar-placeholder">
                <el-icon><UserFilled /></el-icon>
              </div>
              <span class="avatar-hint">{{ avatarUploading ? '上传中...' : '更换头像' }}</span>
            </button>

            <div class="profile-info">
              <h1 class="profile-name">{{ displayName }}</h1>
              <p class="profile-meta">{{ displayRole }}</p>
            </div>

            <input
              ref="avatarInputRef"
              type="file"
              accept="image/*"
              class="hidden-file-input"
              @change="handleAvatarUpload"
            />
          </article>

          <section class="info-card" :class="{ 'is-flash-success': profileSavedFlash }">
            <header class="card-header">
              <h2 class="card-title">个人资料</h2>
              <el-button class="edit-btn" @click="profileEditing = !profileEditing">
                {{ profileEditing ? '取消' : '编辑' }}
              </el-button>
            </header>

            <el-form :model="profileForm" label-position="top" class="info-form">
              <div class="form-row">
                <el-form-item class="form-field" label="昵称">
                  <el-input v-if="profileEditing" v-model="profileForm.nickname" placeholder="请输入昵称" />
                  <p v-else class="field-value">{{ profileForm.nickname || '未设置' }}</p>
                </el-form-item>

                <el-form-item class="form-field" label="生日">
                  <el-date-picker
                    v-if="profileEditing"
                    v-model="profileForm.birthday"
                    type="date"
                    placeholder="选择生日"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                  <p v-else class="field-value">{{ profileForm.birthday || '未设置' }}</p>
                </el-form-item>

                <el-form-item class="form-field" label="手机号">
                  <el-input v-if="profileEditing" v-model="profileForm.phone" placeholder="请输入手机号" />
                  <p v-else class="field-value">{{ profileForm.phone || '未设置' }}</p>
                </el-form-item>

                <el-form-item class="form-field" label="邮箱">
                  <el-input v-if="profileEditing" v-model="profileForm.email" placeholder="请输入邮箱" />
                  <p v-else class="field-value">{{ profileForm.email || '未设置' }}</p>
                </el-form-item>

                <el-form-item class="form-field form-field-full" label="地址">
                  <el-input v-if="profileEditing" v-model="profileForm.address" placeholder="请输入地址" />
                  <p v-else class="field-value">{{ profileForm.address || '未设置' }}</p>
                </el-form-item>
              </div>

              <div class="form-actions" v-if="profileEditing">
                <el-button
                  type="primary"
                  :loading="profileSaving"
                  @click="handleSaveProfile"
                >
                  保存
                </el-button>
              </div>
            </el-form>
          </section>

          <section class="security-card" :class="{ 'is-flash-success': securitySavedFlash }">
            <header class="card-header">
              <h2 class="card-title">修改密码</h2>
            </header>

            <el-form :model="passwordForm" label-position="top" class="security-form">
              <div class="form-row">
                <el-form-item class="form-field" label="当前密码">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
                </el-form-item>

                <el-form-item class="form-field" label="新密码">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
                </el-form-item>

                <el-form-item class="form-field form-field-full" label="确认新密码">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
                </el-form-item>
              </div>

              <div class="form-actions">
                <el-button type="primary" :loading="passwordSaving" @click="handleChangePassword">
                  更新密码
                </el-button>
              </div>

              <div v-if="logoutCountdown > 0" class="logout-notice">
                <p>密码已更新，{{ logoutCountdown }} 秒后自动跳转到登录页...</p>
              </div>
            </el-form>
          </section>
        </div>
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
  background: var(--bg);
}

.main-content {
  flex: 1;
  max-width: 820px;
  margin: 0 auto;
  padding: 32px 24px 48px;
  width: 100%;
}

.profile-layout {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.profile-sidebar {
  width: 180px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: var(--text);
  font-family: var(--sans);
  font-size: 14px;
  text-align: left;
  cursor: pointer;
  transition: background-color var(--duration-fast) ease, color var(--duration-fast) ease;
}

.sidebar-item .el-icon {
  font-size: 16px;
  opacity: 0.8;
}

.sidebar-item.is-active {
  background: var(--accent-bg);
  color: var(--accent);
}

.sidebar-item.is-active .el-icon {
  opacity: 1;
}

.sidebar-item:hover:not(.is-active) {
  background: rgba(255, 255, 255, 0.04);
}

.sidebar-item.is-logout {
  color: #f56c6c;
}

.sidebar-item.is-logout .el-icon {
  color: #f56c6c;
}

.profile-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
}

.avatar-btn {
  position: relative;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 0;
  transition: transform var(--duration-normal) ease;
}

.avatar-btn:hover {
  transform: scale(1.02);
}

.avatar-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.avatar-img,
.avatar-placeholder {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  border: 2px solid var(--border);
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--code-bg);
  color: var(--text-dim);
  font-size: 32px;
}

.avatar-hint {
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 11px;
  color: var(--text-dim);
  white-space: nowrap;
  opacity: 0;
  transition: opacity var(--duration-fast) ease;
}

.avatar-btn:hover .avatar-hint {
  opacity: 1;
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.profile-name {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-h);
  line-height: 1.2;
}

.profile-meta {
  margin: 6px 0 0;
  color: var(--text);
  font-size: 14px;
}

.hidden-file-input {
  display: none;
}

.info-card,
.security-card {
  padding: 24px;
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-h);
}

.edit-btn {
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text);
  font-size: 13px;
}

.edit-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.form-field-full {
  grid-column: 1 / -1;
}

.field-value {
  margin: 0;
  padding: 10px 0;
  color: var(--text-h);
  font-size: 14px;
  border-bottom: 1px solid var(--border);
  min-height: 22px;
}

:deep(.el-form-item__label) {
  color: var(--text-dim);
  font-size: 12px;
}

.form-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.logout-notice {
  margin-top: 16px;
  padding: 12px;
  background: rgba(245, 108, 108, 0.1);
  border-radius: 8px;
  color: #f56c6c;
  font-size: 13px;
  text-align: center;
}

@media (max-width: 768px) {
  .profile-layout {
    flex-direction: column;
  }

  .profile-sidebar {
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 8px;
  }

  .sidebar-item {
    flex: 1;
    min-width: 100px;
    justify-content: center;
  }

  .main-content {
    padding: 20px 16px 32px;
  }

  .profile-header {
    flex-direction: column;
    text-align: center;
    padding: 20px;
  }

  .profile-info {
    text-align: center;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
