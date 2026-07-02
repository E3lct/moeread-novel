<template>
  <div
    class="bg-layer"
    v-if="bgStyle.backgroundImage"
    :style="bgStyle"
  ></div>
  <router-view />
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
    opacity: opacity,
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
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-position: center center;
  background-repeat: no-repeat;
  z-index: -1;
  pointer-events: none;
  transition: opacity 0.3s, background-size 0.2s, transform 0.2s;
}
</style>
