<template>
  <div
    class="bg-layer"
    v-if="bgStyle.backgroundImage"
    :style="bgStyle"
  ></div>
  <div class="app-surface" :class="{ 'has-bg': bgStyle.backgroundImage }">
    <router-view />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useUserStore } from './stores/user'

const userStore = useUserStore()

const bgStyle = computed(() => {
  const img = userStore.userInfo?.mascotImage
  if (!img) return {}
  const url = img.startsWith('http') ? img : '/api' + img
  const opacity = Math.min((userStore.userInfo?.mascotOpacity ?? 100) / 100, 1)
  const scale = userStore.userInfo?.bgScale ?? 100
  const mirror = userStore.userInfo?.bgMirror
  return {
    backgroundImage: `url(${url})`,
    '--bg-overlay': String(1 - opacity),
    backgroundSize: `${scale}% ${scale}%`,
    transform: mirror ? 'scaleX(-1)' : 'scaleX(1)'
  }
})

onMounted(async () => {
  if (userStore.token && !userStore.userInfo?.mascotImage) {
    try {
      await userStore.fetchProfile()
    } catch (e) {
      // token 可能过期，忽略
    }
  }
})
</script>

<style>
.bg-layer {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  background-position: center center;
  background-repeat: no-repeat;
  z-index: 0;
  pointer-events: none;
  transition: background-size 0.2s, transform 0.2s;
}

.bg-layer::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(135deg, rgba(255, 251, 235, var(--bg-overlay)) 0%, rgba(246, 243, 236, calc(var(--bg-overlay) * 0.92)) 48%, rgba(238, 244, 242, calc(var(--bg-overlay) * 0.86)) 100%);
}

.app-surface {
  position: relative;
  z-index: 1;
  min-height: 100vh;
}

.app-surface.has-bg {
  background: transparent;
}
</style>
