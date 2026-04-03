<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock, UserFilled, Star, StarFilled, Setting, SwitchButton, HomeFilled } from '@element-plus/icons-vue'
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

const workspaceNav = computed(() => [
  { key: 'profile', label: '个人资料', icon: UserFilled, path: '/profile' },
  { key: 'favorites', label: '我的收藏', icon: Star, path: '/favorites' },
  { key: 'likes', label: '我的喜欢', icon: StarFilled, path: '/likes' },
  ...(userRole.value === 'admin' ? [{ key: 'admin', label: '管理后台', icon: Setting, path: '/admin' }] : []),
  { key: 'logout', label: '退出登录', icon: SwitchButton, action: 'logout' }
])

const handleWorkspaceAction = async (item) => {
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
  username: '',
  role: '',
  memberSince: '',
  articlesCount: '--'
})

const heroParticles = Array.from({ length: 28 }, (_, index) => index)

const createParticleStyle = (index) => ({
  left: `${6 + ((index * 7) % 88)}%`,
  top: `${14 + ((index * 13) % 68)}%`,
  width: `${2 + (index % 4)}px`,
  height: `${2 + (index % 4)}px`,
  animationDelay: `${(index % 7) * 0.4}s`,
  animationDuration: `${5 + (index % 5) * 1.2}s`
})

const displayAvatar = computed(() => resolveFileUrl(userProfile.value.avatar))

