<template>
  <div class="page-container settings-page">
    <header class="settings-header">
      <h2>设置</h2>
    </header>

    <!-- 个人信息 -->
    <section class="card profile-card">
      <div class="avatar-area">
        <div class="avatar" :style="avatarStyle">
          <img v-if="profile.avatar" :src="avatarUrl" class="avatar-img" />
          <span v-else class="avatar-text">{{ (profile.nickname || profile.username || 'U')[0] }}</span>
        </div>
        <div class="profile-info">
          <p class="profile-name">{{ profile.nickname || profile.username || '读者' }}</p>
          <p class="profile-id">@{{ profile.username }}</p>
        </div>
        <button class="edit-profile-btn" @click="editProfile = !editProfile">
          {{ editProfile ? '收起' : '编辑' }}
        </button>
      </div>

      <!-- 编辑个人信息 -->
      <transition name="fade">
        <div v-if="editProfile" class="profile-edit">
          <div class="input-group">
            <label>昵称</label>
            <input v-model="profile.nickname" type="text" placeholder="设置昵称" />
          </div>
          <button class="btn-primary save-btn" @click="saveProfile">保存</button>
        </div>
      </transition>
    </section>

    <!-- 阅读目标 -->
    <section class="card setting-card">
      <div class="setting-card-header">
        <h3>每日阅读目标</h3>
        <span class="goal-value">{{ profile.dailyGoal || 30 }} 分钟</span>
      </div>
      <div class="slider-wrap">
        <input
          v-model.number="dailyGoal"
          type="range"
          min="10"
          max="120"
          step="5"
          class="slider"
          @change="saveGoal"
        />
        <div class="slider-labels">
          <span>10分</span>
          <span>60分</span>
          <span>120分</span>
        </div>
      </div>
    </section>

    <!-- 修改密码 -->
    <section class="card setting-card">
      <div class="setting-card-header" @click="showPasswordChange = !showPasswordChange">
        <h3>修改密码</h3>
        <svg viewBox="0 0 24 24" width="18" height="18" class="chevron" :class="{ rotated: showPasswordChange }">
          <path d="M9 6L15 12L9 18" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <transition name="fade">
        <div v-if="showPasswordChange" class="expand-content">
          <div class="input-group">
            <label>旧密码</label>
            <input v-model="oldPassword" type="password" placeholder="当前密码" />
          </div>
          <div class="input-group">
            <label>新密码</label>
            <input v-model="newPassword" type="password" placeholder="新密码" />
          </div>
          <button class="btn-primary save-btn" @click="handleChangePassword">确认修改</button>
        </div>
      </transition>
    </section>

    <!-- 导入历史 -->
    <section class="card setting-card">
      <div class="setting-card-header" @click="showImportHistory = !showImportHistory">
        <h3>导入历史</h3>
        <svg viewBox="0 0 24 24" width="18" height="18" class="chevron" :class="{ rotated: showImportHistory }">
          <path d="M9 6L15 12L9 18" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <transition name="fade">
        <div v-if="showImportHistory" class="expand-content">
          <div class="import-list">
            <div
              v-for="book in importHistory"
              :key="book.id"
              class="import-item"
            >
              <span class="import-name">{{ book.title }}</span>
              <span class="import-date">{{ formatDate(book.createTime) }}</span>
            </div>
            <div v-if="!importHistory.length" class="empty-inline">暂无导入记录</div>
          </div>
        </div>
      </transition>
    </section>

    <!-- 关于 -->
    <section class="card setting-card">
      <div class="setting-card-header" @click="showAbout = !showAbout">
        <h3>关于墨读</h3>
        <svg viewBox="0 0 24 24" width="18" height="18" class="chevron" :class="{ rotated: showAbout }">
          <path d="M9 6L15 12L9 18" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <transition name="fade">
        <div v-if="showAbout" class="expand-content about-content">
          <p class="about-version">墨读 Moeread v2.0</p>
          <p class="about-desc">一个沉浸式小说阅读器</p>
          <p class="about-tech">Spring Boot 3 + Vue 3 + MyBatis-Plus</p>
          <p class="about-author">made with care</p>
        </div>
      </transition>
    </section>

    <!-- 退出登录 -->
    <button class="logout-btn" @click="handleLogout">退出登录</button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getProfile, updateProfile, changePassword } from '../api/auth'
import { getBookList } from '../api/book'

const router = useRouter()
const userStore = useUserStore()

const profile = ref({})
const editProfile = ref(false)
const dailyGoal = ref(30)
const showPasswordChange = ref(false)
const showImportHistory = ref(false)
const showAbout = ref(false)
const oldPassword = ref('')
const newPassword = ref('')
const importHistory = ref([])

