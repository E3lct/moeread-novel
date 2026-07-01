<template>
  <div class="settings-page">
    <div class="settings-content">
      <!-- 页头 -->
      <div class="page-header">
        <div class="page-title">设置</div>
      </div>

      <!-- 提示 -->
      <div class="alert alert-success" v-if="successMsg">{{ successMsg }}</div>
      <div class="alert alert-error" v-if="errorMsg">{{ errorMsg }}</div>

      <!-- 个人信息 -->
      <div class="accordion-item" :class="{ open: openSection === 'profile' }">
        <div class="accordion-head" @click="toggleSection('profile')">
          <span class="accordion-title">个人信息</span>
          <span class="accordion-arrow">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
          </span>
        </div>
        <div class="accordion-body">
          <div class="accordion-body-inner">
            <div class="settings-form">
              <div class="form-row">
                <label class="form-label">账号</label>
                <span class="form-static">{{ userStore.username }}</span>
              </div>
              <div class="form-row">
                <label class="form-label">昵称</label>
                <input class="form-input" v-model="profile.nickname" placeholder="设置昵称" />
              </div>
              <div class="form-row">
                <label class="form-label">头像</label>
                <div class="avatar-preview" v-if="profile.avatar">
                  <img :src="profile.avatar" />
                </div>
                <span class="form-static" v-else>未设置</span>
              </div>
              <div class="form-actions">
                <button class="btn-primary" @click="saveProfile">保存</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 个性化 -->
      <div class="accordion-item" :class="{ open: openSection === 'customize' }">
        <div class="accordion-head" @click="toggleSection('customize')">
          <span class="accordion-title">个性化</span>
          <span class="accordion-arrow">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
          </span>
        </div>
        <div class="accordion-body">
          <div class="accordion-body-inner">
            <div class="settings-form">
              <div class="form-row">
                <label class="form-label">背景图</label>
                <div class="bg-upload-area">
                  <div class="bg-preview" v-if="bgSettings.mascotImage">
                    <img :src="bgPreviewUrl" />
                  </div>
                  <span class="form-static" v-else>未设置</span>
                  <div class="bg-actions">
                    <label class="btn-upfile">
                      选择图片
                      <input type="file" accept="image/*" @change="onBgFileSelected" hidden />
                    </label>
                    <button class="btn-danger-ghost-sm" v-if="bgSettings.mascotImage" @click="removeBg">移除</button>
                  </div>
                </div>
              </div>
              <template v-if="bgSettings.mascotImage">
                <div class="form-row">
                  <label class="form-label">透明度</label>
                  <div class="goal-slider-wrap">
                    <input type="range" class="form-range" min="10" max="100" step="5" v-model.number="bgSettings.mascotOpacity"
                      @input="previewBg" @change="saveBg" />
                    <span class="goal-value">{{ bgSettings.mascotOpacity }}%</span>
                  </div>
                </div>
                <div class="form-row">
                  <label class="form-label">缩放</label>
                  <div class="goal-slider-wrap">
                    <input type="range" class="form-range" min="20" max="300" step="10" v-model.number="bgSettings.bgScale"
                      @input="previewBg" @change="saveBg" />
                    <span class="goal-value">{{ bgSettings.bgScale }}%</span>
                  </div>
                </div>
                <div class="form-row">
                  <label class="form-label">镜像翻转</label>
                  <label class="toggle-switch">
                    <input type="checkbox" v-model="bgSettings.bgMirror"
                      @change="previewBg(); saveBg()" />
                    <span class="toggle-slider"></span>
                  </label>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 阅读目标 -->
      <div class="accordion-item" :class="{ open: openSection === 'goal' }">
        <div class="accordion-head" @click="toggleSection('goal')">
          <span class="accordion-title">阅读目标</span>
          <span class="accordion-arrow">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
          </span>
        </div>
        <div class="accordion-body">
          <div class="accordion-body-inner">
            <div class="settings-form">
              <div class="form-row">
                <label class="form-label">每日目标</label>
                <div class="goal-slider-wrap">
                  <input type="range" class="form-range" min="10" max="120" step="5" v-model.number="dailyGoal" />
                  <span class="goal-value">{{ dailyGoal }} 分钟</span>
                </div>
              </div>
              <div class="form-actions">
                <button class="btn-primary" @click="saveGoal">保存</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 修改密码 -->
      <div class="accordion-item" :class="{ open: openSection === 'password' }">
        <div class="accordion-head" @click="toggleSection('password')">
          <span class="accordion-title">修改密码</span>
          <span class="accordion-arrow">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
          </span>
        </div>
        <div class="accordion-body">
          <div class="accordion-body-inner">
            <div class="settings-form">
              <div class="form-row">
                <label class="form-label">旧密码</label>
                <input class="form-input" type="password" v-model="passwordForm.oldPassword" placeholder="请输入旧密码" />
              </div>
              <div class="form-row">
                <label class="form-label">新密码</label>
                <input class="form-input" type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码" />
              </div>
              <div class="form-actions">
                <button class="btn-primary" @click="changePassword">修改密码</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 导入历史 -->
      <div class="accordion-item" :class="{ open: openSection === 'history' }">
        <div class="accordion-head" @click="toggleSection('history')">
          <span class="accordion-title">导入历史</span>
          <span class="accordion-arrow">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
          </span>
        </div>
        <div class="accordion-body">
          <div class="import-list">
            <div class="import-item" v-for="book in importHistory" :key="book.id">
              <div class="import-info">
                <span class="import-title">{{ book.title }}</span>
                <span class="import-meta">{{ book.chapterCount || 0 }} 章</span>
              </div>
              <span class="import-date">{{ formatDate(book.createTime) }}</span>
            </div>
            <div class="empty-mini" v-if="!importHistory.length">暂无导入记录</div>
          </div>
        </div>
      </div>

      <!-- 关于 -->
      <div class="accordion-item" :class="{ open: openSection === 'about' }">
        <div class="accordion-head" @click="toggleSection('about')">
          <span class="accordion-title">关于</span>
          <span class="accordion-arrow">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
          </span>
        </div>
        <div class="accordion-body">
          <div class="about-section">
            <div class="about-logo">
              <div class="about-logo-circle">墨</div>
              <div>
                <div class="about-name">墨读 Moeread</div>
                <div class="about-version">v2.0.0 · Spring Boot + Vue 3</div>
              </div>
            </div>
            <div class="about-desc">一个简洁的本地小说阅读器，支持 TXT/ZIP 导入、章节自动识别、阅读进度同步、摸鱼模式等功能。</div>
            <a href="https://github.com/E3lct/moeread-novel" class="about-github" target="_blank">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M12 .3a12 12 0 0 0-3.8 23.4c.6.1.8-.3.8-.6v-2c-3.3.7-4-1.6-4-1.6-.6-1.4-1.4-1.8-1.4-1.8-1.1-.7.1-.7.1-.7 1.2.1 1.9 1.2 1.9 1.2 1 1.8 2.8 1.3 3.5 1 .1-.8.4-1.3.8-1.6-2.7-.3-5.5-1.3-5.5-6 0-1.2.5-2.3 1.3-3.1-.2-.4-.6-1.6.1-3.2 0 0 1-.3 3.3 1.2a11.5 11.5 0 0 1 6 0c2.3-1.5 3.3-1.2 3.3-1.2.7 1.6.3 2.8.1 3.2.8.8 1.3 1.9 1.3 3.1 0 4.7-2.8 5.7-5.5 6 .5.4.9 1.2.9 2.4v3.6c0 .3.2.7.8.6A12 12 0 0 0 12 .3"/></svg>
              GitHub
            </a>
            <div class="about-tech">
              <span class="tech-tag">Spring Boot 3</span>
              <span class="tech-tag">MyBatis-Plus</span>
              <span class="tech-tag">Vue 3</span>
              <span class="tech-tag">Vite</span>
              <span class="tech-tag">MySQL 8</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 退出登录 -->
      <div class="logout-row">
        <button class="btn-logout" @click="logout">退出登录</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getProfile, updateProfile, changePassword as apiChangePassword } from '../api/auth'
