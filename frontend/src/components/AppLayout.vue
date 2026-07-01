<template>
  <div class="app-layout">
    <TopNav />
    <main class="main-content app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import TopNav from './TopNav.vue'
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.main-content {
    min-height: 100vh;
    padding: 96px clamp(18px, 4vw, 56px) 48px;
    max-width: 1440px;
    margin: 0 auto;
}

@media (max-width: 860px) {
    .main-content {
        padding: 82px 14px 32px;
    }
}
</style>