const formatDisplayDate = (value) => {
  if (!value) return 'UNKNOWN'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const year = `${date.getFullYear()}`
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}.${month}.${day}`
}

const displayName = computed(() => profileForm.nickname || userProfile.value.username || 'CREW_MEMBER')
const displayRole = computed(() => userProfile.value.role || '舰船成员 / Crew Unit')

const stats = computed(() => [
  {
    label: 'MEMBER SINCE',
    value: formatDisplayDate(userProfile.value.memberSince)
  },
  {
    label: 'ARTICLES',
    value: `${userProfile.value.articlesCount}`
  },
  {
    label: 'PROFILE_STATUS',
    value: profileEditing.value ? 'EDITING' : 'SYNCED'
  },
  {
    label: 'SECURITY_LEVEL',
    value: 'OMEGA-3'
  }
])

const passwordStrength = computed(() => {
  const password = passwordForm.newPassword || ''
  let score = 0

  if (password.length >= 8) score += 1
  if (/[A-Z]/.test(password) && /[a-z]/.test(password)) score += 1
  if (/\d/.test(password)) score += 1
  if (/[^A-Za-z0-9]/.test(password)) score += 1

  if (!password) {
    return { score: 0, label: '未检测', levelClass: 'is-none' }
  }

  if (score <= 1) {
    return { score, label: '弱', levelClass: 'is-weak' }
  }
  if (score <= 3) {
    return { score, label: '中', levelClass: 'is-medium' }
  }
  return { score, label: '强', levelClass: 'is-strong' }
})

const fillProfileForm = (user) => {
  profileForm.nickname = user.nickname || ''
  profileForm.birthday = user.birthday || ''
  profileForm.phone = user.phone || ''
  profileForm.email = user.email || ''
  profileForm.address = user.address || ''

  userProfile.value.avatar = user.avatar || ''
  userProfile.value.username = user.username || user.nickname || 'CREW_MEMBER'
  userProfile.value.role = user.role || user.title || '舰船成员 / Crew Unit'
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
    const avatarUrl = res.data?.data?.url || res.data?.url || ''
    if (avatarUrl) {
      userProfile.value.avatar = avatarUrl
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
      <!-- Workspace Navigation Cluster -->
      <nav class="workspace-nav">
        <div class="workspace-nav-inner">
          <button
            v-for="item in workspaceNav"
            :key="item.key"
            class="workspace-nav-item"
            :class="{ 'is-logout': item.action === 'logout' }"
            @click="handleWorkspaceAction(item)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </button>
        </div>
      </nav>

      <section class="profile-shell" v-loading="loading">
        <article class="identity-hero terminal-card section-card">
          <div class="hero-grid" aria-hidden="true"></div>
          <div class="hero-particles" aria-hidden="true">
            <span
              v-for="particle in heroParticles"
              :key="particle"
              class="hero-particle"
              :style="createParticleStyle(particle)"
            ></span>
          </div>

          <div class="hero-content">
            <button
              type="button"
              class="avatar-trigger"
              :disabled="avatarUploading"
              @click="handleAvatarClick"
            >
              <div class="avatar-ring" aria-hidden="true"></div>
              <el-avatar v-if="displayAvatar" :src="displayAvatar" :size="148" class="avatar-image" />
              <div v-else class="avatar-fallback" aria-hidden="true">
                <el-icon><UserFilled /></el-icon>
              </div>
              <span class="avatar-tip">{{ avatarUploading ? 'UPLOADING...' : 'CLICK TO REPLACE AVATAR' }}</span>
            </button>

            <div class="identity-meta">
              <p class="identity-label">CREW PROFILE TERMINAL</p>
              <h1 class="identity-name">{{ displayName }}</h1>
              <p class="identity-role">{{ displayRole }}</p>
              <span class="verified-badge">IDENTITY VERIFIED</span>
            </div>
          </div>

          <input
            ref="avatarInputRef"
            type="file"
            accept="image/*"
            class="hidden-file-input"
            @change="handleAvatarUpload"
          />
        </article>

        <section class="stats-bar terminal-card section-card">
          <div v-for="item in stats" :key="item.label" class="stat-item">
            <p class="stat-label">{{ item.label }}</p>
            <p class="stat-value">{{ item.value }}</p>
          </div>
        </section>

        <section class="profile-card terminal-card section-card" :class="{ 'is-flash-success': profileSavedFlash }">
          <header class="section-header">
            <h2 class="card-title">// PERSONAL_DATA</h2>
            <el-button class="edit-btn" @click="profileEditing = !profileEditing">
              {{ profileEditing ? '取消' : '编辑' }}
            </el-button>
          </header>

          <el-form :model="profileForm" label-position="top" class="profile-form">
            <div class="form-grid">
              <el-form-item class="data-field" label="昵称">
                <el-input v-if="profileEditing" v-model="profileForm.nickname" placeholder="请输入昵称" />
                <p v-else class="field-readonly">{{ profileForm.nickname || '未设置' }}</p>
              </el-form-item>

              <el-form-item class="data-field" label="生日">
                <el-date-picker
                  v-if="profileEditing"
                  v-model="profileForm.birthday"
                  type="date"
                  placeholder="选择生日"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
                <p v-else class="field-readonly">{{ profileForm.birthday || '未设置' }}</p>
              </el-form-item>

              <el-form-item class="data-field" label="手机号">
                <el-input v-if="profileEditing" v-model="profileForm.phone" placeholder="请输入手机号" />
                <p v-else class="field-readonly">{{ profileForm.phone || '未设置' }}</p>
              </el-form-item>

              <el-form-item class="data-field" label="邮箱">
                <el-input v-if="profileEditing" v-model="profileForm.email" placeholder="请输入邮箱" />
                <p v-else class="field-readonly">{{ profileForm.email || '未设置' }}</p>
              </el-form-item>

              <el-form-item class="data-field data-field-wide" label="地址">
                <el-input v-if="profileEditing" v-model="profileForm.address" placeholder="请输入地址" />
                <p v-else class="field-readonly">{{ profileForm.address || '未设置' }}</p>
              </el-form-item>
            </div>

            <div class="actions-row">
              <el-button
                v-if="profileEditing"
                type="primary"
                class="pulse-btn"
                :loading="profileSaving"
                @click="handleSaveProfile"
              >
                保存资料
              </el-button>
            </div>
          </el-form>
        </section>

        <section class="password-card terminal-card section-card" :class="{ 'is-flash-success': securitySavedFlash }">
          <header class="section-header section-header-security">
            <h2 class="card-title">// SECURITY_PROTOCOL</h2>
            <span class="security-lock" aria-hidden="true">
              <el-icon><Lock /></el-icon>
            </span>
          </header>

          <el-form :model="passwordForm" label-position="top" class="password-form">
            <div class="form-grid security-grid">
              <el-form-item class="data-field" label="旧密码">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
              </el-form-item>

              <el-form-item class="data-field" label="新密码">
                <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
              </el-form-item>

              <el-form-item class="data-field data-field-wide" label="确认新密码">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
              </el-form-item>
            </div>

            <div class="strength-box">
              <p class="strength-title">密码强度</p>
              <div class="strength-bar" role="presentation">
                <span
                  v-for="index in 4"
                  :key="index"
                  class="strength-segment"
                  :class="[{ active: index <= passwordStrength.score }, passwordStrength.levelClass]"
                ></span>
              </div>
              <p class="strength-label" :class="passwordStrength.levelClass">{{ passwordStrength.label }}</p>
            </div>

            <div class="actions-row">
              <el-button type="warning" class="security-btn" :loading="passwordSaving" @click="handleChangePassword">
                更新访问密钥
              </el-button>
            </div>

            <div v-if="logoutCountdown > 0" class="countdown-box">
              <div class="countdown-bar" :style="{ '--count-seconds': `${logoutCountdown}s` }"></div>
              <p>安全协议已生效，{{ logoutCountdown }} 秒后自动退出终端...</p>
            </div>
          </el-form>
        </section>
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
  max-width: 1100px;
  margin: 0 auto;
  padding: 28px 24px 40px;
  width: 100%;
}

.profile-shell {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 18px;
  perspective: 1200px;
}

.terminal-card {
  border: 1px solid rgba(0, 240, 255, 0.24);
  border-radius: 18px;
  background:
    linear-gradient(160deg, rgba(10, 20, 38, 0.88), rgba(8, 16, 31, 0.62)),
    radial-gradient(circle at 82% 12%, rgba(0, 240, 255, 0.1), transparent 42%);
  box-shadow: inset 0 0 0 1px rgba(0, 240, 255, 0.06), var(--shadow);
  position: relative;
  overflow: hidden;
  transition: transform 300ms ease, box-shadow 300ms ease, border-color 300ms ease;
  background-attachment: fixed, scroll;
}

.section-card::before,
.section-card::after {
  content: '';
  position: absolute;
  width: 30px;
  height: 16px;
  border-top: 1px solid var(--accent-border);
  border-left: 1px solid var(--accent-border);
  opacity: 0.76;
  z-index: 1;
  transition: transform 300ms ease;
}

.section-card::before {
  top: 10px;
  left: 10px;
}

.section-card::after {
  right: 10px;
  bottom: 10px;
  transform: rotate(180deg);
}

.section-card:hover {
  transform: translateY(-3px) rotateX(0.3deg);
  border-color: var(--accent);
  box-shadow: 0 16px 35px rgba(0, 0, 0, 0.46), 0 0 22px rgba(0, 240, 255, 0.16);
}

.section-card:hover::before {
  transform: translate(2px, 2px);
}

.section-card:hover::after {
  transform: rotate(180deg) translate(2px, 2px);
}

.identity-hero {
  min-height: 260px;
  padding: 30px 28px;
  background:
    linear-gradient(150deg, rgba(8, 22, 42, 0.96), rgba(6, 14, 28, 0.72)),
    radial-gradient(circle at 10% -10%, rgba(0, 240, 255, 0.18), transparent 45%),
    radial-gradient(circle at 88% 10%, rgba(114, 255, 220, 0.15), transparent 44%);
}

.hero-grid,
.hero-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero-grid {
  background-image:
    linear-gradient(rgba(0, 240, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.08) 1px, transparent 1px);
  background-size: 34px 34px;
  mask-image: radial-gradient(circle at 50% 28%, black 25%, transparent 95%);
  animation: grid-shift 11s linear infinite;
  opacity: 0.5;
}

.hero-particles {
  opacity: 0.9;
}

.hero-particle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 240, 255, 1), rgba(0, 240, 255, 0.12) 75%, transparent);
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.55);
  animation: particle-float ease-in-out infinite;
}

.hero-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: flex-end;
  gap: 22px;
  margin-top: 72px;
}

.avatar-trigger {
  width: 156px;
  height: 156px;
  border: 0;
  border-radius: 50%;
  background: transparent;
  display: grid;
  place-items: center;
  cursor: pointer;
  position: relative;
  transform: translateY(30px);
  transition: transform 300ms ease;
}

.avatar-trigger:disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.avatar-trigger:hover {
  transform: translateY(30px) scale(1.04);
}

.avatar-ring {
  position: absolute;
  inset: -3px;
  border-radius: 50%;
  border: 2px solid var(--accent);
  box-shadow: 0 0 24px var(--accent-glow), inset 0 0 14px rgba(0, 240, 255, 0.22);
  animation: avatar-pulse 2.2s ease-in-out infinite;
}

.avatar-trigger:hover .avatar-ring {
  box-shadow: 0 0 34px rgba(0, 240, 255, 0.72), inset 0 0 24px rgba(0, 240, 255, 0.4);
}

.avatar-image,
.avatar-fallback {
  width: 148px;
  height: 148px;
  border-radius: 50%;
  border: 2px solid rgba(0, 240, 255, 0.7);
  background: radial-gradient(circle, rgba(0, 240, 255, 0.12), rgba(7, 11, 20, 0.9));
}

.avatar-fallback {
  display: grid;
  place-items: center;
  color: var(--accent);
  font-size: 58px;
}

.avatar-tip {
  position: absolute;
  bottom: -24px;
  font-size: 11px;
  letter-spacing: 1px;
  color: var(--accent);
  font-family: var(--heading);
  text-shadow: 0 0 12px rgba(0, 240, 255, 0.32);
}

.identity-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-bottom: 2px;
}

.identity-label {
  margin: 0;
  color: var(--accent-secondary);
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 1.3px;
}

.identity-name {
  margin: 0;
  font-size: 40px;
  line-height: 1.1;
  letter-spacing: 1px;
  text-shadow: 0 0 26px rgba(0, 240, 255, 0.28);
}

.identity-role {
  margin: 0;
  color: var(--text);
  font-size: 14px;
  font-family: var(--mono);
  letter-spacing: 0.8px;
}

.verified-badge {
  margin-top: 4px;
  display: inline-flex;
  width: fit-content;
  align-items: center;
  padding: 5px 12px;
  border-radius: 999px;
  border: 1px solid rgba(95, 255, 137, 0.55);
  background: rgba(78, 238, 120, 0.18);
  color: #9fffc2;
  font-family: var(--mono);
  font-size: 11px;
  letter-spacing: 0.9px;
  box-shadow: 0 0 18px rgba(95, 255, 137, 0.24);
}

.profile-switch-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.profile-switch-btn {
  border: 1px solid rgba(0, 240, 255, 0.16);
  background: rgba(6, 14, 28, 0.45);
  color: var(--text);
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 12px;
  font-family: var(--mono);
  cursor: pointer;
  transition: all 0.2s ease;
}

.profile-switch-btn:hover,
.profile-switch-btn.active {
  color: var(--accent);
  border-color: rgba(0, 240, 255, 0.36);
  background: rgba(0, 240, 255, 0.08);
  box-shadow: 0 0 14px rgba(0, 240, 255, 0.12);
}

.hero-actions {
  margin-top: 12px;
}

.logout-btn {
  border-color: rgba(255, 117, 117, 0.3);
  background: rgba(255, 117, 117, 0.08);
  color: #ff9a9a;
}

.logout-btn:hover {
  border-color: rgba(255, 117, 117, 0.48);
  background: rgba(255, 117, 117, 0.14);
  color: #ffd4d4;
}

.stats-bar {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0;
}

.stat-item {
  padding: 14px 16px;
  border-right: 1px solid rgba(0, 240, 255, 0.15);
}

.stat-item:last-child {
  border-bottom: 0;
}

.workspace-nav {
  margin-bottom: 24px;
  border: 1px solid rgba(0, 240, 255, 0.18);
  border-radius: 12px;
  background: linear-gradient(170deg, rgba(10, 20, 38, 0.82), rgba(8, 16, 31, 0.68));
  padding: 4px;
}

.workspace-nav-inner {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.workspace-nav-item {
  flex: 1;
  min-width: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 16px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 10px;
  color: var(--text);
  font-family: var(--heading);
  font-size: 14px;
  letter-spacing: 0.6px;
  cursor: pointer;
  transition: all 0.24s ease;
}

.workspace-nav-item .el-icon {
  font-size: 16px;
  color: var(--accent);
}

.workspace-nav-item:hover {
  background: rgba(0, 240, 255, 0.08);
  border-color: rgba(0, 240, 255, 0.22);
  color: var(--text-h);
}

.workspace-nav-item.is-logout {
  color: #ff6b6b;
}

.workspace-nav-item.is-logout .el-icon {
  color: #ff6b6b;
}

.workspace-nav-item.is-logout:hover {
  background: rgba(255, 107, 107, 0.12);
  border-color: rgba(255, 107, 107, 0.32);
}

.hidden-file-input {
  display: none;
}

.profile-card,
.password-card {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.12);
}

.section-header-security {
  border-bottom-color: rgba(255, 184, 77, 0.15);
}

.card-title {
  margin: 0;
  font-family: var(--mono);
  font-size: 14px;
  color: var(--accent);
  letter-spacing: 1.2px;
}

.edit-btn {
  border: 1px solid rgba(0, 240, 255, 0.22);
  background: transparent;
  color: var(--text);
  font-family: var(--mono);
  font-size: 12px;
}

.edit-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.security-lock {
  color: rgba(255, 184, 77, 0.8);
  font-size: 18px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.security-grid {
  grid-template-columns: repeat(2, 1fr);
}

.data-field-wide {
  grid-column: 1 / -1;
}

.field-readonly {
  margin: 0;
  padding: 8px 12px;
  background: rgba(0, 240, 255, 0.04);
  border: 1px solid rgba(0, 240, 255, 0.08);
  border-radius: 6px;
  color: var(--text-h);
  font-family: var(--mono);
  font-size: 13px;
  min-height: 20px;
}

:deep(.el-form-item__label) {
  color: var(--text-h);
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 0.6px;
  text-transform: uppercase;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
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

.actions-row {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.pulse-btn {
  background: var(--accent);
  border: 1px solid rgba(0, 240, 255, 0.32);
  color: #04131a;
  font-family: var(--heading);
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.3);
}

.pulse-btn:hover {
  background: var(--accent-hover);
  box-shadow: 0 0 25px rgba(0, 240, 255, 0.5);
}

.security-btn {
  font-family: var(--heading);
  letter-spacing: 0.6px;
}

.strength-box {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.strength-title {
  margin: 0;
  font-family: var(--mono);
  font-size: 11px;
  color: var(--text);
  opacity: 0.7;
  white-space: nowrap;
}

.strength-bar {
  display: flex;
  gap: 4px;
  flex: 1;
  max-width: 160px;
}

.strength-segment {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background: rgba(0, 240, 255, 0.1);
  transition: background 0.3s ease;
}

.strength-segment.active.is-weak {
  background: #f56c6c;
}

.strength-segment.active.is-medium {
  background: #e6a23c;
}

.strength-segment.active.is-strong {
  background: #67c23a;
}

.strength-label {
  margin: 0;
  font-family: var(--mono);
  font-size: 11px;
  white-space: nowrap;
}

.strength-label.is-none { color: var(--text); opacity: 0.5; }
.strength-label.is-weak { color: #f56c6c; }
.strength-label.is-medium { color: #e6a23c; }
.strength-label.is-strong { color: #67c23a; }

.countdown-box {
  margin-top: 16px;
  padding: 12px 16px;
  border: 1px solid rgba(255, 184, 77, 0.3);
  border-radius: 8px;
  background: rgba(255, 184, 77, 0.08);
  text-align: center;
  color: #e6a23c;
  font-family: var(--mono);
  font-size: 13px;
}

.countdown-box p {
  margin: 8px 0 0;
}

.countdown-bar {
  height: 3px;
  background: linear-gradient(90deg, #e6a23c, transparent);
  border-radius: 2px;
  animation: countdown-shrink var(--count-seconds) linear forwards;
}

@keyframes countdown-shrink {
  from { width: 100%; }
  to { width: 0%; }
}

@keyframes grid-shift {
  0% { background-position: 0 0, 0 0; }
  100% { background-position: 34px 34px, 34px 34px; }
}

@keyframes particle-float {
  0%, 100% { transform: translate(0, 0) scale(1); opacity: 0.6; }
  50% { transform: translate(var(--float-x, 10px), var(--float-y, -20px)) scale(var(--particle-scale, 1.2)); opacity: 1; }
}

@keyframes avatar-pulse {
  0%, 100% { opacity: 0.7; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.04); }
}

.is-flash-success {
  animation: flash-success 0.7s ease;
}

@keyframes flash-success {
  0% { box-shadow: 0 0 0 rgba(0, 240, 255, 0); }
  30% { box-shadow: 0 0 30px rgba(0, 240, 255, 0.3), inset 0 0 20px rgba(0, 240, 255, 0.1); }
  100% { box-shadow: inset 0 0 0 1px rgba(0, 240, 255, 0.06), var(--shadow); }
}

@media (max-width: 768px) {
  .workspace-nav {
    display: none;
  }
  .main-content {
    padding: 24px 16px;
  }

  .identity-hero {
    padding: 18px 16px 22px;
  }

  .profile-card,
  .password-card {
    padding: 16px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .data-field-wide {
    grid-column: auto;
  }

  .stats-bar {
    grid-template-columns: 1fr;
  }

  .stat-item {
    border-right: 0;
    border-bottom: 1px solid rgba(0, 240, 255, 0.12);
  }

  .stat-item:last-child {
    border-bottom: 0;
  }
}
</style>
