<template>
  <div class="auth-wrapper">
    <section class="reading-scene">
      <div class="book-stage">
        <div class="page page-a"></div>
        <div class="page page-b"></div>
        <div class="page page-c"></div>
        <div class="book-spine"></div>
      </div>
      <div class="scene-copy">
        <span>Moeread</span>
        <h1>把零散的故事，收进一间安静书房。</h1>
        <p>导入、本地阅读、书源搜索、统计和摸鱼模式，都在一个轻量阅读器里。</p>
      </div>
    </section>

    <section class="auth-card">
      <div class="auth-logo">
        <div class="auth-logo-circle">墨</div>
        <div>
          <h2>墨读</h2>
          <p>{{ mode === 'login' ? '欢迎回来' : '创建你的书架' }}</p>
        </div>
      </div>

      <div class="auth-tabs">
        <button class="auth-tab" :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
        <button class="auth-tab" :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
      </div>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <label class="form-group">
          <span>账号</span>
          <input v-model="username" type="text" placeholder="请输入用户名" maxlength="20" />
        </label>

        <label class="form-group">
          <span>密码</span>
          <input v-model="password" type="password" placeholder="请输入密码" maxlength="32" />
        </label>

        <label class="form-group" v-if="mode === 'register'">
          <span>确认密码</span>
          <input v-model="confirmPassword" type="password" placeholder="再次输入密码" maxlength="32" />
        </label>

        <div class="alert alert-error" v-if="errorMsg">{{ errorMsg }}</div>

        <button type="submit" class="auth-btn" :disabled="loading">
          {{ loading ? '处理中' : (mode === 'login' ? '进入书房' : '创建账号') }}
        </button>
      </form>

      <button class="auth-switch" @click="mode = mode === 'login' ? 'register' : 'login'">
        {{ mode === 'login' ? '还没有账号，去注册' : '已有账号，去登录' }}
      </button>
    </section>
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
      mode.value = 'login'
      confirmPassword.value = ''
      errorMsg.value = ''
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
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) 430px;
  gap: 36px;
  align-items: center;
  padding: 48px clamp(24px, 6vw, 86px);
  background:
    radial-gradient(circle at 18% 12%, rgba(232, 198, 135, 0.32), transparent 28%),
    linear-gradient(135deg, #eef3ed, #f8f1e6 48%, #efe5d5);
  overflow: hidden;
}

.reading-scene {
  min-height: 620px;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  border-radius: 34px;
  padding: 44px;
  background:
    linear-gradient(145deg, rgba(37, 55, 50, 0.92), rgba(82, 67, 49, 0.82)),
    radial-gradient(circle at 70% 18%, rgba(255,255,255,0.28), transparent 30%);
  color: #fff;
  box-shadow: 0 34px 90px rgba(53, 45, 33, 0.24);
}

.book-stage {
  position: absolute;
  top: 72px;
  left: 50%;
  width: 360px;
  height: 260px;
  transform: translateX(-50%) rotate(-5deg);
  perspective: 900px;
}

.page {
  position: absolute;
  width: 170px;
  height: 238px;
  top: 12px;
  left: 174px;
  border-radius: 8px 18px 18px 8px;
  background: linear-gradient(90deg, #fff8e8, #f2dfbd);
  transform-origin: left center;
  box-shadow: 0 18px 45px rgba(0,0,0,0.2);
}

.page::before {
  content: '';
  position: absolute;
  inset: 32px 28px;
  background: repeating-linear-gradient(#9c8666 0 1px, transparent 1px 18px);
  opacity: 0.22;
}

.page-a { animation: flipPage 5.5s ease-in-out infinite; }
.page-b { animation: flipPage 5.5s ease-in-out infinite 1.4s; opacity: 0.72; }
.page-c { animation: flipPage 5.5s ease-in-out infinite 2.8s; opacity: 0.5; }

.book-spine {
  position: absolute;
  top: 16px;
  left: 12px;
  width: 176px;
  height: 234px;
  border-radius: 18px 8px 8px 18px;
  background: linear-gradient(135deg, #314f47, #233934);
  box-shadow: inset -18px 0 32px rgba(255,255,255,0.08), 0 18px 45px rgba(0,0,0,0.2);
}

@keyframes flipPage {
  0%, 18% { transform: rotateY(0deg); }
  44%, 62% { transform: rotateY(-145deg); }
  100% { transform: rotateY(-145deg); }
}

.scene-copy {
  position: relative;
  max-width: 520px;
}

.scene-copy span {
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: rgba(255,255,255,0.62);
}

.scene-copy h1 {
  margin: 12px 0;
  font-size: clamp(34px, 5vw, 62px);
  line-height: 1.05;
  letter-spacing: 0;
}

.scene-copy p {
  max-width: 460px;
  color: rgba(255,255,255,0.72);
  line-height: 1.8;
}

.auth-card {
  padding: 30px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(227, 216, 198, 0.86);
  box-shadow: 0 28px 80px rgba(70, 58, 42, 0.16);
  backdrop-filter: blur(22px);
}

.auth-logo {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
}

.auth-logo-circle {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(135deg, #2f5d50, #b98a4c);
}

.auth-logo h2 {
  margin: 0;
  font-size: 24px;
  color: #2e261d;
}

.auth-logo p {
  margin: 2px 0 0;
  color: #817568;
}

.auth-tabs {
  display: flex;
  padding: 5px;
  margin-bottom: 22px;
  border-radius: 999px;
  background: #efe8dc;
}

.auth-tab {
  flex: 1;
  border: 0;
  border-radius: 999px;
  padding: 10px 0;
  color: #71675d;
  background: transparent;
  cursor: pointer;
  font: inherit;
}

.auth-tab.active {
  background: #fff;
  color: #2f5d50;
  font-weight: 800;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.form-group span {
  font-size: 12px;
  font-weight: 800;
  color: #71675d;
}

.form-group input {
  border: 1px solid #ded3c1;
  border-radius: 16px;
  padding: 13px 14px;
  background: rgba(255,255,255,0.82);
  color: #2e261d;
  outline: none;
  font: inherit;
}

.form-group input:focus {
  border-color: #2f5d50;
  box-shadow: 0 0 0 4px rgba(47, 93, 80, 0.10);
}

.auth-btn {
  margin-top: 8px;
  border: 0;
  border-radius: 999px;
  padding: 13px;
  color: #fff;
  background: #2f5d50;
  box-shadow: 0 12px 28px rgba(47, 93, 80, 0.24);
  cursor: pointer;
  font: inherit;
  font-weight: 800;
}

.auth-switch {
  width: 100%;
  margin-top: 18px;
  border: 0;
  background: transparent;
  color: #8a6a3b;
  cursor: pointer;
  font: inherit;
}

@media (max-width: 900px) {
  .auth-wrapper {
    grid-template-columns: 1fr;
    padding: 18px;
  }
  .reading-scene {
    min-height: 360px;
  }
  .book-stage {
    opacity: 0.55;
    transform: translateX(-50%) scale(0.72) rotate(-5deg);
  }
}
</style>
