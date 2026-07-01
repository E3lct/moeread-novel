<template>
  <div class="book-progress-ring" v-if="percent > 0">
    <svg width="52" height="52" viewBox="0 0 52 52">
      <circle cx="26" cy="26" r="22" fill="none" stroke="rgba(255,255,255,0.3)" stroke-width="3.5"/>
      <circle
        cx="26" cy="26" r="22" fill="none"
        stroke="#fff" stroke-width="3.5"
        stroke-linecap="round"
        :stroke-dasharray="circumference"
        :stroke-dashoffset="dashOffset"
        class="ring-progress"
        transform="rotate(-90 26 26)"
      />
      <text x="26" y="30" text-anchor="middle" fill="#fff" font-size="11" font-weight="600">
        {{ percent }}%
      </text>
    </svg>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  percent: { type: Number, default: 0 }
})

const radius = 22
const circumference = 2 * Math.PI * radius
const dashOffset = computed(() => circumference * (1 - props.percent / 100))
</script>

<style scoped>
.book-progress-ring {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 4;
    opacity: 0;
    visibility: hidden;
    transition: opacity 0.2s ease, visibility 0s 0.2s;
    pointer-events: none;
    display: flex;
    align-items: center;
    justify-content: center;
}

.book-progress-ring svg {
    display: block;
    filter: drop-shadow(0 1px 3px rgba(0,0,0,0.3));
}

.ring-progress {
    transition: stroke-dashoffset 1s ease-out;
}
</style>