import { getBookshelf, uploadCover } from '../api/book'

const router = useRouter()
const userStore = useUserStore()

const openSection = ref('profile')
const successMsg = ref('')
const errorMsg = ref('')

const profile = reactive({
  nickname: '',
  avatar: ''
})

const dailyGoal = ref(30)
const passwordForm = reactive({ oldPassword: '', newPassword: '' })
const importHistory = ref([])

const bgSettings = reactive({
  mascotImage: '',
  mascotOpacity: 80,
  bgScale: 100,
  bgMirror: false
})

const bgPreviewUrl = computed(() => {
  const img = bgSettings.mascotImage
  if (!img) return ''
  return img.startsWith('http') ? img : '/api' + img
})

function toggleSection(key) {
  openSection.value = openSection.value === key ? '' : key
}

function formatDate(d) {
  if (!d) return ''
  return d.replace(/T.*/, '')
}

function showMsg(type, msg) {
  if (type === 'success') {
    successMsg.value = msg
    errorMsg.value = ''
  } else {
    errorMsg.value = msg
    successMsg.value = ''
  }
  setTimeout(() => { successMsg.value = ''; errorMsg.value = '' }, 3000)
}

async function saveProfile() {
  try {
    await updateProfile({ nickname: profile.nickname })
    userStore.updateBgPreview({ nickname: profile.nickname })
    showMsg('success', '保存成功')
  } catch (e) {
    showMsg('error', e.message || '保存失败')
  }
}

