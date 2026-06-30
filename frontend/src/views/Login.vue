<template>
  <div class="login-page">
    <!-- 顶部装饰 -->
    <div class="decor-top">
      <div class="decor-circle c1"></div>
      <div class="decor-circle c2"></div>
    </div>

    <!-- Logo -->
    <div class="logo-section">
      <div class="logo-icon">
        <svg viewBox="0 0 48 48" width="56" height="56">
          <rect x="8" y="6" width="32" height="36" rx="4" fill="none" stroke="#fff" stroke-width="2.5"/>
          <line x1="16" y1="16" x2="32" y2="16" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
          <line x1="16" y1="24" x2="32" y2="24" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
          <line x1="16" y1="32" x2="26" y2="32" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <h1 class="logo-text">墨读</h1>
      <p class="logo-sub">Moeread · 沉浸阅读</p>
    </div>

    <!-- 表单卡片 -->
    <div class="form-card card">
      <!-- 切换标签 -->
      <div class="tab-bar">
        <button
          class="tab-btn"
          :class="{ active: mode === 'login' }"
          @click="mode = 'login'"
        >登录</button>
        <button
          class="tab-btn"
          :class="{ active: mode === 'register' }"
          @click="mode = 'register'"
        >注册</button>
        <div class="tab-indicator" :class="{ right: mode === 'register' }"></div>
      </div>

      <!-- 输入框 -->
      <div class="input-group">
        <label>账号</label>
        <input
          v-model="username"
          type="text"
          placeholder="请输入用户名"
          maxlength="20"
          @keyup.enter="handleSubmit"
        />
      </div>

      <div class="input-group">
        <label>密码</label>
        <input
          v-model="password"
          type="password"
          placeholder="请输入密码"
          maxlength="32"
          @keyup.enter="handleSubmit"
        />
      </div>

      <div class="input-group" v-if="mode === 'register'">
        <label>确认密码</label>
        <input
          v-model="confirmPassword"
          type="password"
          placeholder="再次输入密码"
          maxlength="32"
          @keyup.enter="handleSubmit"
        />
      </div>

      <button class="btn-primary" :disabled="loading" @click="handleSubmit">
        {{ loading ? '处理中...' : (mode === 'login' ? '登 录' : '注 册') }}
      </button>
    </div>

    <p class="footer-hint">登录即代表您同意继续阅读之旅</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const mode = ref('login')
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)

async function handleSubmit() {
  if (!username.value.trim()) {
    alert('请输入用户名')
    return
  }
  if (!password.value) {
    alert('请输入密码')
    return
  }

  if (mode.value === 'register') {
    if (password.value !== confirmPassword.value) {
      alert('两次密码不一致')
      return
    }
    loading.value = true
    try {
      await userStore.register(username.value.trim(), password.value)
      alert('注册成功，请登录')
      mode.value = 'login'
      confirmPassword.value = ''
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      loading.value = false
    }
  } else {
    loading.value = true
    try {
      await userStore.login(username.value.trim(), password.value)
      router.push('/home')
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      loading.value = false
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(160deg, var(--color-primary) 0%, var(--color-primary-dark) 60%, var(--color-primary-darker) 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 28px;
  position: relative;
  overflow: hidden;
}

/* 装饰圆 */
.decor-top {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 50%;
  pointer-events: none;
}

.decor-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.c1 {
  width: 280px;
  height: 280px;
  top: -100px;
  right: -80px;
}

.c2 {
  width: 180px;
  height: 180px;
  top: 60px;
  left: -60px;
}

/* Logo */
.logo-section {
  text-align: center;
  margin-bottom: 36px;
  z-index: 1;
}

.logo-icon {
  width: 88px;
  height: 88px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.logo-text {
  color: #fff;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 4px;
}

.logo-sub {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  margin-top: 6px;
  letter-spacing: 1px;
}

/* 表单卡片 */
.form-card {
  width: 100%;
  max-width: 360px;
  padding: 28px 24px 32px;
  z-index: 1;
}

/* 标签栏 */
.tab-bar {
  display: flex;
  position: relative;
  margin-bottom: 28px;
  background: var(--color-primary-lightest);
  border-radius: var(--radius-sm);
  padding: 4px;
}

.tab-btn {
  flex: 1;
  padding: 10px 0;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-hint);
  z-index: 1;
  transition: color 0.25s;
  cursor: pointer;
}

.tab-btn.active {
  color: var(--color-primary-darker);
  font-weight: 600;
}

.tab-indicator {
  position: absolute;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(146, 64, 14, 0.1);
  transition: transform 0.3s ease;
}

.tab-indicator.right {
  transform: translateX(100%);
}

/* 输入框 */
.input-group {
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.input-group input {
  width: 100%;
  padding: 12px 14px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 15px;
  color: var(--color-text);
  transition: border-color 0.2s;
  background: var(--color-primary-lightest);
}

.input-group input:focus {
  border-color: var(--color-primary);
  background: #fff;
}

.footer-hint {
  margin-top: 24px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
  z-index: 1;
}
</style>
