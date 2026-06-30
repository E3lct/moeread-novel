<template>
  <svg class="progress-ring" :width="size" :height="size" :viewBox="`0 0 ${size} ${size}`">
    <circle
      class="ring-bg"
      :cx="size / 2"
      :cy="size / 2"
      :r="radius"
      :stroke-width="strokeWidth"
      fill="none"
    />
    <circle
      class="ring-fill"
      :cx="size / 2"
      :cy="size / 2"
      :r="radius"
      :stroke-width="strokeWidth"
      fill="none"
      :stroke-dasharray="circumference"
      :stroke-dashoffset="offset"
      stroke-linecap="round"
      :transform="`rotate(-90 ${size / 2} ${size / 2})`"
    />
    <text
      v-if="showText"
      class="ring-text"
      :x="size / 2"
      :y="size / 2"
      text-anchor="middle"
      dominant-baseline="central"
      :font-size="size * 0.22"
    >
      {{ percent }}%
    </text>
  </svg>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  percent: { type: Number, default: 0 },
  size: { type: Number, default: 48 },
  strokeWidth: { type: Number, default: 3 },
  showText: { type: Boolean, default: false },
  color: { type: String, default: '#F59E0B' }
})

const radius = computed(() => (props.size - props.strokeWidth) / 2)
const circumference = computed(() => 2 * Math.PI * radius.value)
const offset = computed(() => circumference.value * (1 - props.percent / 100))
</script>

<style scoped>
.progress-ring {
  display: block;
}

.ring-bg {
  stroke: rgba(255, 255, 255, 0.3);
}

.ring-fill {
  stroke: var(--color-primary);
  transition: stroke-dashoffset 0.5s ease;
}

.ring-text {
  fill: #fff;
  font-weight: 600;
}
</style>