async function saveGoal() {
  try {
    await updateProfile({ dailyGoal: dailyGoal.value })
    showMsg('success', '目标已更新')
  } catch (e) {
    showMsg('error', e.message || '保存失败')
  }
}

async function changePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    showMsg('error', '请填写完整')
    return
  }
  try {
    await apiChangePassword(passwordForm.oldPassword, passwordForm.newPassword)
    showMsg('success', '密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
  } catch (e) {
    showMsg('error', e.message || '修改失败')
  }
}

async function onBgFileSelected(e) {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 2 * 1024 * 1024) {
    showMsg('error', '图片大小不能超过2MB')
    return
  }
  try {
    const res = await uploadCover(file)
    bgSettings.mascotImage = res.data.url
    userStore.updateBgPreview({ mascotImage: res.data.url })
    await updateProfile({ mascotImage: res.data.url })
    showMsg('success', '背景图已更新')
  } catch (err) {
    showMsg('error', err.message || '上传失败')
  }
  e.target.value = ''
}

function previewBg() {
  userStore.updateBgPreview({
    mascotImage: bgSettings.mascotImage,
    mascotOpacity: bgSettings.mascotOpacity,
    bgScale: bgSettings.bgScale,
    bgMirror: bgSettings.bgMirror
  })
}

async function saveBg() {
  try {
    await updateProfile({
      mascotOpacity: bgSettings.mascotOpacity,
      bgScale: bgSettings.bgScale,
      bgMirror: bgSettings.bgMirror ? 1 : 0
    })
  } catch (e) {
    // 静默失败
  }
}

async function removeBg() {
  bgSettings.mascotImage = ''
  bgSettings.mascotOpacity = 80
  bgSettings.bgScale = 100
  bgSettings.bgMirror = false
  userStore.updateBgPreview({
    mascotImage: '',
    mascotOpacity: 80,
    bgScale: 100,
    bgMirror: false
  })
  try {
    await updateProfile({
      mascotImage: '',
      mascotOpacity: 80,
      bgScale: 100,
      bgMirror: 0
    })
    showMsg('success', '背景图已移除')
  } catch (e) {
    showMsg('error', '移除失败')
  }
}

function logout() {
  if (!confirm('确定退出登录吗？')) return
  userStore.logout()
  router.push('/login')
}

onMounted(async () => {
  try {
    const res = await getProfile()
    if (res.data) {
      profile.nickname = res.data.nickname || ''
      profile.avatar = res.data.avatar || ''
      dailyGoal.value = res.data.dailyGoal || 30
      bgSettings.mascotImage = res.data.mascotImage || ''
      bgSettings.mascotOpacity = res.data.mascotOpacity ?? 80
      bgSettings.bgScale = res.data.bgScale ?? 100
      bgSettings.bgMirror = !!res.data.bgMirror
      userStore.updateBgPreview({
        mascotImage: bgSettings.mascotImage,
        mascotOpacity: bgSettings.mascotOpacity,
        bgScale: bgSettings.bgScale,
        bgMirror: bgSettings.bgMirror
      })
      userStore.updateBgPreview({ nickname: res.data.nickname })
    }
    const bookRes = await getBookshelf()
    importHistory.value = (bookRes.data || []).sort((a, b) =>
      new Date(b.createTime) - new Date(a.createTime)
    ).slice(0, 10)
  } catch (e) {
    console.error('加载设置失败:', e)
  }
})
</script>

