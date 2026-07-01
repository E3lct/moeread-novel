<template>
  <nav class="navbar">
    <div class="navbar-logo" @click="router.push('/home')">
      <div class="navbar-logo-circle">墨</div>
      <div>
        <span class="navbar-logo-text">墨读</span>
        <span class="navbar-logo-sub">Moeread</span>
      </div>
    </div>

    <div class="navbar-nav">
      <div
        v-for="item in navItems"
        :key="item.path"
        class="navbar-item"
        :class="{ active: route.path === item.path }"
        @click="router.push(item.path)"
      >
        {{ item.label }}
      </div>
    </div>

    <div class="navbar-user" @click="router.push('/settings')">
      <div class="navbar-avatar">{{ avatarText }}</div>
      <div class="navbar-user-meta">
        <span class="navbar-username">{{ userStore.nickname || userStore.username || '用户' }}</span>
        <span class="navbar-user-sub">个人书库</span>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const navItems = [
  { path: '/home', label: '首页' },
  { path: '/bookshelf', label: '书架' },
  { path: '/import', label: '导入' },
  { path: '/stats', label: '统计' },
  { path: '/settings', label: '设置' }
]

const avatarText = computed(() => {
  const name = userStore.nickname || userStore.username || '用'
  return name.charAt(0)
})
</script>

<style scoped>
.navbar {
    position: fixed;
    top: 18px;
    left: 18px;
    bottom: 18px;
    width: 212px;
    background: rgba(255, 255, 255, 0.78);
    backdrop-filter: blur(22px) saturate(160%);
    -webkit-backdrop-filter: blur(22px) saturate(160%);
    border: 1px solid rgba(232, 226, 214, 0.86);
    border-radius: 12px;
    box-shadow: 0 18px 45px rgba(61, 46, 26, 0.10);
    z-index: 1000;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 14px;
    gap: 14px;
}

.navbar-logo {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;
    width: 100%;
    padding: 8px;
    cursor: pointer;
    transition: background 0.15s;
    border-radius: 8px;
}

.navbar-logo:hover {
    background: rgba(245, 158, 11, 0.12);
}

.navbar-logo-circle {
    width: 38px;
    height: 38px;
    border-radius: 10px;
    background: linear-gradient(145deg, var(--color-primary), var(--color-primary-dark));
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    font-weight: 700;
}

.navbar-logo-text {
    display: block;
    font-size: 16px;
    line-height: 1.15;
    font-weight: 700;
    color: var(--color-text);
    letter-spacing: 0;
}

.navbar-logo-sub {
    display: block;
    margin-top: 2px;
    color: var(--color-text-tertiary);
    font-size: 11px;
}

.navbar-nav {
    flex: 1;
    display: flex;
    width: 100%;
    flex-direction: column;
    justify-content: flex-start;
    align-items: center;
    gap: 6px;
    padding-top: 8px;
}

.navbar-item {
    width: 100%;
    padding: 10px 12px;
    border-radius: 8px;
    font-size: 14px;
    color: var(--color-text-secondary);
    cursor: pointer;
    transition: all 0.15s ease;
    user-select: none;
    white-space: nowrap;
}

.navbar-item:hover {
    color: var(--color-text);
    background: rgba(0, 0, 0, 0.05);
}

.navbar-item.active {
    background: #fff4d8;
    color: var(--color-primary-darker);
    font-weight: var(--font-medium);
    box-shadow: inset 3px 0 0 var(--color-primary);
}

.navbar-user {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;
    width: 100%;
    cursor: pointer;
    padding: 10px;
    border-radius: 8px;
    transition: background 0.15s;
    background: rgba(250, 248, 243, 0.78);
}

.navbar-user:hover {
    background: rgba(0, 0, 0, 0.05);
}

.navbar-avatar {
    width: 34px;
    height: 34px;
    border-radius: 10px;
    background: var(--color-primary-pale);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--color-primary-darker);
    font-size: 13px;
    font-weight: 700;
}

.navbar-user-meta {
    min-width: 0;
}

.navbar-username {
    display: block;
    font-size: 13px;
    font-weight: 700;
    color: var(--color-text);
    max-width: 122px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.navbar-user-sub {
    display: block;
    color: var(--color-text-tertiary);
    font-size: 11px;
}

@media (max-width: 860px) {
    .navbar {
        top: 12px;
        left: 12px;
        right: 12px;
        bottom: auto;
        width: auto;
        height: 52px;
        flex-direction: row;
        border-radius: 12px;
        padding: 7px;
    }
    .navbar-logo {
        width: auto;
    }
    .navbar-logo-circle {
        width: 30px;
        height: 30px;
        border-radius: 8px;
        font-size: 14px;
    }
    .navbar-logo-sub,
    .navbar-user-meta {
        display: none;
    }
    .navbar-nav {
        flex-direction: row;
        padding-top: 0;
        overflow-x: auto;
    }
    .navbar-item {
        width: auto;
        padding: 7px 10px;
        font-size: 12px;
    }
    .navbar-user {
        width: auto;
        padding: 4px;
        background: transparent;
    }
}
</style>
