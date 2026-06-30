<template>
  <nav class="bottom-nav">
    <div class="nav-inner">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
      >
        <span class="nav-label">{{ item.label }}</span>
      </router-link>
    </div>
  </nav>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()

const navItems = [
  { label: '首页', path: '/home' },
  { label: '书架', path: '/bookshelf' },
  { label: '统计', path: '/stats' },
  { label: '设置', path: '/settings' }
]

function isActive(path) {
  return route.path.startsWith(path)
}
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  justify-content: center;
  padding: 12px 20px calc(12px + env(safe-area-inset-bottom));
  pointer-events: none;
}

.nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-around;
  width: 100%;
  max-width: 360px;
  height: 52px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 26px;
  box-shadow: 0 4px 24px rgba(146, 64, 14, 0.15), 0 1px 3px rgba(146, 64, 14, 0.08);
  pointer-events: auto;
}

.nav-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  border-radius: 20px;
  transition: all 0.25s ease;
  margin: 0 4px;
}

.nav-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-hint);
  transition: all 0.25s ease;
}

.nav-item.active {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.4);
}

.nav-item.active .nav-label {
  color: #fff;
  font-weight: 600;
}
</style>