<style scoped>
.settings-page {
    min-height: calc(100vh - var(--nav-height));
    padding-bottom: 48px;
}
.settings-content {
    max-width: 640px;
    margin: 0 auto;
    padding: 0;
}

/* 手风琴 */
.accordion-item {
    background: var(--color-bg-card);
    border: 1px solid var(--color-border-light);
    border-radius: 8px;
    margin-bottom: 8px;
    overflow: hidden;
    transition: box-shadow 0.2s;
}
.accordion-item.open {
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.accordion-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 20px;
    cursor: pointer;
    user-select: none;
    transition: background 0.15s;
}
.accordion-head:hover {
    background: #FFFBEB;
}
.accordion-title {
    font-size: 15px;
    font-weight: 500;
    color: var(--color-text);
}
.accordion-arrow {
    color: var(--color-text-tertiary);
    transition: transform 0.2s;
    display: flex;
    align-items: center;
}
.accordion-item.open .accordion-arrow {
    transform: rotate(180deg);
}

.accordion-body {
    max-height: 0;
    overflow: hidden;
    transition: max-height 0.3s ease;
}
.accordion-item.open .accordion-body {
    max-height: 800px;
}

.accordion-body-inner {
    padding: 0 20px 18px;
}

/* 表单 */
.settings-form {
    display: flex;
    flex-direction: column;
    gap: 14px;
    padding-top: 4px;
}
.form-row {
    display: flex;
    align-items: center;
    gap: 16px;
}
.form-label {
    width: 80px;
    font-size: var(--font-size-base);
    color: var(--color-text-secondary);
    flex-shrink: 0;
    text-align: right;
}
.form-static {
    font-size: var(--font-size-md);
    color: var(--color-text-tertiary);
}
.form-input {
    flex: 1;
    padding: 7px 12px;
    border: 1px solid var(--color-border-neutral);
    border-radius: var(--radius-md);
    font-size: var(--font-size-md);
    color: var(--color-text);
    background: var(--color-bg-card);
    outline: none;
    transition: border-color 0.2s;
    font-family: inherit;
}
.form-input:focus { border-color: var(--color-primary); }

/* 滑块 */
.goal-slider-wrap {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12px;
}
.form-range {
    flex: 1;
    height: 6px;
    -webkit-appearance: none;
    appearance: none;
    background: #F3F0E8;
    border-radius: 3px;
    outline: none;
}
.form-range::-webkit-slider-thumb {
    -webkit-appearance: none;
    appearance: none;
    width: 18px; height: 18px;
    border-radius: 50%;
    background: var(--color-primary);
    cursor: pointer;
    box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}
.form-range::-moz-range-thumb {
    width: 18px; height: 18px;
    border: none; border-radius: 50%;
    background: var(--color-primary);
    cursor: pointer;
}
.goal-value {
    font-size: var(--font-size-md);
    font-weight: 600;
    color: var(--color-primary-dark);
    min-width: 72px;
    text-align: right;
}

/* 头像预览 */
.avatar-preview {
    width: 56px; height: 56px;
    border-radius: 50%;
    background: #F5F2EC;
    border: 1px dashed var(--color-border);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
}
.avatar-preview img {
    width: 100%; height: 100%;
    object-fit: cover;
    border-radius: 50%;
}

/* 按钮 */
.form-actions {
    display: flex;
    justify-content: flex-end;
    padding-top: 4px;
}
.btn-primary {
    padding: 7px 24px;
    background: var(--color-primary);
    color: #FFF;
    border: none;
    border-radius: var(--radius-md);
    font-size: var(--font-size-md);
    font-weight: 500;
    cursor: pointer;
    transition: background 0.2s;
    font-family: inherit;
}
.btn-primary:hover { background: var(--color-primary-dark); }

