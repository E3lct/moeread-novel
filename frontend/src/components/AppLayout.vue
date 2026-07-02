<template>
  <div class="app-layout" :class="{ immersive: immersiveActive, 'ripple-in': rippleMode === 'in', 'ripple-out': rippleMode === 'out' }">
    <TopNav @toggle-immersive="toggleImmersive" />
    <main class="main-content app-main">
      <router-view />
    </main>
    <div v-if="rippleMode" class="water-ripple" aria-hidden="true"></div>
    <button
      v-if="immersiveActive"
      class="immersive-catcher"
      type="button"
      aria-label="恢复界面"
      @click="toggleImmersive"
    ></button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import TopNav from './TopNav.vue'

const immersiveActive = ref(false)
const rippleMode = ref('')
let rippleTimer = 0

function toggleImmersive() {
  window.clearTimeout(rippleTimer)
  rippleMode.value = immersiveActive.value ? 'out' : 'in'
  immersiveActive.value = !immersiveActive.value
  rippleTimer = window.setTimeout(() => {
    rippleMode.value = ''
  }, 980)
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  position: relative;
}

.main-content {
    min-height: 100vh;
    padding: 116px clamp(18px, 4vw, 56px) 48px;
    max-width: 1440px;
    margin: 0 auto;
    transition: opacity 0.48s ease, transform 0.62s cubic-bezier(.2,.78,.24,1), filter 0.48s ease;
}

.app-layout :deep(.navbar) {
    transition: opacity 0.42s ease, transform 0.62s cubic-bezier(.2,.78,.24,1), filter 0.42s ease;
}

.app-layout.immersive .main-content {
    opacity: 0;
    pointer-events: none;
    transform: translateY(18px) scale(0.985);
    filter: blur(12px);
}

.app-layout.immersive :deep(.navbar) {
    opacity: 0;
    pointer-events: none;
    transform: translateX(-50%) translateY(-28px) scale(0.94);
    filter: blur(10px);
}

.water-ripple {
    position: fixed;
    inset: 0;
    z-index: 2200;
    pointer-events: none;
    overflow: hidden;
}

.water-ripple::before,
.water-ripple::after {
    content: '';
    position: absolute;
    left: 50%;
    top: 46px;
    width: 52px;
    height: 52px;
    border-radius: 50%;
    transform: translate(-50%, -50%) scale(0.2);
    border: 1px solid rgba(255, 255, 255, 0.68);
    box-shadow:
        0 0 0 1px rgba(47, 93, 80, 0.12),
        0 0 48px rgba(255, 244, 216, 0.52),
        inset 0 0 36px rgba(255, 255, 255, 0.28);
    background:
        radial-gradient(circle, rgba(255,255,255,0.28) 0%, rgba(255,255,255,0.08) 36%, rgba(47, 93, 80, 0.10) 64%, transparent 70%);
}

.water-ripple::after {
    animation-delay: 0.11s;
    opacity: 0.74;
}

.app-layout.ripple-in .water-ripple::before,
.app-layout.ripple-in .water-ripple::after {
    animation: rippleExpand 0.96s cubic-bezier(.18,.82,.21,1) forwards;
}

.app-layout.ripple-out .water-ripple::before,
.app-layout.ripple-out .water-ripple::after {
    animation: rippleReturn 0.96s cubic-bezier(.18,.82,.21,1) forwards;
}

.immersive-catcher {
    position: fixed;
    inset: 0;
    z-index: 2100;
    border: 0;
    padding: 0;
    background: transparent;
    cursor: default;
}

@keyframes rippleExpand {
    0% {
        opacity: 0;
        transform: translate(-50%, -50%) scale(0.12);
    }
    18% {
        opacity: 1;
    }
    100% {
        opacity: 0;
        transform: translate(-50%, -50%) scale(46);
    }
}

@keyframes rippleReturn {
    0% {
        opacity: 0;
        transform: translate(-50%, -50%) scale(46);
    }
    18% {
        opacity: 0.9;
    }
    100% {
        opacity: 0;
        transform: translate(-50%, -50%) scale(0.12);
    }
}

@media (max-width: 860px) {
    .main-content {
        padding: 94px 14px 32px;
    }
    .water-ripple::before,
    .water-ripple::after {
        left: 36px;
        top: 38px;
    }
}
</style>
