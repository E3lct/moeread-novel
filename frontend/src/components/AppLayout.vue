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
  }, 760)
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
    transition: opacity 0.42s ease, transform 0.52s cubic-bezier(.2,.78,.24,1);
}

.app-layout :deep(.navbar) {
    transition: opacity 0.36s ease, transform 0.48s cubic-bezier(.2,.78,.24,1);
}

.app-layout.immersive .main-content {
    opacity: 0;
    pointer-events: none;
    transform: translateY(16px) scale(0.992);
}

.app-layout.immersive :deep(.navbar) {
    opacity: 0;
    pointer-events: none;
    transform: translateX(-50%) translateY(-28px) scale(0.94);
}

.water-ripple {
    position: fixed;
    inset: 0;
    z-index: 2200;
    pointer-events: none;
    overflow: hidden;
    background:
        radial-gradient(circle at 50% 46px, rgba(255,255,255,0.52) 0 2px, transparent 3px),
        repeating-radial-gradient(circle at 50% 46px,
            rgba(255,255,255,0) 0 44px,
            rgba(255,255,255,0.42) 45px 47px,
            rgba(47,93,80,0.10) 48px 50px,
            rgba(255,255,255,0) 52px 92px);
    opacity: 0;
    will-change: clip-path, opacity;
}

.water-ripple::before {
    content: '';
    position: absolute;
    inset: 0;
    background: radial-gradient(circle at 50% 46px, rgba(255, 248, 226, 0.26), transparent 34%);
}

.app-layout.ripple-in .water-ripple {
    animation: rippleExpand 0.72s cubic-bezier(.18,.82,.21,1) forwards;
}

.app-layout.ripple-out .water-ripple {
    animation: rippleReturn 0.72s cubic-bezier(.18,.82,.21,1) forwards;
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
        clip-path: circle(0 at 50% 46px);
    }
    24% {
        opacity: 0.85;
    }
    100% {
        opacity: 0;
        clip-path: circle(142vmax at 50% 46px);
    }
}

@keyframes rippleReturn {
    0% {
        opacity: 0;
        clip-path: circle(142vmax at 50% 46px);
    }
    24% {
        opacity: 0.85;
    }
    100% {
        opacity: 0;
        clip-path: circle(0 at 50% 46px);
    }
}

@media (max-width: 860px) {
    .main-content {
        padding: 94px 14px 32px;
    }
    .water-ripple {
        background:
            radial-gradient(circle at 36px 38px, rgba(255,255,255,0.52) 0 2px, transparent 3px),
            repeating-radial-gradient(circle at 36px 38px,
                rgba(255,255,255,0) 0 38px,
                rgba(255,255,255,0.42) 39px 41px,
                rgba(47,93,80,0.10) 42px 44px,
                rgba(255,255,255,0) 46px 82px);
    }
}
</style>