/* 导入历史 */
.import-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 0 20px 18px;
}
.import-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 9px 14px;
    background: #FAF8F3;
    border-radius: var(--radius-md);
    transition: background 0.2s;
}
.import-item:hover { background: var(--color-primary-palest); }
.import-info {
    display: flex;
    flex-direction: column;
    gap: 1px;
    min-width: 0;
}
.import-title {
    font-size: var(--font-size-md);
    font-weight: 500;
    color: var(--color-text);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.import-meta {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
}
.import-date {
    font-size: var(--font-size-sm);
    color: var(--color-text-light);
    flex-shrink: 0;
    margin-left: 12px;
}
.empty-mini {
    font-size: var(--font-size-base);
    color: var(--color-text-tertiary);
    text-align: center;
    padding: 16px;
}

/* 关于 */
.about-section {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 0 20px 18px;
}
.about-logo {
    display: flex;
    align-items: center;
    gap: 12px;
}
.about-logo-circle {
    width: 40px; height: 40px;
    border-radius: 10px;
    background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
    color: #FFF;
    font-size: 18px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
}
.about-name { font-size: 15px; font-weight: 600; color: var(--color-text); }
.about-version { font-size: var(--font-size-sm); color: var(--color-text-tertiary); }
.about-desc {
    font-size: var(--font-size-base);
    color: var(--color-text-secondary);
    line-height: 1.6;
}
.about-github {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 8px 14px;
    background: #F6F8FA;
    border: 1px solid #E1E4E8;
    border-radius: var(--radius-md);
    font-size: var(--font-size-base);
    color: #24292E;
    transition: background 0.2s;
    width: fit-content;
}
.about-github:hover {
    background: #EDF2F7;
    color: #24292E;
}
.about-tech {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
}
.tech-tag {
    padding: 2px 10px;
    background: var(--color-primary-palest);
    border: 1px solid var(--color-border-light);
    border-radius: var(--radius-pill);
    font-size: var(--font-size-sm);
    color: var(--color-primary-dark);
}

/* 退出登录 */
.logout-row {
    text-align: center;
    padding: 12px 0 4px;
}
.btn-logout {
    display: inline-block;
    padding: 8px 32px;
    color: #DC2626;
    font-size: var(--font-size-md);
    font-weight: 500;
    border: none;
    border-radius: var(--radius-md);
    background: transparent;
    cursor: pointer;
    transition: background 0.2s;
    font-family: inherit;
}
.btn-logout:hover {
    background: #FEF2F2;
    color: #DC2626;
}

/* 个性化 */
.bg-upload-area {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12px;
}
.bg-preview {
    width: 80px;
    height: 80px;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid var(--color-border-light);
    flex-shrink: 0;
}
.bg-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
.bg-actions {
    display: flex;
    flex-direction: column;
    gap: 6px;
}
.btn-upfile {
    display: inline-block;
    padding: 6px 14px;
    background: var(--color-primary-pale);
    color: var(--color-primary-darker);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    font-size: var(--font-size-sm);
    cursor: pointer;
    transition: background 0.15s;
    text-align: center;
    font-family: inherit;
}
.btn-upfile:hover {
    background: #FEF3C7;
    border-color: var(--color-primary);
}
.btn-danger-ghost-sm {
    padding: 6px 14px;
    background: transparent;
    color: #DC2626;
    border: 1px solid #FCA5A5;
    border-radius: var(--radius-md);
    font-size: var(--font-size-sm);
    cursor: pointer;
    transition: background 0.15s;
    font-family: inherit;
}
.btn-danger-ghost-sm:hover {
    background: #FEF2F2;
}
/* 开关 */
.toggle-switch {
    position: relative;
    display: inline-block;
    width: 40px;
    height: 22px;
    cursor: pointer;
}
.toggle-switch input {
    opacity: 0;
    width: 0;
    height: 0;
}
.toggle-slider {
    position: absolute;
    inset: 0;
    background: #D1D5DB;
    border-radius: 22px;
    transition: background 0.2s;
}
.toggle-slider::before {
    content: '';
    position: absolute;
    width: 16px;
    height: 16px;
    left: 3px;
    bottom: 3px;
    background: #fff;
    border-radius: 50%;
    transition: transform 0.2s;
    box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}
.toggle-switch input:checked + .toggle-slider {
    background: var(--color-primary);
}
.toggle-switch input:checked + .toggle-slider::before {
    transform: translateX(18px);
}

/* 响应式 */
@media (max-width: 640px) {
    .form-row { flex-direction: column; align-items: flex-start; gap: 4px; }
    .form-label { text-align: left; width: auto; }
}
</style>