const avatarStyle = computed(() => {
  if (profile.value.avatar) return {}
  return { background: 'linear-gradient(135deg, #F59E0B, #D97706)' }
})

const avatarUrl = computed(() => {
  if (!profile.value.avatar) return ''
  const path = profile.value.avatar
  if (path.startsWith('http')) return path
  return '/api' + path
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.slice(0, 10)
}

async function saveProfile() {
  try {
    await userStore.updateProfile({ nickname: profile.value.nickname })
    editProfile.value = false
  } catch (e) {}
}

async function saveGoal() {
  try {
    await userStore.updateProfile({ dailyGoal: dailyGoal.value })
  } catch (e) {}
}

async function handleChangePassword() {
  if (!oldPassword.value || !newPassword.value) {
    alert('请填写完整')
    return
  }
  try {
    await changePassword(oldPassword.value, newPassword.value)
    alert('密码修改成功')
    oldPassword.value = ''
    newPassword.value = ''
    showPasswordChange.value = false
  } catch (e) {}
}

function handleLogout() {
  if (!confirm('确定退出登录吗？')) return
  userStore.logout()
  router.push('/login')
}

onMounted(async () => {
  try {
    const [profileRes, bookRes] = await Promise.all([
      getProfile(),
      getBookList()
    ])
    profile.value = profileRes.data || {}
    dailyGoal.value = profile.value.dailyGoal || 30
    importHistory.value = (bookRes.data || []).slice().reverse().slice(0, 10)
  } catch (e) {}
})
</script>

<style scoped>
.settings-page {
  padding: 16px 16px 0;
}

.settings-header {
  padding: 12px 4px 16px;
}

.settings-header h2 {
  font-size: 24px;
  font-weight: 800;
  color: var(--color-text);
}

/* 个人信息卡片 */
.profile-card {
  padding: 20px;
  margin-bottom: 16px;
}

.avatar-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-text {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}

.profile-info {
  flex: 1;
}

.profile-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text);
}

.profile-id {
  font-size: 13px;
  color: var(--color-text-hint);
  margin-top: 2px;
}

.edit-profile-btn {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary-dark);
  background: var(--color-primary-lightest);
  padding: 6px 16px;
  border-radius: 16px;
  cursor: pointer;
}

.profile-edit {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}

/* 通用设置卡片 */
.setting-card {
  padding: 0;
  margin-bottom: 16px;
  overflow: hidden;
}

.setting-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  cursor: pointer;
}

.setting-card-header h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.goal-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary-dark);
}

.chevron {
  color: var(--color-text-hint);
  transition: transform 0.3s;
}

.chevron.rotated {
  transform: rotate(90deg);
}

.expand-content {
  padding: 0 20px 20px;
  border-top: 1px solid var(--color-border);
  padding-top: 16px;
}

/* 滑块 */
.slider-wrap {
  padding: 0 4px;
}

.slider {
  width: 100%;
  -webkit-appearance: none;
  appearance: none;
  height: 4px;
  background: var(--color-primary-lighter);
  border-radius: 2px;
  outline: none;
}

.slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 20px;
  height: 20px;
  background: var(--color-primary);
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(245, 158, 11, 0.4);
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 11px;
  color: var(--color-text-hint);
}

/* 输入框 */
.input-group {
  margin-bottom: 14px;
}

.input-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
  margin-bottom: 6px;
}

.input-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1.5px solid var(--color-border);
  border-radius: 8px;
  font-size: 14px;
  color: var(--color-text);
  background: var(--color-primary-lightest);
}

.input-group input:focus {
  border-color: var(--color-primary);
  background: #fff;
}

.save-btn {
  margin-top: 4px;
  max-width: 200px;
}

/* 导入历史 */
.import-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.import-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
}

.import-item:last-child {
  border-bottom: none;
}

.import-name {
  font-size: 13px;
  color: var(--color-text);
  font-weight: 500;
}

.import-date {
  font-size: 12px;
  color: var(--color-text-hint);
}

/* 关于 */
.about-content {
  text-align: center;
}

.about-version {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 8px;
}

.about-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}

.about-tech {
  font-size: 12px;
  color: var(--color-text-hint);
  margin-bottom: 12px;
}

.about-author {
  font-size: 12px;
  color: var(--color-text-hint);
}

/* 退出登录 */
.logout-btn {
  width: 100%;
  padding: 14px 0;
  background: #fff;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  color: #DC2626;
  box-shadow: var(--shadow-soft);
  cursor: pointer;
  margin-bottom: 24px;
  transition: transform 0.15s;
}

.logout-btn:active {
  transform: scale(0.98);
}

.empty-inline {
  text-align: center;
  padding: 20px;
  color: var(--color-text-hint);
  font-size: 13px;
}
</style>
