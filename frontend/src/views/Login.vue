<template>
  <div class="auth-wrapper">
    <div class="auth-card">
      <!-- Logo -->
      <div class="auth-logo">
        <div class="auth-logo-circle">墨</div>
        <h1>墨读</h1>
        <p>Moeread · 沉浸阅读</p>
      </div>

      <!-- 切换标签 -->
      <div class="auth-tabs">
        <button
          class="auth-tab"
          :class="{ active: mode === 'login' }"
          @click="mode = 'login'"
        >登录</button>
        <button
          class="auth-tab"
          :class="{ active: mode === 'register' }"
          @click="mode = 'register'"
        >注册</button>
      </div>

      <!-- 表单 -->
      <form class="auth-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>账号</label>
          <input
            v-model="username"
            type="text"
            placeholder="请输入用户名"
            maxlength="20"
          />
        </div>

        <div class="form-group">
          <label>密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="请输入密码"
            maxlength="32"
          />
        </div>

        <div class="form-group" v-if="mode === 'register'">
          <label>确认密码</label>
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="再次输入密码"
            maxlength="32"
          />
        </div>

        <div class="alert alert-error" v-if="errorMsg">{{ errorMsg }}</div>

        <button type="submit" class="auth-btn" :disabled="loading">
          {{ loading ? '处理中...' : (mode === 'login' ? '登 录' : '注 册') }}
        </button>
      </form>

      <div class="auth-footer">
        {{ mode === 'login' ? '还没有账号？' : '已有账号？' }}
        <a href="javascript:void(0)" @click="mode = mode === 'login' ? 'register' : 'login'">
          {{ mode === 'login' ? '去注册' : '去登录' }}
        </a>
      </div>
    </div>
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
const errorMsg = ref('')

async function handleSubmit() {
  errorMsg.value = ''
  if (!username.value.trim()) {
    errorMsg.value = '请输入用户名'
    return
  }
  if (!password.value) {
    errorMsg.value = '请输入密码'
    return
  }

  if (mode.value === 'register') {
    if (password.value !== confirmPassword.value) {
      errorMsg.value = '两次密码不一致'
      return
    }
    loading.value = true
    try {
      await userStore.register(username.value.trim(), password.value)
      errorMsg.value = ''
      mode.value = 'login'
      confirmPassword.value = ''
      alert('注册成功，请登录')
    } catch (e) {
      errorMsg.value = e.message || '注册失败'
    } finally {
      loading.value = false
    }
  } else {
    loading.value = true
    try {
      await userStore.login(username.value.trim(), password.value)
      router.push('/home')
    } catch (e) {
      errorMsg.value = e.message || '登录失败'
    } finally {
      loading.value = false
    }
  }
}
</script>

<style scoped>
.auth-wrapper {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--color-bg);
    padding: var(--spacing-2xl);
}

.auth-card {
    width: 100%;
    max-width: 380px;
    background: var(--color-bg-card);
    border: 0.5px solid var(--color-border);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-lg);
    padding: var(--spacing-4xl) var(--spacing-3xl);
}

.auth-logo {
    text-align: center;
    margin-bottom: var(--spacing-3xl);
}

.auth-logo-circle {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: var(--color-primary-pale);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: var(--color-primary-darker);
    font-size: 24px;
    font-weight: 600;
    margin-bottom: var(--spacing-lg);
}

.auth-logo h1 {
    font-size: var(--font-size-3xl);
    font-weight: 600;
    color: var(--color-text);
    letter-spacing: 1px;
}

.auth-logo p {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
    margin-top: 4px;
}

/* 切换标签 */
.auth-tabs {
    display: flex;
    background: var(--color-primary-palest);
    border-radius: var(--radius-md);
    padding: 3px;
    margin-bottom: var(--spacing-2xl);
}

.auth-tab {
    flex: 1;
    padding: 8px 0;
    border: none;
    background: transparent;
    border-radius: var(--radius-sm);
    font-size: var(--font-size-md);
    font-weight: 500;
    color: var(--color-text-tertiary);
    cursor: pointer;
    transition: all 0.2s;
    font-family: inherit;
}

.auth-tab.active {
    background: #fff;
    color: var(--color-primary-darker);
    font-weight: 600;
    box-shadow: 0 1px 3px rgba(146, 64, 14, 0.1);
}

/* 表单 */
.auth-form {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-lg);
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.form-group label {
    font-size: var(--font-size-sm);
    font-weight: var(--font-medium);
    color: var(--color-text-secondary);
}

.form-group input {
    padding: 10px 14px;
    border: 0.5px solid var(--color-border-neutral);
    border-radius: var(--radius-sm);
    font-size: var(--font-size-md);
    color: var(--color-text);
    background: var(--color-bg-card);
    transition: border-color 0.15s, box-shadow 0.15s;
    outline: none;
    font-family: inherit;
}

.form-group input:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.1);
}

.form-group input::placeholder {
    color: var(--color-text-light);
}

.auth-btn {
    margin-top: var(--spacing-sm);
    padding: 11px;
    border: none;
    border-radius: var(--radius-sm);
    font-size: var(--font-size-md);
    font-weight: var(--font-medium);
    color: var(--color-text-on-primary);
    background: var(--color-primary);
    cursor: pointer;
    transition: background 0.15s, transform 0.1s;
    font-family: inherit;
}

.auth-btn:hover {
    background: var(--color-primary-dark);
}

.auth-btn:active {
    transform: scale(0.98);
}

.auth-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.auth-footer {
    text-align: center;
    margin-top: var(--spacing-2xl);
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
}

.auth-footer a {
    color: var(--color-primary-dark);
    font-weight: var(--font-medium);
    cursor: pointer;
}

.auth-footer a:hover {
    text-decoration: underline;
}
</style>
