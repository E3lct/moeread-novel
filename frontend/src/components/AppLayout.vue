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
    padding: 34px 36px 48px;
    margin-left: 248px;
}

@media (max-width: 860px) {
    .main-content {
        margin-left: 0;
        padding: 84px 16px 32px;
    }
}
</style>
